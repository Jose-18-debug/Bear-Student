import { GestorAcademico } from '../gestion.js';
import { ValidacionError } from '../errores.js';

const gestor = new GestorAcademico();     // aquí vive todo el estado; la página solo lo muestra
const $ = (id) => document.getElementById(id);
const campos = {
    estudiante: [$('matricula'), $('nombre'), $('grupo')],
    curso: [$('clave'), $('nombre-curso')],
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

    $('btn-inscribir').disabled = estudiantes.length === 0 || cursos.length === 0;
    $('estado').textContent =
        `Estudiantes: ${estudiantes.length}     Cursos: ${cursos.length}     Inscripciones: ${inscripciones.length}`;
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
$('limpiar-estudiante').addEventListener('click', () => limpiar(campos.estudiante));
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