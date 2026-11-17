package bearstudent.logica;

import bearstudent.dto.Calificacion;
import bearstudent.dto.Curso;
import bearstudent.dto.Estudiante;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Módulo de la Persona 2: evaluaciones de cada curso y calificaciones de cada estudiante.
 * Usa RegistroAcademico (Persona 1) para saber qué estudiantes y cursos existen y quién está inscrito.
 *
 * Reglas: la escala es de 0 a 10 (ambos valen); una evaluación pertenece a un curso y su nombre no se
 * repite dentro de él; solo se califica a estudiantes inscritos; capturar de nuevo la misma
 * calificación (mismo estudiante, curso y evaluación) REEMPLAZA el valor anterior.
 */
public class RegistroCalificaciones {
    private final RegistroAcademico registro;

    // clave de curso -> nombres de sus evaluaciones (en orden de registro)
    private final Map<String, Set<String>> evaluaciones = new LinkedHashMap<>();
    // matrícula -> (clave de curso -> (evaluación -> calificación))
    private final Map<String, Map<String, Map<String, Double>>> calificaciones = new LinkedHashMap<>();

    public RegistroCalificaciones(RegistroAcademico registro) {
        this.registro = registro;
    }

    // ---------- Evaluaciones ----------

    /** Orden de validación: 1) el curso existe, 2) el nombre no está vacío, 3) no está repetida. */
    public void registrarEvaluacion(String clave, String nombre) {
        Curso curso = registro.buscarCurso(clave);
        String nom = textoObligatorio(nombre, "El nombre de la evaluación no puede estar vacío");
        Set<String> lista = evaluaciones.computeIfAbsent(curso.getClave(), k -> new LinkedHashSet<>());
        if (!lista.add(nom)) {
            throw new ValidacionException("Ya existe la evaluación " + nom + " en el curso " + curso.getClave());
        }
    }

    /** Nombres de las evaluaciones de un curso (lista vacía si no tiene). */
    public List<String> evaluacionesDeCurso(String clave) {
        return new ArrayList<>(evaluaciones.getOrDefault(limpiar(clave), Set.of()));
    }

    // ---------- Calificaciones ----------

    /** Valida y guarda una calificación. Si ya existía para esa evaluación, la reemplaza. */
    public void capturarCalificacion(String matricula, String clave, String evaluacion, double calificacion) {
        validarCaptura(matricula, clave, evaluacion, calificacion);
        guardar(matricula, clave, evaluacion, calificacion);
    }

    /**
     * Solo valida (no guarda nada). Es de solo lectura, así que se puede llamar desde varios hilos a la vez.
     * Orden de validación: 1) el estudiante existe, 2) el curso existe, 3) está inscrito,
     * 4) la evaluación existe en el curso, 5) es un número, 6) está entre 0 y 10.
     */
    public void validarCaptura(String matricula, String clave, String evaluacion, double calificacion) {
        Estudiante estudiante = registro.buscarEstudiante(matricula);
        Curso curso = registro.buscarCurso(clave);
        if (!registro.estaInscrito(estudiante.getMatricula(), curso.getClave())) {
            throw new ValidacionException("El estudiante " + estudiante.getMatricula()
                    + " no está inscrito en el curso " + curso.getClave());
        }
        String eva = limpiar(evaluacion);
        Set<String> lista = evaluaciones.get(curso.getClave());
        if (lista == null || !lista.contains(eva)) {
            throw new ValidacionException("La evaluación " + eva + " no existe en el curso " + curso.getClave());
        }
        if (Double.isNaN(calificacion) || Double.isInfinite(calificacion)) {
            throw new ValidacionException("La calificación debe ser un número");
        }
        if (calificacion < 0 || calificacion > 10) {
            throw new ValidacionException("La calificación debe estar entre 0 y 10");
        }
    }

    /** Guarda sin validar: solo se llama después de validarCaptura (por eso no es público). */
    void guardar(String matricula, String clave, String evaluacion, double calificacion) {
        calificaciones
                .computeIfAbsent(limpiar(matricula), k -> new LinkedHashMap<>())
                .computeIfAbsent(limpiar(clave), k -> new LinkedHashMap<>())
                .put(limpiar(evaluacion), calificacion);
    }

    // ---------- Consultas (las usará la Persona 3 para los promedios) ----------

    /** Todas las calificaciones de un estudiante (lanza ValidacionException si el estudiante no existe). */
    public List<Calificacion> consultarCalificaciones(String matricula) {
        Estudiante estudiante = registro.buscarEstudiante(matricula);
        List<Calificacion> lista = new ArrayList<>();
        Map<String, Map<String, Double>> porCurso = calificaciones.getOrDefault(estudiante.getMatricula(), Map.of());
        for (Map.Entry<String, Map<String, Double>> curso : porCurso.entrySet()) {
            for (Map.Entry<String, Double> eva : curso.getValue().entrySet()) {
                lista.add(new Calificacion(estudiante.getMatricula(), curso.getKey(), eva.getKey(), eva.getValue()));
            }
        }
        return lista;
    }

    /** Calificaciones (solo los números) de un estudiante en un curso; vacía si no tiene ninguna. */
    public List<Double> calificacionesDeCurso(String matricula, String clave) {
        Map<String, Double> porEvaluacion = calificaciones
                .getOrDefault(limpiar(matricula), Map.of())
                .getOrDefault(limpiar(clave), Map.of());
        return new ArrayList<>(porEvaluacion.values());
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
