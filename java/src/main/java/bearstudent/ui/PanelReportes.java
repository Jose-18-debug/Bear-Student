package bearstudent.ui;

import bearstudent.dto.Curso;
import bearstudent.logica.PromediosAcademicos;
import bearstudent.logica.RegistroAcademico;
import bearstudent.logica.RegistroCalificaciones;
import bearstudent.logica.ValidacionException;
import bearstudent.logica.estadisticas.EstadisticasAcademicasUseCase.ClasificacionEstudiante;
import bearstudent.logica.estadisticas.EstadisticasAcademicasUseCase.ResumenEstadistico;
import bearstudent.logica.reportes.ExportadorReportes;
import bearstudent.logica.reportes.GeneradorReportes;
import bearstudent.logica.reportes.GeneradorReportes.Reporte;
import bearstudent.ui.componentes.BotonRedondo;
import bearstudent.ui.componentes.Opcion;
import bearstudent.ui.componentes.PanelTabla;

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

import static bearstudent.ui.componentes.Componentes.*;
import static bearstudent.ui.componentes.Estilo.*;

/**
 * Pestaña de la Persona 5: reporte general, por grupo o por asignatura, con las estadísticas
 * de la Persona 4 (aprobados, reprobados, riesgo, promedio general, más alto, más bajo y % de aprobación)
 * y botones para exportarlo a JSON o CSV.
 */
public class PanelReportes extends JPanel implements Refrescable {
    private static final String GENERAL = "general:";
    private static final String GRUPO = "grupo:";
    private static final String CURSO = "curso:";

    private static final String[] NOMBRES_ESTADISTICAS = {
            "Evaluados", "Aprobados", "Reprobados", "En riesgo",
            "Promedio general", "Promedio más alto", "Promedio más bajo", "Aprobación"};

    private final RegistroAcademico academico;
    private final GeneradorReportes generador;

    private final JComboBox<Opcion> selector = new JComboBox<>();
    private final JLabel[] valores = new JLabel[NOMBRES_ESTADISTICAS.length];
    private final PanelTabla tabla = new PanelTabla(
            new String[]{"Matrícula", "Nombre", "Grupo", "Promedio", "Estado", "En riesgo"},
            new Class<?>[]{String.class, String.class, String.class, Double.class, String.class, String.class},
            new int[]{120, 260, 90, 100, 120, 90},
            "No hay estudiantes para este reporte. Registra estudiantes y captura calificaciones.");
    private final BotonRedondo botonJson = new BotonRedondo("Exportar JSON", true, () -> exportar("json"));
    private final BotonRedondo botonCsv = new BotonRedondo("Exportar CSV", false, () -> exportar("csv"));
    private Reporte actual;
    private boolean cargando;   // evita que la lista reaccione mientras se rellena

    public PanelReportes(RegistroAcademico academico, RegistroCalificaciones notas) {
        super(new BorderLayout());
        this.academico = academico;
        this.generador = new GeneradorReportes(academico, new PromediosAcademicos(academico, notas));
        setOpaque(false);
        estilizarCombo(selector);
        selector.setPreferredSize(new Dimension(360, 32));
        tabla.setPreferredSize(new Dimension(100, 300));

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

        // Estadísticas: un cuadro por cada dato
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
        add(pagina(tarjeta("Reportes y estadísticas", cuerpo), null), BorderLayout.CENTER);

        selector.addActionListener(e -> { if (!cargando) refrescarReporte(); });
        refrescar();
    }

    /** Vuelve a llenar la lista de reportes (grupos y cursos pueden haber cambiado) y recalcula el actual. */
    @Override
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
        actual = null;
        String id = idElegido(selector);
        if (!id.isEmpty()) {
            try {
                actual = generar(id);
            } catch (ValidacionException e) {
                actual = null;          // p. ej. el grupo se quedó sin estudiantes
            }
        }
        List<Object[]> filas = new ArrayList<>();
        if (actual != null) {
            for (ClasificacionEstudiante c : actual.estudiantes()) {
                filas.add(new Object[]{
                        c.matricula(), c.nombre(), c.grupo(),
                        c.promedio() == null ? "Sin notas" : c.promedio(),
                        estado(c), c.promedio() == null ? "—" : (c.enRiesgo() ? "Sí" : "No")});
            }
        }
        tabla.llenar(filas);
        mostrarEstadisticas(actual == null ? null : actual.resumen());
        botonJson.setEnabled(!filas.isEmpty());
        botonCsv.setEnabled(!filas.isEmpty());
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
        valores[5].setText(dos(s.promedioMaximo()));
        valores[6].setText(dos(s.promedioMinimo()));
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
            aviso(this, "Listo", "Reporte guardado en:\n" + archivo.getAbsolutePath(), JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | ValidacionException e) {
            aviso(this, "No se pudo exportar", "No se pudo guardar el reporte: " + e.getMessage(),
                    JOptionPane.ERROR_MESSAGE);
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
