package bearstudent.logica.estadisticas;

import bearstudent.dto.PromedioEstudiante;
import bearstudent.logica.PromediosAcademicos;

import java.util.List;

/** Conecta las estadísticas (parte 4) con los promedios que calcula PromediosAcademicos (parte 3). */
public class PromediosAcademicosAdapter implements FuentePromediosPort {
    private final PromediosAcademicos promedios;

    public PromediosAcademicosAdapter(PromediosAcademicos promedios) {
        this.promedios = promedios;
    }

    @Override
    public List<PromedioEstudiante> obtenerPromediosGlobales() {
        return promedios.ordenarPorPromedio(true);
    }

    @Override
    public List<PromedioEstudiante> obtenerPromediosPorCurso(String claveCurso) {
        return promedios.ordenarPorPromedioEnCurso(claveCurso, true);
    }
}
