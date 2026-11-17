package bearstudent.logica.estadisticas;
import bearstudent.logica.*;
import bearstudent.dto.*;


import java.util.List;

public class EstadisticasAcademicasService implements EstadisticasAcademicasUseCase {
    private final FuentePromediosPort fuente;

    public EstadisticasAcademicasService(FuentePromediosPort fuente) {
        this.fuente = fuente;
    }

    @Override
    public List<ClasificacionEstudiante> clasificarGlobales() {
        return clasificar(fuente.obtenerPromediosGlobales());
    }

    @Override
    public List<ClasificacionEstudiante> clasificarPorCurso(String claveCurso) {
        validarClave(claveCurso);
        return clasificar(fuente.obtenerPromediosPorCurso(claveCurso));
    }

    @Override
    public ResumenEstadistico resumenGlobal() {
        return resumir(clasificarGlobales());
    }

    @Override
    public ResumenEstadistico resumenPorCurso(String claveCurso) {
        validarClave(claveCurso);
        return resumir(clasificarPorCurso(claveCurso));
    }

    private void validarClave(String claveCurso) {
        if (claveCurso == null || claveCurso.trim().isEmpty()) {
            throw new ValidacionException("La clave del curso no puede estar vacía");
        }
    }

    private List<ClasificacionEstudiante> clasificar(List<FuentePromediosPort.PromedioEstudiante> lista) {
        return lista.stream().map(p -> {
            Double prom = p.promedio();
            EstadoAcademico estado;
            boolean enRiesgo;
            if (prom == null) {
                estado = EstadoAcademico.SIN_EVALUAR;
                enRiesgo = false;
            } else if (prom >= 7.0) {
                estado = EstadoAcademico.APROBADO;
                enRiesgo = prom <= 7.5;
            } else {
                estado = EstadoAcademico.REPROBADO;
                enRiesgo = true;
            }
            return new ClasificacionEstudiante(p.matricula(), p.nombre(), p.grupo(), prom, estado, enRiesgo);
        }).toList();
    }

    private ResumenEstadistico resumir(List<ClasificacionEstudiante> lista) {
        int total = lista.size();
        int evaluados = 0;
        int aprobados = 0;
        int reprobados = 0;
        int enRiesgo = 0;
        double suma = 0.0;
        double max = 0.0;
        double min = 0.0;
        boolean hay = false;
        for (ClasificacionEstudiante c : lista) {
            if (c.promedio() == null) continue;
            evaluados++;
            if (c.estado() == EstadoAcademico.APROBADO) aprobados++;
            if (c.estado() == EstadoAcademico.REPROBADO) reprobados++;
            if (c.enRiesgo()) enRiesgo++;
            suma += c.promedio();
            if (!hay) { max = c.promedio(); min = c.promedio(); hay = true; }
            else { max = Math.max(max, c.promedio()); min = Math.min(min, c.promedio()); }
        }
        double promedioGeneral = (evaluados == 0) ? 0.0 : suma / evaluados;
        double porcentaje = (evaluados == 0) ? 0.0 : (aprobados * 100.0) / evaluados;
        if (!hay) { max = 0.0; min = 0.0; }
        return new ResumenEstadistico(total, evaluados, aprobados, reprobados, enRiesgo, promedioGeneral, max, min, porcentaje);
    }
}




