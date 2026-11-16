//Error propio de Bear Student, se lanza cuando un dato no cumple las reglas de negocio.
export class ValidacionError extends Error {
    constructor(mensaje) {
        super(mensaje);
        this.name = 'ValidacionError';
    }
}