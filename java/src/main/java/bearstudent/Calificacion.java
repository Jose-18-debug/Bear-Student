package bearstudent;

/**
 * Una calificación capturada: de qué estudiante, en qué curso, en qué evaluación y cuánto sacó.
 * Se usa tanto para consultar calificaciones como para describir cada registro de un lote.
 * (record = clase inmutable; Java genera constructor, getters como matricula() y toString.)
 */
public record Calificacion(String matricula, String clave, String evaluacion, double calificacion) {
}