import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { GestorAcademico } from '../src/gestion.js';
import { ValidacionError } from '../src/errores.js';
import {
    ReportesAcademicos, reporteAJSON, reporteACSV, resumenACSV, exportarReporte, nombreArchivoReporte,
} from '../src/reportes.js';

//Dos grupos (302 y 303), dos asignaturas y un estudiante sin calificaciones
//Promedios finales de MAT101: Ana 8.6, Luis 7.6, Sofía 9, Mario sin evaluar
//Promedios generales: Ana (8.6 y 6) 7.3, Luis 7.6, Sofía (9 y 5) 7, Mario sin evaluar
function sistemaConDatos() {
    const g = new GestorAcademico();
    g.registrarEstudiante('A001', 'Ana Pérez', '302');
    g.registrarEstudiante('A002', 'Luis Gómez', '302');
    g.registrarEstudiante('A003', 'Sofía Ruiz', '303');
    g.registrarEstudiante('A004', 'Mario Soto', '303');
    g.registrarCurso('MAT101', 'Matemáticas');
    g.registrarCurso('FIS101', 'Física');
    for (const m of ['A001', 'A002', 'A003', 'A004']) g.inscribir(m, 'MAT101');
    g.inscribir('A001', 'FIS101');
    g.inscribir('A003', 'FIS101');
    g.registrarEvaluacion('MAT101', 'Parcial', 40);
    g.registrarEvaluacion('MAT101', 'Final', 60);
    g.registrarEvaluacion('FIS101', 'Examen', 100);
    const { errores } = g.procesarCalificaciones([
        { matricula: 'A001', clave: 'MAT101', evaluacion: 'Parcial', valor: 8 },
        { matricula: 'A001', clave: 'MAT101', evaluacion: 'Final', valor: 9 },
        { matricula: 'A002', clave: 'MAT101', evaluacion: 'Parcial', valor: 10 },
        { matricula: 'A002', clave: 'MAT101', evaluacion: 'Final', valor: 6 },
        { matricula: 'A003', clave: 'MAT101', evaluacion: 'Parcial', valor: 9 },
        { matricula: 'A003', clave: 'MAT101', evaluacion: 'Final', valor: 9 },
        { matricula: 'A001', clave: 'FIS101', evaluacion: 'Examen', valor: 6 },
        { matricula: 'A003', clave: 'FIS101', evaluacion: 'Examen', valor: 5 },
        { matricula: 'A004', clave: 'MAT101', evaluacion: 'Parcial', valor: 11 },   //fuera de rango: no cuenta
    ]);
    assert.equal(errores.length, 1);
    return { g, reportes: new ReportesAcademicos(g) };
}

const matriculas = (reporte) => reporte.estudiantes.map((e) => e.matricula);

describe('ReportesAcademicos - por asignatura', () => {
    it('lista a los inscritos ordenados por promedio, los sin evaluar al final', () => {
        const { reportes } = sistemaConDatos();
        const r = reportes.reportePorAsignatura('MAT101');
        assert.equal(r.titulo, 'Reporte de la asignatura MAT101 - Matemáticas');
        assert.deepEqual(r.asignatura, { clave: 'MAT101', nombre: 'Matemáticas' });
        assert.equal(r.grupo, null);
        assert.deepEqual(matriculas(r), ['A003', 'A001', 'A002', 'A004']);
        assert.equal(r.estudiantes[3].estado, 'SIN_EVALUAR');
        assert.equal(r.estudiantes[3].promedio, null);
    });

    it('calcula las estadísticas de la asignatura', () => {
        const { reportes } = sistemaConDatos();
        assert.deepEqual(reportes.reportePorAsignatura('MAT101').estadisticas, {
            totalEstudiantes: 4, totalEvaluados: 3, totalAprobados: 3, totalReprobados: 0, totalEnRiesgo: 0,
            promedioGeneral: 8.4, calificacionMaxima: 9, calificacionMinima: 7.6, porcentajeAprobacion: 100,
        });
    });

    it('trae el desglose por grupo y no el de asignaturas', () => {
        const { reportes } = sistemaConDatos();
        const r = reportes.reportePorAsignatura('MAT101');
        assert.equal(r.porAsignatura, null);
        assert.deepEqual(r.porGrupo.map((x) => x.grupo), ['302', '303']);
        assert.equal(r.porGrupo[0].estadisticas.promedioGeneral, 8.1);
        assert.equal(r.porGrupo[1].estadisticas.totalEvaluados, 1);
        assert.equal(r.porGrupo[1].estadisticas.totalEstudiantes, 2);
    });
});

