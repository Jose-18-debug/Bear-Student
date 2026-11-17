package bearstudent.logica.estadisticas;
import bearstudent.logica.*;
import bearstudent.dto.*;

import java.util.List;

public interface EstadisticasAcademicasUseCase {
    enum EstadoAcademico { APROBADO, REPROBADO, SIN_EVALUAR }

    record ClasificacionEstudiante(String matricula, String nombre, String grupo, Double promedio, EstadoAcademico estado, boolean enRiesgo) {}

    record ResumenEstadistico(int totalEstudiantes, int totalEvaluados, int totalAprobados, int totalReprobados, int totalEnRiesgo, double promedioGeneral, double calificacionMaxima, double calificacionMinima, double porcentajeAprobacion) {}

    List<ClasificacionEstudiante> clasificarGlobales();

    List<ClasificacionEstudiante> clasificarPorCurso(String claveCurso);

    ResumenEstadistico resumenGlobal();

    ResumenEstadistico resumenPorCurso(String claveCurso);
}



