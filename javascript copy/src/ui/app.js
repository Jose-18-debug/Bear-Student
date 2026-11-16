import { GestorAcademico } from '../gestion.js';
import { ValidacionError } from '../errores.js';

const gestor = new GestorAcademico();     // aquí vive todo el estado; la página solo lo muestra
const $ = (id) => document.getElementById(id);
const campos = {
    estudiante: [$('matricula'), $('nombre'), $('grupo')],
    curso: [$('clave'), $('nombre-curso')],
    evaluacion: [$('ev-nombre'), $('ev-ponderacion')],
    calificacion: [$('cal-valor')],
};
let temporizadorAviso;

// Muestra un aviso verde (ok) o rojo (error) durante 4 segundos.
function aviso(texto, tipo) {
    const caja = $('aviso');
    caja.textContent = texto;
    caja.className = `aviso ${tipo}`;
    clearTimeout(temporizadorAviso);
    temporizadorAviso = setTimeout(() => { caja.textContent = ''; caja.className = 'aviso'; }, 4000);
}

// Marca en rojo los campos vacíos (o el primero si ninguno está vacío) y manda el foco ahí.
function marcarError(lista) {
    const vacios = lista.filter((c) => !c.value.trim());
    const marcados = vacios.length ? vacios : lista.slice(0, 1);
    marcados.forEach((c) => c.classList.add('invalid'));
    marcados[0].focus();
}

function limpiar(lista) {
    lista.forEach((c) => { c.value = ''; c.classList.remove('invalid'); });
    lista[0].focus();
}

// Ejecuta una acción del gestor; solo atrapa los errores de validación y muestra su mensaje tal cual.
function intentar(accion, mensajeOk, lista) {
    try {
        accion();
        if (lista) limpiar(lista);
        actualizar();
        aviso(mensajeOk, 'ok');
    } catch (error) {
        if (!(error instanceof ValidacionError)) throw error;
        if (lista) marcarError(lista);
        aviso(error.message, 'error');
    }
}

// Rellena una tabla; textContent evita que un nombre con < o > rompa la página.
function llenarTabla(nombre, filas) {
    $(`tbody-${nombre}`).replaceChildren(...filas.map((fila) => {
        const tr = document.createElement('tr');
        fila.forEach((valor) => {
            const td = document.createElement('td');
            td.textContent = valor;
            if (typeof valor === 'number') td.className = 'num';
            tr.append(td);
        });
        return tr;
    }));
    $(`vacio-${nombre}`).hidden = filas.length > 0;
}

// opciones: lista de [valor, texto]. Conserva la selección si sigue existiendo.
function llenarSelect(select, opciones) {
    const previo = select.value;
    select.replaceChildren(...opciones.map(([valor, texto]) => new Option(texto, valor)));
    if (opciones.some(([valor]) => valor === previo)) select.value = previo;
}

// Los selects de captura dependen uno del otro: estudiante -> sus cursos -> evaluaciones del curso.
function actualizarSelectsCalificacion() {
    const matricula = $('cal-estudiante').value;
    llenarSelect($('cal-curso'), gestor.cursosDeEstudiante(matricula).map((clave) =>
        [clave, `${clave} | ${gestor.cursos.get(clave).nombre}`]));
    llenarSelect($('cal-evaluacion'), gestor.evaluacionesDeCurso($('cal-curso').value).map((ev) =>
        [ev.nombre, `${ev.nombre} (${ev.ponderacion}%)`]));
    $('btn-calificacion').disabled = $('cal-evaluacion').options.length === 0;
}

function mostrarPonderacionDisponible() {
    const clave = $('ev-curso').value;
    $('ev-disponible').textContent = clave
        ? `Ponderación disponible en ${clave}: ${+(100 - gestor.ponderacionTotal(clave)).toFixed(2)}%`
        : '';
}

// Muestra las calificaciones del estudiante elegido, agrupadas por curso, con sus promedios.
function mostrarConsulta() {
    const matricula = $('con-estudiante').value;
    if (!matricula) {
        $('con-resumen').textContent = '';
        llenarTabla('consulta', []);
        return;
    }
    const { cursos, promedioGeneral } = gestor.consultarEstudiante(matricula);
    $('con-resumen').textContent = `Promedio general: ${promedioGeneral === null ? 'sin calificaciones' : promedioGeneral}`;
    llenarTabla('consulta', cursos.flatMap((curso) =>
        curso.calificaciones.map((c) => [curso.nombre, c.evaluacion, c.ponderacion, c.valor, curso.promedioFinal])));
}