describe('ReportesAcademicos - por grupo', () => {
    it('solo trae a los estudiantes del grupo con su promedio general', () => {
        const { reportes } = sistemaConDatos();
        const r = reportes.reportePorGrupo('302');
        assert.equal(r.titulo, 'Reporte del grupo 302');
        assert.deepEqual(matriculas(r), ['A002', 'A001']);
        assert.deepEqual(r.estudiantes.map((e) => e.promedio), [7.6, 7.3]);
        assert.equal(r.estadisticas.totalEnRiesgo, 1);
        assert.equal(r.estadisticas.promedioGeneral, 7.45);
    });

    it('desglosa el grupo por asignatura y omite las que no tienen a nadie del grupo', () => {
        const { reportes } = sistemaConDatos();
        const r = reportes.reportePorGrupo('302');
        assert.equal(r.porGrupo, null);
        assert.deepEqual(r.porAsignatura.map((a) => a.clave), ['MAT101', 'FIS101']);
        assert.equal(r.porAsignatura[0].estadisticas.promedioGeneral, 8.1);
        const fisica = r.porAsignatura[1].estadisticas;
        assert.equal(fisica.totalReprobados, 1);
        assert.equal(fisica.porcentajeAprobacion, 0);
    });

    it('ignora mayúsculas y espacios al buscar el grupo', () => {
        const g = new GestorAcademico();
        g.registrarEstudiante('B1', 'Uno', '3A');
        const r = new ReportesAcademicos(g).reportePorGrupo('  3a ');
        assert.equal(r.estudiantes.length, 1);
        assert.equal(r.grupo, '3a');
    });
});

describe('ReportesAcademicos - general y combinado', () => {
    it('el reporte general trae a todos y los dos desgloses', () => {
        const { reportes } = sistemaConDatos();
        const r = reportes.reporteGeneral();
        assert.equal(r.titulo, 'Reporte general');
        assert.deepEqual(matriculas(r), ['A002', 'A001', 'A003', 'A004']);
        assert.deepEqual(r.estadisticas, {
            totalEstudiantes: 4, totalEvaluados: 3, totalAprobados: 3, totalReprobados: 0, totalEnRiesgo: 2,
            promedioGeneral: 7.3, calificacionMaxima: 7.6, calificacionMinima: 7, porcentajeAprobacion: 100,
        });
        assert.deepEqual(r.porAsignatura.map((a) => a.clave), ['MAT101', 'FIS101']);
        assert.deepEqual(r.porGrupo.map((x) => x.grupo), ['302', '303']);
    });

    it('un grupo en una asignatura', () => {
        const { reportes } = sistemaConDatos();
        const r = reportes.reporte({ grupo: '302', clave: 'FIS101' });
        assert.equal(r.titulo, 'Reporte del grupo 302 en FIS101 - Física');
        assert.deepEqual(matriculas(r), ['A001']);
        assert.equal(r.estudiantes[0].estado, 'REPROBADO');
        assert.equal(r.porAsignatura, null);
        assert.equal(r.porGrupo, null);
    });

    it('un grupo sin estudiantes en esa asignatura da un reporte vacío', () => {
        const { g, reportes } = sistemaConDatos();
        g.registrarCurso('QUI101', 'Química');
        g.inscribir('A003', 'QUI101');
        const r = reportes.reporte({ grupo: '302', clave: 'QUI101' });
        assert.deepEqual(r.estudiantes, []);
        assert.equal(r.estadisticas.totalEstudiantes, 0);
        assert.equal(r.estadisticas.promedioGeneral, 0);
    });

    it('trata grupo o clave vacíos como "todos"', () => {
        const { reportes } = sistemaConDatos();
        assert.deepEqual(reportes.reporte({ grupo: '', clave: '  ' }), reportes.reporteGeneral());
        assert.deepEqual(reportes.reporte(), reportes.reporteGeneral());
    });

    it('sin datos devuelve listas vacías y estadísticas en cero', () => {
        const r = new ReportesAcademicos(new GestorAcademico()).reporteGeneral();
        assert.deepEqual(r.estudiantes, []);
        assert.deepEqual(r.porAsignatura, []);
        assert.deepEqual(r.porGrupo, []);
        assert.equal(r.estadisticas.totalEstudiantes, 0);
        assert.equal(r.estadisticas.promedioGeneral, 0);
    });
});

describe('ReportesAcademicos - validaciones', () => {
    it('rechaza un grupo o una asignatura que no existen', () => {
        const { reportes } = sistemaConDatos();
        assert.throws(() => reportes.reportePorGrupo('999'), ValidacionError);
        assert.throws(() => reportes.reportePorAsignatura('NOPE'), ValidacionError);
        assert.throws(() => reportes.reporte({ grupo: '999' }), ValidacionError);
        assert.throws(() => reportes.reporte({ clave: 'NOPE' }), ValidacionError);
    });

    it('rechaza grupo o clave vacíos en los reportes que los exigen', () => {
        const { reportes } = sistemaConDatos();
        for (const v of ['', '   ', null, undefined]) {
            assert.throws(() => reportes.reportePorGrupo(v), ValidacionError);
            assert.throws(() => reportes.reportePorAsignatura(v), ValidacionError);
        }
    });
});

