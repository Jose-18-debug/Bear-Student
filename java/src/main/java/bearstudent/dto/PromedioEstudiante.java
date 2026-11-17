package bearstudent.dto;

/**
 * Promedio de un estudiante: el promedio final o el de un curso. promedio es null si no tiene calificaciones.
 * Lo comparten los promedios (parte 3), las estadísticas (parte 4) y los reportes (parte 5).
 */
public record PromedioEstudiante(String matricula, String nombre, String grupo, Double promedio) {
}