function mostrarRanking() {
    const descendente = $('orden-ranking').value === 'desc';
    llenarTabla('ranking', gestor.estudiantesPorPromedio(descendente).map((e, i) =>
        [i + 1, e.matricula, e.nombre, e.grupo, e.promedio === null ? '—' : e.promedio]));
}

// Vuelve a pintar todo desde el estado del gestor.
function actualizar() {
    const estudiantes = [...gestor.estudiantes.values()];
    const cursos = [...gestor.cursos.values()];

    llenarSelect($('sel-estudiante'), estudiantes.map((e) => [e.matricula, `${e.matricula} | ${e.nombre} (${e.grupo})`]));
    llenarSelect($('sel-curso'), cursos.map((c) => [c.clave, `${c.clave} | ${c.nombre}`]));

    llenarTabla('estudiantes', estudiantes.map((e) =>
        [e.matricula, e.nombre, e.grupo, gestor.cursosDeEstudiante(e.matricula).length]));
    llenarTabla('cursos', cursos.map((c) =>
        [c.clave, c.nombre, gestor.estudiantesDeCurso(c.clave).length]));
    const inscripciones = estudiantes.flatMap((e) =>
        gestor.cursosDeEstudiante(e.matricula).map((clave) => [e.nombre, gestor.cursos.get(clave).nombre]));
    llenarTabla('inscripciones', inscripciones);

    llenarSelect($('ev-curso'), cursos.map((c) => [c.clave, `${c.clave} | ${c.nombre}`]));
    llenarSelect($('cal-estudiante'), estudiantes.map((e) => [e.matricula, `${e.matricula} | ${e.nombre}`]));
    actualizarSelectsCalificacion();
    mostrarPonderacionDisponible();
    $('btn-evaluacion').disabled = cursos.length === 0;

    const evaluaciones = cursos.flatMap((c) =>
        gestor.evaluacionesDeCurso(c.clave).map((ev) => [c.nombre, ev.nombre, ev.ponderacion]));
    llenarTabla('evaluaciones', evaluaciones);
    const calificaciones = estudiantes.flatMap((e) => gestor.cursosDeEstudiante(e.matricula).flatMap((clave) =>
        gestor.calificacionesDe(e.matricula, clave).map((c) =>
            [e.nombre, gestor.cursos.get(clave).nombre, c.evaluacion, c.valor])));
    llenarTabla('calificaciones', calificaciones);

    llenarSelect($('con-estudiante'), estudiantes.map((e) => [e.matricula, `${e.matricula} | ${e.nombre}`]));
    mostrarConsulta();
    llenarTabla('promedios', gestor.promediosPorCurso().map((c) =>
        [c.clave, c.nombre, c.inscritos, c.promedio === null ? '—' : c.promedio]));
    mostrarRanking();

    $('btn-inscribir').disabled = estudiantes.length === 0 || cursos.length === 0;
    $('estado').textContent =
        `Estudiantes: ${estudiantes.length}     Cursos: ${cursos.length}     Inscripciones: ${inscripciones.length}` +
        `     Evaluaciones: ${evaluaciones.length}     Calificaciones: ${calificaciones.length}`;
}

// ---- Eventos ----
$('form-estudiante').addEventListener('submit', (e) => {
    e.preventDefault();
    intentar(() => gestor.registrarEstudiante($('matricula').value, $('nombre').value, $('grupo').value),
        'Estudiante registrado.', campos.estudiante);
});
$('form-curso').addEventListener('submit', (e) => {
    e.preventDefault();
    intentar(() => gestor.registrarCurso($('clave').value, $('nombre-curso').value),
        'Curso registrado.', campos.curso);
});
$('btn-inscribir').addEventListener('click', () =>
    intentar(() => gestor.inscribir($('sel-estudiante').value, $('sel-curso').value), 'Inscripción realizada.'));
