# Bear-Student
Un proyecto escolar colaborativo sobre un sistema de control de estudiantes, calificaciones y cursos en una escuela ficticia.

## Versión en Python (rama `rama-python`)

Aplicación de escritorio hecha con Tkinter. Requiere Python 3.10 o superior; `Pillow` es opcional (solo mejora cómo se ve el logo).

### Ejecutar

Desde la carpeta del proyecto:

```bash
python Estudiantes_Cursos.py
```

### Pestañas

| Pestaña | Para qué sirve | Responsable |
|---|---|---|
| Registro | Registrar estudiantes y cursos, e inscribir estudiantes en cursos | Persona 1 |
| Estudiantes / Cursos / Inscripciones | Tablas con lo registrado (Cursos incluye el promedio del curso) | Persona 1 |
| Evaluaciones | Registrar evaluaciones y capturar calificaciones de varios estudiantes a la vez | Persona 2 |
| Promedios | Ranking de estudiantes por promedio (general o por curso) y consulta de las calificaciones de un estudiante | Persona 3 |

### Cómo se calculan los promedios

- Todas las notas se pasan a escala de 0 a 10 (una evaluación puede ser sobre 10, 20, 100...).
- **Promedio en un curso:** media de las evaluaciones de ese curso que ya tienen calificación. Una evaluación sin calificar no cuenta como 0.
- **Promedio final:** media de los promedios por curso; cada curso pesa lo mismo.
- **Promedio del curso:** media de los promedios de sus estudiantes con notas.
- Se aprueba con 6. Los empates en el ranking se ordenan por nombre.

### Estructura

```
src/bear_student/
├── domain/              Lógica sin interfaz (se puede probar sola)
│   ├── students_courses.py   Persona 1
│   ├── evaluations.py        Persona 2
│   └── averages.py           Persona 3
├── ui/                  Pantallas Tkinter
│   ├── evaluations_view.py
│   ├── averages_view.py
│   ├── widgets.py            Botón y pestaña con scroll compartidos
│   └── theme.py              Colores y tipografía
└── main.py              Ventana principal
tests/                   Pruebas automáticas
```

### Pruebas

```bash
python -m unittest discover -s tests -v
```
