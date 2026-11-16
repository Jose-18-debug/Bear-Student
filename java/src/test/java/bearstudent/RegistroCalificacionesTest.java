package bearstudent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegistroCalificacionesTest {
    private RegistroAcademico academico;
    private RegistroCalificaciones notas;

    /** Escenario base: Ana (A001) inscrita en MAT101, Luis (A002) sin inscribir, y MAT101 con la evaluación Parcial 1. */
    @BeforeEach
    void preparar() {
        academico = new RegistroAcademico();
        academico.registrarEstudiante("A001", "Ana Pérez", "302");
        academico.registrarEstudiante("A002", "Luis Hernández", "302");
        academico.registrarCurso("MAT101", "Matemáticas");
        academico.inscribir("A001", "MAT101");
        notas = new RegistroCalificaciones(academico);
        notas.registrarEvaluacion("MAT101", "Parcial 1");
    }

    @Test
    void noCalificaAUnEstudianteNoInscrito() {
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> notas.capturarCalificacion("A002", "MAT101", "Parcial 1", 8.0));
        assertEquals("El estudiante A002 no está inscrito en el curso MAT101", e.getMessage());
    }

    @Test
    void rechazaCalificacionesFueraDeRangoYAceptaLosExtremos() {
        ValidacionException alta = assertThrows(ValidacionException.class,
                () -> notas.capturarCalificacion("A001", "MAT101", "Parcial 1", 10.01));
        assertEquals("La calificación debe estar entre 0 y 10", alta.getMessage());
        assertThrows(ValidacionException.class,
                () -> notas.capturarCalificacion("A001", "MAT101", "Parcial 1", -0.5));

        notas.capturarCalificacion("A001", "MAT101", "Parcial 1", 0);    // el 0 es válido
        notas.capturarCalificacion("A001", "MAT101", "Parcial 1", 10);   // el 10 es válido
        assertEquals(10.0, notas.calificacionesDeCurso("A001", "MAT101").get(0).doubleValue());
    }

    @Test
    void recapturarReemplazaElValorAnterior() {
        notas.capturarCalificacion("A001", "MAT101", "Parcial 1", 6.0);
        notas.capturarCalificacion("A001", "MAT101", "Parcial 1", 9.5);
        assertEquals(1, notas.calificacionesDeCurso("A001", "MAT101").size());
        assertEquals(9.5, notas.calificacionesDeCurso("A001", "MAT101").get(0).doubleValue());
    }

    @Test
    void consultarCalificacionesDevuelveLoCapturado() {
        notas.registrarEvaluacion("MAT101", "Parcial 2");
        notas.capturarCalificacion("A001", "MAT101", "Parcial 1", 8.0);
        notas.capturarCalificacion("A001", "MAT101", "Parcial 2", 7.5);
        assertEquals(java.util.List.of(
                        new Calificacion("A001", "MAT101", "Parcial 1", 8.0),
                        new Calificacion("A001", "MAT101", "Parcial 2", 7.5)),
                notas.consultarCalificaciones("A001"));
    }

    @Test
    void rechazaEvaluacionRepetidaOInexistente() {
        ValidacionException repetida = assertThrows(ValidacionException.class,
                () -> notas.registrarEvaluacion("MAT101", "Parcial 1"));
        assertEquals("Ya existe la evaluación Parcial 1 en el curso MAT101", repetida.getMessage());
        ValidacionException inexistente = assertThrows(ValidacionException.class,
                () -> notas.capturarCalificacion("A001", "MAT101", "Final", 8.0));
        assertEquals("La evaluación Final no existe en el curso MAT101", inexistente.getMessage());
    }
}