$('form-evaluacion').addEventListener('submit', (e) => {
    e.preventDefault();
    intentar(() => gestor.registrarEvaluacion($('ev-curso').value, $('ev-nombre').value, $('ev-ponderacion').value),
        'Evaluación registrada.', campos.evaluacion);
});
$('form-calificacion').addEventListener('submit', (e) => {
    e.preventDefault();
    intentar(() => gestor.capturarCalificacion($('cal-estudiante').value, $('cal-curso').value,
        $('cal-evaluacion').value, $('cal-valor').value), 'Calificación guardada.', campos.calificacion);
});
$('cal-estudiante').addEventListener('change', actualizarSelectsCalificacion);
$('cal-curso').addEventListener('change', actualizarSelectsCalificacion);
$('ev-curso').addEventListener('change', mostrarPonderacionDisponible);
$('con-estudiante').addEventListener('change', mostrarConsulta);
$('orden-ranking').addEventListener('change', mostrarRanking);

// Convierte el texto del lote en objetos para el gestor. Guarda el número de línea de cada uno
// para que los errores digan en qué línea del cuadro de texto están.
function leerLote(texto) {
    const datos = [];
    const lineas = [];
    const erroresFormato = [];
    texto.split(/\r?\n/).forEach((linea, i) => {
        if (!linea.trim()) return;
        const partes = linea.split(',').map((p) => p.trim());
        if (partes.length !== 4) {
            erroresFormato.push({ linea: i + 1, texto: linea,
                mensaje: 'Debe tener 4 valores separados por coma (usa punto decimal, no coma)' });
            return;
        }
        const [matricula, clave, evaluacion, valor] = partes;
        datos.push({ matricula, clave, evaluacion, valor });
        lineas.push({ linea: i + 1, texto: linea });
    });
    return { datos, lineas, erroresFormato };
}

$('btn-lote').addEventListener('click', () => {
    const { datos, lineas, erroresFormato } = leerLote($('lote').value);
    if (datos.length === 0 && erroresFormato.length === 0) {
        $('lote').classList.add('invalid');
        $('lote').focus();
        aviso('Escribe al menos una calificación.', 'error');
        return;
    }
    const { exitosas, errores } = gestor.procesarCalificaciones(datos);
    const todos = [...erroresFormato,
        ...errores.map((e) => ({ ...lineas[e.fila - 1], mensaje: e.mensaje }))].sort((a, b) => a.linea - b.linea);
    $('lote-errores').replaceChildren(...todos.map((e) => {
        const li = document.createElement('li');
        li.textContent = `Línea ${e.linea} (${e.texto.trim()}): ${e.mensaje}`;
        return li;
    }));
    if (todos.length === 0) $('lote').value = '';
    actualizar();
    aviso(`${exitosas.length} guardadas, ${todos.length} con error.`, todos.length ? 'error' : 'ok');
});
$('limpiar-lote').addEventListener('click', () => {
    $('lote').value = '';
    $('lote').classList.remove('invalid');
    $('lote-errores').replaceChildren();
    $('lote').focus();
});
$('lote').addEventListener('input', () => $('lote').classList.remove('invalid'));

$('limpiar-estudiante').addEventListener('click', () => limpiar(campos.estudiante));
$('limpiar-evaluacion').addEventListener('click', () => limpiar(campos.evaluacion));
$('limpiar-calificacion').addEventListener('click', () => limpiar(campos.calificacion));
$('limpiar-curso').addEventListener('click', () => limpiar(campos.curso));

// Quita el rojo de un campo en cuanto el usuario vuelve a escribir.
Object.values(campos).flat().forEach((c) => c.addEventListener('input', () => c.classList.remove('invalid')));

// Pestañas: muestra solo el panel de la pestaña elegida.
document.querySelectorAll('.tab').forEach((boton) => {
    boton.addEventListener('click', () => {
        document.querySelectorAll('.tab').forEach((b) => b.classList.toggle('activa', b === boton));
        document.querySelectorAll('.tab-panel').forEach((p) => { p.hidden = p.id !== `tab-${boton.dataset.tab}`; });
    });
});

// Seleccionar una fila de cualquier tabla (queda en azul hielo).
document.querySelectorAll('tbody').forEach((cuerpo) => cuerpo.addEventListener('click', (e) => {
    const fila = e.target.closest('tr');
    if (!fila) return;
    cuerpo.querySelectorAll('tr.sel').forEach((tr) => tr.classList.remove('sel'));
    fila.classList.add('sel');
}));

actualizar();