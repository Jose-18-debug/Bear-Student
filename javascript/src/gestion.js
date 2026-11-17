import { Estudiante, Curso, Inscripcion, Evaluacion, Calificacion } from './modelo.js';
import { ValidacionError } from './errores.js';
import { textoObligatorio, numeroEnRango } from './validacion.js';

//Redondea a 2 decimales (igual en Java y Python para que los resultados coincidan)
function redondear(numero) {
    return Math.round(numero * 100) / 100;
}

function media(numeros) {
    return numeros.length === 0 ? null : redondear(numeros.reduce((a, b) => a + b, 0) / numeros.length);
}

export class GestorAcademico {
    constructor() {
        this.estudiantes = new Map();          //matricula -> Estudiante
        this.cursos = new Map();               //clave -> Curso
        //Índices con Set para consultar rápido incluso con 50,000 estudiantes
        this.cursosPorEstudiante = new Map();  //matricula -> Set de claves de curso
        this.estudiantesPorCurso = new Map();  //clave -> Set de matrículas
        this.evaluaciones = new Map();         //clave -> Map nombre -> Evaluacion
        this.calificaciones = new Map();       //"matricula|clave" -> Map nombre de evaluación -> Calificacion
    }

    registrarEstudiante(matricula, nombre, grupo) {
        const mat = textoObligatorio(matricula, 'La matrícula no puede estar vacía');
        const nom = textoObligatorio(nombre, 'El nombre no puede estar vacío');
        const gru = textoObligatorio(grupo, 'El grupo no puede estar vacío');
        if (this.estudiantes.has(mat)) {
            throw new ValidacionError(`Ya existe un estudiante con la matrícula ${mat}`);
        }
        const estudiante = new Estudiante(mat, nom, gru);
        this.estudiantes.set(mat, estudiante);
        this.cursosPorEstudiante.set(mat, new Set());
        return estudiante;
    }

    registrarCurso(clave, nombre) {
        const cla = textoObligatorio(clave, 'La clave no puede estar vacía');
        const nom = textoObligatorio(nombre, 'El nombre no puede estar vacío');
        if (this.cursos.has(cla)) {
            throw new ValidacionError(`Ya existe un curso con la clave ${cla}`);
        }
        const curso = new Curso(cla, nom);
        this.cursos.set(cla, curso);
        this.estudiantesPorCurso.set(cla, new Set());
        this.evaluaciones.set(cla, new Map());
        return curso;
    }

    inscribir(matricula, clave) {
        if (!this.estudiantes.has(matricula)) {
            throw new ValidacionError(`El estudiante ${matricula} no existe`);
        }
        if (!this.cursos.has(clave)) {
            throw new ValidacionError(`El curso ${clave} no existe`);
        }
        if (this.estaInscrito(matricula, clave)) {
            throw new ValidacionError(`El estudiante ${matricula} ya está inscrito en el curso ${clave}`);
        }
        this.cursosPorEstudiante.get(matricula).add(clave);
        this.estudiantesPorCurso.get(clave).add(matricula);
        return new Inscripcion(matricula, clave);
    }

    //Las siguientes tres funciones serán necesarias para lo demás de la interfaz
    estaInscrito(matricula, clave) {
        const cursos = this.cursosPorEstudiante.get(matricula);
        return cursos !== undefined && cursos.has(clave);
    }

    cursosDeEstudiante(matricula) {
        return [...(this.cursosPorEstudiante.get(matricula) ?? [])];
    }

    estudiantesDeCurso(clave) {
        return [...(this.estudiantesPorCurso.get(clave) ?? [])];
    }

