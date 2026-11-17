package bearstudent.logica;

import bearstudent.dto.Calificacion;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Procesa las calificaciones de varios estudiantes de una sola vez (lote), en modo secuencial o concurrente.
 * Los dos modos dan EXACTAMENTE el mismo resultado; solo cambia cómo se reparte el trabajo.
 */
public final class ProcesadorLote {

    public enum Modo { SECUENCIAL, CONCURRENTE }

    /** Un registro que no se pudo capturar: su posición en el lote (empieza en 0) y el mensaje de error. */
    public record ErrorLote(int indice, String mensaje) { }

    /** Resultado del lote: cuántas se capturaron y cuáles fallaron (ordenadas por índice). */
    public record ResultadoLote(int capturadas, List<ErrorLote> errores) { }

    private ProcesadorLote() { }

    public static ResultadoLote procesar(RegistroCalificaciones registro, List<Calificacion> registros, Modo modo) {
        return modo == Modo.CONCURRENTE ? concurrente(registro, registros) : secuencial(registro, registros);
    }

    /** Un registro tras otro: valida y guarda cada uno en orden. */
    private static ResultadoLote secuencial(RegistroCalificaciones registro, List<Calificacion> registros) {
        List<ErrorLote> errores = new ArrayList<>();
        int capturadas = 0;
        for (int i = 0; i < registros.size(); i++) {
            Calificacion c = registros.get(i);
            try {
                registro.capturarCalificacion(c.matricula(), c.clave(), c.evaluacion(), c.calificacion());
                capturadas++;
            } catch (ValidacionException e) {
                errores.add(new ErrorLote(i, e.getMessage()));
            }
        }
        return new ResultadoLote(capturadas, errores);
    }

    /**
     * Dos fases: (1) todas las validaciones se reparten entre varios hilos (parallel stream), porque solo
     * leen datos; (2) los guardados se hacen en orden, en un solo hilo, para que si hay registros repetidos
     * gane siempre el último, igual que en el modo secuencial.
     */
    private static ResultadoLote concurrente(RegistroCalificaciones registro, List<Calificacion> registros) {
        int n = registros.size();
        String[] mensajes = new String[n];          // cada hilo escribe solo en su propia posición
        IntStream.range(0, n).parallel().forEach(i -> {
            Calificacion c = registros.get(i);
            try {
                registro.validarCaptura(c.matricula(), c.clave(), c.evaluacion(), c.calificacion());
            } catch (ValidacionException e) {
                mensajes[i] = e.getMessage();
            }
        });

        List<ErrorLote> errores = new ArrayList<>();
        int capturadas = 0;
        for (int i = 0; i < n; i++) {
            if (mensajes[i] == null) {
                Calificacion c = registros.get(i);
                registro.guardar(c.matricula(), c.clave(), c.evaluacion(), c.calificacion());
                capturadas++;
            } else {
                errores.add(new ErrorLote(i, mensajes[i]));
            }
        }
        return new ResultadoLote(capturadas, errores);
    }
}
