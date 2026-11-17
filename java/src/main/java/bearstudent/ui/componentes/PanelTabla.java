package bearstudent.ui.componentes;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.Locale;

import static bearstudent.ui.componentes.Estilo.*;

/** Tabla de solo lectura con encabezado azul escarcha, filas alternas y un aviso cuando está vacía. */
public class PanelTabla extends JPanel {
    private final DefaultTableModel modelo;
    private final CardLayout cartas = new CardLayout();

    /** tipos indica la clase de cada columna: las de números (Integer, Double) se alinean a la derecha. */
    public PanelTabla(String[] columnas, Class<?>[] tipos, int[] anchos, String textoVacio) {
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
                if (valor instanceof Double) setText(String.format(Locale.US, "%.2f", valor));
                setFont(fuente(CELDA, false));
                setForeground(GRIS_CARBON);
                setBackground(seleccionada ? AZUL_HIELO : (fila % 2 == 0 ? BLANCO : AZUL_NIEBLA));
                setBorder(new EmptyBorder(0, 8, 0, 8));
                // Según la columna y no según el valor: así "Sin notas" o "—" quedan alineados con los números
                setHorizontalAlignment(esNumerica(t, col) ? RIGHT : LEFT);
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
                setHorizontalAlignment(esNumerica(t, col) ? RIGHT : LEFT);
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

    /** Reemplaza todas las filas y muestra la tabla, o el aviso si no hay ninguna. */
    public void llenar(List<Object[]> filas) {
        modelo.setRowCount(0);
        for (Object[] fila : filas) modelo.addRow(fila);
        cartas.show(this, filas.isEmpty() ? "vacio" : "tabla");
    }

    private static boolean esNumerica(JTable t, int col) {
        return Number.class.isAssignableFrom(t.getColumnClass(col));
    }
}
