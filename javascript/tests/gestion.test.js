import test from 'node:test';
import assert from 'node:assert/strict';
import { GestorAcademico } from '../src/gestion.js';
import { ValidacionError } from '../src/errores.js';

test('rechaza matrícula duplicada', () => {
    const g = new GestorAcademico();
    g.registrarEstudiante('A001', 'Ana Pérez', '302');
    assert.throws(() => g.registrarEstudiante('A001', 'Otra Persona', '302'), ValidacionError);
});

test('rechaza nombre vacío', () => {
    const g = new GestorAcademico();
    assert.throws(() => g.registrarEstudiante('A002', '   ', '302'), ValidacionError);
});

test('inscribe a un estudiante y rechaza la inscripción duplicada', () => {
    const g = new GestorAcademico();
    g.registrarEstudiante('A001', 'Ana Pérez', '302');
    g.registrarCurso('MAT101', 'Matemáticas');
    g.inscribir('A001', 'MAT101');
    assert.equal(g.estaInscrito('A001', 'MAT101'), true);
    assert.throws(() => g.inscribir('A001', 'MAT101'), ValidacionError);
});