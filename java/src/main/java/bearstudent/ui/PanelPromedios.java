package bearstudent.ui;
import bearstudent.dto.*;
import bearstudent.logica.*;

import bearstudent.logica.PromediosAcademicos.PromedioCurso;
import bearstudent.logica.PromediosAcademicos.PromedioEstudiante;
import bearstudent.logica.PromediosAcademicos.ReporteEstudiante;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static bearstudent.ui.VentanaPrincipal.*;

public class PanelPromedios extends JPanel {
    private static final String SIN_NOTAS = "Sin notas";

    private final RegistroAcademico academico;
    private final PromediosAcademicos promedios;

    private final JComboBox<Opcion> consultaEstudiante = new JComboBox<>();
    private final JLabel promedioFinal = new JLabel(" ");
    private final JComboBox<Opcion> rankingBase = new JComboBox<>();
    private final JComboBox<String> rankingOrden = new JComboBox<>(new String[]{"Mayor a menor", "Menor a mayor"});
    private final JLabel promedioCurso = new JLabel(" ");

    private final PanelTabla tablaCalificaciones;
    private final PanelTabla tablaPromedios;
    private final PanelTabla tablaRanking;
    private boolean cargando;

    public PanelPromedios(RegistroAcademico academico, RegistroCalificaciones notas) {
        super(new BorderLayout());
        this.academico = academico;
        this.promedios = new PromediosAcademicos(academico, notas);
        setOpaque(false);
        for (JComboBox<?> combo : List.of(consultaEstudiante, rankingBase, rankingOrden)) {
            estilizarCombo(combo);
        }
        consultaEstudiante.setPreferredSize(new Dimension(340, 32));
        rankingBase.setPreferredSize(new Dimension(340, 32));

        tablaCalificaciones = new PanelTabla(
                new String[]{"Curso", "Evaluación", "Calificación"},
                new Class<?>[]{String.class, String.class, Double.class},
                new int[]{260, 160, 110},
                "Este estudiante todavía no tiene calificaciones.");
        tablaPromedios = new PanelTabla(
                new String[]{"Curso", "Promedio"},
                new Class<?>[]{String.class, Double.class},
                new int[]{260, 110},
                "Este estudiante no está inscrito en ningún curso.");
        tablaRanking = new PanelTabla(
                new String[]{"Lugar", "Matrícula", "Nombre", "Grupo", "Promedio"},
                new Class<?>[]{Integer.class, String.class, String.class, String.class, Double.class},
                new int[]{70, 140, 280, 100, 110},
                "Todavía no hay estudiantes registrados.");

        promedioFinal.setFont(fuente(SUBTITULO, true));
        promedioFinal.setForeground(AZUL_INVIERNO);
        promedioCurso.setFont(fuente(TEXTO, true));
        promedioCurso.setForeground(AZUL_INVIERNO);

        JPanel selectorConsulta = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        selectorConsulta.setOpaque(false);
        selectorConsulta.add(etiqueta("Estudiante"));
        selectorConsulta.add(consultaEstudiante);
        JPanel tablas = new JPanel(new GridLayout(1, 2, 12, 0));
        tablas.setOpaque(false);
        tablas.add(tablaCalificaciones);
        tablas.add(tablaPromedios);
        tablas.setPreferredSize(new Dimension(100, 230));
        JPanel cuerpoConsulta = new JPanel(new BorderLayout(0, 8));
        cuerpoConsulta.setOpaque(false);
        cuerpoConsulta.add(selectorConsulta, BorderLayout.NORTH);
        cuerpoConsulta.add(tablas, BorderLayout.CENTER);
        cuerpoConsulta.add(promedioFinal, BorderLayout.SOUTH);

        JPanel selectorRanking = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        selectorRanking.setOpaque(false);
        selectorRanking.add(etiqueta("Ordenar por"));
        selectorRanking.add(rankingBase);
        selectorRanking.add(etiqueta("Orden"));
        selectorRanking.add(rankingOrden);
        JPanel cabeceraRanking = new JPanel(new BorderLayout());
        cabeceraRanking.setOpaque(false);
        cabeceraRanking.add(selectorRanking, BorderLayout.WEST);
        cabeceraRanking.add(promedioCurso, BorderLayout.EAST);
        tablaRanking.setPreferredSize(new Dimension(100, 280));
        JPanel cuerpoRanking = new JPanel(new BorderLayout(0, 8));
        cuerpoRanking.setOpaque(false);
        cuerpoRanking.add(cabeceraRanking, BorderLayout.NORTH);
        cuerpoRanking.add(tablaRanking, BorderLayout.CENTER);

        JPanel arriba = new JPanel();
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        arriba.setOpaque(false);
        arriba.add(tarjeta("Calificaciones y promedios de un estudiante", cuerpoConsulta));
        arriba.add(Box.createVerticalStrut(12));
        arriba.add(tarjeta("Estudiantes ordenados por promedio", cuerpoRanking));

        PaginaScrollable pagina = new PaginaScrollable();
        pagina.setBorder(new EmptyBorder(12, 12, 12, 12));
        pagina.add(arriba, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(pagina,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(NIEVE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        consultaEstudiante.addActionListener(e -> { if (!cargando) refrescarConsulta(); });
        rankingBase.addActionListener(e -> { if (!cargando) refrescarRanking(); });
        rankingOrden.addActionListener(e -> { if (!cargando) refrescarRanking(); });
        refrescar();
    }

    public void refrescar() {
        List<Opcion> estudiantes = new ArrayList<>();
        for (Estudiante e : academico.listarEstudiantes()) {
            estudiantes.add(new Opcion(e.getMatricula(),
                    e.getMatricula() + " | " + e.getNombre() + " (" + e.getGrupo() + ")"));
        }
        List<Opcion> bases = new ArrayList<>();
        bases.add(new Opcion("", "Promedio final"));
        for (Curso c : academico.listarCursos()) {
            bases.add(new Opcion(c.getClave(), "Curso: " + c.getClave() + " | " + c.getNombre()));
        }
        cargando = true;
        try {
            llenarCombo(consultaEstudiante, estudiantes);
            llenarCombo(rankingBase, bases);
        } finally {
            cargando = false;
        }
        refrescarConsulta();
        refrescarRanking();
    }

    private void refrescarConsulta() {
        tablaCalificaciones.modelo.setRowCount(0);
        tablaPromedios.modelo.setRowCount(0);
        promedioFinal.setText(" ");
        Opcion estudiante = (Opcion) consultaEstudiante.getSelectedItem();
        if (estudiante != null) {
            ReporteEstudiante reporte = promedios.reporteDeEstudiante(estudiante.id);
            for (Calificacion c : reporte.calificaciones()) {
                tablaCalificaciones.modelo.addRow(new Object[]{
                        c.clave() + " | " + academico.buscarCurso(c.clave()).getNombre(),
                        c.evaluacion(), c.calificacion()});
            }
            for (PromedioCurso pc : reporte.promediosPorCurso()) {
                tablaPromedios.modelo.addRow(new Object[]{
                        pc.clave() + " | " + pc.nombreCurso(),
                        pc.promedio() == null ? (Object) SIN_NOTAS : pc.promedio()});
            }
            promedioFinal.setText("Promedio final: " + formato(reporte.promedioFinal()));
        }
        tablaCalificaciones.mostrar(tablaCalificaciones.modelo.getRowCount() > 0);
        tablaPromedios.mostrar(tablaPromedios.modelo.getRowCount() > 0);
    }

    private void refrescarRanking() {
        tablaRanking.modelo.setRowCount(0);
        Opcion base = (Opcion) rankingBase.getSelectedItem();
        boolean descendente = rankingOrden.getSelectedIndex() == 0;
        List<PromedioEstudiante> lista;
        if (base == null || base.id.isEmpty()) {
            lista = promedios.ordenarPorPromedio(descendente);
            promedioCurso.setText(" ");
        } else {
            lista = promedios.ordenarPorPromedioEnCurso(base.id, descendente);
            promedioCurso.setText("Promedio del curso: " + formato(promedios.promedioDeCurso(base.id)));
        }
        int lugar = 0, contados = 0;
        Double previo = null;
        for (PromedioEstudiante p : lista) {
            Object colLugar = "—";
            Object colPromedio = SIN_NOTAS;
            if (p.promedio() != null) {
                contados++;
                if (previo == null || Double.compare(p.promedio(), previo) != 0) lugar = contados;
                previo = p.promedio();
                colLugar = lugar;
                colPromedio = p.promedio();
            }
            tablaRanking.modelo.addRow(new Object[]{colLugar, p.matricula(), p.nombre(), p.grupo(), colPromedio});
        }
        tablaRanking.mostrar(!lista.isEmpty());
    }

    private static String formato(Double valor) {
        return valor == null ? "sin calificaciones" : String.format(Locale.US, "%.2f", valor);
    }
}


