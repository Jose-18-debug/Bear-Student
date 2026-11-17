// Junta la interfaz (HTML, CSS y los módulos de JavaScript) en un solo archivo, abrir-bear-student.html,
// que se abre con doble clic sin servidor. Los navegadores bloquean los módulos ES (import/export)
// cuando la página se abre como archivo (file://), por eso aquí se quitan y todo queda en un solo <script>.
//
// Hay que volver a generarlo cada vez que cambie algo en src/:   npm run empaquetar
import { readFileSync, writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';

const raiz = join(dirname(fileURLToPath(import.meta.url)), '..');
export const RUTA_SALIDA = join(raiz, 'abrir-bear-student.html');

// Orden en que se juntan: cada archivo va después de los que importa
const MODULOS = [
    'src/errores.js', 'src/validacion.js', 'src/modelo.js', 'src/gestion.js',
    'src/estadisticas.js', 'src/reportes.js', 'src/ui/reportes-ui.js', 'src/ui/app.js',
];

const leer = (ruta) => readFileSync(join(raiz, ruta), 'utf8').replace(/\r\n/g, '\n');

// Quita los import y la palabra export; falla si encuentra una forma que no sabe convertir
function sinModulos(codigo, ruta) {
    const limpio = codigo
        .replace(/^import\s[\s\S]*?from\s+['"][^'"]+['"];?[ \t]*\n/gm, '')
        .replace(/^export\s+(?=(class|function|const|let)\s)/gm, '');
    if (/^\s*(import|export)\b/m.test(limpio)) {
        throw new Error(`${ruta}: hay un import/export que el empaquetador no sabe convertir`);
    }
    return `// ---- ${ruta} ----\n${limpio.trim()}\n`;
}

// Cambia el texto exacto `buscar` por `nuevo`; si no aparece, avisa en lugar de generar un HTML roto
function reemplazar(texto, buscar, nuevo) {
    if (!texto.includes(buscar)) throw new Error(`No se encontró en index.html: ${buscar}`);
    return texto.replace(buscar, () => nuevo);
}

export function generarHtml() {
    const js = MODULOS.map((ruta) => sinModulos(leer(ruta), ruta)).join('\n');
    // El logo se queda como archivo aparte; la ruta cambia porque el HTML queda en la carpeta javascript/
    const css = leer('src/ui/estilos.css').replace("url('img/logo.png')", "url('src/ui/img/logo.png')");
    if (js.includes('</script') || css.includes('</style')) {
        throw new Error('El código contiene </script> o </style> y rompería el HTML');
    }

    let html = leer('src/ui/index.html');
    html = reemplazar(html, '<link rel="stylesheet" href="estilos.css">', `<style>\n${css.trim()}\n</style>`);
    html = reemplazar(html, '<script type="module" src="app.js"></script>',
        `<script>\n(() => {\n'use strict';\n${js}})();\n</script>`);
    return '<!-- ARCHIVO GENERADO por scripts/empaquetar.js. No lo edites a mano: cambia src/ y ejecuta npm run empaquetar -->\n'
        + html;
}

// Solo escribe el archivo cuando se ejecuta directamente (no cuando lo importa la prueba)
if (process.argv[1] && fileURLToPath(import.meta.url) === process.argv[1]) {
    writeFileSync(RUTA_SALIDA, generarHtml(), 'utf8');
    console.log(`Generado ${RUTA_SALIDA}`);
}