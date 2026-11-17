package bearstudent.ui;

import bearstudent.dto.Calificacion;
import bearstudent.dto.Estudiante;
import bearstudent.logica.ProcesadorLote;
import bearstudent.logica.RegistroAcademico;
import bearstudent.logica.RegistroCalificaciones;
import bearstudent.logica.ValidacionException;
import bearstudent.ui.componentes.BotonRedondo;
import bearstudent.ui.componentes.Campo;
import bearstudent.ui.componentes.Opcion;
import bearstudent.ui.componentes.PanelTabla;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static bearstudent.ui.componentes.Componentes.*;
import static bearstudent.ui.componentes.Estilo.*;

/**
 * Pestaña de la parte 2: evaluaciones, calificaciones y procesamiento por lote.
 * No contiene lógica de negocio: solo llama a RegistroCalificaciones y ProcesadorLote.
 */
public class PanelCalificaciones extends JPanel implements Refrescable {
    private static final int[] CANTIDADES = {1_000, 10_000, 100_000};

    private final RegistroAcademico academico;
    private final RegistroCalificaciones notas;

    // Registrar evaluación
    private final JComboBox<Opcion> evaluacionCurso = new JComboBox<>();
    private final Campo evaluacionNombre = new Campo();
    // Capturar calificación
    private final JComboBox<Opcion> capturaEstudiante = new JComboBox<>();
    private final JComboBox<Opcion> capturaCurso = new JComboBox<>();
    private final JComboBox<Opcion> capturaEvaluacion = new JComboBox<>();
    private final Campo capturaCalificacion = new Campo();
    // Lote
    private final JComboBox<String> loteModo = new JComboBox<>(new String[]{"Secuencial", "Concurrente"});
    private final JComboBox<String> loteCantidad = new JComboBox<>(new String[]{"1,000", "10,000", "100,000"});
    private final JLabel loteResultado = new JLabel(" ");

    private final BotonRedondo botonEvaluacion = new BotonRedondo("Guardar evaluación", true, this::registrarEvaluacion);
    private final BotonRedondo botonCaptura = new BotonRedondo("Guardar calificación", true, this::capturarCalificacion);
    private final PanelTabla tabla = new PanelTabla(
            new String[]{"Curso", "Evaluación", "Calificación"},
            new Class<?>[]{String.class, String.class, Double.class},
            new int[]{300, 200, 120},
            "Este estudiante todavía no tiene calificaciones.");
    private boolean cargando;   // evita que los combos reaccionen mientras se rellenan

    public PanelCalificaciones(RegistroAcademico academico, RegistroCalificaciones notas) {
        super(new BorderLayout());
        this.academico = academico;
        this.notas = notas;
        setOpaque(false);
        for (JComboBox<?> combo : List.of(evaluacionCurso, capturaEstudiante, capturaCurso, capturaEvaluacion,
                loteModo, loteCantidad)) {
            estilizarCombo(combo);
        }

        JPanel tarjetaEvaluacion = tarjeta("Registrar evaluación", formulario(
                new String[]{"Curso", "Evaluación"},
                new JComponent[]{evaluacionCurso, evaluacionNombre},
                botonEvaluacion,
                new BotonRedondo("Limpiar", false, () -> limpiar(evaluacionNombre))));
        JPanel tarjetaCaptura = tarjeta("Capturar calificación", formulario(
                new String[]{"Estudiante", "Curso", "Evaluación", "Calificación (0 a 10)"},
                new JComponent[]{capturaEstudiante, capturaCurso, capturaEvaluacion, capturaCalificacion},
                botonCaptura,
                new BotonRedondo("Limpiar", false, () -> limpiar(capturaCalificacion))));
        JPanel dos = new JPanel(new GridLayout(1, 2, 12, 0));
        dos.setOpaque(false);
        dos.add(tarjetaEvaluacion);
        dos.add(tarjetaCaptura);

        JPanel arriba = new JPanel();
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        arriba.setOpaque(false);
        arriba.add(dos);
        arriba.add(Box.createVerticalStrut(12));
        arriba.add(tarjeta("Procesar calificaciones por lote", crearPanelLote()));
        arriba.add(Box.createVerticalStrut(12));

        JPanel tarjetaTabla = tarjeta("Calificaciones del estudiante seleccionado", tabla);
        tarjetaTabla.setPreferredSize(new Dimension(100, 300));
        add(pagina(arriba, tarjetaTabla), BorderLayout.CENTER);

        // Al cambiar de curso se actualizan sus evaluaciones; al cambiar de estudiante, la tabla
        capturaCurso.addActionListener(e -> { if (!cargando) llenarEvaluaciones(); });
        capturaEstudiante.addActionListener(e -> { if (!cargando) refrescarTabla(); });
        refrescar();
    }

