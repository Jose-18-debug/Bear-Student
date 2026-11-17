package bearstudent.logica;

import bearstudent.dto.PromedioEstudiante;
import bearstudent.logica.PromediosAcademicos.PromedioCurso;
import bearstudent.logica.PromediosAcademicos.ReporteEstudiante;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PromediosAcademicosTest {
    private static final double DELTA = 1e-9;
    private PromediosAcademicos promedios;

    /**
     * Ana: MAT101 (8 y 10, promedio 9), HIS200 (6) y FIS100 sin calificaciones.
     * Luis: MAT101 (7). Juan: sin cursos. Bea y Zoe: 8 en HIS200 (empate).
     */
    @BeforeEach
    void preparar() {
        RegistroAcademico academico = new RegistroAcademico();
        academico.registrarEstudiante("A001", "Ana", "302");
        academico.registrarEstudiante("A002", "Luis", "302");
        academico.registrarEstudiante("A003", "Juan", "301");
        academico.registrarEstudiante("A004", "Zoe", "301");
        academico.registrarEstudiante("A005", "Bea", "301");
        academico.registrarCurso("MAT101", "Matemáticas");
        academico.registrarCurso("HIS200", "Historia");
        academico.registrarCurso("FIS100", "Física");
        for (String[] i : new String[][]{{"A001", "MAT101"}, {"A001", "HIS200"}, {"A001", "FIS100"},
                {"A002", "MAT101"}, {"A004", "HIS200"}, {"A005", "HIS200"}}) {
            academico.inscribir(i[0], i[1]);
        }
        RegistroCalificaciones notas = new RegistroCalificaciones(academico);
        notas.registrarEvaluacion("MAT101", "Parcial 1");
        notas.registrarEvaluacion("MAT101", "Parcial 2");
        notas.registrarEvaluacion("HIS200", "Parcial 1");
        notas.capturarCalificacion("A001", "MAT101", "Parcial 1", 8);
        notas.capturarCalificacion("A001", "MAT101", "Parcial 2", 10);
        notas.capturarCalificacion("A001", "HIS200", "Parcial 1", 6);
        notas.capturarCalificacion("A002", "MAT101", "Parcial 1", 7);
        notas.capturarCalificacion("A004", "HIS200", "Parcial 1", 8);
        notas.capturarCalificacion("A005", "HIS200", "Parcial 1", 8);
        promedios = new PromediosAcademicos(academico, notas);
    }

    private static List<String> nombres(List<PromedioEstudiante> lista) {
        return lista.stream().map(PromedioEstudiante::nombre).toList();
    }

    @Test
    void promedioDeCadaCursoDelEstudiante() {
        List<PromedioCurso> lista = promedios.promediosPorCurso("A001");
        assertEquals(List.of("MAT101", "HIS200", "FIS100"), lista.stream().map(PromedioCurso::clave).toList());
        assertEquals(9.0, lista.get(0).promedio(), DELTA);
        assertEquals(6.0, lista.get(1).promedio(), DELTA);
        assertNull(lista.get(2).promedio());                       // sin calificaciones: null, no 0
    }

    @Test
    void promedioFinalIgnoraLosCursosSinCalificaciones() {
        assertEquals(7.5, promedios.promedioFinal("A001"), DELTA);   // (9 + 6) / 2
        assertNull(promedios.promedioFinal("A003"));                 // Juan no tiene cursos
    }

    @Test
    void promedioDeUnCursoEsElDeSusInscritosConCalificaciones() {
        assertEquals(8.0, promedios.promedioDeCurso("MAT101"), DELTA); // (9 + 7) / 2
        assertNull(promedios.promedioDeCurso("FIS100"));
    }

    @Test
    void ordenaPorPromedioConLosSinNotasAlFinalYEmpatesPorNombre() {
        assertEquals(List.of("Bea", "Zoe", "Ana", "Luis", "Juan"), nombres(promedios.ordenarPorPromedio(true)));
        assertEquals(List.of("Luis", "Ana", "Bea", "Zoe", "Juan"), nombres(promedios.ordenarPorPromedio(false)));
        assertEquals(List.of("Ana", "Luis"), nombres(promedios.ordenarPorPromedioEnCurso("MAT101", true)));
        assertEquals(Arrays.asList(9.0, 7.0),
                promedios.ordenarPorPromedioEnCurso("MAT101", true).stream().map(PromedioEstudiante::promedio).toList());
    }

    @Test
    void reporteDeUnEstudiante() {
        ReporteEstudiante r = promedios.reporteDeEstudiante(" A001 ");
        assertEquals("Ana", r.nombre());
        assertEquals(3, r.calificaciones().size());
        assertEquals(3, r.promediosPorCurso().size());
        assertEquals(7.5, r.promedioFinal(), DELTA);
    }

    @Test
    void estudianteOCursoInexistente() {
        assertEquals("El estudiante A999 no existe",
                assertThrows(ValidacionException.class, () -> promedios.promedioFinal("A999")).getMessage());
        assertEquals("El curso QUI300 no existe",
                assertThrows(ValidacionException.class, () -> promedios.promedioDeCurso("QUI300")).getMessage());
    }
}
