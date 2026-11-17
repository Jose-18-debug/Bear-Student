package bearstudent.logica;
import bearstudent.dto.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Módulo de la Persona 1: registro de estudiantes y cursos, e inscripción
 * de estudiantes en cursos. Las demás personas pueden consumir este módulo
 * mediante los métodos públicos (buscar, listar, obtener inscritos, etc.).
 */
public class RegistroAcademico {
    // LinkedHashMap conserva el orden de registro (útil para reportes reproducibles)
    private final Map<String, Estudiante> estudiantes = new LinkedHashMap<>();
    private final Map<String, Curso> cursos = new LinkedHashMap<>();

    // ---------- Registro ----------

    public void registrarEstudiante(String matricula, String nombre, String grupo) {
        String mat = textoObligatorio(matricula, "La matrícula no puede estar vacía");
        String nom = textoObligatorio(nombre, "El nombre no puede estar vacío");
        String gru = textoObligatorio(grupo, "El grupo no puede estar vacío");
        if (estudiantes.containsKey(mat)) {
            throw new ValidacionException("Ya existe un estudiante con la matrícula " + mat);
        }
        estudiantes.put(mat, new Estudiante(mat, nom, gru));
    }

    public void registrarCurso(String clave, String nombre) {
        String cla = textoObligatorio(clave, "La clave no puede estar vacía");
        String nom = textoObligatorio(nombre, "El nombre no puede estar vacío");
        if (cursos.containsKey(cla)) {
            throw new ValidacionException("Ya existe un curso con la clave " + cla);
        }
        cursos.put(cla, new Curso(cla, nom));
    }

    // ---------- Inscripción ----------

    public void inscribir(String matricula, String clave) {
        Estudiante estudiante = buscarEstudiante(matricula);
        Curso curso = buscarCurso(clave);
        if (estudiante.estaInscritoEn(curso.getClave())) {
            throw new ValidacionException("El estudiante " + estudiante.getMatricula()
                    + " ya está inscrito en el curso " + curso.getClave());
        }
        estudiante.agregarCurso(curso.getClave());
        curso.agregarEstudiante(estudiante.getMatricula());
    }

    /**
     * Inscribe a varios estudiantes en un curso. Devuelve los mensajes de error
     * de los que no se pudieron inscribir (lista vacía si todo salió bien).
     */
    public List<String> inscribirVarios(List<String> matriculas, String clave) {
        List<String> errores = new ArrayList<>();
        for (String matricula : matriculas) {
            try {
                inscribir(matricula, clave);
            } catch (ValidacionException e) {
                errores.add(matricula + ": " + e.getMessage());
            }
        }
        return errores;
    }

    // ---------- Consultas (las usarán los demás módulos) ----------

    public Estudiante buscarEstudiante(String matricula) {
        Estudiante e = estudiantes.get(limpiar(matricula));
        if (e == null) {
            throw new ValidacionException("El estudiante " + matricula + " no existe");
        }
        return e;
    }

    public Curso buscarCurso(String clave) {
        Curso c = cursos.get(limpiar(clave));
        if (c == null) {
            throw new ValidacionException("El curso " + clave + " no existe");
        }
        return c;
    }

    /** true si el estudiante existe, el curso existe y el estudiante está inscrito. */
    public boolean estaInscrito(String matricula, String clave) {
        Estudiante e = estudiantes.get(limpiar(matricula));
        return e != null && e.estaInscritoEn(limpiar(clave));
    }

    /** Claves de los cursos de un estudiante (lista vacía si el estudiante no existe). */
    public List<String> cursosDeEstudiante(String matricula) {
        Estudiante e = estudiantes.get(limpiar(matricula));
        return e == null ? new ArrayList<>() : new ArrayList<>(e.getCodigosCursos());
    }

    /** Estudiantes inscritos en un curso (lanza ValidacionException si el curso no existe). */
    public List<Estudiante> estudiantesDeCurso(String clave) {
        Curso curso = buscarCurso(clave);
        List<Estudiante> lista = new ArrayList<>();
        for (String matricula : curso.getMatriculasInscritas()) {
            lista.add(estudiantes.get(matricula));
        }
        return lista;
    }

    public Collection<Estudiante> listarEstudiantes() {
        return new ArrayList<>(estudiantes.values());
    }

    public Collection<Curso> listarCursos() {
        return new ArrayList<>(cursos.values());
    }

    /** Estudiantes de un grupo (usa lambda/stream). */
    public List<Estudiante> estudiantesPorGrupo(String grupo) {
        return estudiantes.values().stream()
                .filter(e -> e.getGrupo().equalsIgnoreCase(grupo))
                .sorted(Comparator.comparing(Estudiante::getNombre))
                .collect(Collectors.toList());
    }

    // ---------- Utilidades ----------

    private static String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private static String textoObligatorio(String valor, String mensaje) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ValidacionException(mensaje);
        }
        return valor.trim();
    }
}

