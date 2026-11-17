package bearstudent.logica.reportes;

import bearstudent.logica.PromediosAcademicos;
import bearstudent.logica.RegistroAcademico;
import bearstudent.logica.RegistroCalificaciones;

/**
 * Datos de prueba compartidos por las pruebas de reportes:
 *  - Grupo 302: Ana (9.0 en MAT101), Luis (6.0 en MAT101) y Juan (sin cursos ni notas).
 *  - Grupo 301: "Ruiz, Sofía "Sofi"" (7.2 en MAT101); el nombre trae coma y comillas a propósito.
 *  - HIS200: solo Ana inscrita y sin calificaciones.
 */
final class EscenarioReportes {
    private EscenarioReportes() { }

    static GeneradorReportes crear() {
        RegistroAcademico academico = new RegistroAcademico();
        academico.registrarEstudiante("A001", "Ana Pérez", "302");
        academico.registrarEstudiante("A002", "Luis Hernández", "302");
        academico.registrarEstudiante("A003", "Ruiz, Sofía \"Sofi\"", "301");
        academico.registrarEstudiante("A004", "Juan López", "302");
        academico.registrarCurso("MAT101", "Matemáticas");
        academico.registrarCurso("HIS200", "Historia");
        academico.inscribir("A001", "MAT101");
        academico.inscribir("A002", "MAT101");
        academico.inscribir("A003", "MAT101");
        academico.inscribir("A001", "HIS200");

        RegistroCalificaciones notas = new RegistroCalificaciones(academico);
        notas.registrarEvaluacion("MAT101", "Parcial 1");
        notas.capturarCalificacion("A001", "MAT101", "Parcial 1", 9.0);
        notas.capturarCalificacion("A002", "MAT101", "Parcial 1", 6.0);
        notas.capturarCalificacion("A003", "MAT101", "Parcial 1", 7.2);

        return new GeneradorReportes(academico, new PromediosAcademicos(academico, notas));
    }
}
