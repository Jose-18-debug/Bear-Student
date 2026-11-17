package bearstudent.logica;

import bearstudent.dto.Curso;
import bearstudent.dto.Estudiante;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Módulo de la Persona 1: registro de estudiantes y cursos, e inscripción
 * de estudiantes en cursos. Las demás personas pueden consumir este módulo
 * mediante los métodos públicos (buscar, listar, obtener inscritos, etc.).
 */
public class RegistroAcademico {
    // LinkedHashMap y LinkedHashSet conservan el orden de registro (útil para reportes reproducibles)
    private final Map<String, Estudiante> estudiantes = new LinkedHashMap<>();
    private final Map<String, Curso> cursos = new LinkedHashMap<>();
    // Las inscripciones se guardan en los dos sentidos para no tener que buscar:
    // matrícula -> claves de sus cursos, y clave -> matrículas inscritas. Un Set no admite repetidos.
    private final Map<String, Set<String>> cursosPorEstudiante = new LinkedHashMap<>();
    private final Map<String, Set<String>> inscritosPorCurso = new LinkedHashMap<>();

    // ---------- Registro ----------

    public void registrarEstudiante(String matricula, String nombre, String grupo) {
        String mat = textoObligatorio(matricula, "La matrícula no puede estar vacía");
        String nom = textoObligatorio(nombre, "El nombre no puede estar vacío");
        String gru = textoObligatorio(grupo, "El grupo no puede estar vacío");
        if (estudiantes.containsKey(mat)) {
            throw new ValidacionException("Ya existe un estudiante con la matrícula " + mat);
        }
        estudiantes.put(mat, new Estudiante(mat, nom, gru));
        cursosPorEstudiante.put(mat, new LinkedHashSet<>());
    }

    public void registrarCurso(String clave, String nombre) {
        String cla = textoObligatorio(clave, "La clave no puede estar vacía");
        String nom = textoObligatorio(nombre, "El nombre no puede estar vacío");
        if (cursos.containsKey(cla)) {
            throw new ValidacionException("Ya existe un curso con la clave " + cla);
        }
        cursos.put(cla, new Curso(cla, nom));
        inscritosPorCurso.put(cla, new LinkedHashSet<>());
    }

    // ---------- Inscripción ----------

    public void inscribir(String matricula, String clave) {
        Estudiante estudiante = buscarEstudiante(matricula);
        Curso curso = buscarCurso(clave);
        if (estaInscrito(estudiante.getMatricula(), curso.getClave())) {
            throw new ValidacionException("El estudiante " + estudiante.getMatricula()
                    + " ya está inscrito en el curso " + curso.getClave());
        }
        cursosPorEstudiante.get(estudiante.getMatricula()).add(curso.getClave());
        inscritosPorCurso.get(curso.getClave()).add(estudiante.getMatricula());
    }

    // ---------- Consultas (las usan los demás módulos) ----------

    public Estudiante buscarEstudiante(String matricula) {
        Estudiante e = estudiantes.get(limpiar(matricula));
        if (e == null) {
            throw new ValidacionException("El estudiante " + limpiar(matricula) + " no existe");
        }
        return e;
    }

    public Curso buscarCurso(String clave) {
        Curso c = cursos.get(limpiar(clave));
        if (c == null) {
            throw new ValidacionException("El curso " + limpiar(clave) + " no existe");
        }
        return c;
    }

    /** true si el estudiante existe, el curso existe y el estudiante está inscrito. */
    public boolean estaInscrito(String matricula, String clave) {
        return cursosPorEstudiante.getOrDefault(limpiar(matricula), Set.of()).contains(limpiar(clave));
    }

    /** Claves de los cursos de un estudiante, en orden de inscripción (lista vacía si el estudiante no existe). */
    public List<String> cursosDeEstudiante(String matricula) {
        return new ArrayList<>(cursosPorEstudiante.getOrDefault(limpiar(matricula), Set.of()));
    }

    /** Estudiantes inscritos en un curso (lanza ValidacionException si el curso no existe). */
    public List<Estudiante> estudiantesDeCurso(String clave) {
        Curso curso = buscarCurso(clave);
        List<Estudiante> lista = new ArrayList<>();
        for (String matricula : inscritosPorCurso.get(curso.getClave())) {
            lista.add(estudiantes.get(matricula));
        }
        return lista;
    }

    /** Cuántos estudiantes tiene un curso (0 si el curso no existe). */
    public int totalInscritos(String clave) {
        return inscritosPorCurso.getOrDefault(limpiar(clave), Set.of()).size();
    }

    public Collection<Estudiante> listarEstudiantes() {
        return new ArrayList<>(estudiantes.values());
    }

    public Collection<Curso> listarCursos() {
        return new ArrayList<>(cursos.values());
    }

    /** Estudiantes de un grupo, ordenados por nombre; el grupo no distingue mayúsculas ni espacios sobrantes. */
    public List<Estudiante> estudiantesPorGrupo(String grupo) {
        String gru = limpiar(grupo);
        return estudiantes.values().stream()
                .filter(e -> e.getGrupo().equalsIgnoreCase(gru))
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
