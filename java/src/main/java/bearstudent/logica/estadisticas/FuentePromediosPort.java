package bearstudent.logica.estadisticas;

import bearstudent.dto.PromedioEstudiante;

import java.util.List;

/**
 * Puerto de entrada de las estadísticas: de aquí salen los promedios que se clasifican y resumen.
 * La implementación real es PromediosAcademicosAdapter; las pruebas usan listas fijas.
 */
public interface FuentePromediosPort {

    /** Promedio final de cada estudiante, de mayor a menor (los que no tienen calificaciones van al final). */
    List<PromedioEstudiante> obtenerPromediosGlobales();

    /** Promedio en el curso de cada inscrito, de mayor a menor. */
    List<PromedioEstudiante> obtenerPromediosPorCurso(String claveCurso);
}
