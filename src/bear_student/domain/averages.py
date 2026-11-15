"""Promedios y consultas de calificaciones del bloque Persona 3.

Cada evaluación puede tener una calificación máxima distinta (10, 20, 100...),
así que todas las notas se convierten a la escala de 0 a 10 antes de promediar.

- Promedio de un estudiante en un curso: media de sus notas en las
  evaluaciones de ese curso que ya tienen calificación.
- Promedio final de un estudiante: media de sus promedios por curso
  (cada curso pesa lo mismo, sin importar cuántas evaluaciones tenga).
- Promedio de un curso: media de los promedios de sus estudiantes con notas.

Las evaluaciones sin calificar no cuentan como cero: simplemente no entran
en el promedio hasta que se capturen.
"""

from dataclasses import dataclass

ESCALA = 10.0
CALIFICACION_APROBATORIA = 6.0


def a_escala(nota, maxima):
    """Convierte una nota sobre ``maxima`` a la escala de 0 a 10."""
    return nota / maxima * ESCALA


def media(valores):
    valores = list(valores)
    return sum(valores) / len(valores) if valores else None


def estado_de(promedio):
    if promedio is None:
        return "Sin calificaciones"
    if round(promedio, 2) >= CALIFICACION_APROBATORIA:
        return "Aprobado"
    return "Reprobado"


@dataclass(frozen=True)
class CalificacionConsultada:
    curso_clave: str
    curso_nombre: str
    evaluacion_clave: str
    evaluacion_nombre: str
    nota: float
    maxima: float

    @property
    def en_escala(self):
        return a_escala(self.nota, self.maxima)


@dataclass(frozen=True)
class PromedioEstudiante:
    matricula: str
    nombre: str
    grupo: str
    promedio: float | None
    cursos_con_notas: int

    @property
    def estado(self):
        return estado_de(self.promedio)


@dataclass(frozen=True)
class PromedioCurso:
    clave: str
    nombre: str
    promedio: float | None
    estudiantes_con_notas: int
    inscritos: int

    @property
    def estado(self):
        return estado_de(self.promedio)


class ServicioPromedios:
    """Calcula promedios a partir de los datos de Persona 1 y Persona 2."""

    def __init__(self, sistema_escolar, servicio_evaluaciones):
        self.sistema = sistema_escolar
        self.evaluaciones = servicio_evaluaciones

    # ----- Consultas -------------------------------------------------------

    def calificaciones_de_estudiante(self, matricula, curso_clave=None):
        """Todas las notas del estudiante, ordenadas por curso y evaluación."""
        if matricula not in self.sistema.estudiantes:
            raise ValueError("Selecciona un estudiante válido.")
        if curso_clave is not None and curso_clave not in self.sistema.cursos:
            raise ValueError("Selecciona un curso válido.")

        resultado = []
        for evaluacion in self.evaluaciones.evaluaciones.values():
            if curso_clave is not None and evaluacion.curso_clave != curso_clave:
                continue
            curso = self.sistema.cursos.get(evaluacion.curso_clave)
            notas = self.evaluaciones.obtener_calificaciones(evaluacion.clave)
            if curso is None or matricula not in notas:
                continue
            resultado.append(CalificacionConsultada(
                curso.clave,
                curso.nombre,
                evaluacion.clave,
                evaluacion.nombre,
                notas[matricula],
                evaluacion.calificacion_maxima,
            ))
        # sorted es estable: dentro de un curso se respeta el orden de registro.
        return sorted(resultado, key=lambda c: c.curso_nombre.casefold())

    # ----- Promedios -------------------------------------------------------

    def promedio_en_curso(self, matricula, curso_clave):
        notas = self.calificaciones_de_estudiante(matricula, curso_clave)
        return media(c.en_escala for c in notas)

    def promedios_por_curso_de_estudiante(self, matricula):
        """{clave_curso: promedio} solo de los cursos que ya tienen notas."""
        por_curso = {}
        for calificacion in self.calificaciones_de_estudiante(matricula):
            por_curso.setdefault(calificacion.curso_clave, []).append(calificacion.en_escala)
        return {clave: media(notas) for clave, notas in por_curso.items()}

    def promedio_final(self, matricula):
        return media(self.promedios_por_curso_de_estudiante(matricula).values())

    def promedio_del_curso(self, curso_clave):
        return self._resumen_curso(curso_clave).promedio

    def promedios_de_cursos(self):
        """Resumen de cada curso, en orden de nombre."""
        cursos = sorted(self.sistema.cursos.values(), key=lambda c: c.nombre.casefold())
        return [self._resumen_curso(curso.clave) for curso in cursos]

    def _resumen_curso(self, curso_clave):
        curso = self.sistema.cursos.get(curso_clave)
        if curso is None:
            raise ValueError("Selecciona un curso válido.")
        inscritos = self.sistema.estudiantes_inscritos(curso_clave)
        promedios = [
            p for p in (self.promedio_en_curso(e.matricula, curso_clave) for e in inscritos)
            if p is not None
        ]
        return PromedioCurso(curso.clave, curso.nombre, media(promedios),
                             len(promedios), len(inscritos))

    # ----- Ordenamiento ----------------------------------------------------

    def ranking(self, curso_clave=None):
        """Estudiantes ordenados de mayor a menor promedio.

        Sin ``curso_clave`` usa el promedio final y lista a todos los
        estudiantes; con ``curso_clave`` usa el promedio en ese curso y lista
        solo a los inscritos. Los empates se ordenan por nombre y quienes aún
        no tienen calificaciones van al final.
        """
        if curso_clave is None:
            estudiantes = list(self.sistema.estudiantes.values())
        else:
            if curso_clave not in self.sistema.cursos:
                raise ValueError("Selecciona un curso válido.")
            estudiantes = self.sistema.estudiantes_inscritos(curso_clave)

        filas = []
        for estudiante in estudiantes:
            if curso_clave is None:
                por_curso = self.promedios_por_curso_de_estudiante(estudiante.matricula)
                promedio = media(por_curso.values())
                cursos = len(por_curso)
            else:
                promedio = self.promedio_en_curso(estudiante.matricula, curso_clave)
                cursos = 0 if promedio is None else 1
            filas.append(PromedioEstudiante(
                estudiante.matricula, estudiante.nombre, estudiante.grupo, promedio, cursos,
            ))

        return sorted(filas, key=lambda f: (
            f.promedio is None,
            -round(f.promedio, 6) if f.promedio is not None else 0,
            f.nombre.casefold(),
        ))
