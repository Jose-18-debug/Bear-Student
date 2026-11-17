package bearstudent.logica.estadisticas;

import bearstudent.dto.PromedioEstudiante;
import bearstudent.logica.ValidacionException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Módulo de la Persona 4: clasifica a cada estudiante (aprobado, reprobado, en riesgo) y resume una lista.
 * No sabe de dónde salen los promedios: los pide a una FuentePromediosPort.
 */
public class EstadisticasAcademicasService implements EstadisticasAcademicasUseCase {
    /** Promedio mínimo para aprobar. */
    static final double APROBATORIO = 7.0;
    /** Un aprobado con este promedio o menos está en riesgo académico. */
    static final double LIMITE_RIESGO = 7.5;

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

    /**
     * Se clasifica con el promedio redondeado a 2 decimales, que es el que se ve en pantalla y en los reportes.
     * Así, un 6.9967 que se muestra como 7.00 cuenta como aprobado.
     */
    private List<ClasificacionEstudiante> clasificar(List<PromedioEstudiante> lista) {
        return lista.stream().map(p -> {
            Double prom = p.promedio();
            EstadoAcademico estado;
            boolean enRiesgo;
            if (prom == null) {
                estado = EstadoAcademico.SIN_EVALUAR;
                enRiesgo = false;
            } else if (redondear(prom) >= APROBATORIO) {
                estado = EstadoAcademico.APROBADO;
                enRiesgo = redondear(prom) <= LIMITE_RIESGO;
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

    /** Redondeo a 2 decimales igual al de String.format("%.2f"). */
    static double redondear(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
