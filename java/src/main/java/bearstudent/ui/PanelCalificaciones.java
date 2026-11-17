package bearstudent.ui;
import bearstudent.dto.*;
import bearstudent.logica.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static bearstudent.ui.VentanaPrincipal.*;

/**
 * Interfaz gráfica de la Parte 2: evaluaciones, calificaciones y procesamiento por lote.
 * No contiene lógica de negocio: solo llama a RegistroCalificaciones y ProcesadorLote.
 * Reutiliza los componentes de VentanaPrincipal (Campo, BotonRedondo, PanelTabla, paleta, fuentes).
 */
public class PanelCalificaciones extends JPanel {
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

    private BotonRedondo botonEvaluacion;
    private BotonRedondo botonCaptura;
    private PanelTabla tabla;
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

        // Tarjeta 1: registrar evaluación
        botonEvaluacion = new BotonRedondo("Guardar evaluación", true, this::registrarEvaluacion);
        JPanel tarjetaEvaluacion = tarjeta("Registrar evaluación", cuerpo(
                new String[]{"Curso", "Evaluación"},
                new JComponent[]{evaluacionCurso, evaluacionNombre},
                botonEvaluacion,
                new BotonRedondo("Limpiar", false, () -> limpiar(new Campo[]{evaluacionNombre}))));

        // Tarjeta 2: capturar calificación
        botonCaptura = new BotonRedondo("Guardar calificación", true, this::capturarCalificacion);
        JPanel tarjetaCaptura = tarjeta("Capturar calificación", cuerpo(
                new String[]{"Estudiante", "Curso", "Evaluación", "Calificación (0 a 10)"},
                new JComponent[]{capturaEstudiante, capturaCurso, capturaEvaluacion, capturaCalificacion},
                botonCaptura,
                new BotonRedondo("Limpiar", false, () -> limpiar(new Campo[]{capturaCalificacion}))));

        JPanel dos = new JPanel(new GridLayout(1, 2, 12, 0));
        dos.setOpaque(false);
        dos.add(tarjetaEvaluacion);
        dos.add(tarjetaCaptura);

        // Tarjeta 3: lote
        loteResultado.setFont(fuente(TEXTO, false));
        loteResultado.setForeground(GRIS_PIZARRA);
        JPanel cuerpoLote = new JPanel(new GridBagLayout());
        cuerpoLote.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.gridy = 0; gc.weightx = 0; gc.fill = GridBagConstraints.NONE; gc.insets = new Insets(0, 0, 0, 8);
        gc.gridx = 0; cuerpoLote.add(etiqueta("Modo"), gc);
        gc.gridx = 1; cuerpoLote.add(loteModo, gc);
        gc.gridx = 2; gc.insets = new Insets(0, 16, 0, 8); cuerpoLote.add(etiqueta("Registros"), gc);
        gc.gridx = 3; gc.insets = new Insets(0, 0, 0, 16); cuerpoLote.add(loteCantidad, gc);
        gc.gridx = 4; gc.insets = new Insets(0, 0, 0, 0);
        cuerpoLote.add(new BotonRedondo("Procesar lote de ejemplo", true, this::procesarLote), gc);
        gc.gridx = 5; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        cuerpoLote.add(Box.createHorizontalGlue(), gc);
        gc.gridx = 0; gc.gridy = 1; gc.gridwidth = 6; gc.insets = new Insets(8, 0, 0, 0);
        cuerpoLote.add(loteResultado, gc);

        // Tabla con las calificaciones del estudiante elegido
        tabla = new PanelTabla(
                new String[]{"Curso", "Evaluación", "Calificación"},
                new Class<?>[]{String.class, String.class, Double.class},
                new int[]{300, 200, 120},
                "Este estudiante todavía no tiene calificaciones.");

        JPanel arriba = new JPanel();
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        arriba.setOpaque(false);
        arriba.add(dos);
        arriba.add(Box.createVerticalStrut(12));
        arriba.add(tarjeta("Procesar calificaciones por lote", cuerpoLote));
        arriba.add(Box.createVerticalStrut(12));

        JPanel tarjetaTabla = tarjeta("Calificaciones del estudiante seleccionado", tabla);
        tarjetaTabla.setPreferredSize(new Dimension(100, 300));

        PaginaScrollable pagina = new PaginaScrollable();
        pagina.setBorder(new EmptyBorder(12, 12, 12, 12));
        pagina.add(arriba, BorderLayout.NORTH);
        pagina.add(tarjetaTabla, BorderLayout.CENTER);
        JScrollPane scroll = new JScrollPane(pagina,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(NIEVE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // Al cambiar de curso se actualizan sus evaluaciones; al cambiar de estudiante, la tabla
        capturaCurso.addActionListener(e -> { if (!cargando) llenarEvaluaciones(); });
        capturaEstudiante.addActionListener(e -> refrescarTabla());
        refrescar();
    }

    /** Formulario con etiquetas a la izquierda, controles a la derecha y una fila de botones. */
    private static JPanel cuerpo(String[] etiquetas, JComponent[] controles, JComponent... botones) {
        JPanel cuerpo = new JPanel(new GridBagLayout());
        cuerpo.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < etiquetas.length; i++) {
            gc.gridy = i;
            gc.gridx = 0; gc.weightx = 0; gc.fill = GridBagConstraints.NONE; gc.insets = new Insets(5, 0, 5, 12);
            cuerpo.add(etiqueta(etiquetas[i]), gc);
            gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(5, 0, 5, 0);
            cuerpo.add(controles[i], gc);
        }
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fila.setOpaque(false);
        for (int i = 0; i < botones.length; i++) {
            if (i > 0) fila.add(Box.createHorizontalStrut(8));
            fila.add(botones[i]);
        }
        gc.gridx = 0; gc.gridy = etiquetas.length; gc.gridwidth = 2; gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE; gc.insets = new Insets(6, 0, 0, 0);
        cuerpo.add(fila, gc);
        gc.gridy = etiquetas.length + 1; gc.weighty = 1; gc.fill = GridBagConstraints.VERTICAL;   // empuja todo hacia arriba
        gc.insets = new Insets(0, 0, 0, 0);
        JPanel relleno = new JPanel();
        relleno.setOpaque(false);
        cuerpo.add(relleno, gc);
        return cuerpo;
    }

