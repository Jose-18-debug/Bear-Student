import { ValidacionError } from '../errores.js';
import { ReportesAcademicos, exportarReporte, nombreArchivoReporte } from '../reportes.js';

const ESTADOS_REPORTE = { APROBADO: 'Aprobado', REPROBADO: 'Reprobado', SIN_EVALUAR: 'Sin evaluar' };

export function iniciarReportes(gestor, { $, aviso, llenarTabla, llenarSelect }) {
    const reportes = new ReportesAcademicos(gestor);
    let reporteActual = null;

    const boton = document.createElement('button');
    boton.className = 'tab';
    boton.dataset.tab = 'reportes';
    boton.setAttribute('role', 'tab');
    boton.textContent = 'Reportes';
    const lista = document.querySelector('.tab-lista');
    lista.append(boton);

    const panel = document.createElement('div');
    panel.className = 'tab-panel';
    panel.id = 'tab-reportes';
    panel.hidden = true;
    panel.innerHTML = `
        <div class="formularios">
            <div>
                <label for="rep-grupo">Grupo</label>
                <select id="rep-grupo"></select>
            </div>
            <div>
                <label for="rep-curso">Asignatura</label>
                <select id="rep-curso"></select>
            </div>
        </div>
        <div class="botones">
            <button type="button" class="btn primario" id="rep-generar">Generar reporte</button>
            <button type="button" class="btn secundario" id="rep-json" disabled>Descargar JSON</button>
            <button type="button" class="btn secundario" id="rep-csv" disabled>Descargar CSV</button>
            <button type="button" class="btn secundario" id="rep-csv-resumen" disabled>Descargar CSV de resumen</button>
        </div>
        <h2 id="rep-titulo"></h2>
        <p class="nota" id="rep-estadisticas"></p>
        <table>
            <thead><tr><th>Matrícula</th><th>Nombre</th><th>Grupo</th><th class="num">Promedio</th><th>Estado</th><th>Riesgo</th></tr></thead>
            <tbody id="tbody-reporte"></tbody>
        </table>
        <p class="vacio" id="vacio-reporte">Elige un grupo y/o una asignatura y presiona Generar reporte.</p>
        <div id="rep-asignaturas" hidden>
            <h2>Por asignatura</h2>
            <table>
                <thead><tr><th>Asignatura</th><th class="num">Estudiantes</th><th class="num">Promedio</th><th class="num">Máxima</th><th class="num">Mínima</th><th class="num">Aprobación (%)</th></tr></thead>
                <tbody id="tbody-reporte-asignaturas"></tbody>
            </table>
            <p class="vacio" id="vacio-reporte-asignaturas">No hay asignaturas con estudiantes.</p>
        </div>
        <div id="rep-grupos" hidden>
            <h2>Por grupo</h2>
            <table>
                <thead><tr><th>Grupo</th><th class="num">Estudiantes</th><th class="num">Promedio</th><th class="num">Máxima</th><th class="num">Mínima</th><th class="num">Aprobación (%)</th></tr></thead>
                <tbody id="tbody-reporte-grupos"></tbody>
            </table>
            <p class="vacio" id="vacio-reporte-grupos">No hay grupos con estudiantes.</p>
        </div>`;
    lista.parentElement.append(panel);

    function llenarFiltros() {
        const grupos = new Map();
        for (const e of gestor.estudiantes.values()) {
            const llave = e.grupo.toLowerCase();
            if (!grupos.has(llave)) grupos.set(llave, e.grupo);
        }
        const ordenados = [...grupos.values()].sort((a, b) => a.localeCompare(b, 'es', { numeric: true }));
        llenarSelect($('rep-grupo'), [['', 'Todos los grupos'], ...ordenados.map((g) => [g, g])]);
        llenarSelect($('rep-curso'), [['', 'Todas las asignaturas'],
            ...[...gestor.cursos.values()].map((c) => [c.clave, `${c.clave} | ${c.nombre}`])]);
    }

    function filaDesglose(nombre, est) {
        return [nombre, est.totalEstudiantes, est.promedioGeneral, est.calificacionMaxima,
            est.calificacionMinima, est.porcentajeAprobacion];
    }

    function mostrar(reporte) {
        const est = reporte.estadisticas;
        $('rep-titulo').textContent = reporte.titulo;
        $('rep-estadisticas').textContent =
            `Estudiantes: ${est.totalEstudiantes} · Evaluados: ${est.totalEvaluados} · Aprobados: ${est.totalAprobados}` +
            ` · Reprobados: ${est.totalReprobados} · En riesgo: ${est.totalEnRiesgo} · Promedio general: ${est.promedioGeneral}` +
            ` · Máxima: ${est.calificacionMaxima} · Mínima: ${est.calificacionMinima} · Aprobación: ${est.porcentajeAprobacion}%`;
        llenarTabla('reporte', reporte.estudiantes.map((e) => [
            e.matricula, e.nombre, e.grupo, typeof e.promedio === 'number' ? e.promedio : '—',
            ESTADOS_REPORTE[e.estado] ?? e.estado, e.enRiesgo ? 'En riesgo' : '']));
        $('rep-asignaturas').hidden = reporte.porAsignatura === null;
        llenarTabla('reporte-asignaturas', (reporte.porAsignatura ?? []).map((a) =>
            filaDesglose(`${a.clave} | ${a.nombre}`, a.estadisticas)));
        $('rep-grupos').hidden = reporte.porGrupo === null;
        llenarTabla('reporte-grupos', (reporte.porGrupo ?? []).map((g) => filaDesglose(g.grupo, g.estadisticas)));
        ['rep-json', 'rep-csv', 'rep-csv-resumen'].forEach((id) => { $(id).disabled = false; });
    }

    function generar() {
        try {
            reporteActual = reportes.reporte({ grupo: $('rep-grupo').value, clave: $('rep-curso').value });
            mostrar(reporteActual);
            aviso('Reporte generado.', 'ok');
        } catch (error) {
            if (!(error instanceof ValidacionError)) throw error;
            aviso(error.message, 'error');
        }
    }

    function descargar(formato) {
        const nombre = nombreArchivoReporte(reporteActual, formato);
        const texto = exportarReporte(reporteActual, formato);
        const esJson = formato === 'json';
        const archivo = new Blob([esJson ? texto : `\uFEFF${texto}`],
            { type: esJson ? 'application/json;charset=utf-8' : 'text/csv;charset=utf-8' });
        const url = URL.createObjectURL(archivo);
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = nombre;
        document.body.append(enlace);
        enlace.click();
        enlace.remove();
        URL.revokeObjectURL(url);
        aviso(`Archivo ${nombre} descargado.`, 'ok');
    }

    boton.addEventListener('click', llenarFiltros);
    $('rep-generar').addEventListener('click', generar);
    $('rep-json').addEventListener('click', () => descargar('json'));
    $('rep-csv').addEventListener('click', () => descargar('csv'));
    $('rep-csv-resumen').addEventListener('click', () => descargar('csv-resumen'));
    llenarFiltros();
}