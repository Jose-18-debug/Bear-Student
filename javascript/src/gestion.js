import { Estudiante, Curso, Inscripcion } from './modelo.js';
import { ValidacionError } from './errores.js';
import { textoObligatorio } from './validacion.js';

export class GestorAcademico {
    constructor() {
        this.estudiantes = new Map();          //matricula -> Estudiante
        this.cursos = new Map();               //clave -> Curso
        //Índices con Set para consultar rápido incluso con 50,000 estudiantes
        this.cursosPorEstudiante = new Map();  //matricula -> Set de claves de curso
        this.estudiantesPorCurso = new Map();  //clave -> Set de matrículas
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
}