describe('exportar - JSON', () => {
    it('el JSON se puede leer de vuelta y queda igual al reporte', () => {
        const { reportes } = sistemaConDatos();
        const r = reportes.reporteGeneral();
        assert.deepEqual(JSON.parse(reporteAJSON(r)), r);
        assert.equal(exportarReporte(r, 'json'), reporteAJSON(r));
    });
});

describe('exportar - CSV', () => {
    it('una fila por estudiante con encabezado, promedio vacío si no hay calificaciones', () => {
        const { reportes } = sistemaConDatos();
        assert.equal(reporteACSV(reportes.reportePorAsignatura('MAT101')), [
            'matricula,nombre,grupo,promedio,estado,enRiesgo',
            'A003,Sofía Ruiz,303,9,APROBADO,false',
            'A001,Ana Pérez,302,8.6,APROBADO,false',
            'A002,Luis Gómez,302,7.6,APROBADO,false',
            'A004,Mario Soto,303,,SIN_EVALUAR,false',
        ].join('\r\n'));
    });

    it('el resumen trae el total del reporte y su desglose', () => {
        const { reportes } = sistemaConDatos();
        assert.equal(resumenACSV(reportes.reporteGeneral()), [
            'ambito,nombre,totalEstudiantes,totalEvaluados,totalAprobados,totalReprobados,totalEnRiesgo,promedioGeneral,calificacionMaxima,calificacionMinima,porcentajeAprobacion',
            'reporte,Reporte general,4,3,3,0,2,7.3,7.6,7,100',
            'asignatura,MAT101 - Matemáticas,4,3,3,0,0,8.4,9,7.6,100',
            'asignatura,FIS101 - Física,2,2,0,2,2,5.5,6,5,0',
            'grupo,302,2,2,2,0,1,7.45,7.6,7.3,100',
            'grupo,303,2,1,1,0,1,7,7,7,100',
        ].join('\r\n'));
    });

    it('pone entre comillas lo que trae comas, comillas o saltos de línea', () => {
        const g = new GestorAcademico();
        g.registrarEstudiante('C1', 'Pérez, Juan "Chuy"', '1');
        const csv = reporteACSV(new ReportesAcademicos(g).reporteGeneral());
        assert.equal(csv.split('\r\n')[1], 'C1,"Pérez, Juan ""Chuy""",1,,SIN_EVALUAR,false');
    });

    it('un nombre que empieza como fórmula no se ejecuta en Excel', () => {
        const g = new GestorAcademico();
        g.registrarEstudiante('C1', '=SUMA(1+1)', '1');
        g.registrarEstudiante('C2', '@cmd', '1');
        const lineas = reporteACSV(new ReportesAcademicos(g).reporteGeneral()).split('\r\n');
        assert.ok(lineas.some((l) => l.startsWith("C1,'=SUMA(1+1),")));
        assert.ok(lineas.some((l) => l.startsWith("C2,'@cmd,")));
    });

    it('un nombre con salto de línea sigue siendo una sola fila lógica', () => {
        const g = new GestorAcademico();
        g.registrarEstudiante('C1', 'Linea1\nLinea2', '1');
        const csv = reporteACSV(new ReportesAcademicos(g).reporteGeneral());
        assert.ok(csv.includes('C1,"Linea1\nLinea2",1'));
    });
});

describe('exportarReporte y nombreArchivoReporte', () => {
    it('acepta json, csv y csv-resumen sin importar mayúsculas', () => {
        const { reportes } = sistemaConDatos();
        const r = reportes.reportePorGrupo('302');
        assert.equal(exportarReporte(r, 'CSV'), reporteACSV(r));
        assert.equal(exportarReporte(r, ' csv-resumen '), resumenACSV(r));
    });

    it('rechaza un formato que no existe', () => {
        const { reportes } = sistemaConDatos();
        const r = reportes.reporteGeneral();
        for (const f of ['xml', '', null, undefined]) {
            assert.throws(() => exportarReporte(r, f), ValidacionError);
        }
    });

    it('arma un nombre de archivo seguro', () => {
        const { reportes } = sistemaConDatos();
        assert.equal(nombreArchivoReporte(reportes.reporteGeneral(), 'json'), 'reporte-general.json');
        assert.equal(nombreArchivoReporte(reportes.reporte({ grupo: '302', clave: 'MAT101' }), 'csv'),
            'reporte-302-MAT101.csv');
        assert.equal(nombreArchivoReporte(reportes.reportePorGrupo('303'), 'csv-resumen'),
            'reporte-303-resumen.csv');
        const g = new GestorAcademico();
        g.registrarEstudiante('B1', 'Uno', '../A B');
        assert.equal(nombreArchivoReporte(new ReportesAcademicos(g).reportePorGrupo('../A B'), 'csv'),
            'reporte-A-B.csv');
    });
});