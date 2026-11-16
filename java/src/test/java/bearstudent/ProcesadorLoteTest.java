package bearstudent;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcesadorLoteTest {

    /** Crea un escenario con 200 estudiantes inscritos en MAT101 (evaluación Parcial 1). */
    private RegistroCalificaciones escenario() {
        RegistroAcademico academico = new RegistroAcademico();
        academico.registrarCurso("MAT101", "Matemáticas");
        for (int i = 0; i < 200; i++) {
            academico.registrarEstudiante("M" + i, "Estudiante " + i, "302");
            academico.inscribir("M" + i, "MAT101");
        }
        RegistroCalificaciones notas = new RegistroCalificaciones(academico);
        notas.registrarEvaluacion("MAT101", "Parcial 1");
        return notas;
    }

    /** Lote con registros válidos, un estudiante que no existe cada 7, notas fuera de rango cada 11 y repetidos. */
    private List<Calificacion> lote() {
        List<Calificacion> lote = new ArrayList<>();
        for (int i = 0; i < 600; i++) {
            String matricula = (i % 7 == 0) ? "NO-EXISTE" : "M" + (i % 200);
            double nota = (i % 11 == 0) ? 11.5 : (i % 10) + 0.5;
            lote.add(new Calificacion(matricula, "MAT101", "Parcial 1", nota));
        }
        return lote;
    }

    @Test
    void secuencialYConcurrenteDanElMismoResultado() {
        RegistroCalificaciones a = escenario();
        RegistroCalificaciones b = escenario();
        ProcesadorLote.ResultadoLote secuencial = ProcesadorLote.procesar(a, lote(), ProcesadorLote.Modo.SECUENCIAL);
        ProcesadorLote.ResultadoLote concurrente = ProcesadorLote.procesar(b, lote(), ProcesadorLote.Modo.CONCURRENTE);

        assertEquals(secuencial, concurrente);                      // mismas capturadas y mismos errores
        assertTrue(secuencial.capturadas() > 0 && !secuencial.errores().isEmpty());
        for (int i = 0; i < 200; i++) {                             // y quedan guardadas las mismas notas
            assertEquals(a.consultarCalificaciones("M" + i), b.consultarCalificaciones("M" + i));
        }
    }

    @Test
    void unLoteVacioNoFalla() {
        for (ProcesadorLote.Modo modo : ProcesadorLote.Modo.values()) {
            ProcesadorLote.ResultadoLote r = ProcesadorLote.procesar(escenario(), List.of(), modo);
            assertEquals(0, r.capturadas());
            assertTrue(r.errores().isEmpty());
        }
    }
}