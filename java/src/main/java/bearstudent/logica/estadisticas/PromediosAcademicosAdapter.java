package bearstudent.logica.estadisticas;
import bearstudent.logica.*;
import bearstudent.dto.*;


import java.util.List;

public class PromediosAcademicosAdapter implements FuentePromediosPort {
    private final PromediosAcademicos promedios;

    public PromediosAcademicosAdapter(PromediosAcademicos promedios) {
        this.promedios = promedios;
    }

    @Override
    public List<PromedioEstudiante> obtenerPromediosGlobales() {
        return promedios.ordenarPorPromedio(true).stream()
                .map(p -> new PromedioEstudiante(p.matricula(), p.nombre(), p.grupo(), p.promedio()))
                .toList();
    }

    @Override
    public List<PromedioEstudiante> obtenerPromediosPorCurso(String claveCurso) {
        return promedios.ordenarPorPromedioEnCurso(claveCurso, true).stream()
                .map(p -> new PromedioEstudiante(p.matricula(), p.nombre(), p.grupo(), p.promedio()))
                .toList();
    }
}




