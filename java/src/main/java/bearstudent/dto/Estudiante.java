package bearstudent.dto;

/**
 * Datos de un estudiante. Es inmutable: sus inscripciones las guarda RegistroAcademico,
 * así ninguna otra clase puede agregar o quitar cursos por su cuenta.
 */
public class Estudiante {
    private final String matricula;
    private final String nombre;
    private final String grupo;

    public Estudiante(String matricula, String nombre, String grupo) {
        this.matricula = matricula;
        this.nombre = nombre;
        this.grupo = grupo;
    }

    public String getMatricula() { return matricula; }
    public String getNombre() { return nombre; }
    public String getGrupo() { return grupo; }

    @Override
    public String toString() {
        return String.format("[%s] %s (Grupo %s)", matricula, nombre, grupo);
    }
}
