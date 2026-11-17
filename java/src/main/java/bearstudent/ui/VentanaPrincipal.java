package bearstudent.ui;

import bearstudent.dto.Estudiante;
import bearstudent.logica.RegistroAcademico;
import bearstudent.logica.RegistroCalificaciones;
import bearstudent.ui.componentes.PestanasUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

import static bearstudent.ui.componentes.Estilo.*;

/**
 * Ventana principal (Swing): barra superior con el logo, una pestaña por cada parte del equipo y la barra
 * de estado. Guarda una sola instancia de RegistroAcademico y de RegistroCalificaciones y se las pasa a las
 * pestañas, así todas ven los mismos datos. No contiene lógica de negocio.
 */
public class VentanaPrincipal extends JFrame {
    private final RegistroAcademico registro = new RegistroAcademico();   // aquí vive todo el estado
    private final RegistroCalificaciones calificaciones = new RegistroCalificaciones(registro);
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
        actualizarEstado();
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

    private void crearInterfaz() {
        getContentPane().setBackground(NIEVE);
        setLayout(new BorderLayout());
        add(crearBarraSuperior(), BorderLayout.NORTH);

        estado.setOpaque(true);
        estado.setBackground(GRIS_ESCARCHA);
        estado.setForeground(GRIS_PIZARRA);
        estado.setFont(fuente(NOTA, false));
        estado.setBorder(new EmptyBorder(6, 16, 6, 16));
        add(estado, BorderLayout.SOUTH);

        // Pestañas principales: una por cada parte del equipo (partes 1 a 5)
        JTabbedPane principal = new JTabbedPane();
        principal.setUI(new PestanasUI());
        principal.setFont(fuente(BOTON, true));
        principal.setBackground(NIEVE);
        principal.addTab("Estudiantes y cursos", new PanelEstudiantes(registro, this::actualizarEstado));
        principal.addTab("Calificaciones", new PanelCalificaciones(registro, calificaciones));
        principal.addTab("Promedios", new PanelPromedios(registro, calificaciones));
        principal.addTab("Reportes", new PanelReportes(registro, calificaciones));
        principal.addChangeListener(e -> {
            if (principal.getSelectedComponent() instanceof Refrescable pestana) pestana.refrescar();
        });
        JPanel marco = new JPanel(new BorderLayout());
        marco.setOpaque(false);
        marco.setBorder(new EmptyBorder(8, 8, 8, 8));
        marco.add(principal, BorderLayout.CENTER);
        add(marco, BorderLayout.CENTER);
    }

    /** Barra blanca con el logo a la izquierda, la descripción a la derecha y una línea azul hielo debajo. */
    private JPanel crearBarraSuperior() {
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
        return norte;
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

    /** Totales de la barra de estado; se llama al abrir la ventana y después de cada registro o inscripción. */
    private void actualizarEstado() {
        int inscripciones = 0;
        for (Estudiante e : registro.listarEstudiantes()) {
            inscripciones += registro.cursosDeEstudiante(e.getMatricula()).size();
        }
        estado.setText(String.format("Estudiantes: %d     Cursos: %d     Inscripciones: %d",
                registro.listarEstudiantes().size(), registro.listarCursos().size(), inscripciones));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