    // ---------- Acciones (solo llaman a la lógica) ----------

    private void registrarEvaluacion() {
        Opcion curso = (Opcion) evaluacionCurso.getSelectedItem();
        Campo[] campos = {evaluacionNombre};
        try {
            notas.registrarEvaluacion(curso == null ? "" : curso.id, evaluacionNombre.getText());
            limpiar(campos);
            refrescar();
            aviso("Listo", "Evaluación registrada.", JOptionPane.INFORMATION_MESSAGE);
        } catch (ValidacionException e) {
            marcarError(campos);
            aviso("No se pudo registrar", e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void capturarCalificacion() {
        Opcion estudiante = (Opcion) capturaEstudiante.getSelectedItem();
        Opcion curso = (Opcion) capturaCurso.getSelectedItem();
        Opcion evaluacion = (Opcion) capturaEvaluacion.getSelectedItem();
        Campo[] campos = {capturaCalificacion};
        String texto = capturaCalificacion.getText().trim();
        try {
            if (!texto.matches("-?\\d+([.,]\\d+)?")) {
                throw new ValidacionException("La calificación debe ser un número");
            }
            double valor = Double.parseDouble(texto.replace(',', '.'));
            notas.capturarCalificacion(estudiante == null ? "" : estudiante.id,
                    curso == null ? "" : curso.id, evaluacion == null ? "" : evaluacion.id, valor);
            limpiar(campos);
            refrescarTabla();
            aviso("Listo", "Calificación guardada.", JOptionPane.INFORMATION_MESSAGE);
        } catch (ValidacionException e) {
            marcarError(campos);
            aviso("No se pudo guardar", e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Genera un lote de ejemplo con los datos registrados y lo procesa en el modo elegido, midiendo el tiempo. */
    private void procesarLote() {
        int cantidad = new int[]{1_000, 10_000, 100_000}[loteCantidad.getSelectedIndex()];
        List<Calificacion> lote = generarLote(cantidad);
        if (lote.isEmpty()) {
            aviso("Faltan datos", "Primero registra estudiantes, cursos, inscripciones y evaluaciones.",
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

    private void aviso(String titulo, String mensaje, int tipo) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, tipo);
    }

    // ---------- Actualización de la pantalla ----------

    /** Vuelve a leer estudiantes, cursos y evaluaciones. Se llama al abrir la pestaña. */
    public void refrescar() {
        List<Opcion> estudiantes = new ArrayList<>();
        for (Estudiante e : academico.listarEstudiantes()) {
            estudiantes.add(new Opcion(e.getMatricula(), e.getMatricula() + " | " + e.getNombre() + " (" + e.getGrupo() + ")"));
        }
        List<Opcion> cursos = new ArrayList<>();
        for (Curso c : academico.listarCursos()) {
            cursos.add(new Opcion(c.getClave(), c.getClave() + " | " + c.getNombre()));
        }
        cargando = true;
        try {
            llenarCombo(evaluacionCurso, cursos);
            llenarCombo(capturaEstudiante, estudiantes);
            llenarCombo(capturaCurso, cursos);
            llenarEvaluaciones();
        } finally {
            cargando = false;
        }
        refrescarTabla();
        botonEvaluacion.setEnabled(!cursos.isEmpty());
    }

    private void llenarEvaluaciones() {
        Opcion curso = (Opcion) capturaCurso.getSelectedItem();
        List<Opcion> opciones = new ArrayList<>();
        if (curso != null) {
            for (String nombre : notas.evaluacionesDeCurso(curso.id)) opciones.add(new Opcion(nombre, nombre));
        }
        llenarCombo(capturaEvaluacion, opciones);
        botonCaptura.setEnabled(capturaEstudiante.getItemCount() > 0 && !opciones.isEmpty());
    }

    private void refrescarTabla() {
        if (cargando || tabla == null) return;
        tabla.modelo.setRowCount(0);
        Opcion estudiante = (Opcion) capturaEstudiante.getSelectedItem();
        if (estudiante != null) {
            for (Calificacion c : notas.consultarCalificaciones(estudiante.id)) {
                tabla.modelo.addRow(new Object[]{
                        c.clave() + " | " + academico.buscarCurso(c.clave()).getNombre(),
                        c.evaluacion(), c.calificacion()});
            }
        }
        tabla.mostrar(tabla.modelo.getRowCount() > 0);
    }
}

