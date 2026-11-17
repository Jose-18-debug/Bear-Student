# Bear Student (Java)

Requisitos: JDK 17 o superior y Maven.

## Pruebas
    cd java
    mvn test

## Interfaz
Abrir el proyecto en IntelliJ (pom.xml como proyecto Maven) y ejecutar la clase `VentanaPrincipal`.
Sin IntelliJ:

    mvn compile
    java -cp target/classes bearstudent.ui.VentanaPrincipal

La clase `MenuConsola` es un menú de texto con las funciones de la parte 1.

## Estructura
- `dto`: datos (`Estudiante`, `Curso`, `Calificacion`, `PromedioEstudiante`).
- `logica`: reglas y cálculos; no usa nada de Swing. `estadisticas` y `reportes` son las partes 4 y 5.
- `ui`: ventana y una clase por pestaña (`PanelEstudiantes`, `PanelCalificaciones`, `PanelPromedios`,
  `PanelReportes`). `ui/componentes` tiene la paleta (`Estilo`) y las piezas que comparten todas.

## Pestañas de la interfaz
- **Estudiantes y cursos** (Persona 1): registro e inscripciones.
- **Calificaciones** (Persona 2): evaluaciones, captura y procesamiento por lote.
- **Promedios** (Persona 3): consulta por estudiante y ranking por promedio.
- **Reportes** (Personas 4 y 5): reporte general, por grupo o por asignatura, con aprobados,
  reprobados, riesgo académico, promedio general, promedio más alto y más bajo, y % de aprobación.
  Los botones **Exportar JSON** y **Exportar CSV** guardan el reporte que se está viendo.
