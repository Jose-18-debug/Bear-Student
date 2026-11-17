package bearstudent.logica.reportes;

import bearstudent.logica.ValidacionException;
import bearstudent.logica.estadisticas.EstadisticasAcademicasUseCase.ClasificacionEstudiante;
import bearstudent.logica.estadisticas.EstadisticasAcademicasUseCase.EstadoAcademico;
import bearstudent.logica.estadisticas.EstadisticasAcademicasUseCase.ResumenEstadistico;
import bearstudent.logica.reportes.GeneradorReportes.Reporte;
import bearstudent.logica.reportes.GeneradorReportes.TipoReporte;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeneradorReportesTest {
    private static final double DELTA = 1e-9;
    private GeneradorReportes generador;

    @BeforeEach
    void preparar() {
        generador = EscenarioReportes.crear();
    }

    private static List<String> matriculas(Reporte r) {
        return r.estudiantes().stream().map(ClasificacionEstudiante::matricula).toList();
    }

    @Test
    void reporteGeneralIncluyeATodosOrdenadosPorPromedio() {
        Reporte r = generador.reporteGeneral();
        assertEquals(TipoReporte.GENERAL, r.tipo());
        assertEquals(List.of("A001", "A003", "A002", "A004"), matriculas(r));   // sin notas al final

        ResumenEstadistico s = r.resumen();
        assertEquals(4, s.totalEstudiantes());
        assertEquals(3, s.totalEvaluados());
        assertEquals(2, s.totalAprobados());
        assertEquals(1, s.totalReprobados());
        assertEquals(2, s.totalEnRiesgo());                   // Luis (6.0) y Sofía (7.2)
        assertEquals(7.4, s.promedioGeneral(), DELTA);
        assertEquals(9.0, s.calificacionMaxima(), DELTA);
        assertEquals(6.0, s.calificacionMinima(), DELTA);
        assertEquals(200.0 / 3, s.porcentajeAprobacion(), DELTA);
    }

    @Test
    void reportePorGrupoSoloIncluyeAlGrupo() {
        Reporte r = generador.reportePorGrupo(" 302 ");
        assertEquals(TipoReporte.GRUPO, r.tipo());
        assertEquals("302", r.clave());
        assertEquals("Reporte del grupo 302", r.titulo());
        assertEquals(List.of("A001", "A002", "A004"), matriculas(r));
        assertEquals(EstadoAcademico.SIN_EVALUAR, r.estudiantes().get(2).estado());
        assertEquals(2, r.resumen().totalEvaluados());
        assertEquals(50.0, r.resumen().porcentajeAprobacion(), DELTA);
    }

    @Test
    void reportePorAsignaturaUsaElPromedioDelCurso() {
        Reporte r = generador.reportePorAsignatura("MAT101");
        assertEquals(TipoReporte.ASIGNATURA, r.tipo());
        assertEquals("Reporte de MAT101 | Matemáticas", r.titulo());
        assertEquals(List.of("A001", "A003", "A002"), matriculas(r));
        assertEquals(EstadoAcademico.REPROBADO, r.estudiantes().get(2).estado());
        assertTrue(r.estudiantes().get(2).enRiesgo());
    }

    @Test
    void asignaturaSinCalificacionesNoTieneEvaluados() {
        Reporte r = generador.reportePorAsignatura("HIS200");
        assertEquals(1, r.resumen().totalEstudiantes());
        assertEquals(0, r.resumen().totalEvaluados());
    }

    @Test
    void erroresDeValidacion() {
        assertEquals("No hay estudiantes en el grupo 999",
                assertThrows(ValidacionException.class, () -> generador.reportePorGrupo("999")).getMessage());
        assertEquals("El grupo no puede estar vacío",
                assertThrows(ValidacionException.class, () -> generador.reportePorGrupo("  ")).getMessage());
        assertEquals("El curso FIS100 no existe",
                assertThrows(ValidacionException.class, () -> generador.reportePorAsignatura("FIS100")).getMessage());
    }

    @Test
    void listaLosGruposSinRepetir() {
        assertEquals(List.of("301", "302"), generador.listarGrupos());
    }
}
