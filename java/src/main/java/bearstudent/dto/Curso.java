package bearstudent.dto;

/**
 * Datos de un curso o asignatura. Es inmutable: quién está inscrito lo guarda RegistroAcademico.
 * Los campos se llaman igual que en el JSON de entrada (clave, nombre).
 */
public class Curso {
    private final String clave;
    private final String nombre;

    public Curso(String clave, String nombre) {
        this.clave = clave;
        this.nombre = nombre;
    }

    public String getClave() { return clave; }
    public String getNombre() { return nombre; }

    @Override
    public String toString() {
        return String.format("[%s] %s", clave, nombre);
    }
}
