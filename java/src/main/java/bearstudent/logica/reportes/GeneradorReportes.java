package bearstudent.logica.reportes;

import bearstudent.dto.Curso;
import bearstudent.dto.Estudiante;
import bearstudent.dto.PromedioEstudiante;
import bearstudent.logica.PromediosAcademicos;
import bearstudent.logica.RegistroAcademico;
import bearstudent.logica.ValidacionException;
import bearstudent.logica.estadisticas.EstadisticasAcademicasService;
import bearstudent.logica.estadisticas.EstadisticasAcademicasUseCase;
import bearstudent.logica.estadisticas.EstadisticasAcademicasUseCase.ClasificacionEstudiante;
import bearstudent.logica.estadisticas.EstadisticasAcademicasUseCase.ResumenEstadistico;
import bearstudent.logica.estadisticas.FuentePromediosPort;
import bearstudent.logica.estadisticas.PromediosAcademicosAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Módulo de la Persona 5: reportes general, por grupo y por asignatura.
 * Integra a los demás: toma los promedios de la Persona 3 (PromediosAcademicos) y los clasifica
 * y resume con el servicio de estadísticas de la Persona 4 (aprobados, reprobados, riesgo, etc.).
 */
public class GeneradorReportes {

    public enum TipoReporte { GENERAL, GRUPO, ASIGNATURA }

    /**
     * Un reporte listo para mostrar o exportar.
     * clave = el grupo o la clave del curso (vacía en el reporte general).
     * estudiantes va ordenado de mayor a menor promedio (los que no tienen notas al final).
     */
    public record Reporte(TipoReporte tipo, String clave, String titulo,
                          List<ClasificacionEstudiante> estudiantes, ResumenEstadistico resumen) { }

    private final RegistroAcademico academico;
    private final PromediosAcademicos promedios;
    /** Estadísticas conectadas a los promedios reales por medio de PromediosAcademicosAdapter. */
    private final EstadisticasAcademicasUseCase estadisticas;

    public GeneradorReportes(RegistroAcademico academico, PromediosAcademicos promedios) {
        this.academico = academico;
        this.promedios = promedios;
        this.estadisticas = new EstadisticasAcademicasService(new PromediosAcademicosAdapter(promedios));
    }

    /** Todos los estudiantes, con su promedio final. */
    public Reporte reporteGeneral() {
        return new Reporte(TipoReporte.GENERAL, "", "Reporte general",
                estadisticas.clasificarGlobales(), estadisticas.resumenGlobal());
    }

    /** Estudiantes de un grupo (el grupo no distingue mayúsculas), con su promedio final. */
    public Reporte reportePorGrupo(String grupo) {
        String gru = grupo == null ? "" : grupo.trim();
        if (gru.isEmpty()) {
            throw new ValidacionException("El grupo no puede estar vacío");
        }
        List<PromedioEstudiante> delGrupo = promedios.ordenarPorPromedio(true).stream()
                .filter(p -> p.grupo().equalsIgnoreCase(gru))
                .toList();
        if (delGrupo.isEmpty()) {
            throw new ValidacionException("No hay estudiantes en el grupo " + gru);
        }
        EstadisticasAcademicasUseCase delGrupoStats = new EstadisticasAcademicasService(fuenteFija(delGrupo));
        return new Reporte(TipoReporte.GRUPO, gru, "Reporte del grupo " + gru,
                delGrupoStats.clasificarGlobales(), delGrupoStats.resumenGlobal());
    }

    /** Estudiantes inscritos en un curso, con su promedio en ese curso. */
    public Reporte reportePorAsignatura(String clave) {
        Curso curso = academico.buscarCurso(clave);
        return new Reporte(TipoReporte.ASIGNATURA, curso.getClave(),
                "Reporte de " + curso.getClave() + " | " + curso.getNombre(),
                estadisticas.clasificarPorCurso(curso.getClave()), estadisticas.resumenPorCurso(curso.getClave()));
    }

    /** Grupos registrados, sin repetir y en orden alfabético (para llenar listas en la interfaz). */
    public List<String> listarGrupos() {
        TreeSet<String> grupos = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Estudiante e : academico.listarEstudiantes()) {
            grupos.add(e.getGrupo());
        }
        return new ArrayList<>(grupos);
    }

    /** Fuente que siempre entrega la misma lista: el reporte por grupo la usa con los estudiantes ya filtrados. */
    private static FuentePromediosPort fuenteFija(List<PromedioEstudiante> datos) {
        return new FuentePromediosPort() {
            @Override
            public List<PromedioEstudiante> obtenerPromediosGlobales() { return datos; }

            @Override
            public List<PromedioEstudiante> obtenerPromediosPorCurso(String claveCurso) { return datos; }
        };
    }
}
