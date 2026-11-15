/**
 * Excepción para errores de validación en el registro de estudiantes,
 * cursos e inscripciones.
 */
public class RegistroException extends Exception {
    public RegistroException(String mensaje) {
        super(mensaje);
    }
}
