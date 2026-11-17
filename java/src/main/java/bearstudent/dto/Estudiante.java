package bearstudent.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa a un estudiante. Guarda las claves de los cursos en los que
 * está inscrito (los demás módulos pueden usarlas para ligar calificaciones).
 */
public class Estudiante {
    private final String matricula;
    private final String nombre;
    private final String grupo;
    private final List<String> codigosCursos = new ArrayList<>();

    public Estudiante(String matricula, String nombre, String grupo) {
        this.matricula = matricula;
        this.nombre = nombre;
        this.grupo = grupo;
    }

    public String getMatricula() { return matricula; }
    public String getNombre() { return nombre; }
    public String getGrupo() { return grupo; }

    /** Vista de solo lectura de las claves de los cursos del estudiante. */
    public List<String> getCodigosCursos() {
        return Collections.unmodifiableList(codigosCursos);
    }

    public boolean estaInscritoEn(String claveCurso) {
        return codigosCursos.contains(claveCurso);
    }

    /** Uso interno: la inscripción se hace desde RegistroAcademico (es público porque está en otro paquete). */
    public void agregarCurso(String claveCurso) {
        codigosCursos.add(claveCurso);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (Grupo %s) - Cursos: %s",
                matricula, nombre, grupo, codigosCursos);
    }
}
