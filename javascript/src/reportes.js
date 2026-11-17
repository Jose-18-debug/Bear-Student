import { ValidacionError } from '../../javascript copy/src/errores.js';
import { EstadisticasAcademicas } from '../../javascript copy/src/estadisticas.js';

class EstadisticasDeReporte extends EstadisticasAcademicas {
    resumir(clasificados) {
        return this._calcular(clasificados);
    }
}

const COLACION = new Intl.Collator('es');
const COLACION_NUMERICA = new Intl.Collator('es', { numeric: true });

function textoOpcional(valor) {
    return typeof valor === 'string' ? valor.trim() : '';
}

function mismoGrupo(a, b) {
    return String(a).trim().toLowerCase() === String(b).trim().toLowerCase();
}

function compararPorPromedio(a, b) {
    const sinA = typeof a.promedio !== 'number';
    const sinB = typeof b.promedio !== 'number';
    if (sinA !== sinB) return sinA ? 1 : -1;
    if (!sinA && a.promedio !== b.promedio) return b.promedio - a.promedio;
    return COLACION.compare(a.nombre, b.nombre) || COLACION.compare(a.matricula, b.matricula);
}

//Genera reportes a partir de GestorAcademico (Persona 1 y 2) y EstadisticasAcademicas (Persona 4)
export class ReportesAcademicos {
    constructor(gestor) {
        this.gestor = gestor;
        this.estadisticas = new EstadisticasDeReporte(gestor);
    }

    reporteGeneral() {
        return this.reporte({});
    }

    reportePorGrupo(grupo) {
        if (textoOpcional(grupo) === '') {
            throw new ValidacionError('El grupo no puede estar vacío');
        }
        return this.reporte({ grupo });
    }

    reportePorAsignatura(clave) {
        if (textoOpcional(clave) === '') {
            throw new ValidacionError('La clave no puede estar vacía');
        }
        return this.reporte({ clave });
    }


    reporte({ grupo, clave } = {}) {
        const grp = textoOpcional(grupo);
        const cla = textoOpcional(clave);

        let curso = null;
        if (cla !== '') {
            if (!this.gestor.cursos.has(cla)) {
                throw new ValidacionError(`El curso ${cla} no existe`);
            }
            curso = this.gestor.cursos.get(cla);
        }
        if (grp !== '') {
            const existe = [...this.gestor.estudiantes.values()].some((e) => mismoGrupo(e.grupo, grp));
            if (!existe) {
                throw new ValidacionError(`El grupo ${grp} no existe`);
            }
        }

        const base = curso
            ? this.estadisticas.clasificarEstudiantesPorCurso(cla)
            : this.estadisticas.clasificarEstudiantesGlobal();
        const clasificados = (grp === '' ? base : base.filter((e) => mismoGrupo(e.grupo, grp)))
            .sort(compararPorPromedio);

        return {
            titulo: this._titulo(grp, curso),
            grupo: grp === '' ? null : grp,
            asignatura: curso ? { clave: curso.clave, nombre: curso.nombre } : null,
            estadisticas: this.estadisticas.resumir(clasificados),
            estudiantes: clasificados,
            porAsignatura: curso ? null : this._porAsignatura(grp),
            porGrupo: grp === '' ? this._porGrupo(clasificados) : null,
        };
    }

    _titulo(grupo, curso) {
        const asignatura = curso ? `${curso.clave} - ${curso.nombre}` : '';
        if (grupo && curso) return `Reporte del grupo ${grupo} en ${asignatura}`;
        if (grupo) return `Reporte del grupo ${grupo}`;
        if (curso) return `Reporte de la asignatura ${asignatura}`;
        return 'Reporte general';
    }

    _porAsignatura(grupo) {
        const filas = [];
        for (const curso of this.gestor.cursos.values()) {
            const clasificados = this.estadisticas.clasificarEstudiantesPorCurso(curso.clave)
                .filter((e) => grupo === '' || mismoGrupo(e.grupo, grupo));
            if (clasificados.length === 0) continue;
            filas.push({
                clave: curso.clave,
                nombre: curso.nombre,
                estadisticas: this.estadisticas.resumir(clasificados),
            });
        }
        return filas;
    }

    _porGrupo(clasificados) {
        const grupos = new Map();
        for (const e of clasificados) {
            const llave = e.grupo.trim().toLowerCase();
            if (!grupos.has(llave)) grupos.set(llave, { grupo: e.grupo, lista: [] });
            grupos.get(llave).lista.push(e);
        }
        return [...grupos.values()]
            .sort((a, b) => COLACION_NUMERICA.compare(a.grupo, b.grupo))
            .map(({ grupo, lista }) => ({ grupo, estadisticas: this.estadisticas.resumir(lista) }));
    }
}

function escaparCSV(valor) {
    if (valor === null || valor === undefined) return '';
    let texto = String(valor);
    if (typeof valor === 'string' && /^[=+\-@\t\r]/.test(texto)) {
        texto = `'${texto}`;
    }
    return /[",\r\n]/.test(texto) ? `"${texto.replace(/"/g, '""')}"` : texto;
}

function filaCSV(valores) {
    return valores.map(escaparCSV).join(',');
}

const COLUMNAS_ESTUDIANTES = ['matricula', 'nombre', 'grupo', 'promedio', 'estado', 'enRiesgo'];
const COLUMNAS_RESUMEN = ['totalEstudiantes', 'totalEvaluados', 'totalAprobados', 'totalReprobados',
    'totalEnRiesgo', 'promedioGeneral', 'calificacionMaxima', 'calificacionMinima', 'porcentajeAprobacion'];

export function reporteAJSON(reporte) {
    return JSON.stringify(reporte, null, 2);
}

export function reporteACSV(reporte) {
    const filas = reporte.estudiantes.map((e) => filaCSV(COLUMNAS_ESTUDIANTES.map((c) => e[c])));
    return [filaCSV(COLUMNAS_ESTUDIANTES), ...filas].join('\r\n');
}

export function resumenACSV(reporte) {
    const filas = [['reporte', reporte.titulo, reporte.estadisticas]];
    for (const a of reporte.porAsignatura ?? []) {
        filas.push(['asignatura', `${a.clave} - ${a.nombre}`, a.estadisticas]);
    }
    for (const g of reporte.porGrupo ?? []) {
        filas.push(['grupo', g.grupo, g.estadisticas]);
    }
    const encabezado = filaCSV(['ambito', 'nombre', ...COLUMNAS_RESUMEN]);
    return [encabezado, ...filas.map(([ambito, nombre, est]) =>
        filaCSV([ambito, nombre, ...COLUMNAS_RESUMEN.map((c) => est[c])]))].join('\r\n');
}

export function exportarReporte(reporte, formato) {
    const tipo = typeof formato === 'string' ? formato.trim().toLowerCase() : formato;
    if (tipo === 'json') return reporteAJSON(reporte);
    if (tipo === 'csv') return reporteACSV(reporte);
    if (tipo === 'csv-resumen') return resumenACSV(reporte);
    throw new ValidacionError('Formato no válido: usa json, csv o csv-resumen');
}

export function nombreArchivoReporte(reporte, formato) {
    const partes = ['reporte'];
    if (reporte.grupo) partes.push(reporte.grupo);
    if (reporte.asignatura) partes.push(reporte.asignatura.clave);
    if (partes.length === 1) partes.push('general');
    if (formato === 'csv-resumen') partes.push('resumen');
    const base = partes.join('-').replace(/[^A-Za-z0-9_-]+/g, '-').replace(/-{2,}/g, '-');
    return `${base}.${formato === 'json' ? 'json' : 'csv'}`;
}