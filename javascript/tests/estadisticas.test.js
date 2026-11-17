import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { GestorAcademico } from '../src/gestion.js';
import { ValidacionError } from '../src/errores.js';
import { EstadisticasAcademicas } from '../src/estadisticas.js';

function gestorConNotas() {
    const g = new GestorAcademico();
    g.registrarEstudiante('A001', 'Ana Pérez', '302');
    g.registrarEstudiante('A002', 'Luis Gómez', '302');
    g.registrarEstudiante('A003', 'Sofía Ruiz', '303');
    g.registrarCurso('MAT101', 'Matemáticas');
    g.inscribir('A001', 'MAT101');
    g.inscribir('A002', 'MAT101');
    g.inscribir('A003', 'MAT101');
    g.registrarEvaluacion('MAT101', 'Examen', 100);
    return g;
}

function estudianteConPromedio(g, matricula, valor) {
    g.capturarCalificacion(matricula, 'MAT101', 'Examen', valor);
}

describe('EstadisticasAcademicas - clasificación', () => {
    it('clasifica valores frontera exactos', () => {
        const g = gestorConNotas();
        estudianteConPromedio(g, 'A001', 6.99);
        estudianteConPromedio(g, 'A002', 7.0);
        const stats = new EstadisticasAcademicas(g);
        const clas = stats.clasificarEstudiantesPorCurso('MAT101');
        const porMat = Object.fromEntries(clas.map((e) => [e.matricula, e]));
        assert.equal(porMat.A001.estado, 'REPROBADO');
        assert.equal(porMat.A001.enRiesgo, true);
        assert.equal(porMat.A002.estado, 'APROBADO');
        assert.equal(porMat.A002.enRiesgo, true);
    });

    it('clasifica 7.5 y 7.51 correctamente', () => {
        const g = new GestorAcademico();
        g.registrarEstudiante('B1', 'Uno', '1');
        g.registrarEstudiante('B2', 'Dos', '1');
        g.registrarCurso('C1', 'Curso');
        g.inscribir('B1', 'C1');
        g.inscribir('B2', 'C1');
        g.registrarEvaluacion('C1', 'Examen', 100);
        g.capturarCalificacion('B1', 'C1', 'Examen', 7.5);
        g.capturarCalificacion('B2', 'C1', 'Examen', 7.51);
        const stats = new EstadisticasAcademicas(g);
        const clas = stats.clasificarEstudiantesPorCurso('C1');
        const porMat = Object.fromEntries(clas.map((e) => [e.matricula, e]));
        assert.equal(porMat.B1.estado, 'APROBADO');
        assert.equal(porMat.B1.enRiesgo, true);
        assert.equal(porMat.B2.estado, 'APROBADO');
        assert.equal(porMat.B2.enRiesgo, false);
    });

    it('clasifica sin calificación como SIN_EVALUAR', () => {
        const g = gestorConNotas();
        const stats = new EstadisticasAcademicas(g);
        const clas = stats.clasificarEstudiantesPorCurso('MAT101');
        const a3 = clas.find((e) => e.matricula === 'A003');
        assert.equal(a3.estado, 'SIN_EVALUAR');
        assert.equal(a3.enRiesgo, false);
        assert.equal(a3.promedio, null);
    });
});

describe('EstadisticasAcademicas - riesgo', () => {
    it('detecta riesgo global', () => {
        const g = gestorConNotas();
        estudianteConPromedio(g, 'A001', 6.99);
        estudianteConPromedio(g, 'A002', 7.0);
        const stats = new EstadisticasAcademicas(g);
        const riesgo = stats.detectarRiesgoGlobal();
        assert.equal(riesgo.length, 2);
    });
});

describe('EstadisticasAcademicas - estadísticas', () => {
    it('calcula estadísticas por curso excluyendo sin evaluar', () => {
        const g = gestorConNotas();
        estudianteConPromedio(g, 'A001', 6.99);
        estudianteConPromedio(g, 'A002', 7.0);
        const stats = new EstadisticasAcademicas(g);
        const r = stats.calcularEstadisticasPorCurso('MAT101');
        assert.equal(r.totalEstudiantes, 3);
        assert.equal(r.totalEvaluados, 2);
        assert.equal(r.totalAprobados, 1);
        assert.equal(r.totalReprobados, 1);
        assert.equal(r.totalEnRiesgo, 2);
        assert.equal(r.calificacionMaxima, 7);
        assert.equal(r.calificacionMinima, 6.99);
        assert.equal(r.porcentajeAprobacion, 50);
    });

    it('devuelve ceros cuando no hay evaluados', () => {
        const g = gestorConNotas();
        const stats = new EstadisticasAcademicas(g);
        const r = stats.calcularEstadisticasPorCurso('MAT101');
        assert.deepEqual(r, {
            totalEstudiantes: 3,
            totalEvaluados: 0,
            totalAprobados: 0,
            totalReprobados: 0,
            totalEnRiesgo: 0,
            promedioGeneral: 0,
            calificacionMaxima: 0,
            calificacionMinima: 0,
            porcentajeAprobacion: 0,
        });
        assert.ok(!Number.isNaN(r.promedioGeneral));
    });

    it('calcula estadísticas globales', () => {
        const g = gestorConNotas();
        estudianteConPromedio(g, 'A001', 8);
        const stats = new EstadisticasAcademicas(g);
        const r = stats.calcularEstadisticasGlobal();
        assert.equal(r.totalEstudiantes, 3);
        assert.equal(r.totalEvaluados, 1);
    });
});

describe('EstadisticasAcademicas - validaciones', () => {
    it('lanza ValidacionError con clave vacía', () => {
        const g = gestorConNotas();
        const stats = new EstadisticasAcademicas(g);
        assert.throws(() => stats.clasificarEstudiantesPorCurso(''), ValidacionError);
        assert.throws(() => stats.clasificarEstudiantesPorCurso(null), ValidacionError);
    });

    it('lanza ValidacionError con curso inexistente', () => {
        const g = gestorConNotas();
        const stats = new EstadisticasAcademicas(g);
        assert.throws(() => stats.calcularEstadisticasPorCurso('NOPE'), ValidacionError);
    });

    it('valida en detectarRiesgoPorCurso también', () => {
        const g = gestorConNotas();
        const stats = new EstadisticasAcademicas(g);
        assert.throws(() => stats.detectarRiesgoPorCurso(''), ValidacionError);
    });
});

describe('EstadisticasAcademicas - integración real', () => {
    it('usa GestorAcademico real', () => {
        const g = new GestorAcademico();
        g.registrarEstudiante('X1', 'X Uno', '1');
        g.registrarCurso('MAT', 'Mat');
        g.inscribir('X1', 'MAT');
        g.registrarEvaluacion('MAT', 'Final', 100);
        g.capturarCalificacion('X1', 'MAT', 'Final', 9);
        const stats = new EstadisticasAcademicas(g);
        const r = stats.calcularEstadisticasGlobal();
        assert.equal(r.totalEvaluados, 1);
        assert.equal(r.promedioGeneral, 9);
    });
});