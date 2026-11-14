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