    private JPanel crearPanelLote() {
        loteResultado.setFont(fuente(TEXTO, false));
        loteResultado.setForeground(GRIS_PIZARRA);
        JPanel cuerpo = new JPanel(new GridBagLayout());
        cuerpo.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.gridy = 0; gc.weightx = 0; gc.fill = GridBagConstraints.NONE; gc.insets = new Insets(0, 0, 0, 8);
        gc.gridx = 0; cuerpo.add(etiqueta("Modo"), gc);
        gc.gridx = 1; cuerpo.add(loteModo, gc);
        gc.gridx = 2; gc.insets = new Insets(0, 16, 0, 8); cuerpo.add(etiqueta("Registros"), gc);
        gc.gridx = 3; gc.insets = new Insets(0, 0, 0, 16); cuerpo.add(loteCantidad, gc);
        gc.gridx = 4; gc.insets = new Insets(0, 0, 0, 0);
        cuerpo.add(new BotonRedondo("Procesar lote de ejemplo", true, this::procesarLote), gc);
        gc.gridx = 5; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        cuerpo.add(Box.createHorizontalGlue(), gc);
        gc.gridx = 0; gc.gridy = 1; gc.gridwidth = 6; gc.insets = new Insets(8, 0, 0, 0);
        cuerpo.add(loteResultado, gc);
        return cuerpo;
    }

    // ---------- Acciones (solo llaman a la lógica) ----------

