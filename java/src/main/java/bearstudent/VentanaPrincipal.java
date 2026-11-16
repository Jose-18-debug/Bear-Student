package bearstudent;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Interfaz gráfica (Swing) de la Parte 1: estudiantes, cursos e inscripciones.
 * No contiene lógica de negocio: solo llama a RegistroAcademico y muestra el resultado.
 * To do está en un archivo (las clases de apoyo son internas y estáticas).
 */
public class VentanaPrincipal extends JFrame {

    // ---------- Paleta invierno (la misma que Python y JavaScript) ----------
    static final Color AZUL_HIELO = new Color(0xA8D8F0);
    static final Color AZUL_ESCARCHA = new Color(0xD4E9F7);
    static final Color AZUL_NIEBLA = new Color(0xEAF4FB);
    static final Color AZUL_GLACIAR = new Color(0x5FA8D3);
    static final Color AZUL_INVIERNO = new Color(0x2E6FA7);
    static final Color AZUL_PROFUNDO = new Color(0x255C8E);
    static final Color AZUL_NOCHE = new Color(0x1B4568);
    static final Color NIEVE = new Color(0xF7FAFC);
    static final Color BLANCO = Color.WHITE;
    static final Color GRIS_ESCARCHA = new Color(0xEDF1F5);
    static final Color GRIS_BORDE = new Color(0xD5DDE5);
    static final Color GRIS_ACERO = new Color(0x8A99A8);
    static final Color GRIS_PIZARRA = new Color(0x5C6B7A);
    static final Color GRIS_CARBON = new Color(0x1F2D3A);
    static final Color ROJO_ERROR = new Color(0xD9534F);

    // Swing mide las fuentes en píxeles, por eso se usan los tamaños de la columna "Web"
    // de la guía de diseño (se ven igual que los 11 pt de Tkinter).
    static final int TITULO = 24, SUBTITULO = 18, TEXTO = 14, BOTON = 14, ENC_TABLA = 13, CELDA = 13, NOTA = 12;
    static final int ALTO_LOGO = 56;

    static final String FAMILIA = elegirFamilia();

