package bearstudent.ui.componentes;

import bearstudent.dto.Curso;
import bearstudent.dto.Estudiante;

/** Elemento de una lista desplegable: guarda el identificador real (matrícula o clave) y el texto que se ve. */
public record Opcion(String id, String texto) {

    /** "2024001 | Ana López (302)" */
    public static Opcion de(Estudiante e) {
        return new Opcion(e.getMatricula(), e.getMatricula() + " | " + e.getNombre() + " (" + e.getGrupo() + ")");
    }

    /** "POO | Programación orientada a objetos" */
    public static Opcion de(Curso c) {
        return new Opcion(c.getClave(), c.getClave() + " | " + c.getNombre());
    }

    @Override
    public String toString() {
        return texto;
    }
}
