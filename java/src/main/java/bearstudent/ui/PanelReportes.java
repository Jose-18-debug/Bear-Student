package bearstudent.ui;

import bearstudent.logica.PromediosAcademicos;
import bearstudent.logica.RegistroAcademico;
import bearstudent.logica.RegistroCalificaciones;
import bearstudent.dto.Curso;
import bearstudent.logica.ValidacionException;
import bearstudent.logica.estadisticas.EstadisticasAcademicasUseCase.ClasificacionEstudiante;
import bearstudent.logica.estadisticas.EstadisticasAcademicasUseCase.ResumenEstadistico;
import bearstudent.logica.reportes.ExportadorReportes;
import bearstudent.logica.reportes.GeneradorReportes;
import bearstudent.logica.reportes.GeneradorReportes.Reporte;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static bearstudent.ui.VentanaPrincipal.*;

/**
 * Pestaña de la Persona 5: reporte general, por grupo o por asignatura, con las estadísticas
 * de la Persona 4 (aprobados, reprobados, riesgo, promedio general, máxima, mínima y % de aprobación)
 * y botones para exportarlo a JSON o CSV.
 */
public class PanelReportes extends JPanel {
    private static final String GENERAL = "general:";
    private static final String GRUPO = "grupo:";
    private static final String CURSO = "curso:";

    private static final String[] NOMBRES_ESTADISTICAS = {
            "Evaluados", "Aprobados", "Reprobados", "En riesgo",
            "Promedio general", "Calificación máxima", "Calificación mínima", "Aprobación"};

    private final RegistroAcademico academico;
    private final GeneradorReportes generador;

    private final JComboBox<Opcion> selector = new JComboBox<>();
    private final JLabel[] valores = new JLabel[NOMBRES_ESTADISTICAS.length];
    private final PanelTabla tabla;
    private final BotonRedondo botonJson;
    private final BotonRedondo botonCsv;
    private Reporte actual;
    private boolean cargando;

