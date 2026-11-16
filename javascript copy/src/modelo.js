// Entidades básicas del sistema. Los nombres de campo deben ser iguales en Java y Python.
export class Estudiante {
    constructor(matricula, nombre, grupo) {
        this.matricula = matricula;
        this.nombre = nombre;
        this.grupo = grupo;
    }
}

export class Curso {
    constructor(clave, nombre) {
        this.clave = clave;
        this.nombre = nombre;
    }
}

export class Inscripcion {
    constructor(matricula, clave) {
        this.matricula = matricula;
        this.clave = clave;
    }
}

//Una evaluación pertenece a un curso y vale un porcentaje (ponderacion) de la calificación final
export class Evaluacion {
    constructor(clave, nombre, ponderacion) {
        this.clave = clave;
        this.nombre = nombre;
        this.ponderacion = ponderacion;
    }
}

//Calificación de 0 a 10 que obtuvo un estudiante en una evaluación de un curso
export class Calificacion {
    constructor(matricula, clave, evaluacion, valor) {
        this.matricula = matricula;
        this.clave = clave;
        this.evaluacion = evaluacion;
        this.valor = valor;
    }
}