    //La ponderación es el porcentaje que vale la evaluación; las de un curso no pueden sumar más de 100
    registrarEvaluacion(clave, nombre, ponderacion) {
        if (!this.cursos.has(clave)) {
            throw new ValidacionError(`El curso ${clave} no existe`);
        }
        const nom = textoObligatorio(nombre, 'El nombre de la evaluación no puede estar vacío');
        const valorPonderacion = typeof ponderacion === 'string'
            ? ponderacion.trim().replace(/\s*%$/, '')
            : ponderacion;
        const pon = numeroEnRango(valorPonderacion, 0, 100, 'La ponderación debe ser un número entre 0 y 100');
        if (pon === 0) {
            throw new ValidacionError('La ponderación debe ser mayor que 0');
        }
        const delCurso = this.evaluaciones.get(clave);
        if (delCurso.has(nom)) {
            throw new ValidacionError(`El curso ${clave} ya tiene una evaluación llamada ${nom}`);
        }
        const suma = this.ponderacionTotal(clave);
        if (suma + pon > 100 + 1e-9) {   //el margen evita errores de redondeo como 0.1 + 0.2
            throw new ValidacionError(`Las ponderaciones del curso ${clave} sumarían ${suma + pon}%, el máximo es 100%`);
        }
        const evaluacion = new Evaluacion(clave, nom, pon);
        delCurso.set(nom, evaluacion);
        return evaluacion;
    }

    //Calificación de 0 a 10; solo para estudiantes inscritos y una sola vez por evaluación
    capturarCalificacion(matricula, clave, nombreEvaluacion, valor) {
        if (!this.estudiantes.has(matricula)) {
            throw new ValidacionError(`El estudiante ${matricula} no existe`);
        }
        if (!this.cursos.has(clave)) {
            throw new ValidacionError(`El curso ${clave} no existe`);
        }
        if (!this.estaInscrito(matricula, clave)) {
            throw new ValidacionError(`El estudiante ${matricula} no está inscrito en el curso ${clave}`);
        }
        const nom = typeof nombreEvaluacion === 'string' ? nombreEvaluacion.trim() : nombreEvaluacion;
        if (!this.evaluaciones.get(clave).has(nom)) {
            throw new ValidacionError(`El curso ${clave} no tiene una evaluación llamada ${nom}`);
        }
        const val = numeroEnRango(valor, 0, 10, 'La calificación debe ser un número entre 0 y 10');
        const llave = `${matricula}|${clave}`;
        if (!this.calificaciones.has(llave)) {
            this.calificaciones.set(llave, new Map());
        }
        const delEstudiante = this.calificaciones.get(llave);
        if (delEstudiante.has(nom)) {
            throw new ValidacionError(`El estudiante ${matricula} ya tiene calificación en ${nom} del curso ${clave}`);
        }
        const calificacion = new Calificacion(matricula, clave, nom, val);
        delEstudiante.set(nom, calificacion);
        return calificacion;
    }

    evaluacionesDeCurso(clave) {
        return [...(this.evaluaciones.get(clave)?.values() ?? [])];
    }

    ponderacionTotal(clave) {
        return this.evaluacionesDeCurso(clave).reduce((suma, e) => suma + e.ponderacion, 0);
    }

    //Calificaciones de un estudiante en un curso; las usará el cálculo del promedio final
    calificacionesDe(matricula, clave) {
        return [...(this.calificaciones.get(`${matricula}|${clave}`)?.values() ?? [])];
    }

    //Captura muchas calificaciones de una vez. Cada elemento es { matricula, clave, evaluacion, valor }.
    //No se detiene en el primer error: guarda las válidas y regresa qué filas fallaron y por qué.
    procesarCalificaciones(lista) {
        if (!Array.isArray(lista)) {
            throw new ValidacionError('Se esperaba una lista de calificaciones');
        }
        const exitosas = [];
        const errores = [];
        lista.forEach((dato, i) => {
            try {
                if (dato === null || typeof dato !== 'object') {
                    throw new ValidacionError('La fila no tiene datos de calificación');
                }
                const { matricula, clave, evaluacion, valor } = dato;
                exitosas.push(this.capturarCalificacion(matricula, clave, evaluacion, valor));
            } catch (error) {
                if (!(error instanceof ValidacionError)) throw error;
                errores.push({ fila: i + 1, dato, mensaje: error.message });
            }
        });
        return { exitosas, errores };
    }

