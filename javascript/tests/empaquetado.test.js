import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { generarHtml, RUTA_SALIDA } from '../scripts/empaquetar.js';

// Si alguien cambia src/ y olvida ejecutar "npm run empaquetar", esta prueba lo avisa
test('abrir-bear-student.html está al día con el código de src/', () => {
    const guardado = readFileSync(RUTA_SALIDA, 'utf8').replace(/\r\n/g, '\n');
    assert.equal(guardado, generarHtml(),
        'abrir-bear-student.html está desactualizado: ejecuta "npm run empaquetar" dentro de javascript/');
});

test('el archivo empaquetado no usa módulos, para poder abrirse con doble clic', () => {
    const html = generarHtml();
    assert.doesNotMatch(html, /type="module"/);
    assert.doesNotMatch(html, /^\s*(import|export)\s/m);
});
