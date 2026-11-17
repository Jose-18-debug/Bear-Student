package bearstudent.logica;
import bearstudent.dto.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PromediosAcademicos {
    public record PromedioCurso(String clave, String nombreCurso, Double promedio) { }

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

    public Double promedioDeEstudianteEnCurso(String matricula, String clave) {
        Estudiante estudiante = academico.buscarEstudiante(matricula);
        Curso curso = academico.buscarCurso(clave);
        return promedio(notas.calificacionesDeCurso(estudiante.getMatricula(), curso.getClave()));
    }

    public List<PromedioCurso> promediosPorCurso(String matricula) {
        Estudiante estudiante = academico.buscarEstudiante(matricula);
        List<PromedioCurso> lista = new ArrayList<>();
        for (String clave : estudiante.getCodigosCursos()) {
            Curso curso = academico.buscarCurso(clave);
            lista.add(new PromedioCurso(clave, curso.getNombre(),
                    promedio(notas.calificacionesDeCurso(estudiante.getMatricula(), clave))));
        }
        return lista;
    }

    public Double promedioFinal(String matricula) {
        List<Double> deCada = new ArrayList<>();
        for (PromedioCurso pc : promediosPorCurso(matricula)) {
            if (pc.promedio() != null) deCada.add(pc.promedio());
        }
        return promedio(deCada);
    }

    public Double promedioDeCurso(String clave) {
        Curso curso = academico.buscarCurso(clave);
        List<Double> deCada = new ArrayList<>();
        for (String matricula : curso.getMatriculasInscritas()) {
            Double p = promedio(notas.calificacionesDeCurso(matricula, curso.getClave()));
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

    public List<PromedioEstudiante> ordenarPorPromedio(boolean descendente) {
        return ordenar(academico.listarEstudiantes(), e -> promedioFinal(e.getMatricula()), descendente);
    }

    public List<PromedioEstudiante> ordenarPorPromedioEnCurso(String clave, boolean descendente) {
        Curso curso = academico.buscarCurso(clave);
        return ordenar(academico.estudiantesDeCurso(curso.getClave()),
                e -> promedioDeEstudianteEnCurso(e.getMatricula(), curso.getClave()), descendente);
    }

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

