# Bear Student — Sistema de Administración Escolar

## 1. Descripción del proyecto

Bear Student es una aplicación de escritorio desarrollada en Python con la biblioteca Tkinter. Su objetivo es facilitar la administración de estudiantes, cursos, evaluaciones y calificaciones, además de permitir el análisis del rendimiento académico mediante promedios, estadísticas y reportes.

El sistema está organizado por módulos para facilitar su mantenimiento, comprensión e integración entre los integrantes del equipo.

## 2. Requisitos para ejecutar el proyecto

Antes de ejecutar la aplicación, es necesario contar con:

- Python 3.10 o superior.
- PyCharm o cualquier editor compatible con Python.
- Biblioteca Tkinter (generalmente incluida en la instalación de Python).
- Pillow (opcional, utilizada para mejorar la visualización del logotipo).

## 3. Instalación y ejecución

### Paso 1. Abrir el proyecto

Descargar o clonar el repositorio de Git y abrir la carpeta `Bear-Student-Python` en PyCharm.

### Paso 2. Instalar las dependencias

Abrir la terminal de PyCharm, ubicarse en la carpeta principal del proyecto y ejecutar:

```bash
pip install -r requirements.txt
```

### Paso 3. Ejecutar la aplicación

Para iniciar el sistema, ejecutar el siguiente archivo:

```bash
python Estudiantes_Cursos.py
```

También se puede abrir el archivo `Estudiantes_Cursos.py` desde PyCharm y presionar el botón Run.

**Importante:** Este es el archivo que debe ejecutarse para iniciar Bear Student. No es necesario ejecutar individualmente los archivos que se encuentran dentro de las carpetas `domain` y `ui`, ya que sus funciones son utilizadas por la aplicación principal.

## 4. Componentes y funcionamiento del sistema

La aplicación cuenta con cinco apartados principales:

### 4.1. Registro

Permite administrar la información básica de los estudiantes y cursos.

Sus funciones son:

- Registrar estudiantes mediante matrícula, nombre y grupo.
- Registrar cursos mediante clave y nombre de la asignatura.
- Inscribir estudiantes en los cursos disponibles.
- Consultar los estudiantes registrados.
- Visualizar las inscripciones realizadas.

### 4.2. Evaluaciones

Permite administrar las evaluaciones y las calificaciones de los estudiantes.

Sus funciones son:

- Registrar evaluaciones asociadas a un curso.
- Establecer el nombre de la evaluación y su calificación máxima.
- Capturar las calificaciones de los estudiantes inscritos.
- Registrar calificaciones de varios estudiantes en una misma operación.
- Consultar las calificaciones registradas.

### 4.3. Promedios

Permite consultar el rendimiento académico de los estudiantes y los cursos.

Sus funciones son:

- Calcular el promedio final de cada estudiante.
- Calcular los promedios por curso.
- Consultar las calificaciones individuales de un estudiante.
- Ordenar a los estudiantes según su promedio académico.
- Consultar el ranking de estudiantes.

### 4.4. Aprobación y estadísticas

Permite analizar los resultados académicos y clasificar a los estudiantes.

Sus funciones son:

- Clasificar estudiantes aprobados y reprobados.
- Considerar una calificación mínima de 7.0 para aprobar.
- Identificar estudiantes que presentan riesgo académico.
- Calcular el promedio general.
- Identificar la calificación máxima y mínima.
- Calcular el porcentaje de aprobación.
- Consultar los resultados mediante tablas y estadísticas.

### 4.5. Reportes

Permite generar reportes académicos y exportarlos a diferentes formatos.

Sus funciones son:

- Generar reportes por grupo.
- Generar reportes por asignatura.
- Consultar información de estudiantes, cursos, evaluaciones y calificaciones.
- Exportar reportes en formato JSON.
- Exportar reportes en formato CSV.

**¿Cómo generar un reporte?**

