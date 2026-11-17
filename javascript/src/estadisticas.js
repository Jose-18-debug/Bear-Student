import { ValidacionError } from './errores.js';
import { textoObligatorio } from './validacion.js';

//Redondea a 2 decimales (igual que en gestion.js)
function redondear(numero) {
    return Math.round(numero * 100) / 100;
}

//Normaliza un promedio: solo se acepta un número finito y no NaN.
function esPromedioValido(promedio) {
    return typeof promedio === 'number' && !Number.isNaN(promedio) && Number.isFinite(promedio);
}

export class EstadisticasAcademicas {
    constructor(gestor) {
        this.gestor = gestor;
    }

    _validarClave(clave) {
        const cla = textoObligatorio(clave, 'La clave no puede estar vacía');
        if (!this.gestor || !this.gestor.cursos || !this.gestor.cursos.has(cla)) {
            throw new ValidacionError(`El curso ${cla} no existe`);
        }
        return cla;
    }

    _promedioFinal(matricula, clave) {
        if (this.gestor && typeof this.gestor.promedioFinal === 'function') {
            return this.gestor.promedioFinal(matricula, clave);
        }
        const pesos = this.gestor.evaluaciones ? this.gestor.evaluaciones.get(clave) : null;
        const calificaciones = (typeof this.gestor.calificacionesDe === 'function')
            ? this.gestor.calificacionesDe(matricula, clave)
            : [...(this.gestor.calificaciones.get(`${matricula}|${clave}`)?.values() ?? [])];
        let suma = 0;
        let peso = 0;
        for (const cal of calificaciones) {
            const ponderacion = pesos ? pesos.get(cal.evaluacion)?.ponderacion : undefined;
            if (typeof ponderacion === 'number') {
                suma += cal.valor * ponderacion;
                peso += ponderacion;
            }
        }
        return peso === 0 ? null : redondear(suma / peso);
    }

    _promedioGeneral(matricula) {
        if (this.gestor && typeof this.gestor.promedioGeneral === 'function') {
            return this.gestor.promedioGeneral(matricula);
        }
        const cursos = (typeof this.gestor.cursosDeEstudiante === 'function')
            ? this.gestor.cursosDeEstudiante(matricula)
            : [...(this.gestor.cursosPorEstudiante?.get(matricula) ?? [])];
        const finales = cursos
            .map((clave) => this._promedioFinal(matricula, clave))
            .filter((p) => esPromedioValido(p));
        return finales.length === 0 ? null : redondear(finales.reduce((a, b) => a + b, 0) / finales.length);
    }

    _estudiantes() {
        if (this.gestor && this.gestor.estudiantes) {
            return [...this.gestor.estudiantes.values()];
        }
        return [];
    }

    _clasificar(promedio, base) {
        const valido = esPromedioValido(promedio);
        if (!valido) {
            return { ...base, promedio: promedio ?? null, estado: 'SIN_EVALUAR', enRiesgo: false };
        }
        const estado = promedio >= 7.0 ? 'APROBADO' : 'REPROBADO';
        const enRiesgo = promedio <= 7.5;
        return { ...base, promedio, estado, enRiesgo };
    }

    clasificarEstudiantesGlobal() {
        const estudiantes = this._estudiantes();
        return estudiantes.map((e) => this._clasificar(
            this._promedioGeneral(e.matricula),
            { matricula: e.matricula, nombre: e.nombre, grupo: e.grupo }
        ));
    }

    clasificarEstudiantesPorCurso(clave) {
        const cla = this._validarClave(clave);
        const matriculas = (typeof this.gestor.estudiantesDeCurso === 'function')
            ? this.gestor.estudiantesDeCurso(cla)
            : [...(this.gestor.estudiantesPorCurso?.get(cla) ?? [])];
        return matriculas.map((matricula) => {
            const estudiante = this.gestor.estudiantes ? this.gestor.estudiantes.get(matricula) : null;
            return this._clasificar(
                this._promedioFinal(matricula, cla),
                {
                    matricula,
                    nombre: estudiante ? estudiante.nombre : matricula,
                    grupo: estudiante ? estudiante.grupo : '',
                }
            );
        });
    }

    detectarRiesgoGlobal() {
        return this.clasificarEstudiantesGlobal().filter((e) => e.enRiesgo);
    }

    detectarRiesgoPorCurso(clave) {
        return this.clasificarEstudiantesPorCurso(clave).filter((e) => e.enRiesgo);
    }

    _calcular(clasificados) {
        const validos = clasificados.filter((e) => esPromedioValido(e.promedio));
        const totalEstudiantes = clasificados.length;
        const totalEvaluados = validos.length;
        const totalAprobados = validos.filter((e) => e.estado === 'APROBADO').length;
        const totalReprobados = validos.filter((e) => e.estado === 'REPROBADO').length;
        const totalEnRiesgo = validos.filter((e) => e.enRiesgo).length;

        if (totalEvaluados === 0) {
            return {
                totalEstudiantes,
                totalEvaluados: 0,
                totalAprobados: 0,
                totalReprobados: 0,
                totalEnRiesgo: 0,
                promedioGeneral: 0,
                calificacionMaxima: 0,
                calificacionMinima: 0,
                porcentajeAprobacion: 0,
            };
        }

        const suma = validos.reduce((acc, e) => acc + e.promedio, 0);
        const promedioGeneral = redondear(suma / totalEvaluados);
        const calificacionMaxima = redondear(Math.max(...validos.map((e) => e.promedio)));
        const calificacionMinima = redondear(Math.min(...validos.map((e) => e.promedio)));
        const porcentajeAprobacion = redondear((totalAprobados * 100) / totalEvaluados);

        return {
            totalEstudiantes,
            totalEvaluados,
            totalAprobados,
            totalReprobados,
            totalEnRiesgo,
            promedioGeneral,
            calificacionMaxima,
            calificacionMinima,
            porcentajeAprobacion,
        };
    }

    calcularEstadisticasGlobal() {
        return this._calcular(this.clasificarEstudiantesGlobal());
    }

    calcularEstadisticasPorCurso(clave) {
        return this._calcular(this.clasificarEstudiantesPorCurso(clave));
    }
}