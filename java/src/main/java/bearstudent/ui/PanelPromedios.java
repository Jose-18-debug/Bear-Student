package bearstudent.ui;

import bearstudent.dto.Calificacion;
import bearstudent.dto.Curso;
import bearstudent.dto.PromedioEstudiante;
import bearstudent.logica.PromediosAcademicos;
import bearstudent.logica.PromediosAcademicos.PromedioCurso;
import bearstudent.logica.PromediosAcademicos.ReporteEstudiante;
import bearstudent.logica.RegistroAcademico;
import bearstudent.logica.RegistroCalificaciones;
import bearstudent.ui.componentes.Opcion;
import bearstudent.ui.componentes.PanelTabla;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static bearstudent.ui.componentes.Componentes.*;
import static bearstudent.ui.componentes.Estilo.*;

/**
 * Pestaña de la parte 3: calificaciones y promedios de un estudiante, y estudiantes ordenados por promedio.
 * No contiene lógica de negocio: solo llama a PromediosAcademicos.
 */
public class PanelPromedios extends JPanel implements Refrescable {
    private static final String SIN_NOTAS = "Sin notas";

    private final RegistroAcademico academico;
    private final PromediosAcademicos promedios;

    private final JComboBox<Opcion> consultaEstudiante = new JComboBox<>();
    private final JLabel promedioFinal = new JLabel(" ");
    private final JComboBox<Opcion> rankingBase = new JComboBox<>();
    private final JComboBox<String> rankingOrden = new JComboBox<>(new String[]{"Mayor a menor", "Menor a mayor"});
    private final JLabel promedioCurso = new JLabel(" ");

    private final PanelTabla tablaCalificaciones = new PanelTabla(
            new String[]{"Curso", "Evaluación", "Calificación"},
            new Class<?>[]{String.class, String.class, Double.class},
            new int[]{260, 160, 110},
            "Este estudiante todavía no tiene calificaciones.");
    private final PanelTabla tablaPromedios = new PanelTabla(
            new String[]{"Curso", "Promedio"},
            new Class<?>[]{String.class, Double.class},
            new int[]{260, 110},
            "Este estudiante no está inscrito en ningún curso.");
    private final PanelTabla tablaRanking = new PanelTabla(
            new String[]{"Lugar", "Matrícula", "Nombre", "Grupo", "Promedio"},
            new Class<?>[]{Integer.class, String.class, String.class, String.class, Double.class},
            new int[]{70, 140, 280, 100, 110},
            "Todavía no hay estudiantes registrados.");
    private boolean cargando;   // evita que los combos reaccionen mientras se rellenan

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
        promedioFinal.setFont(fuente(SUBTITULO, true));
        promedioFinal.setForeground(AZUL_INVIERNO);
        promedioCurso.setFont(fuente(TEXTO, true));
        promedioCurso.setForeground(AZUL_INVIERNO);

        JPanel arriba = new JPanel();
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        arriba.setOpaque(false);
        arriba.add(tarjeta("Calificaciones y promedios de un estudiante", crearConsulta()));
        arriba.add(Box.createVerticalStrut(12));
        arriba.add(tarjeta("Estudiantes ordenados por promedio", crearRanking()));
        add(pagina(arriba, null), BorderLayout.CENTER);

