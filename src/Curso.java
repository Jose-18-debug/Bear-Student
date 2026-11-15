import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa un curso/asignatura con cupo máximo y lista de matrículas inscritas.
 */
public class Curso {
    private final String codigo;
    private final String nombre;
    private final int cupoMaximo;
    private final List<String> matriculasInscritas = new ArrayList<>();

    public Curso(String codigo, String nombre, int cupoMaximo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.cupoMaximo = cupoMaximo;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public int getCupoMaximo() { return cupoMaximo; }

    /** Vista de solo lectura de las matrículas inscritas. */
    public List<String> getMatriculasInscritas() {
        return Collections.unmodifiableList(matriculasInscritas);
    }

    public int getTotalInscritos() { return matriculasInscritas.size(); }

    public boolean tieneCupo() { return matriculasInscritas.size() < cupoMaximo; }

    /** Uso interno: la inscripción se hace desde RegistroAcademico. */
    void agregarEstudiante(String matricula) {
        matriculasInscritas.add(matricula);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - Inscritos: %d/%d",
                codigo, nombre, matriculasInscritas.size(), cupoMaximo);
    }
}
