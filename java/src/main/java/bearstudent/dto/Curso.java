package bearstudent.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa un curso/asignatura y la lista de matrículas inscritas.
 * Los campos se llaman igual que en el JSON de entrada (clave, nombre).
 */
public class Curso {
    private final String clave;
    private final String nombre;
    private final List<String> matriculasInscritas = new ArrayList<>();

    public Curso(String clave, String nombre) {
        this.clave = clave;
        this.nombre = nombre;
    }

    public String getClave() { return clave; }
    public String getNombre() { return nombre; }

    /** Vista de solo lectura de las matrículas inscritas. */
    public List<String> getMatriculasInscritas() {
        return Collections.unmodifiableList(matriculasInscritas);
    }

    public int getTotalInscritos() { return matriculasInscritas.size(); }

    /** Uso interno: la inscripción se hace desde RegistroAcademico. */
    void agregarEstudiante(String matricula) {
        matriculasInscritas.add(matricula);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - Inscritos: %d",
                clave, nombre, matriculasInscritas.size());
    }
}
