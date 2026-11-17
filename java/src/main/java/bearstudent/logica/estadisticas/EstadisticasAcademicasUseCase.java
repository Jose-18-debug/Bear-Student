package bearstudent.logica.estadisticas;

import java.util.List;

/**
 * Módulo de la Persona 4: qué se puede pedir a las estadísticas académicas.
 * Un estudiante con promedio de 7.0 o más está aprobado; con 7.5 o menos (o reprobado) está en riesgo.
 */
public interface EstadisticasAcademicasUseCase {

    enum EstadoAcademico { APROBADO, REPROBADO, SIN_EVALUAR }

    /** Un estudiante ya clasificado. promedio es null si no tiene calificaciones. */
    record ClasificacionEstudiante(String matricula, String nombre, String grupo, Double promedio,
                                   EstadoAcademico estado, boolean enRiesgo) { }

    /**
     * Resumen de una lista de estudiantes. Los que no tienen calificaciones solo cuentan en totalEstudiantes.
     * promedioMaximo y promedioMinimo son el promedio más alto y el más bajo (no una calificación suelta).
     */
    record ResumenEstadistico(int totalEstudiantes, int totalEvaluados, int totalAprobados, int totalReprobados,
                              int totalEnRiesgo, double promedioGeneral, double promedioMaximo,
                              double promedioMinimo, double porcentajeAprobacion) { }

    /** Todos los estudiantes, clasificados con su promedio final. */
    List<ClasificacionEstudiante> clasificarGlobales();

    /** Los inscritos en un curso, clasificados con su promedio en ese curso. */
    List<ClasificacionEstudiante> clasificarPorCurso(String claveCurso);

    ResumenEstadistico resumenGlobal();

    ResumenEstadistico resumenPorCurso(String claveCurso);
}