    /** Nunito si está instalada; si no, Segoe UI; si no, Arial. */
    static String elegirFamilia() {
        Set<String> disponibles = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String nombre : new String[]{"Nunito", "Segoe UI", "Arial"}) {
            if (disponibles.contains(nombre)) return nombre;
        }
        return Font.SANS_SERIF;
    }

    static Font fuente(int tam, boolean negrita) {
        return new Font(FAMILIA, negrita ? Font.BOLD : Font.PLAIN, tam);
    }

    // ---------- Estado de la ventana ----------
    private final RegistroAcademico registro = new RegistroAcademico();   // aquí vive todo el estado
    private final RegistroCalificaciones calificaciones = new RegistroCalificaciones(registro);   // Parte 2

    private final Campo matricula = new Campo();
    private final Campo nombre = new Campo();
    private final Campo grupo = new Campo();
    private final Campo clave = new Campo();
    private final Campo nombreCurso = new Campo();
    private final JComboBox<Opcion> comboEstudiante = new JComboBox<>();
    private final JComboBox<Opcion> comboCurso = new JComboBox<>();
    private BotonRedondo botonInscribir;
    private PanelTabla tablaEstudiantes;
    private PanelTabla tablaCursos;
    private PanelTabla tablaInscripciones;
    private final JLabel estado = new JLabel();

    public VentanaPrincipal() {
        super("Bear Student · Administración de estudiantes y cursos");
        configurarAspecto();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Tamaño inicial: el que quepa en la pantalla (sin la barra de tareas); luego arranca maximizada
        Rectangle pantalla = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(Math.min(1000, pantalla.width), Math.min(820, pantalla.height));
        setMinimumSize(new Dimension(640, 480));
        crearInterfaz();
        actualizar();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void configurarAspecto() {
        UIManager.put("OptionPane.background", BLANCO);
        UIManager.put("OptionPane.messageForeground", GRIS_CARBON);
        UIManager.put("OptionPane.messageFont", fuente(TEXTO, false));
        UIManager.put("OptionPane.buttonFont", fuente(BOTON, true));
        UIManager.put("TextField.selectionBackground", AZUL_HIELO);
        UIManager.put("TextField.selectionForeground", GRIS_CARBON);
    }

    // ---------- Construcción de la interfaz ----------

    private void crearInterfaz() {
        getContentPane().setBackground(NIEVE);
        setLayout(new BorderLayout());

        // Barra superior con el logo
        Color[] fondoLogo = {BLANCO};
        ImageIcon logo = cargarLogo(fondoLogo);
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(fondoLogo[0]);
        JLabel izquierda = logo != null ? new JLabel(logo) : new JLabel("Bear Student");
        izquierda.setFont(fuente(TITULO, true));
        izquierda.setForeground(AZUL_INVIERNO);
        izquierda.setBorder(new EmptyBorder(10, 24, 10, 24));
        JLabel derecha = new JLabel("<html><div style='text-align:right'>Registro de estudiantes,<br>cursos e inscripciones</div></html>");
        derecha.setFont(fuente(TEXTO, false));
        derecha.setForeground(GRIS_PIZARRA);
        derecha.setBorder(new EmptyBorder(0, 24, 0, 24));
        barra.add(izquierda, BorderLayout.WEST);
        barra.add(derecha, BorderLayout.EAST);
        JPanel linea = new JPanel();
        linea.setBackground(AZUL_HIELO);
        linea.setPreferredSize(new Dimension(0, 3));
        JPanel norte = new JPanel(new BorderLayout());
        norte.add(barra, BorderLayout.NORTH);
        norte.add(linea, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        // Barra de estado
        estado.setOpaque(true);
        estado.setBackground(GRIS_ESCARCHA);
        estado.setForeground(GRIS_PIZARRA);
        estado.setFont(fuente(NOTA, false));
        estado.setBorder(new EmptyBorder(6, 16, 6, 16));
        add(estado, BorderLayout.SOUTH);

        // Formularios de estudiante y curso, lado a lado
        JPanel formularios = new JPanel(new GridLayout(1, 2, 12, 0));
        formularios.setOpaque(false);
        formularios.add(tarjeta("Registrar estudiante", formulario(
                new String[]{"Matrícula", "Nombre", "Grupo"},
                new Campo[]{matricula, nombre, grupo},
                "Guardar estudiante", this::registrarEstudiante)));
        formularios.add(tarjeta("Registrar curso", formulario(
                new String[]{"Clave", "Nombre del curso"},
                new Campo[]{clave, nombreCurso},
                "Guardar curso", this::registrarCurso)));

        // Panel para inscribir
        JPanel arriba = new JPanel();
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        arriba.setOpaque(false);
        arriba.add(formularios);
        arriba.add(Box.createVerticalStrut(12));
        arriba.add(tarjeta("Inscribir estudiante en un curso", crearPanelInscribir()));
        arriba.add(Box.createVerticalStrut(12));

        // Pestañas con las tablas
        tablaEstudiantes = new PanelTabla(
                new String[]{"Matrícula", "Nombre", "Grupo", "Cursos"},
                new Class<?>[]{String.class, String.class, String.class, Integer.class},
                new int[]{140, 300, 100, 90},
                "Aún no hay estudiantes. Regístralos con el formulario de arriba.");
        tablaCursos = new PanelTabla(
                new String[]{"Clave", "Nombre del curso", "Inscritos"},
                new Class<?>[]{String.class, String.class, Integer.class},
                new int[]{140, 300, 90},
                "Aún no hay cursos. Registra el primero con el formulario de arriba.");
        tablaInscripciones = new PanelTabla(
                new String[]{"Estudiante", "Curso"},
                new Class<?>[]{String.class, String.class},
                new int[]{300, 300},
                "Todavía no hay inscripciones. Registra un estudiante y un curso para inscribirlo.");
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setUI(new PestanasUI());
        pestanas.setFont(fuente(BOTON, true));
        pestanas.setBackground(NIEVE);
        pestanas.setPreferredSize(new Dimension(100, 330));
        pestanas.addTab("Estudiantes", tablaEstudiantes);
        pestanas.addTab("Cursos", tablaCursos);
        pestanas.addTab("Inscripciones", tablaInscripciones);

        // Página de estudiantes y cursos: si la ventana es baja, se puede deslizar hacia abajo
        PaginaScrollable pagina = new PaginaScrollable();
        pagina.setBorder(new EmptyBorder(12, 12, 12, 12));
        pagina.add(arriba, BorderLayout.NORTH);
        pagina.add(pestanas, BorderLayout.CENTER);
        JScrollPane paginaEstudiantes = new JScrollPane(pagina,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        paginaEstudiantes.setBorder(BorderFactory.createEmptyBorder());
        paginaEstudiantes.getViewport().setBackground(NIEVE);
        paginaEstudiantes.getVerticalScrollBar().setUnitIncrement(16);

        // Pestañas principales: una por cada parte del equipo (aquí van la Parte 1 y la Parte 2)
        PanelCalificaciones panelCalificaciones = new PanelCalificaciones(registro, calificaciones);
        JTabbedPane principal = new JTabbedPane();
        principal.setUI(new PestanasUI());
        principal.setFont(fuente(BOTON, true));
        principal.setBackground(NIEVE);
        principal.addTab("Estudiantes y cursos", paginaEstudiantes);
        principal.addTab("Calificaciones", panelCalificaciones);
        principal.addChangeListener(e -> {
            if (principal.getSelectedComponent() == panelCalificaciones) panelCalificaciones.refrescar();
        });
        JPanel marco = new JPanel(new BorderLayout());
        marco.setOpaque(false);
        marco.setBorder(new EmptyBorder(8, 8, 8, 8));
        marco.add(principal, BorderLayout.CENTER);
        add(marco, BorderLayout.CENTER);
    }

    private JPanel crearPanelInscribir() {
        JPanel cuerpo = new JPanel(new GridBagLayout());
        cuerpo.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;

        gc.gridy = 0; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.insets = new Insets(8, 0, 4, 0);
        cuerpo.add(etiqueta("Estudiante"), gc);
        gc.gridx = 1; gc.insets = new Insets(8, 8, 4, 0);
        cuerpo.add(etiqueta("Curso"), gc);

        estilizarCombo(comboEstudiante);
        estilizarCombo(comboCurso);
        gc.gridy = 1;
        gc.gridx = 0; gc.insets = new Insets(0, 0, 0, 0);
        cuerpo.add(comboEstudiante, gc);
        gc.gridx = 1; gc.insets = new Insets(0, 8, 0, 0);
        cuerpo.add(comboCurso, gc);

        botonInscribir = new BotonRedondo("Inscribir", true, this::inscribir);
        gc.gridx = 2; gc.weightx = 0; gc.fill = GridBagConstraints.NONE; gc.insets = new Insets(0, 16, 0, 0);
        cuerpo.add(botonInscribir, gc);
        return cuerpo;
    }

    static void estilizarCombo(JComboBox<?> combo) {
        combo.setFont(fuente(TEXTO, false));
        combo.setBackground(BLANCO);
        combo.setForeground(GRIS_CARBON);
        combo.setPreferredSize(new Dimension(200, 32));
    }

    static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(TEXTO, false));
        l.setForeground(GRIS_CARBON);
        return l;
    }

    /** Tarjeta blanca con borde azul escarcha y un título. */
    static JPanel tarjeta(String titulo, JComponent cuerpo) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(BLANCO);
        p.setBorder(new CompoundBorder(new LineBorder(AZUL_ESCARCHA, 1), new EmptyBorder(12, 12, 12, 12)));
        JLabel t = new JLabel(titulo);
        t.setFont(fuente(SUBTITULO, true));
        t.setForeground(AZUL_INVIERNO);
        p.add(t, BorderLayout.NORTH);
        p.add(cuerpo, BorderLayout.CENTER);
        return p;
    }

    /** Formulario: etiquetas a la izquierda, campos a la derecha y botones Guardar / Limpiar. */
    private JPanel formulario(String[] etiquetas, Campo[] campos, String textoGuardar, Runnable guardar) {
        JPanel cuerpo = new JPanel(new GridBagLayout());
        cuerpo.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < etiquetas.length; i++) {
            gc.gridy = i;
            gc.gridx = 0; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
            gc.insets = new Insets(5, 0, 5, 12);
            cuerpo.add(etiqueta(etiquetas[i]), gc);
            gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
            gc.insets = new Insets(5, 0, 5, 0);
            cuerpo.add(campos[i], gc);
        }
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        botones.setOpaque(false);
        botones.add(new BotonRedondo(textoGuardar, true, guardar));
        botones.add(Box.createHorizontalStrut(8));
        botones.add(new BotonRedondo("Limpiar", false, () -> limpiar(campos)));
        gc.gridx = 0; gc.gridy = etiquetas.length; gc.gridwidth = 2;
        gc.weightx = 0; gc.fill = GridBagConstraints.NONE; gc.insets = new Insets(6, 0, 0, 0);
        cuerpo.add(botones, gc);
        // Fila de relleno: empuja el contenido hacia arriba para que ambos formularios queden alineados
        gc.gridy = etiquetas.length + 1; gc.weighty = 1; gc.fill = GridBagConstraints.VERTICAL;
        gc.insets = new Insets(0, 0, 0, 0);
        JPanel relleno = new JPanel();
        relleno.setOpaque(false);
        cuerpo.add(relleno, gc);
        return cuerpo;
    }

    /**
     * Carga el logo de src/main/resources/img/logo.png, recorta el mismo margen que la versión
     * de Python (5%-95% de ancho, 24%-78% de alto) y lo escala a ALTO_LOGO px de alto.
     * fondoSalida[0] recibe el color del fondo del logo para pintar la barra superior.
     */
    private ImageIcon cargarLogo(Color[] fondoSalida) {
        try {
            URL url = VentanaPrincipal.class.getResource("/img/logo.png");
            if (url == null) return null;
            BufferedImage original = ImageIO.read(url);
            int w = original.getWidth(), h = original.getHeight();
            BufferedImage recorte = original.getSubimage(
                    (int) (w * 0.05), (int) (h * 0.24), (int) (w * 0.90), (int) (h * 0.54));
            BufferedImage base = new BufferedImage(recorte.getWidth(), recorte.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = base.createGraphics();
            g.setColor(Color.WHITE);                // si el PNG es transparente, queda sobre blanco
            g.fillRect(0, 0, base.getWidth(), base.getHeight());
            g.drawImage(recorte, 0, 0, null);
            g.dispose();
            fondoSalida[0] = new Color(base.getRGB(0, 0));
            int ancho = Math.round(base.getWidth() * (float) ALTO_LOGO / base.getHeight());
            return new ImageIcon(base.getScaledInstance(ancho, ALTO_LOGO, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return null;                            // sin logo se muestra el texto "Bear Student"
        }
    }

    // ---------- Acciones (solo llaman a RegistroAcademico) ----------

    private void registrarEstudiante() {
        Campo[] campos = {matricula, nombre, grupo};
        try {
            registro.registrarEstudiante(matricula.getText(), nombre.getText(), grupo.getText());
            limpiar(campos);
            actualizar();
            aviso("Listo", "Estudiante registrado.", JOptionPane.INFORMATION_MESSAGE);
        } catch (ValidacionException e) {
            marcarError(campos);
            aviso("No se pudo registrar", e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarCurso() {
        Campo[] campos = {clave, nombreCurso};
        try {
            registro.registrarCurso(clave.getText(), nombreCurso.getText());
            limpiar(campos);
            actualizar();
            aviso("Listo", "Curso registrado.", JOptionPane.INFORMATION_MESSAGE);
        } catch (ValidacionException e) {
            marcarError(campos);
            aviso("No se pudo registrar", e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void inscribir() {
        Opcion e = (Opcion) comboEstudiante.getSelectedItem();
        Opcion c = (Opcion) comboCurso.getSelectedItem();
        try {
            registro.inscribir(e == null ? "" : e.id, c == null ? "" : c.id);
            actualizar();
            aviso("Listo", "Inscripción realizada.", JOptionPane.INFORMATION_MESSAGE);
        } catch (ValidacionException ex) {
            aviso("No se pudo inscribir", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aviso(String titulo, String mensaje, int tipo) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, tipo);
    }

    static void limpiar(Campo[] campos) {
        for (Campo c : campos) {
            c.setText("");
            c.setInvalido(false);
        }
        campos[0].requestFocusInWindow();
    }

    /** Marca en rojo los campos vacíos (o el primero si ninguno está vacío) y manda el foco ahí. */
    static void marcarError(Campo[] campos) {
        List<Campo> vacios = new ArrayList<>();
        for (Campo c : campos) if (c.getText().trim().isEmpty()) vacios.add(c);
        List<Campo> marcados = vacios.isEmpty() ? List.of(campos[0]) : vacios;
        for (Campo c : marcados) c.setInvalido(true);
        marcados.get(0).requestFocusInWindow();
    }

    /** Vuelve a pintar todo a partir del estado de RegistroAcademico. */
    private void actualizar() {
        Collection<Estudiante> estudiantes = registro.listarEstudiantes();
        Collection<Curso> cursos = registro.listarCursos();

        List<Opcion> opcionesEstudiantes = new ArrayList<>();
        for (Estudiante e : estudiantes) {
            opcionesEstudiantes.add(new Opcion(e.getMatricula(),
                    e.getMatricula() + " | " + e.getNombre() + " (" + e.getGrupo() + ")"));
        }
        List<Opcion> opcionesCursos = new ArrayList<>();
        for (Curso c : cursos) {
            opcionesCursos.add(new Opcion(c.getClave(), c.getClave() + " | " + c.getNombre()));
        }
        llenarCombo(comboEstudiante, opcionesEstudiantes);
        llenarCombo(comboCurso, opcionesCursos);

        tablaEstudiantes.modelo.setRowCount(0);
        for (Estudiante e : estudiantes) {
            tablaEstudiantes.modelo.addRow(new Object[]{
                    e.getMatricula(), e.getNombre(), e.getGrupo(),
                    registro.cursosDeEstudiante(e.getMatricula()).size()});
        }
        tablaCursos.modelo.setRowCount(0);
        for (Curso c : cursos) {
            tablaCursos.modelo.addRow(new Object[]{c.getClave(), c.getNombre(), c.getTotalInscritos()});
        }
        tablaInscripciones.modelo.setRowCount(0);
        int totalInscripciones = 0;
        for (Estudiante e : estudiantes) {
            for (String claveCurso : registro.cursosDeEstudiante(e.getMatricula())) {
                tablaInscripciones.modelo.addRow(new Object[]{
                        e.getNombre(), registro.buscarCurso(claveCurso).getNombre()});
                totalInscripciones++;
            }
        }
        tablaEstudiantes.mostrar(!estudiantes.isEmpty());
        tablaCursos.mostrar(!cursos.isEmpty());
        tablaInscripciones.mostrar(totalInscripciones > 0);

        botonInscribir.setEnabled(!estudiantes.isEmpty() && !cursos.isEmpty());
        estado.setText(String.format("Estudiantes: %d     Cursos: %d     Inscripciones: %d",
                estudiantes.size(), cursos.size(), totalInscripciones));
    }

    /** Rellena el combo y conserva la selección anterior si todavía existe. */
    static void llenarCombo(JComboBox<Opcion> combo, List<Opcion> opciones) {
        Opcion previa = (Opcion) combo.getSelectedItem();
        combo.removeAllItems();
        for (Opcion o : opciones) combo.addItem(o);
        if (previa != null) {
            for (Opcion o : opciones) if (o.id.equals(previa.id)) combo.setSelectedItem(o);
        }
    }

    // ---------- Clases de apoyo ----------

    /** Elemento de un combo: guarda el id real (matrícula o clave) y el texto que se ve. */
    static class Opcion {
        final String id;
        final String texto;
        Opcion(String id, String texto) { this.id = id; this.texto = texto; }
        @Override public String toString() { return texto; }
    }

    /** Campo de texto con borde gris, azul glaciar al enfocarlo y rojo si es inválido. */
    static class Campo extends JTextField {
        private boolean invalido;
        private boolean enfocado;

        Campo() {
            super(18);
            setFont(fuente(TEXTO, false));
            setForeground(GRIS_CARBON);
            setBackground(BLANCO);
            setCaretColor(GRIS_CARBON);
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { enfocado = true; pintarBorde(); }
                @Override public void focusLost(FocusEvent e) { enfocado = false; pintarBorde(); }
            });
            addKeyListener(new KeyAdapter() {
                @Override public void keyTyped(KeyEvent e) { if (invalido) setInvalido(false); }
            });
            pintarBorde();
        }

        void setInvalido(boolean valor) {
            invalido = valor;
            pintarBorde();
        }

        private void pintarBorde() {
            Color color = invalido ? ROJO_ERROR : (enfocado ? AZUL_GLACIAR : GRIS_BORDE);
            setBorder(new CompoundBorder(new LineBorder(color, 1), new EmptyBorder(5, 8, 5, 8)));
        }
    }

    /** Botón en forma de pastilla: primario (relleno azul) o secundario (solo borde). */
    static class BotonRedondo extends JButton {
        private final boolean primario;

        BotonRedondo(String texto, boolean primario, Runnable accion) {
            super(texto);
            this.primario = primario;
            setFont(fuente(BOTON, true));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            FontMetrics fm = getFontMetrics(getFont());
            setPreferredSize(new Dimension(fm.stringWidth(texto) + 48, 32));
            addActionListener(e -> accion.run());
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { repaint(); }
                @Override public void focusLost(FocusEvent e) { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            ButtonModel m = getModel();
            Color relleno, borde, texto;
            if (!isEnabled()) {
                relleno = primario ? GRIS_BORDE : BLANCO; borde = GRIS_BORDE; texto = GRIS_ACERO;
            } else if (m.isPressed()) {
                relleno = primario ? AZUL_NOCHE : AZUL_ESCARCHA;
                borde = primario ? AZUL_NOCHE : AZUL_INVIERNO;
                texto = primario ? BLANCO : AZUL_INVIERNO;
            } else if (m.isRollover()) {
                relleno = primario ? AZUL_PROFUNDO : AZUL_NIEBLA;
                borde = primario ? AZUL_PROFUNDO : AZUL_INVIERNO;
                texto = primario ? BLANCO : AZUL_INVIERNO;
            } else {
                relleno = primario ? AZUL_INVIERNO : BLANCO;
                borde = AZUL_INVIERNO;
                texto = primario ? BLANCO : AZUL_INVIERNO;
            }
            int w = getWidth(), h = getHeight();
            g2.setColor(borde);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
            if (!borde.equals(relleno)) {
                g2.setColor(relleno);
                g2.fill(new RoundRectangle2D.Float(2, 2, w - 4, h - 4, h - 4, h - 4));
            }
            if (isFocusOwner() && isEnabled()) {      // aro azul glaciar para navegar con teclado
                g2.setColor(AZUL_GLACIAR);
                g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 3, h - 3, h - 2, h - 2));
            }
            g2.setFont(getFont());
            g2.setColor(texto);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Tabla con encabezado azul escarcha, filas alternas y un mensaje cuando está vacía. */
    static class PanelTabla extends JPanel {
        final DefaultTableModel modelo;
        private final CardLayout cartas = new CardLayout();

        PanelTabla(String[] columnas, Class<?>[] tipos, int[] anchos, String textoVacio) {
            setLayout(cartas);
            setBackground(BLANCO);
            setBorder(new EmptyBorder(8, 8, 8, 8));

            modelo = new DefaultTableModel(columnas, 0) {
                @Override public boolean isCellEditable(int fila, int col) { return false; }
                @Override public Class<?> getColumnClass(int col) { return tipos[col]; }
            };
            JTable tabla = new JTable(modelo);
            tabla.setRowHeight(28);
            tabla.setShowGrid(false);
            tabla.setIntercellSpacing(new Dimension(0, 0));
            tabla.setFillsViewportHeight(true);
            tabla.setBackground(BLANCO);
            tabla.setSelectionBackground(AZUL_HIELO);
            tabla.setSelectionForeground(GRIS_CARBON);
            tabla.setFont(fuente(CELDA, false));
            for (int i = 0; i < anchos.length; i++) {
                tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
            }

            DefaultTableCellRenderer celdas = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object valor, boolean seleccionada,
                                                               boolean foco, int fila, int col) {
                    super.getTableCellRendererComponent(t, valor, seleccionada, false, fila, col);
                    if (valor instanceof Double) setText(String.format(java.util.Locale.US, "%.2f", valor));
                    setFont(fuente(CELDA, false));
                    setForeground(GRIS_CARBON);
                    setBackground(seleccionada ? AZUL_HIELO : (fila % 2 == 0 ? BLANCO : AZUL_NIEBLA));
                    setBorder(new EmptyBorder(0, 8, 0, 8));
                    setHorizontalAlignment(valor instanceof Number ? RIGHT : LEFT);
                    return this;
                }
            };
            tabla.setDefaultRenderer(Object.class, celdas);
            tabla.setDefaultRenderer(String.class, celdas);
            tabla.setDefaultRenderer(Integer.class, celdas);
            tabla.setDefaultRenderer(Double.class, celdas);

            tabla.getTableHeader().setReorderingAllowed(false);
            tabla.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object valor, boolean seleccionada,
                                                               boolean foco, int fila, int col) {
                    super.getTableCellRendererComponent(t, valor, false, false, fila, col);
                    setBackground(AZUL_ESCARCHA);
                    setForeground(AZUL_NOCHE);
                    setFont(fuente(ENC_TABLA, true));
                    setBorder(new EmptyBorder(8, 8, 8, 8));
                    setHorizontalAlignment(Number.class.isAssignableFrom(t.getColumnClass(col)) ? RIGHT : LEFT);
                    return this;
                }
            });

            JScrollPane scroll = new JScrollPane(tabla);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.getViewport().setBackground(BLANCO);

            JPanel vacio = new JPanel(new GridBagLayout());
            vacio.setBackground(BLANCO);
            JLabel mensaje = new JLabel("<html><div style='text-align:center;width:380px'>" + textoVacio + "</div></html>");
            mensaje.setFont(fuente(TEXTO, false));
            mensaje.setForeground(GRIS_PIZARRA);
            vacio.add(mensaje);

            add(scroll, "tabla");
            add(vacio, "vacio");
        }

        void mostrar(boolean hayDatos) {
            cartas.show(this, hayDatos ? "tabla" : "vacio");
        }
    }

    /** Pestañas planas: la activa en blanco con línea inferior azul invierno, las demás azul niebla. */
    static class PestanasUI extends BasicTabbedPaneUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabInsets = new Insets(8, 16, 8, 16);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
            tabAreaInsets = new Insets(0, 0, 0, 0);
            contentBorderInsets = new Insets(1, 1, 1, 1);
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                          int x, int y, int w, int h, boolean isSelected) {
            g.setColor(isSelected ? BLANCO : AZUL_NIEBLA);
            g.fillRect(x, y, w, h);
            if (isSelected) {
                g.setColor(AZUL_INVIERNO);
                g.fillRect(x, y + h - 3, w, 3);
            }
        }

        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                      int x, int y, int w, int h, boolean isSelected) {
            g.setColor(AZUL_ESCARCHA);
            g.drawRect(x, y, w - 1, h - 1);
        }

        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                                 int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(font);
            g.setColor(isSelected ? AZUL_INVIERNO : GRIS_PIZARRA);
            g.drawString(title, textRect.x, textRect.y + metrics.getAscent());
        }

        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            Insets ins = tabPane.getInsets();
            int y = ins.top + calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
            g.setColor(AZUL_ESCARCHA);
            g.drawRect(ins.left, y - 1, tabPane.getWidth() - ins.left - ins.right - 1,
                    tabPane.getHeight() - y - ins.bottom);
        }

        @Override
        protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex,
                                           Rectangle iconRect, Rectangle textRect, boolean isSelected) {
            // sin rectángulo punteado de foco
        }
    }

    /** Panel que se ajusta al ancho de la ventana y, si la ventana es baja, se puede deslizar hacia abajo. */
    static class PaginaScrollable extends JPanel implements Scrollable {
        PaginaScrollable() {
            super(new BorderLayout());
            setOpaque(false);
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle area, int orientacion, int direccion) { return 24; }
        @Override public int getScrollableBlockIncrement(Rectangle area, int orientacion, int direccion) {
            return orientacion == SwingConstants.VERTICAL ? area.height : area.width;
        }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        /** Si la ventana es más alta que el contenido, lo estira; si es más baja, deja que se deslice. */
        @Override public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof JViewport vp && vp.getHeight() > getPreferredSize().height;
        }
    }

    // ---------- Arranque ----------

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}