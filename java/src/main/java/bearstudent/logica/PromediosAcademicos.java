package bearstudent.logica;

import bearstudent.dto.Calificacion;
import bearstudent.dto.Curso;
import bearstudent.dto.Estudiante;
import bearstudent.dto.PromedioEstudiante;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Módulo de la Persona 3: promedios por curso, promedio final y estudiantes ordenados por promedio.
 * Usa RegistroAcademico (parte 1) para saber quién está inscrito y RegistroCalificaciones (parte 2)
 * para las calificaciones. Un promedio sin calificaciones es null, para distinguirlo de un 0.
 */
public class PromediosAcademicos {

    /** Promedio de un estudiante en uno de sus cursos (null si no tiene calificaciones en él). */
    public record PromedioCurso(String clave, String nombreCurso, Double promedio) { }

    /** Todo lo que se muestra de un estudiante: sus calificaciones, el promedio de cada curso y el final. */
    public record ReporteEstudiante(String matricula, String nombre,
                                    List<Calificacion> calificaciones,
                                    List<PromedioCurso> promediosPorCurso,
                                    Double promedioFinal) { }

    private final RegistroAcademico academico;
    private final RegistroCalificaciones notas;

    public PromediosAcademicos(RegistroAcademico academico, RegistroCalificaciones notas) {
        this.academico = academico;
        this.notas = notas;
    }

    /** Promedio de las calificaciones del estudiante en ese curso. */
    public Double promedioDeEstudianteEnCurso(String matricula, String clave) {
        Estudiante estudiante = academico.buscarEstudiante(matricula);
        Curso curso = academico.buscarCurso(clave);
        return promedio(notas.calificacionesDeCurso(estudiante.getMatricula(), curso.getClave()));
    }

    /** Un PromedioCurso por cada curso en el que está inscrito el estudiante. */
    public List<PromedioCurso> promediosPorCurso(String matricula) {
        Estudiante estudiante = academico.buscarEstudiante(matricula);
        List<PromedioCurso> lista = new ArrayList<>();
        for (String clave : academico.cursosDeEstudiante(estudiante.getMatricula())) {
            Curso curso = academico.buscarCurso(clave);
            lista.add(new PromedioCurso(clave, curso.getNombre(),
                    promedio(notas.calificacionesDeCurso(estudiante.getMatricula(), clave))));
        }
        return lista;
    }

    /** Promedio de los promedios de sus cursos; los cursos sin calificaciones no cuentan. */
    public Double promedioFinal(String matricula) {
        List<Double> deCada = new ArrayList<>();
        for (PromedioCurso pc : promediosPorCurso(matricula)) {
            if (pc.promedio() != null) deCada.add(pc.promedio());
        }
        return promedio(deCada);
    }

    /** Promedio de los promedios de los inscritos que tienen calificaciones en el curso. */
    public Double promedioDeCurso(String clave) {
        Curso curso = academico.buscarCurso(clave);
        List<Double> deCada = new ArrayList<>();
        for (Estudiante e : academico.estudiantesDeCurso(curso.getClave())) {
            Double p = promedio(notas.calificacionesDeCurso(e.getMatricula(), curso.getClave()));
            if (p != null) deCada.add(p);
        }
        return promedio(deCada);
    }

    public ReporteEstudiante reporteDeEstudiante(String matricula) {
        Estudiante estudiante = academico.buscarEstudiante(matricula);
        return new ReporteEstudiante(
                estudiante.getMatricula(),
                estudiante.getNombre(),
                notas.consultarCalificaciones(estudiante.getMatricula()),
                promediosPorCurso(estudiante.getMatricula()),
                promedioFinal(estudiante.getMatricula()));
    }

    /** Todos los estudiantes con su promedio final; los que no tienen calificaciones van al final. */
    public List<PromedioEstudiante> ordenarPorPromedio(boolean descendente) {
        return ordenar(academico.listarEstudiantes(), e -> promedioFinal(e.getMatricula()), descendente);
    }

    /** Los inscritos en un curso con su promedio en ese curso; los que no tienen calificaciones van al final. */
    public List<PromedioEstudiante> ordenarPorPromedioEnCurso(String clave, boolean descendente) {
        Curso curso = academico.buscarCurso(clave);
        return ordenar(academico.estudiantesDeCurso(curso.getClave()),
                e -> promedioDeEstudianteEnCurso(e.getMatricula(), curso.getClave()), descendente);
    }

    /** Los empates se ordenan por nombre y después por matrícula, para que el resultado siempre sea el mismo. */
    private List<PromedioEstudiante> ordenar(Collection<Estudiante> estudiantes,
                                             Function<Estudiante, Double> calculo, boolean descendente) {
        Comparator<Double> porPromedio = Comparator.naturalOrder();
        if (descendente) porPromedio = porPromedio.reversed();
        Comparator<PromedioEstudiante> orden = Comparator
                .comparing(PromedioEstudiante::promedio, Comparator.nullsLast(porPromedio))
                .thenComparing(PromedioEstudiante::nombre, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(PromedioEstudiante::matricula);
        return estudiantes.stream()
                .map(e -> new PromedioEstudiante(e.getMatricula(), e.getNombre(), e.getGrupo(), calculo.apply(e)))
                .sorted(orden)
                .collect(Collectors.toList());
    }

    private static Double promedio(Collection<Double> valores) {
        if (valores.isEmpty()) return null;
        return valores.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
    }
}