    private void registrarEvaluacion() {
        try {
            notas.registrarEvaluacion(idElegido(evaluacionCurso), evaluacionNombre.getText());
            limpiar(evaluacionNombre);
            refrescar();
            aviso(this, "Listo", "Evaluación registrada.", JOptionPane.INFORMATION_MESSAGE);
        } catch (ValidacionException e) {
            marcarError(evaluacionNombre);
            aviso(this, "No se pudo registrar", e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    /** La interfaz solo convierte el texto a número (acepta coma o punto decimal); la lógica valida el rango. */
    private void capturarCalificacion() {
        String texto = capturaCalificacion.getText().trim();
        try {
            if (!texto.matches("-?\\d+([.,]\\d+)?")) {
                throw new ValidacionException("La calificación debe ser un número");
            }
            double valor = Double.parseDouble(texto.replace(',', '.'));
            notas.capturarCalificacion(idElegido(capturaEstudiante), idElegido(capturaCurso),
                    idElegido(capturaEvaluacion), valor);
            limpiar(capturaCalificacion);
            refrescarTabla();
            aviso(this, "Listo", "Calificación guardada.", JOptionPane.INFORMATION_MESSAGE);
        } catch (ValidacionException e) {
            marcarError(capturaCalificacion);
            aviso(this, "No se pudo guardar", e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Genera un lote de ejemplo con los datos registrados y lo procesa en el modo elegido, midiendo el tiempo. */
    private void procesarLote() {
        int cantidad = CANTIDADES[loteCantidad.getSelectedIndex()];
        List<Calificacion> lote = generarLote(cantidad);
        if (lote.isEmpty()) {
            aviso(this, "Faltan datos", "Primero registra estudiantes, cursos, inscripciones y evaluaciones.",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        ProcesadorLote.Modo modo = loteModo.getSelectedIndex() == 0
                ? ProcesadorLote.Modo.SECUENCIAL : ProcesadorLote.Modo.CONCURRENTE;
        long inicio = System.nanoTime();
        ProcesadorLote.ResultadoLote resultado = ProcesadorLote.procesar(notas, lote, modo);
        double milisegundos = (System.nanoTime() - inicio) / 1_000_000.0;

        String ejemplo = resultado.errores().isEmpty() ? ""
                : "<br>Ejemplo de error: " + resultado.errores().get(0).mensaje();
        loteResultado.setText(String.format(Locale.US,
                "<html>Modo %s · %,d registros · capturadas: %,d · con error: %,d · %.1f ms%s</html>",
                modo.name().toLowerCase(Locale.ROOT), cantidad, resultado.capturadas(),
                resultado.errores().size(), milisegundos, ejemplo));
        refrescarTabla();
    }

    /**
     * Lote de ejemplo: recorre las combinaciones (estudiante, curso, evaluación) que existen y les pone una nota
     * al azar con semilla fija (siempre el mismo lote). Uno de cada 25 registros trae un error a propósito.
     */
    private List<Calificacion> generarLote(int cantidad) {
        List<String[]> combinaciones = new ArrayList<>();
        for (Estudiante e : academico.listarEstudiantes()) {
            for (String clave : academico.cursosDeEstudiante(e.getMatricula())) {
                for (String evaluacion : notas.evaluacionesDeCurso(clave)) {
                    combinaciones.add(new String[]{e.getMatricula(), clave, evaluacion});
                }
            }
        }
        List<Calificacion> lote = new ArrayList<>();
        if (combinaciones.isEmpty()) return lote;
        Random azar = new Random(42);
        for (int i = 0; i < cantidad; i++) {
            String[] c = combinaciones.get(i % combinaciones.size());
            String matricula = (i % 25 == 0) ? "NO-EXISTE" : c[0];
            lote.add(new Calificacion(matricula, c[1], c[2], Math.round(azar.nextDouble() * 100) / 10.0));
        }
        return lote;
    }

    // ---------- Actualización de la pantalla ----------

    /** Vuelve a leer estudiantes, cursos y evaluaciones. Se llama al abrir la pestaña. */
    @Override
    public void refrescar() {
        List<Opcion> cursos = opcionesCursos(academico.listarCursos());
        cargando = true;
        try {
            llenarCombo(evaluacionCurso, cursos);
            llenarCombo(capturaEstudiante, opcionesEstudiantes(academico.listarEstudiantes()));
            llenarCombo(capturaCurso, cursos);
            llenarEvaluaciones();
        } finally {
            cargando = false;
        }
        refrescarTabla();
        botonEvaluacion.setEnabled(!cursos.isEmpty());
    }

    private void llenarEvaluaciones() {
        List<Opcion> opciones = new ArrayList<>();
        for (String nombre : notas.evaluacionesDeCurso(idElegido(capturaCurso))) {
            opciones.add(new Opcion(nombre, nombre));
        }
        llenarCombo(capturaEvaluacion, opciones);
        botonCaptura.setEnabled(capturaEstudiante.getItemCount() > 0 && !opciones.isEmpty());
    }

    private void refrescarTabla() {
        List<Object[]> filas = new ArrayList<>();
        String matricula = idElegido(capturaEstudiante);
        if (!matricula.isEmpty()) {
            for (Calificacion c : notas.consultarCalificaciones(matricula)) {
                filas.add(new Object[]{c.clave() + " | " + academico.buscarCurso(c.clave()).getNombre(),
                        c.evaluacion(), c.calificacion()});
            }
        }
        tabla.llenar(filas);
    }
}
