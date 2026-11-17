package bearstudent.logica.reportes;

import bearstudent.dto.Curso;
import bearstudent.dto.Estudiante;
import bearstudent.logica.PromediosAcademicos;
import bearstudent.logica.RegistroAcademico;
import bearstudent.logica.ValidacionException;
import bearstudent.logica.estadisticas.EstadisticasAcademicasService;
import bearstudent.logica.estadisticas.EstadisticasAcademicasUseCase.ClasificacionEstudiante;
import bearstudent.logica.estadisticas.EstadisticasAcademicasUseCase.ResumenEstadistico;
import bearstudent.logica.estadisticas.FuentePromediosPort;

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

    public GeneradorReportes(RegistroAcademico academico, PromediosAcademicos promedios) {
        this.academico = academico;
        this.promedios = promedios;
    }

    /** Todos los estudiantes, con su promedio final. */
    public Reporte reporteGeneral() {
        return armar(TipoReporte.GENERAL, "", "Reporte general", promedios.ordenarPorPromedio(true));
    }

    /** Estudiantes de un grupo, con su promedio final (el grupo no distingue mayúsculas). */
    public Reporte reportePorGrupo(String grupo) {
        String gru = grupo == null ? "" : grupo.trim();
        if (gru.isEmpty()) {
            throw new ValidacionException("El grupo no puede estar vacío");
        }
        List<PromediosAcademicos.PromedioEstudiante> delGrupo = promedios.ordenarPorPromedio(true).stream()
                .filter(p -> p.grupo().equalsIgnoreCase(gru))
                .toList();
        if (delGrupo.isEmpty()) {
            throw new ValidacionException("No hay estudiantes en el grupo " + gru);
        }
        return armar(TipoReporte.GRUPO, gru, "Reporte del grupo " + gru, delGrupo);
    }

    /** Estudiantes inscritos en un curso, con su promedio en ese curso. */
    public Reporte reportePorAsignatura(String clave) {
        Curso curso = academico.buscarCurso(clave);
        return armar(TipoReporte.ASIGNATURA, curso.getClave(),
                "Reporte de " + curso.getClave() + " | " + curso.getNombre(),
                promedios.ordenarPorPromedioEnCurso(curso.getClave(), true));
    }

    /** Grupos registrados, sin repetir y en orden alfabético (para llenar listas en la interfaz). */
    public List<String> listarGrupos() {
        TreeSet<String> grupos = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Estudiante e : academico.listarEstudiantes()) {
            grupos.add(e.getGrupo());
        }
        return new ArrayList<>(grupos);
    }

    // ---------- Apoyo ----------

    private static Reporte armar(TipoReporte tipo, String clave, String titulo,
                                 List<PromediosAcademicos.PromedioEstudiante> lista) {
        List<FuentePromediosPort.PromedioEstudiante> datos = lista.stream()
                .map(p -> new FuentePromediosPort.PromedioEstudiante(p.matricula(), p.nombre(), p.grupo(), p.promedio()))
                .toList();
        EstadisticasAcademicasService estadisticas = new EstadisticasAcademicasService(fuenteFija(datos));
        return new Reporte(tipo, clave, titulo, estadisticas.clasificarGlobales(), estadisticas.resumenGlobal());
    }

    /** Fuente que siempre entrega la misma lista: así se reutiliza la Persona 4 con cualquier grupo de estudiantes. */
    private static FuentePromediosPort fuenteFija(List<FuentePromediosPort.PromedioEstudiante> datos) {
        return new FuentePromediosPort() {
            @Override
            public List<PromedioEstudiante> obtenerPromediosGlobales() { return datos; }

            @Override
            public List<PromedioEstudiante> obtenerPromediosPorCurso(String claveCurso) { return datos; }
        };
    }
}
