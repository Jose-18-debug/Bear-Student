import test from 'node:test';
import assert from 'node:assert/strict';
import { GestorAcademico } from '../src/gestion.js';
import { ValidacionError } from '../src/errores.js';

//A001 y A002 en MAT101 (Parcial 40% + Final 60%); A001 también en FIS201 (Examen 100%); A003 sin inscribir
function gestorConNotas() {
    const g = new GestorAcademico();
    g.registrarEstudiante('A001', 'Ana Pérez', '302');
    g.registrarEstudiante('A002', 'Luis Gómez', '302');
    g.registrarEstudiante('A003', 'Sofía Ruiz', '303');
    g.registrarCurso('MAT101', 'Matemáticas');
    g.registrarCurso('FIS201', 'Física');
    g.inscribir('A001', 'MAT101');
    g.inscribir('A002', 'MAT101');
    g.inscribir('A001', 'FIS201');
    g.registrarEvaluacion('MAT101', 'Parcial', 40);
    g.registrarEvaluacion('MAT101', 'Final', 60);
    g.registrarEvaluacion('FIS201', 'Examen', 100);
    g.capturarCalificacion('A001', 'MAT101', 'Parcial', 8);    // 8*0.4 + 10*0.6 = 9.2
    g.capturarCalificacion('A001', 'MAT101', 'Final', 10);
    g.capturarCalificacion('A002', 'MAT101', 'Parcial', 6);    // 6*0.4 + 7*0.6 = 6.6
    g.capturarCalificacion('A002', 'MAT101', 'Final', 7);
    g.capturarCalificacion('A001', 'FIS201', 'Examen', 7);
    return g;
}

test('promedio final ponderado de un estudiante en un curso', () => {
    const g = gestorConNotas();
    assert.equal(g.promedioFinal('A001', 'MAT101'), 9.2);
    assert.equal(g.promedioFinal('A002', 'MAT101'), 6.6);
});

test('promedio final con evaluaciones pendientes usa solo las ya calificadas; sin ninguna es null', () => {
    const g = gestorConNotas();
    g.registrarEstudiante('A004', 'Marta Díaz', '303');
    g.inscribir('A004', 'MAT101');
    assert.equal(g.promedioFinal('A004', 'MAT101'), null);
    g.capturarCalificacion('A004', 'MAT101', 'Final', 9);
    assert.equal(g.promedioFinal('A004', 'MAT101'), 9);
});

test('promedio de un curso: media de los promedios finales de sus estudiantes', () => {
    const g = gestorConNotas();
    assert.equal(g.promedioDeCurso('MAT101'), 7.9);   // (9.2 + 6.6) / 2
    assert.equal(g.promedioDeCurso('FIS201'), 7);
});

test('promedio de un curso sin calificaciones es null', () => {
    const g = gestorConNotas();
    g.registrarCurso('QUI301', 'Química');
    assert.equal(g.promedioDeCurso('QUI301'), null);
});

test('promedio general de un estudiante: media de sus promedios por curso', () => {
    const g = gestorConNotas();
    assert.equal(g.promedioGeneral('A001'), 8.1);     // (9.2 + 7) / 2
    assert.equal(g.promedioGeneral('A003'), null);
});

test('consulta las calificaciones de un estudiante agrupadas por curso', () => {
    const g = gestorConNotas();
    const consulta = g.consultarEstudiante('A001');
    assert.equal(consulta.estudiante.nombre, 'Ana Pérez');
    assert.equal(consulta.cursos.length, 2);
    const mat = consulta.cursos.find((c) => c.clave === 'MAT101');
    assert.deepEqual(mat.calificaciones, [
        { evaluacion: 'Parcial', ponderacion: 40, valor: 8 },
        { evaluacion: 'Final', ponderacion: 60, valor: 10 },
    ]);
    assert.equal(mat.promedioFinal, 9.2);
    assert.equal(consulta.promedioGeneral, 8.1);
});

test('consultar un estudiante inexistente lanza ValidacionError', () => {
    const g = gestorConNotas();
    assert.throws(() => g.consultarEstudiante('Z999'), ValidacionError);
});

test('ordena estudiantes por promedio de mayor a menor y deja sin promedio al final', () => {
    const g = gestorConNotas();
    assert.deepEqual(g.estudiantesPorPromedio().map((e) => e.matricula), ['A001', 'A002', 'A003']);
    assert.deepEqual(g.estudiantesPorPromedio(false).map((e) => e.matricula), ['A002', 'A001', 'A003']);
});

test('los empates se ordenan por nombre', () => {
    const g = new GestorAcademico();
    g.registrarEstudiante('B2', 'Zoe', '1');
    g.registrarEstudiante('B1', 'Abel', '1');
    g.registrarCurso('C1', 'Curso');
    g.registrarEvaluacion('C1', 'Examen', 100);
    for (const m of ['B1', 'B2']) { g.inscribir(m, 'C1'); g.capturarCalificacion(m, 'C1', 'Examen', 9); }
    assert.deepEqual(g.estudiantesPorPromedio().map((e) => e.nombre), ['Abel', 'Zoe']);
});

test('promedios por curso lista todos los cursos con sus inscritos', () => {
    const g = gestorConNotas();
    assert.deepEqual(g.promediosPorCurso(), [
        { clave: 'MAT101', nombre: 'Matemáticas', inscritos: 2, promedio: 7.9 },
        { clave: 'FIS201', nombre: 'Física', inscritos: 1, promedio: 7 },
    ]);
});
