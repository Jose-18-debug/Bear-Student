package bearstudent.ui.componentes;

import bearstudent.dto.Curso;
import bearstudent.dto.Estudiante;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static bearstudent.ui.componentes.Estilo.*;

/** Piezas que comparten todas las pestañas: tarjetas, formularios, listas desplegables y avisos. */
public final class Componentes {

    private Componentes() { }

    public static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(TEXTO, false));
        l.setForeground(GRIS_CARBON);
        return l;
    }

    public static void estilizarCombo(JComboBox<?> combo) {
        combo.setFont(fuente(TEXTO, false));
        combo.setBackground(BLANCO);
        combo.setForeground(GRIS_CARBON);
        combo.setPreferredSize(new Dimension(200, 32));
    }

    /** Tarjeta blanca con borde azul escarcha y un título. */
    public static JPanel tarjeta(String titulo, JComponent cuerpo) {
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

    /** Formulario: etiquetas a la izquierda, controles a la derecha y una fila de botones debajo. */
    public static JPanel formulario(String[] etiquetas, JComponent[] controles, JComponent... botones) {
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
        // Fila de relleno: empuja el contenido hacia arriba para que los formularios vecinos queden alineados
        gc.gridy = etiquetas.length + 1; gc.weighty = 1; gc.fill = GridBagConstraints.VERTICAL;
        gc.insets = new Insets(0, 0, 0, 0);
        JPanel relleno = new JPanel();
        relleno.setOpaque(false);
        cuerpo.add(relleno, gc);
        return cuerpo;
    }

    /**
     * Página de una pestaña: arriba va el contenido principal y centro (opcional) ocupa el resto del alto.
     * Se ajusta al ancho de la ventana y, si la ventana es baja, se puede deslizar hacia abajo.
     */
    public static JScrollPane pagina(JComponent arriba, JComponent centro) {
        PaginaScrollable pagina = new PaginaScrollable();
        pagina.setBorder(new EmptyBorder(12, 12, 12, 12));
        pagina.add(arriba, BorderLayout.NORTH);
        if (centro != null) pagina.add(centro, BorderLayout.CENTER);
        JScrollPane scroll = new JScrollPane(pagina,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(NIEVE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    public static List<Opcion> opcionesEstudiantes(Collection<Estudiante> estudiantes) {
        List<Opcion> lista = new ArrayList<>();
        for (Estudiante e : estudiantes) lista.add(Opcion.de(e));
        return lista;
    }

    public static List<Opcion> opcionesCursos(Collection<Curso> cursos) {
        List<Opcion> lista = new ArrayList<>();
        for (Curso c : cursos) lista.add(Opcion.de(c));
        return lista;
    }

    /** Rellena la lista desplegable y conserva la opción elegida si todavía existe. */
    public static void llenarCombo(JComboBox<Opcion> combo, List<Opcion> opciones) {
        Opcion previa = (Opcion) combo.getSelectedItem();
        combo.removeAllItems();
        for (Opcion o : opciones) combo.addItem(o);
        if (previa != null) {
            for (Opcion o : opciones) if (o.id().equals(previa.id())) combo.setSelectedItem(o);
        }
    }

    /** Matrícula o clave de la opción elegida, o "" si la lista está vacía (la lógica avisará del error). */
    public static String idElegido(JComboBox<Opcion> combo) {
        Opcion o = (Opcion) combo.getSelectedItem();
        return o == null ? "" : o.id();
    }

    /** Vacía los campos, les quita la marca de error y regresa el foco al primero. */
    public static void limpiar(Campo... campos) {
        for (Campo c : campos) {
            c.setText("");
            c.setInvalido(false);
        }
        campos[0].requestFocusInWindow();
    }

    /** Marca en rojo los campos vacíos (o el primero si ninguno está vacío) y manda el foco ahí. */
    public static void marcarError(Campo... campos) {
        List<Campo> vacios = new ArrayList<>();
        for (Campo c : campos) if (c.getText().trim().isEmpty()) vacios.add(c);
        List<Campo> marcados = vacios.isEmpty() ? List.of(campos[0]) : vacios;
        for (Campo c : marcados) c.setInvalido(true);
        marcados.get(0).requestFocusInWindow();
    }

    public static void aviso(Component padre, String titulo, String mensaje, int tipo) {
        JOptionPane.showMessageDialog(padre, mensaje, titulo, tipo);
    }
}