        consultaEstudiante.addActionListener(e -> { if (!cargando) refrescarConsulta(); });
        rankingBase.addActionListener(e -> { if (!cargando) refrescarRanking(); });
        rankingOrden.addActionListener(e -> { if (!cargando) refrescarRanking(); });
        refrescar();
    }

    private JPanel crearConsulta() {
        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        selector.setOpaque(false);
        selector.add(etiqueta("Estudiante"));
        selector.add(consultaEstudiante);
        JPanel tablas = new JPanel(new GridLayout(1, 2, 12, 0));
        tablas.setOpaque(false);
        tablas.add(tablaCalificaciones);
        tablas.add(tablaPromedios);
        tablas.setPreferredSize(new Dimension(100, 230));
        JPanel cuerpo = new JPanel(new BorderLayout(0, 8));
        cuerpo.setOpaque(false);
        cuerpo.add(selector, BorderLayout.NORTH);
        cuerpo.add(tablas, BorderLayout.CENTER);
        cuerpo.add(promedioFinal, BorderLayout.SOUTH);
        return cuerpo;
    }

    private JPanel crearRanking() {
        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        selector.setOpaque(false);
        selector.add(etiqueta("Ordenar por"));
        selector.add(rankingBase);
        selector.add(etiqueta("Orden"));
        selector.add(rankingOrden);
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        cabecera.add(selector, BorderLayout.WEST);
        cabecera.add(promedioCurso, BorderLayout.EAST);
        tablaRanking.setPreferredSize(new Dimension(100, 280));
        JPanel cuerpo = new JPanel(new BorderLayout(0, 8));
        cuerpo.setOpaque(false);
        cuerpo.add(cabecera, BorderLayout.NORTH);
        cuerpo.add(tablaRanking, BorderLayout.CENTER);
        return cuerpo;
    }

    /** Vuelve a leer estudiantes y cursos. Se llama al abrir la pestaña. */
    @Override
    public void refrescar() {
        List<Opcion> bases = new ArrayList<>();
        bases.add(new Opcion("", "Promedio final"));
        for (Curso c : academico.listarCursos()) {
            bases.add(new Opcion(c.getClave(), "Curso: " + c.getClave() + " | " + c.getNombre()));
        }
        cargando = true;
        try {
            llenarCombo(consultaEstudiante, opcionesEstudiantes(academico.listarEstudiantes()));
            llenarCombo(rankingBase, bases);
        } finally {
            cargando = false;
        }
        refrescarConsulta();
        refrescarRanking();
    }

    private void refrescarConsulta() {
        List<Object[]> calificaciones = new ArrayList<>();
        List<Object[]> porCurso = new ArrayList<>();
        promedioFinal.setText(" ");
        String matricula = idElegido(consultaEstudiante);
        if (!matricula.isEmpty()) {
            ReporteEstudiante reporte = promedios.reporteDeEstudiante(matricula);
            for (Calificacion c : reporte.calificaciones()) {
                calificaciones.add(new Object[]{c.clave() + " | " + academico.buscarCurso(c.clave()).getNombre(),
                        c.evaluacion(), c.calificacion()});
            }
            for (PromedioCurso pc : reporte.promediosPorCurso()) {
                porCurso.add(new Object[]{pc.clave() + " | " + pc.nombreCurso(),
                        pc.promedio() == null ? SIN_NOTAS : pc.promedio()});
            }
            promedioFinal.setText("Promedio final: " + formato(reporte.promedioFinal()));
        }
        tablaCalificaciones.llenar(calificaciones);
        tablaPromedios.llenar(porCurso);
    }

    /** Los empates comparten lugar (1, 1, 3...) y los estudiantes sin notas van al final sin lugar. */
    private void refrescarRanking() {
        String clave = idElegido(rankingBase);
        boolean descendente = rankingOrden.getSelectedIndex() == 0;
        List<PromedioEstudiante> lista;
        if (clave.isEmpty()) {
            lista = promedios.ordenarPorPromedio(descendente);
            promedioCurso.setText(" ");
        } else {
            lista = promedios.ordenarPorPromedioEnCurso(clave, descendente);
            promedioCurso.setText("Promedio del curso: " + formato(promedios.promedioDeCurso(clave)));
        }
        List<Object[]> filas = new ArrayList<>();
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
            filas.add(new Object[]{colLugar, p.matricula(), p.nombre(), p.grupo(), colPromedio});
        }
        tablaRanking.llenar(filas);
    }

    private static String formato(Double valor) {
        return valor == null ? "sin calificaciones" : String.format(Locale.US, "%.2f", valor);
    }
}
