package bearstudent.logica;

/**
 * Error de validación de Bear Student: un dato no cumple las reglas de negocio.
 * Es unchecked (RuntimeException) para poder usarla dentro de lambdas y streams
 * sin tener que envolverla en try/catch.
 */
public class ValidacionException extends RuntimeException {
    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