1. Entrar en la pestaña `Reportes`.
2. Seleccionar el tipo de reporte: `Por grupo` o `Por asignatura`.
3. Elegir el grupo o la asignatura correspondiente.
4. Presionar el botón `Generar reporte`.
5. Revisar la información presentada en la tabla.
6. Seleccionar `Exportar JSON` o `Exportar CSV`.
7. Elegir la ubicación y el nombre del archivo para guardarlo.

El formato JSON conserva la información organizada en una estructura de datos, mientras que CSV permite consultar los resultados en programas como Microsoft Excel.

## 5. Estructura del proyecto

El proyecto está organizado de la siguiente manera:

```text
Bear-Student-Python/
│
├── src/
│   └── bear_student/
│       ├── assets/
│       │   └── logo.png
│       │
│       ├── domain/
│       │   ├── approval.py
│       │   ├── AprobadosVista.py
│       │   ├── averages.py
│       │   ├── evaluations.py
│       │   ├── reports.py
│       │   ├── students_courses.py
│       │   └── TestEstadisticas.py
│       │
│       ├── ui/
│       │   ├── averages_view.py
│       │   ├── evaluations_view.py
│       │   ├── reports_view.py
│       │   ├── theme.py
│       │   └── widgets.py
│       │
│       └── main.py
│
├── tests/
│   ├── test_dominio.py
│   ├── test_interfaz.py
│   └── test_reportes.py
│
├── Estudiantes_Cursos.py
├── requirements.txt
├── README.md
└── .gitignore
```

### Descripción de las carpetas

**domain:** contiene la lógica principal del sistema, como el registro de estudiantes, cursos, evaluaciones, promedios, aprobación, estadísticas y generación de reportes.

**ui:** contiene los componentes visuales de la aplicación, incluyendo las pestañas, tablas, botones, estilos y formularios.

**assets:** almacena los recursos gráficos utilizados por el sistema, como el logotipo.

**tests:** contiene las pruebas utilizadas para verificar el funcionamiento de los módulos.

**main.py:** integra los servicios y las vistas para construir la aplicación principal.

**Estudiantes_Cursos.py:** archivo lanzador que permite iniciar el sistema desde la carpeta principal del proyecto.

## 6. Ejecución de pruebas

Para comprobar el funcionamiento de los módulos, abrir la terminal desde la carpeta principal del proyecto y ejecutar:

```bash
python -m unittest discover -s tests -v
```

Este comando ejecuta las pruebas disponibles para comprobar la lógica del sistema, los cálculos, las exportaciones y el funcionamiento de la interfaz cuando existe un entorno gráfico disponible.

## 7. Exportación y archivos generados

Los reportes pueden guardarse en dos formatos:

- **JSON (.json):** permite almacenar información estructurada de estudiantes, cursos, evaluaciones y resultados académicos.
- **CSV (.csv):** permite almacenar información en filas y columnas para facilitar su consulta y análisis en hojas de cálculo.

Los archivos se guardan en la ubicación seleccionada por el usuario mediante el cuadro de diálogo de exportación.

La información de trabajo se administra en memoria durante la ejecución de la aplicación. Los reportes exportados sí quedan guardados como archivos en la ubicación seleccionada.

## 8. Trabajo colaborativo

El desarrollo del proyecto se distribuyó entre cinco integrantes:

- Persona 1: Registro de estudiantes y cursos.
- Persona 2: Evaluaciones y calificaciones.
- Persona 3: Promedios y consultas.
- Persona 4: Aprobación y estadísticas.
- Persona 5: Reportes, exportaciones e integración de módulos.

El proyecto se desarrolla mediante Git, utilizando la rama `rama-python` para integrar los cambios correspondientes a la versión de Python.

## 9. Información adicional

Bear Student es un proyecto académico desarrollado con fines educativos. Su estructura modular permite incorporar nuevas funcionalidades y realizar modificaciones sin necesidad de reconstruir toda la aplicación.

Para iniciar el sistema, ejecutar siempre:

```bash
python Estudiantes_Cursos.py
```

**Bear Student — Administración escolar.**