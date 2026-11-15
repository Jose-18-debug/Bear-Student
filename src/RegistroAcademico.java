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

    public void registrarEstudiante(String matricula, String nombre, String grupo)
            throws RegistroException {
        validarTexto(matricula, "La matrícula");
        validarTexto(nombre, "El nombre");
        validarTexto(grupo, "El grupo");

        String clave = matricula.trim();
        if (estudiantes.containsKey(clave)) {
            throw new RegistroException("Ya existe un estudiante con la matrícula " + clave);
        }
        estudiantes.put(clave, new Estudiante(clave, nombre.trim(), grupo.trim()));
    }

    public void registrarCurso(String codigo, String nombre, int cupoMaximo)
            throws RegistroException {
        validarTexto(codigo, "El código");
        validarTexto(nombre, "El nombre del curso");
        if (cupoMaximo <= 0) {
            throw new RegistroException("El cupo máximo debe ser mayor que 0");
        }

        String clave = codigo.trim();
        if (cursos.containsKey(clave)) {
            throw new RegistroException("Ya existe un curso con el código " + clave);
        }
        cursos.put(clave, new Curso(clave, nombre.trim(), cupoMaximo));
    }

    // ---------- Inscripción ----------

    public void inscribir(String matricula, String codigoCurso) throws RegistroException {
        Estudiante estudiante = buscarEstudiante(matricula);
        Curso curso = buscarCurso(codigoCurso);

        if (estudiante.estaInscritoEn(curso.getCodigo())) {
            throw new RegistroException(estudiante.getNombre()
                    + " ya está inscrito en " + curso.getNombre());
        }
        if (!curso.tieneCupo()) {
            throw new RegistroException("El curso " + curso.getNombre() + " no tiene cupo");
        }

        estudiante.agregarCurso(curso.getCodigo());
        curso.agregarEstudiante(estudiante.getMatricula());
    }

    /**
     * Inscribe a varios estudiantes en un curso. Devuelve los mensajes de error
     * de los que no se pudieron inscribir (lista vacía si todo salió bien).
     */
    public List<String> inscribirVarios(List<String> matriculas, String codigoCurso) {
        List<String> errores = new ArrayList<>();
        for (String matricula : matriculas) {
            try {
                inscribir(matricula, codigoCurso);
            } catch (RegistroException e) {
                errores.add(matricula + ": " + e.getMessage());
            }
        }
        return errores;
    }

    // ---------- Consultas (las usarán los demás módulos) ----------

    public Estudiante buscarEstudiante(String matricula) throws RegistroException {
        Estudiante e = estudiantes.get(matricula == null ? "" : matricula.trim());
        if (e == null) {
            throw new RegistroException("No existe el estudiante con matrícula " + matricula);
        }
        return e;
    }

    public Curso buscarCurso(String codigo) throws RegistroException {
        Curso c = cursos.get(codigo == null ? "" : codigo.trim());
        if (c == null) {
            throw new RegistroException("No existe el curso con código " + codigo);
        }
        return c;
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

    /** Estudiantes inscritos en un curso. */
    public List<Estudiante> estudiantesDeCurso(String codigoCurso) throws RegistroException {
        Curso curso = buscarCurso(codigoCurso);
        List<Estudiante> lista = new ArrayList<>();
        for (String matricula : curso.getMatriculasInscritas()) {
            lista.add(estudiantes.get(matricula));
        }
        return lista;
    }

    // ---------- Utilidades ----------

    private void validarTexto(String valor, String campo) throws RegistroException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new RegistroException(campo + " no puede estar vacío");
        }
    }
}
