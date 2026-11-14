import { ValidacionError } from './errores.js';

//Verifica que el valor sea un texto con contenido, no vacío ni solo espacios
//Devuelve el texto sin espacios sobrantes, si no cumple, lanza ValidacionError con el mensaje dado
export function textoObligatorio(valor, mensaje) {
    if (typeof valor !== 'string' || valor.trim() === '') {
        throw new ValidacionError(mensaje);
    }
    return valor.trim();
}