    public PanelReportes(RegistroAcademico academico, RegistroCalificaciones notas) {
        super(new BorderLayout());
        this.academico = academico;
        this.generador = new GeneradorReportes(academico, new PromediosAcademicos(academico, notas));
        setOpaque(false);
        estilizarCombo(selector);
        selector.setPreferredSize(new Dimension(360, 32));

        tabla = new PanelTabla(
                new String[]{"Matrícula", "Nombre", "Grupo", "Promedio", "Estado", "En riesgo"},
                new Class<?>[]{String.class, String.class, String.class, Double.class, String.class, String.class},
                new int[]{120, 260, 90, 100, 120, 90},
                "No hay estudiantes para este reporte. Registra estudiantes y captura calificaciones.");
        tabla.setPreferredSize(new Dimension(100, 300));

        botonJson = new BotonRedondo("Exportar JSON", true, () -> exportar("json"));
        botonCsv = new BotonRedondo("Exportar CSV", false, () -> exportar("csv"));

        // Selector del reporte (izquierda) y botones de exportar (derecha)
        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        izquierda.setOpaque(false);
        izquierda.add(etiqueta("Reporte de"));
        izquierda.add(selector);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.setOpaque(false);
        derecha.add(botonJson);
        derecha.add(botonCsv);
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        cabecera.add(izquierda, BorderLayout.WEST);
        cabecera.add(derecha, BorderLayout.EAST);

        // Estadísticas: un cuadrito por cada dato
        JPanel cuadros = new JPanel(new GridLayout(2, 4, 8, 8));
        cuadros.setOpaque(false);
        for (int i = 0; i < NOMBRES_ESTADISTICAS.length; i++) {
            valores[i] = new JLabel("—");
            cuadros.add(cuadroEstadistica(NOMBRES_ESTADISTICAS[i], valores[i]));
        }

        JPanel cuerpo = new JPanel(new BorderLayout(0, 12));
        cuerpo.setOpaque(false);
        cuerpo.add(cabecera, BorderLayout.NORTH);
        cuerpo.add(cuadros, BorderLayout.CENTER);
        cuerpo.add(tabla, BorderLayout.SOUTH);

        PaginaScrollable pagina = new PaginaScrollable();
        pagina.setBorder(new EmptyBorder(12, 12, 12, 12));
        pagina.add(tarjeta("Reportes y estadísticas", cuerpo), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(pagina,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(NIEVE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        selector.addActionListener(e -> { if (!cargando) refrescarReporte(); });
        refrescar();
    }

    /** Vuelve a llenar la lista de reportes (grupos y cursos pueden haber cambiado) y recalcula el actual. */
    public void refrescar() {
        List<Opcion> opciones = new ArrayList<>();
        opciones.add(new Opcion(GENERAL, "General (todos los estudiantes)"));
        for (String grupo : generador.listarGrupos()) {
            opciones.add(new Opcion(GRUPO + grupo, "Grupo " + grupo));
        }
        for (Curso c : academico.listarCursos()) {
            opciones.add(new Opcion(CURSO + c.getClave(), "Asignatura: " + c.getClave() + " | " + c.getNombre()));
        }
        cargando = true;
        try {
            llenarCombo(selector, opciones);
        } finally {
            cargando = false;
        }
        refrescarReporte();
    }

    private void refrescarReporte() {
        tabla.modelo.setRowCount(0);
        actual = null;
        Opcion opcion = (Opcion) selector.getSelectedItem();
        if (opcion != null) {
            try {
                actual = generar(opcion.id);
            } catch (ValidacionException e) {
                actual = null;          // p. ej. el grupo se quedó sin estudiantes
            }
        }
        if (actual != null) {
            for (ClasificacionEstudiante c : actual.estudiantes()) {
                tabla.modelo.addRow(new Object[]{
                        c.matricula(), c.nombre(), c.grupo(),
                        c.promedio() == null ? (Object) "Sin notas" : c.promedio(),
                        estado(c), c.promedio() == null ? "—" : (c.enRiesgo() ? "Sí" : "No")});
            }
        }
        mostrarEstadisticas(actual == null ? null : actual.resumen());
        boolean hayDatos = actual != null && !actual.estudiantes().isEmpty();
        tabla.mostrar(hayDatos);
        botonJson.setEnabled(hayDatos);
        botonCsv.setEnabled(hayDatos);
    }

    private Reporte generar(String id) {
        if (id.startsWith(GRUPO)) return generador.reportePorGrupo(id.substring(GRUPO.length()));
        if (id.startsWith(CURSO)) return generador.reportePorAsignatura(id.substring(CURSO.length()));
        return generador.reporteGeneral();
    }

    private void mostrarEstadisticas(ResumenEstadistico s) {
        if (s == null || s.totalEvaluados() == 0) {
            valores[0].setText(s == null ? "—" : "0 de " + s.totalEstudiantes());
            for (int i = 1; i < valores.length; i++) valores[i].setText("—");
            return;
        }
        valores[0].setText(s.totalEvaluados() + " de " + s.totalEstudiantes());
        valores[1].setText(String.valueOf(s.totalAprobados()));
        valores[2].setText(String.valueOf(s.totalReprobados()));
        valores[3].setText(String.valueOf(s.totalEnRiesgo()));
        valores[4].setText(dos(s.promedioGeneral()));
        valores[5].setText(dos(s.calificacionMaxima()));
        valores[6].setText(dos(s.calificacionMinima()));
        valores[7].setText(String.format(Locale.US, "%.1f %%", s.porcentajeAprobacion()));
    }

    private void exportar(String extension) {
        if (actual == null) return;
        String formato = extension.toUpperCase(Locale.ROOT);
        JFileChooser elegir = new JFileChooser();
        elegir.setDialogTitle("Exportar reporte a " + formato);
        elegir.setFileFilter(new FileNameExtensionFilter("Archivo " + formato + " (*." + extension + ")", extension));
        elegir.setSelectedFile(new File(ExportadorReportes.nombreSugerido(actual, extension)));
        if (elegir.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File archivo = elegir.getSelectedFile();
        if (!archivo.getName().toLowerCase(Locale.ROOT).endsWith("." + extension)) {
            archivo = new File(archivo.getAbsoluteFile().getParentFile(), archivo.getName() + "." + extension);
        }
        if (archivo.exists() && JOptionPane.showConfirmDialog(this,
                "El archivo " + archivo.getName() + " ya existe. ¿Quieres reemplazarlo?",
                "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            ExportadorReportes.exportar(actual, archivo.toPath());
            JOptionPane.showMessageDialog(this, "Reporte guardado en:\n" + archivo.getAbsolutePath(),
                    "Listo", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | ValidacionException e) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el reporte: " + e.getMessage(),
                    "No se pudo exportar", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- Apoyo ----------

    private static JPanel cuadroEstadistica(String nombre, JLabel valor) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(AZUL_NIEBLA);
        p.setBorder(new CompoundBorder(new LineBorder(AZUL_ESCARCHA, 1), new EmptyBorder(8, 12, 8, 12)));
        JLabel titulo = new JLabel(nombre);
        titulo.setFont(fuente(NOTA, false));
        titulo.setForeground(GRIS_PIZARRA);
        valor.setFont(fuente(SUBTITULO, true));
        valor.setForeground(AZUL_INVIERNO);
        p.add(titulo, BorderLayout.NORTH);
        p.add(valor, BorderLayout.CENTER);
        return p;
    }

    private static String estado(ClasificacionEstudiante c) {
        return switch (c.estado()) {
            case APROBADO -> "Aprobado";
            case REPROBADO -> "Reprobado";
            case SIN_EVALUAR -> "Sin evaluar";
        };
    }

    private static String dos(double valor) {
        return String.format(Locale.US, "%.2f", valor);
    }
}
