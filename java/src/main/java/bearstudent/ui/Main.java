package bearstudent.ui;
import bearstudent.dto.*;
import bearstudent.logica.*;

import java.util.Collection;
import java.util.List;
import java.util.Scanner;

/**
 * Menú de consola de la Parte 1 (modo texto). La interfaz gráfica está en VentanaPrincipal.
 */
public class Main {
    private static final Scanner entrada = new Scanner(System.in);
    private static final RegistroAcademico registro = new RegistroAcademico();

    public static void main(String[] args) {
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Elige una opción: ");
            System.out.println();

            switch (opcion) {
                case 1: registrarEstudiante(); break;
                case 2: registrarCurso(); break;
                case 3: inscribirEstudiante(); break;
                case 4: listarEstudiantes(); break;
                case 5: listarCursos(); break;
                case 6: verInscritosDeCurso(); break;
                case 7: verEstudiantesPorGrupo(); break;
                case 0: System.out.println("Hasta luego."); break;
                default: System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    // ---------- Menú ----------

    private static void mostrarMenu() {
        System.out.println("\n===== REGISTRO ACADÉMICO =====");
        System.out.println("1. Registrar estudiante");
        System.out.println("2. Registrar curso");
        System.out.println("3. Inscribir estudiante en curso");
        System.out.println("4. Listar estudiantes");
        System.out.println("5. Listar cursos");
        System.out.println("6. Ver inscritos de un curso");
        System.out.println("7. Ver estudiantes por grupo");
        System.out.println("0. Salir");
    }

    // ---------- Opciones ----------

    private static void registrarEstudiante() {
        String matricula = leerTexto("Matrícula: ");
        String nombre = leerTexto("Nombre: ");
        String grupo = leerTexto("Grupo: ");
        try {
            registro.registrarEstudiante(matricula, nombre, grupo);
            System.out.println("Estudiante registrado correctamente.");
        } catch (ValidacionException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void registrarCurso() {
        String clave = leerTexto("Clave del curso: ");
        String nombre = leerTexto("Nombre del curso: ");
        try {
            registro.registrarCurso(clave, nombre);
            System.out.println("Curso registrado correctamente.");
        } catch (ValidacionException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void inscribirEstudiante() {
        String matricula = leerTexto("Matrícula del estudiante: ");
        String clave = leerTexto("Clave del curso: ");
        try {
            registro.inscribir(matricula, clave);
            System.out.println("Inscripción realizada correctamente.");
        } catch (ValidacionException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void listarEstudiantes() {
        Collection<Estudiante> lista = registro.listarEstudiantes();
        if (lista.isEmpty()) {
            System.out.println("No hay estudiantes registrados.");
            return;
        }
        System.out.println("--- Estudiantes ---");
        lista.forEach(Main::imprimir);
    }

    private static void listarCursos() {
        Collection<Curso> lista = registro.listarCursos();
        if (lista.isEmpty()) {
            System.out.println("No hay cursos registrados.");
            return;
        }
        System.out.println("--- Cursos ---");
        lista.forEach(c -> System.out.println(c + " - Inscritos: " + registro.totalInscritos(c.getClave())));
    }

    private static void verInscritosDeCurso() {
        String clave = leerTexto("Clave del curso: ");
        try {
            List<Estudiante> inscritos = registro.estudiantesDeCurso(clave);
            if (inscritos.isEmpty()) {
                System.out.println("El curso no tiene estudiantes inscritos.");
            } else {
                System.out.println("--- Inscritos ---");
                inscritos.forEach(Main::imprimir);
            }
        } catch (ValidacionException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void verEstudiantesPorGrupo() {
        String grupo = leerTexto("Grupo: ");
        List<Estudiante> lista = registro.estudiantesPorGrupo(grupo);
        if (lista.isEmpty()) {
            System.out.println("No hay estudiantes en ese grupo.");
        } else {
            System.out.println("--- Grupo " + grupo + " ---");
            lista.forEach(Main::imprimir);
        }
    }

    private static void imprimir(Estudiante e) {
        System.out.println(e + " - Cursos: " + registro.cursosDeEstudiante(e.getMatricula()));
    }

    // ---------- Lectura de datos ----------

    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return entrada.nextLine();
    }

    /** Pide un entero y vuelve a preguntar hasta que el usuario escriba uno válido. */
    private static int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = entrada.nextLine().trim();
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Escribe un número entero válido.");
            }
        }
    }
}

