package bearstudent.logica;

import bearstudent.dto.Estudiante;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroAcademicoTest {
    private RegistroAcademico registro;

    /** Escenario base: Ana (302), Luis (302) y Sofía (301); cursos MAT101 y HIS200; Ana inscrita en MAT101. */
    @BeforeEach
    void preparar() {
        registro = new RegistroAcademico();
        registro.registrarEstudiante("A001", "Ana Pérez", "302");
        registro.registrarEstudiante("A002", "Luis Hernández", "302");
        registro.registrarEstudiante("A003", "Sofía Ruiz", "301");
        registro.registrarCurso("MAT101", "Matemáticas");
        registro.registrarCurso("HIS200", "Historia");
        registro.inscribir("A001", "MAT101");
    }

    private static String mensaje(Runnable accion) {
        return assertThrows(ValidacionException.class, accion::run).getMessage();
    }

    @Test
    void recortaLosEspaciosAntesDeGuardarYBuscar() {
        registro.registrarEstudiante("  A010 ", " Eva Gómez ", " 303 ");
        Estudiante eva = registro.buscarEstudiante("A010");
        assertEquals("Eva Gómez", eva.getNombre());
        assertEquals("303", eva.getGrupo());
        registro.inscribir(" A010", "HIS200 ");
        assertTrue(registro.estaInscrito("A010 ", " HIS200"));
    }

    @Test
    void avisaDelPrimerCampoVacio() {
        assertEquals("La matrícula no puede estar vacía", mensaje(() -> registro.registrarEstudiante(" ", "", "")));
        assertEquals("El nombre no puede estar vacío", mensaje(() -> registro.registrarEstudiante("A020", null, "302")));
        assertEquals("El grupo no puede estar vacío", mensaje(() -> registro.registrarEstudiante("A020", "Eva", "  ")));
        assertEquals("La clave no puede estar vacía", mensaje(() -> registro.registrarCurso("", "Física")));
        assertEquals("El nombre no puede estar vacío", mensaje(() -> registro.registrarCurso("FIS100", " ")));
    }

    @Test
    void noAdmiteRepetidos() {
        assertEquals("Ya existe un estudiante con la matrícula A001",
                mensaje(() -> registro.registrarEstudiante(" A001 ", "Otra", "302")));
        assertEquals("Ya existe un curso con la clave MAT101",
                mensaje(() -> registro.registrarCurso("MAT101", "Otro")));
        assertEquals("El estudiante A001 ya está inscrito en el curso MAT101",
                mensaje(() -> registro.inscribir("A001", "MAT101")));
        assertEquals(1, registro.totalInscritos("MAT101"));
        assertEquals(List.of("MAT101"), registro.cursosDeEstudiante("A001"));
    }

    @Test
    void noInscribeSiFaltaElEstudianteOElCurso() {
        assertEquals("El estudiante A999 no existe", mensaje(() -> registro.inscribir("A999", "MAT101")));
        assertEquals("El curso FIS100 no existe", mensaje(() -> registro.inscribir("A002", "FIS100")));
        assertTrue(registro.cursosDeEstudiante("A002").isEmpty());
    }

    @Test
    void unRegistroConErrorNoQuedaAMedias() {
        assertThrows(ValidacionException.class, () -> registro.registrarEstudiante("A030", "Sin grupo", ""));
        assertThrows(ValidacionException.class, () -> registro.buscarEstudiante("A030"));
        assertEquals(3, registro.listarEstudiantes().size());
    }

    @Test
    void lasInscripcionesSeVenDesdeLosDosLados() {
        registro.inscribir("A002", "MAT101");
        registro.inscribir("A001", "HIS200");
        assertEquals(List.of("MAT101", "HIS200"), registro.cursosDeEstudiante("A001"));
        assertEquals(List.of("A001", "A002"),
                registro.estudiantesDeCurso("MAT101").stream().map(Estudiante::getMatricula).toList());
        assertEquals(2, registro.totalInscritos("MAT101"));
        assertTrue(registro.estaInscrito("A002", "MAT101"));
        assertFalse(registro.estaInscrito("A002", "HIS200"));
    }

    @Test
    void consultasDeAlgoQueNoExiste() {
        assertTrue(registro.cursosDeEstudiante("A999").isEmpty());
        assertEquals(0, registro.totalInscritos("FIS100"));
        assertFalse(registro.estaInscrito("A999", "MAT101"));
        assertEquals("El curso FIS100 no existe", mensaje(() -> registro.estudiantesDeCurso("FIS100")));
    }

    @Test
    void lasListasDevueltasSonCopias() {
        registro.cursosDeEstudiante("A001").add("HIS200");
        registro.listarEstudiantes().clear();
        assertEquals(List.of("MAT101"), registro.cursosDeEstudiante("A001"));
        assertEquals(3, registro.listarEstudiantes().size());
    }

    @Test
    void estudiantesPorGrupoIgnoraMayusculasYEspaciosYOrdenaPorNombre() {
        registro.registrarEstudiante("A004", "Beto Díaz", "302A");
        assertEquals(List.of("Ana Pérez", "Luis Hernández"),
                registro.estudiantesPorGrupo(" 302 ").stream().map(Estudiante::getNombre).toList());
        assertEquals(List.of("Beto Díaz"),
                registro.estudiantesPorGrupo("302a").stream().map(Estudiante::getNombre).toList());
    }
}
