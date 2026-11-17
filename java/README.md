# Bear Student (Java)

Requisitos: JDK 17 o superior y Maven.

## Pruebas
    cd java
    mvn test

## Interfaz
Abrir el proyecto en IntelliJ (pom.xml como proyecto Maven) y ejecutar la clase `VentanaPrincipal`.

## Pestañas de la interfaz
- **Estudiantes y cursos** (Persona 1): registro e inscripciones.
- **Calificaciones** (Persona 2): evaluaciones, captura y procesamiento por lote.
- **Promedios** (Persona 3): consulta por estudiante y ranking por promedio.
- **Reportes** (Personas 4 y 5): reporte general, por grupo o por asignatura, con aprobados,
  reprobados, riesgo académico, promedio general, máxima, mínima y % de aprobación.
  Los botones **Exportar JSON** y **Exportar CSV** guardan el reporte que se está viendo.