    // ---- Persona 3: promedios y consultas ----

    //Promedio final de un estudiante en un curso, de 0 a 10 con 2 decimales.
    //Es ponderado: suma(valor * ponderacion) / suma(ponderaciones de las evaluaciones ya calificadas).
    //Si todavía no tiene ninguna calificación en el curso regresa null (no es lo mismo que 0).
    promedioFinal(matricula, clave) {
        const pesos = this.evaluaciones.get(clave);
        if (pesos === undefined) return null;
        let suma = 0;
        let peso = 0;
        for (const cal of this.calificacionesDe(matricula, clave)) {
            const ponderacion = pesos.get(cal.evaluacion).ponderacion;
            suma += cal.valor * ponderacion;
            peso += ponderacion;
        }
        return peso === 0 ? null : redondear(suma / peso);
    }

    //Promedio de un curso: media de los promedios finales de sus estudiantes que ya tienen calificaciones
    promedioDeCurso(clave) {
        return media(this.estudiantesDeCurso(clave)
            .map((matricula) => this.promedioFinal(matricula, clave))
            .filter((promedio) => promedio !== null));
    }

    //Promedio general de un estudiante: media de sus promedios finales por curso (solo cursos con calificaciones)
    promedioGeneral(matricula) {
        return media(this.cursosDeEstudiante(matricula)
            .map((clave) => this.promedioFinal(matricula, clave))
            .filter((promedio) => promedio !== null));
    }

    //Consulta las calificaciones de un estudiante, agrupadas por curso, con su promedio final y general.
    //Regresa { estudiante, cursos: [{ clave, nombre, calificaciones: [{ evaluacion, ponderacion, valor }], promedioFinal }], promedioGeneral }
    consultarEstudiante(matricula) {
        const estudiante = this.estudiantes.get(typeof matricula === 'string' ? matricula.trim() : matricula);
        if (estudiante === undefined) {
            throw new ValidacionError(`El estudiante ${matricula} no existe`);
        }
        const cursos = this.cursosDeEstudiante(estudiante.matricula).map((clave) => ({
            clave,
            nombre: this.cursos.get(clave).nombre,
            calificaciones: this.calificacionesDe(estudiante.matricula, clave).map((cal) => ({
                evaluacion: cal.evaluacion,
                ponderacion: this.evaluaciones.get(clave).get(cal.evaluacion).ponderacion,
                valor: cal.valor,
            })),
            promedioFinal: this.promedioFinal(estudiante.matricula, clave),
        }));
        return { estudiante, cursos, promedioGeneral: this.promedioGeneral(estudiante.matricula) };
    }

    //Estudiantes ordenados por promedio general. Los que aún no tienen calificaciones quedan al final.
    //Empates: por nombre. Regresa [{ matricula, nombre, grupo, promedio }]
    estudiantesPorPromedio(descendente = true) {
        const filas = [...this.estudiantes.values()].map((e) => ({
            matricula: e.matricula, nombre: e.nombre, grupo: e.grupo, promedio: this.promedioGeneral(e.matricula),
        }));
        const factor = descendente ? -1 : 1;
        return filas.sort((a, b) => {
            if (a.promedio === null || b.promedio === null) {
                return (a.promedio === null) - (b.promedio === null);   //los null siempre al final
            }
            return factor * (a.promedio - b.promedio) || a.nombre.localeCompare(b.nombre, 'es');
        });
    }

    //Promedio de cada curso: [{ clave, nombre, inscritos, promedio }]
    promediosPorCurso() {
        return [...this.cursos.values()].map((c) => ({
            clave: c.clave, nombre: c.nombre,
            inscritos: this.estudiantesDeCurso(c.clave).length,
            promedio: this.promedioDeCurso(c.clave),
        }));
    }
}
