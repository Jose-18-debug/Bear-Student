import { ValidacionError } from './errores.js';

//Verifica que el valor sea un texto con contenido, no vacío ni solo espacios
//Devuelve el texto sin espacios sobrantes, si no cumple, lanza ValidacionError con el mensaje dado
export function textoObligatorio(valor, mensaje) {
    if (typeof valor !== 'string' || valor.trim() === '') {
        throw new ValidacionError(mensaje);
    }
    return valor.trim();
}

//Verifica que el valor sea un número (o un texto con un número) dentro de [minimo, maximo]
//Devuelve el número convertido, si no cumple, lanza ValidacionError con el mensaje dado
export function numeroEnRango(valor, minimo, maximo, mensaje) {
    const texto = typeof valor === 'string' ? valor.trim() : valor;
    const numero = texto === '' || texto === null || texto === undefined ? NaN : Number(texto);
    if (!Number.isFinite(numero) || numero < minimo || numero > maximo) {
        throw new ValidacionError(mensaje);
    }
    return numero;
}
