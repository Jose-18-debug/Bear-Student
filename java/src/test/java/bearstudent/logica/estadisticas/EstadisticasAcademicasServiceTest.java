package bearstudent.logica.estadisticas;

import bearstudent.dto.PromedioEstudiante;
import bearstudent.logica.ValidacionException;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EstadisticasAcademicasServiceTest {

    private static FuentePromediosPort portCon(List<PromedioEstudiante> globales,
                                               List<PromedioEstudiante> porCurso) {
        return new FuentePromediosPort() {
            @Override
            public List<PromedioEstudiante> obtenerPromediosGlobales() { return globales; }
            @Override
            public List<PromedioEstudiante> obtenerPromediosPorCurso(String claveCurso) { return porCurso; }
        };
    }

    @Test
    void clasificacionFronteras() {
        var p1 = new PromedioEstudiante("1","A","G1",6.99);
        var p2 = new PromedioEstudiante("2","B","G1",7.0);
        var p3 = new PromedioEstudiante("3","C","G1",7.5);
        var p4 = new PromedioEstudiante("4","D","G1",7.51);
        var p5 = new PromedioEstudiante("5","E","G1",null);
        var port = portCon(List.of(p1,p2,p3,p4,p5), List.of());
        var svc = new EstadisticasAcademicasService(port);
        var cls = svc.clasificarGlobales();
        assertEquals(EstadisticasAcademicasUseCase.EstadoAcademico.REPROBADO, cls.get(0).estado());
        assertTrue(cls.get(0).enRiesgo());
        assertEquals(EstadisticasAcademicasUseCase.EstadoAcademico.APROBADO, cls.get(1).estado());
        assertTrue(cls.get(1).enRiesgo()); // 7.0 <=7.5
        assertEquals(EstadisticasAcademicasUseCase.EstadoAcademico.APROBADO, cls.get(2).estado());
        assertTrue(cls.get(2).enRiesgo()); // 7.5
        assertEquals(EstadisticasAcademicasUseCase.EstadoAcademico.APROBADO, cls.get(3).estado());
        assertFalse(cls.get(3).enRiesgo()); // 7.51
        assertEquals(EstadisticasAcademicasUseCase.EstadoAcademico.SIN_EVALUAR, cls.get(4).estado());
        assertFalse(cls.get(4).enRiesgo());
    }

    @Test
    void clasificaConElPromedioQueSeMuestra() {
        var seVe700 = new PromedioEstudiante("1", "A", "G1", 6.9967);   // en pantalla: 7.00
        var seVe699 = new PromedioEstudiante("2", "B", "G1", 6.9949);   // en pantalla: 6.99
        var seVe750 = new PromedioEstudiante("3", "C", "G1", 7.5049);   // en pantalla: 7.50
        var seVe751 = new PromedioEstudiante("4", "D", "G1", 7.5051);   // en pantalla: 7.51
        var cls = new EstadisticasAcademicasService(portCon(List.of(seVe700, seVe699, seVe750, seVe751), List.of()))
                .clasificarGlobales();
        assertEquals(EstadisticasAcademicasUseCase.EstadoAcademico.APROBADO, cls.get(0).estado());
        assertEquals(EstadisticasAcademicasUseCase.EstadoAcademico.REPROBADO, cls.get(1).estado());
        assertTrue(cls.get(2).enRiesgo());
        assertFalse(cls.get(3).enRiesgo());
    }

    @Test
    void resumenVacio() {
        var port = portCon(List.of(), List.of());
        var svc = new EstadisticasAcademicasService(port);
        var r = svc.resumenGlobal();
        assertEquals(0, r.totalEstudiantes());
        assertEquals(0, r.totalEvaluados());
        assertEquals(0.0, r.promedioGeneral());
        assertEquals(0.0, r.promedioMaximo());
        assertEquals(0.0, r.promedioMinimo());
        assertEquals(0.0, r.porcentajeAprobacion());
    }

    @Test
    void resumenSinEvaluados() {
        var p = new PromedioEstudiante("1","A","G1",null);
        var port = portCon(List.of(p), List.of());
        var svc = new EstadisticasAcademicasService(port);
        var r = svc.resumenGlobal();
        assertEquals(1, r.totalEstudiantes());
        assertEquals(0, r.totalEvaluados());
        assertEquals(0.0, r.porcentajeAprobacion());
    }

    @Test
    void validacionClaveCurso() {
        var port = portCon(List.of(), List.of());
        var svc = new EstadisticasAcademicasService(port);
        assertThrows(ValidacionException.class, () -> svc.clasificarPorCurso(null));
        assertThrows(ValidacionException.class, () -> svc.clasificarPorCurso("   "));
        assertThrows(ValidacionException.class, () -> svc.resumenPorCurso(null));
    }

    @Test
    void resumenCalculos() {
        var p1 = new PromedioEstudiante("1","A","G1",8.0);
        var p2 = new PromedioEstudiante("2","B","G1",6.0);
        var port = portCon(List.of(p1,p2), List.of(p1,p2));
        var svc = new EstadisticasAcademicasService(port);
        var r = svc.resumenGlobal();
        assertEquals(2, r.totalEstudiantes());
        assertEquals(2, r.totalEvaluados());
        assertEquals(1, r.totalAprobados());
        assertEquals(1, r.totalReprobados());
        assertEquals(1, r.totalEnRiesgo()); // 6.0 reprobado
        assertEquals(7.0, r.promedioGeneral(), 0.001);
        assertEquals(8.0, r.promedioMaximo());
        assertEquals(6.0, r.promedioMinimo());
        assertEquals(50.0, r.porcentajeAprobacion());
    }
}
