package bearstudent.logica.estadisticas;
import bearstudent.logica.*;
import bearstudent.dto.*;

import java.util.List;

public interface FuentePromediosPort {
    record PromedioEstudiante(String matricula, String nombre, String grupo, Double promedio) {}

    List<PromedioEstudiante> obtenerPromediosGlobales();

    List<PromedioEstudiante> obtenerPromediosPorCurso(String claveCurso);
}



