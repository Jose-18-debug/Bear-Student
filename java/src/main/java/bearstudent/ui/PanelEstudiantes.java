package bearstudent.ui;

import bearstudent.dto.Curso;
import bearstudent.dto.Estudiante;
import bearstudent.logica.RegistroAcademico;
import bearstudent.logica.ValidacionException;
import bearstudent.ui.componentes.BotonRedondo;
import bearstudent.ui.componentes.Campo;
import bearstudent.ui.componentes.Opcion;
import bearstudent.ui.componentes.PanelTabla;
import bearstudent.ui.componentes.PestanasUI;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static bearstudent.ui.componentes.Componentes.*;
import static bearstudent.ui.componentes.Estilo.*;

/**
 * Pestaña de la parte 1: registrar estudiantes y cursos, inscribir y consultar las tres tablas.
 * No contiene lógica de negocio: solo llama a RegistroAcademico y muestra el resultado.
 */
public class PanelEstudiantes extends JPanel implements Refrescable {
    private final RegistroAcademico registro;
    private final Runnable alCambiar;   // avisa a la ventana para que actualice la barra de estado

    private final Campo matricula = new Campo();
    private final Campo nombre = new Campo();
    private final Campo grupo = new Campo();
    private final Campo clave = new Campo();
    private final Campo nombreCurso = new Campo();
    private final JComboBox<Opcion> comboEstudiante = new JComboBox<>();
    private final JComboBox<Opcion> comboCurso = new JComboBox<>();
    private final BotonRedondo botonInscribir = new BotonRedondo("Inscribir", true, this::inscribir);
    private final PanelTabla tablaEstudiantes = new PanelTabla(
            new String[]{"Matrícula", "Nombre", "Grupo", "Cursos"},
            new Class<?>[]{String.class, String.class, String.class, Integer.class},
            new int[]{140, 300, 100, 90},
            "Aún no hay estudiantes. Regístralos con el formulario de arriba.");
    private final PanelTabla tablaCursos = new PanelTabla(
            new String[]{"Clave", "Nombre del curso", "Inscritos"},
            new Class<?>[]{String.class, String.class, Integer.class},
            new int[]{140, 300, 90},
            "Aún no hay cursos. Registra el primero con el formulario de arriba.");
    private final PanelTabla tablaInscripciones = new PanelTabla(
            new String[]{"Estudiante", "Curso"},
            new Class<?>[]{String.class, String.class},
            new int[]{300, 300},
            "Todavía no hay inscripciones. Registra un estudiante y un curso para inscribirlo.");

    public PanelEstudiantes(RegistroAcademico registro, Runnable alCambiar) {
        super(new BorderLayout());
        this.registro = registro;
        this.alCambiar = alCambiar;
        setOpaque(false);

        // Formularios de estudiante y curso, lado a lado
        JPanel formularios = new JPanel(new GridLayout(1, 2, 12, 0));
        formularios.setOpaque(false);
        formularios.add(tarjeta("Registrar estudiante", formularioConLimpiar(
                new String[]{"Matrícula", "Nombre", "Grupo"},
                new Campo[]{matricula, nombre, grupo},
                "Guardar estudiante", this::registrarEstudiante)));
        formularios.add(tarjeta("Registrar curso", formularioConLimpiar(
                new String[]{"Clave", "Nombre del curso"},
                new Campo[]{clave, nombreCurso},
                "Guardar curso", this::registrarCurso)));

        JPanel arriba = new JPanel();
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        arriba.setOpaque(false);
        arriba.add(formularios);
        arriba.add(Box.createVerticalStrut(12));
        arriba.add(tarjeta("Inscribir estudiante en un curso", crearPanelInscribir()));
        arriba.add(Box.createVerticalStrut(12));

        // Tablas en pestañas
        JTabbedPane tablas = new JTabbedPane();
        tablas.setUI(new PestanasUI());
        tablas.setFont(fuente(BOTON, true));
        tablas.setBackground(NIEVE);
        tablas.setPreferredSize(new Dimension(100, 330));
        tablas.addTab("Estudiantes", tablaEstudiantes);
        tablas.addTab("Cursos", tablaCursos);
        tablas.addTab("Inscripciones", tablaInscripciones);

        add(pagina(arriba, tablas), BorderLayout.CENTER);
        refrescar();
    }

    private static JPanel formularioConLimpiar(String[] etiquetas, Campo[] campos, String textoGuardar, Runnable guardar) {
        return formulario(etiquetas, campos,
                new BotonRedondo(textoGuardar, true, guardar),
                new BotonRedondo("Limpiar", false, () -> limpiar(campos)));
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

        gc.gridx = 2; gc.weightx = 0; gc.fill = GridBagConstraints.NONE; gc.insets = new Insets(0, 16, 0, 0);
        cuerpo.add(botonInscribir, gc);
        return cuerpo;
    }

    // ---------- Acciones (solo llaman a RegistroAcademico) ----------

    private void registrarEstudiante() {
        try {
            registro.registrarEstudiante(matricula.getText(), nombre.getText(), grupo.getText());
            limpiar(matricula, nombre, grupo);
            cambio();
            aviso(this, "Listo", "Estudiante registrado.", JOptionPane.INFORMATION_MESSAGE);
        } catch (ValidacionException e) {
            marcarError(matricula, nombre, grupo);
            aviso(this, "No se pudo registrar", e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarCurso() {
        try {
            registro.registrarCurso(clave.getText(), nombreCurso.getText());
            limpiar(clave, nombreCurso);
            cambio();
            aviso(this, "Listo", "Curso registrado.", JOptionPane.INFORMATION_MESSAGE);
        } catch (ValidacionException e) {
            marcarError(clave, nombreCurso);
            aviso(this, "No se pudo registrar", e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void inscribir() {
        try {
            registro.inscribir(idElegido(comboEstudiante), idElegido(comboCurso));
            cambio();
            aviso(this, "Listo", "Inscripción realizada.", JOptionPane.INFORMATION_MESSAGE);
        } catch (ValidacionException e) {
            aviso(this, "No se pudo inscribir", e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cambio() {
        refrescar();
        alCambiar.run();
    }

    // ---------- Actualización de la pantalla ----------

    /** Vuelve a llenar las listas desplegables, las tres tablas y el botón Inscribir. */
    @Override
    public void refrescar() {
        Collection<Estudiante> estudiantes = registro.listarEstudiantes();
        Collection<Curso> cursos = registro.listarCursos();
        llenarCombo(comboEstudiante, opcionesEstudiantes(estudiantes));
        llenarCombo(comboCurso, opcionesCursos(cursos));

        List<Object[]> filasEstudiantes = new ArrayList<>();
        List<Object[]> filasInscripciones = new ArrayList<>();
        for (Estudiante e : estudiantes) {
            List<String> suyos = registro.cursosDeEstudiante(e.getMatricula());
            filasEstudiantes.add(new Object[]{e.getMatricula(), e.getNombre(), e.getGrupo(), suyos.size()});
            for (String claveCurso : suyos) {
                filasInscripciones.add(new Object[]{e.getNombre(), registro.buscarCurso(claveCurso).getNombre()});
            }
        }
        List<Object[]> filasCursos = new ArrayList<>();
        for (Curso c : cursos) {
            filasCursos.add(new Object[]{c.getClave(), c.getNombre(), registro.totalInscritos(c.getClave())});
        }
        tablaEstudiantes.llenar(filasEstudiantes);
        tablaCursos.llenar(filasCursos);
        tablaInscripciones.llenar(filasInscripciones);

        botonInscribir.setEnabled(!estudiantes.isEmpty() && !cursos.isEmpty());
    }
}
