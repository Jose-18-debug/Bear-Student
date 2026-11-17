import test from 'node:test';
import assert from 'node:assert/strict';
import { GestorAcademico } from '../src/gestion.js';
import { ValidacionError } from '../src/errores.js';

//Gestor con dos estudiantes inscritos en MAT101 y uno sin inscribir
function gestorConDatos() {
    const g = new GestorAcademico();
    g.registrarEstudiante('A001', 'Ana Pérez', '302');
    g.registrarEstudiante('A002', 'Luis Gómez', '302');
    g.registrarEstudiante('A003', 'Sofía Ruiz', '303');
    g.registrarCurso('MAT101', 'Matemáticas');
    g.inscribir('A001', 'MAT101');
    g.inscribir('A002', 'MAT101');
    return g;
}

// ---- Evaluaciones ----
test('registra evaluaciones y suma sus ponderaciones', () => {
    const g = gestorConDatos();
    g.registrarEvaluacion('MAT101', 'Parcial 1', 40);
    g.registrarEvaluacion('MAT101', 'Final', '60');
    assert.equal(g.evaluacionesDeCurso('MAT101').length, 2);
    assert.equal(g.ponderacionTotal('MAT101'), 100);
});

test('acepta la ponderación con el símbolo de porcentaje', () => {
    const g = gestorConDatos();
    const evaluacion = g.registrarEvaluacion('MAT101', 'Final', '100%');
    assert.equal(evaluacion.ponderacion, 100);
    assert.equal(g.ponderacionTotal('MAT101'), 100);
});

test('rechaza evaluación en un curso que no existe', () => {
    const g = gestorConDatos();
    assert.throws(() => g.registrarEvaluacion('FIS999', 'Parcial 1', 30), ValidacionError);
});

test('rechaza evaluación con nombre vacío o repetido en el mismo curso', () => {
    const g = gestorConDatos();
    assert.throws(() => g.registrarEvaluacion('MAT101', '  ', 30), ValidacionError);
    g.registrarEvaluacion('MAT101', 'Parcial 1', 30);
    assert.throws(() => g.registrarEvaluacion('MAT101', 'Parcial 1', 10), ValidacionError);
});

test('rechaza ponderación de 0, negativa, mayor a 100 o que no es número', () => {
    const g = gestorConDatos();
    for (const p of [0, -5, 101, 'abc', '']) {
        assert.throws(() => g.registrarEvaluacion('MAT101', `Eval ${p}`, p), ValidacionError);
    }
});

test('rechaza que las ponderaciones de un curso sumen más de 100', () => {
    const g = gestorConDatos();
    g.registrarEvaluacion('MAT101', 'Parcial 1', 70);
    assert.throws(() => g.registrarEvaluacion('MAT101', 'Final', 31), ValidacionError);
    assert.doesNotThrow(() => g.registrarEvaluacion('MAT101', 'Final', 30));
});

// ---- Calificaciones ----
test('captura una calificación y la consulta', () => {
    const g = gestorConDatos();
    g.registrarEvaluacion('MAT101', 'Parcial 1', 40);
    g.capturarCalificacion('A001', 'MAT101', 'Parcial 1', '8.5');
    const lista = g.calificacionesDe('A001', 'MAT101');
    assert.equal(lista.length, 1);
    assert.equal(lista[0].valor, 8.5);
});

test('rechaza calificación de un estudiante no inscrito o inexistente', () => {
    const g = gestorConDatos();
    g.registrarEvaluacion('MAT101', 'Parcial 1', 40);
    assert.throws(() => g.capturarCalificacion('A003', 'MAT101', 'Parcial 1', 9), ValidacionError);
    assert.throws(() => g.capturarCalificacion('Z999', 'MAT101', 'Parcial 1', 9), ValidacionError);
});

test('rechaza calificación en una evaluación que no existe', () => {
    const g = gestorConDatos();
    assert.throws(() => g.capturarCalificacion('A001', 'MAT101', 'Parcial 9', 9), ValidacionError);
});

test('rechaza calificaciones fuera de 0 a 10', () => {
    const g = gestorConDatos();
    g.registrarEvaluacion('MAT101', 'Parcial 1', 40);
    for (const v of [-1, 10.5, 'diez', '']) {
        assert.throws(() => g.capturarCalificacion('A001', 'MAT101', 'Parcial 1', v), ValidacionError);
    }
    assert.doesNotThrow(() => g.capturarCalificacion('A001', 'MAT101', 'Parcial 1', 0));
    assert.doesNotThrow(() => g.capturarCalificacion('A002', 'MAT101', 'Parcial 1', 10));
});

test('rechaza capturar dos veces la misma calificación', () => {
    const g = gestorConDatos();
    g.registrarEvaluacion('MAT101', 'Parcial 1', 40);
    g.capturarCalificacion('A001', 'MAT101', 'Parcial 1', 8);
    assert.throws(() => g.capturarCalificacion('A001', 'MAT101', 'Parcial 1', 9), ValidacionError);
    assert.equal(g.calificacionesDe('A001', 'MAT101')[0].valor, 8);
});

// ---- Procesamiento en lote ----
test('procesa varias calificaciones: guarda las válidas y reporta las que fallan', () => {
    const g = gestorConDatos();
    g.registrarEvaluacion('MAT101', 'Parcial 1', 40);
    const { exitosas, errores } = g.procesarCalificaciones([
        { matricula: 'A001', clave: 'MAT101', evaluacion: 'Parcial 1', valor: 9 },
        { matricula: 'A002', clave: 'MAT101', evaluacion: 'Parcial 1', valor: 12 },
        { matricula: 'A001', clave: 'MAT101', evaluacion: 'Parcial 1', valor: 7 },
        null,
        { matricula: 'A002', clave: 'MAT101', evaluacion: 'Parcial 1', valor: '6.5' },
    ]);
    assert.equal(exitosas.length, 2);
    assert.deepEqual(errores.map((e) => e.fila), [2, 3, 4]);
    assert.equal(g.calificacionesDe('A002', 'MAT101')[0].valor, 6.5);
});

test('rechaza procesar algo que no es una lista', () => {
    const g = gestorConDatos();
    assert.throws(() => g.procesarCalificaciones('A001,MAT101,Parcial 1,9'), ValidacionError);
});
