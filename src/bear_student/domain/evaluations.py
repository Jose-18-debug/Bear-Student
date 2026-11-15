"""Registro de evaluaciones y calificaciones del bloque Persona 2."""

import math
from dataclasses import dataclass


@dataclass
class Evaluacion:
    clave: str
    curso_clave: str
    nombre: str
    calificacion_maxima: float


class ServicioEvaluaciones:
    """Mantiene evaluaciones y notas sin encargarse de la interfaz gráfica."""

    def __init__(self, curso_existe, obtener_estudiantes_inscritos):
        self.curso_existe = curso_existe
        self.obtener_estudiantes_inscritos = obtener_estudiantes_inscritos
        self.evaluaciones: dict[str, Evaluacion] = {}
        self.calificaciones: dict[str, dict[str, float]] = {}

    def registrar_evaluacion(self, curso_clave, nombre, calificacion_maxima=10):
        nombre = nombre.strip()
        if not self.curso_existe(curso_clave):
            raise ValueError("Selecciona un curso válido.")
        if not nombre:
            raise ValueError("Escribe el nombre de la evaluación.")
        try:
            maxima = float(calificacion_maxima)
        except (TypeError, ValueError):
            raise ValueError("La calificación máxima debe ser un número.")
        if not math.isfinite(maxima) or maxima <= 0:
            raise ValueError("La calificación máxima debe ser mayor que cero.")

        numero = len(self.evaluaciones) + 1
        clave = f"EV{numero:03d}"
        while clave in self.evaluaciones:
            numero += 1
            clave = f"EV{numero:03d}"

        evaluacion = Evaluacion(clave, curso_clave, nombre, maxima)
        self.evaluaciones[clave] = evaluacion
        self.calificaciones[clave] = {}
        return evaluacion

    def registrar_calificacion(self, evaluacion_clave, matricula, calificacion):
        self.registrar_varias_calificaciones(evaluacion_clave, {matricula: calificacion})
        return self.calificaciones[evaluacion_clave][matricula]

    def registrar_varias_calificaciones(self, evaluacion_clave, notas_por_matricula):
        """Valida todas las notas antes de guardar cualquiera de ellas."""
        evaluacion = self.evaluaciones.get(evaluacion_clave)
        if evaluacion is None:
            raise ValueError("Selecciona una evaluación válida.")
        if not notas_por_matricula:
            raise ValueError("Escribe al menos una calificación.")

        notas_validadas = {}
        inscritos = {
            estudiante.matricula
            for estudiante in self.obtener_estudiantes_inscritos(evaluacion.curso_clave)
        }
        for matricula, valor in notas_por_matricula.items():
            if matricula not in inscritos:
                raise ValueError(
                    f"La matrícula {matricula} no está inscrita en el curso."
                )
            try:
                nota = float(valor)
            except (TypeError, ValueError):
                raise ValueError(f"La calificación de {matricula} debe ser numérica.")
            if (
                not math.isfinite(nota)
                or nota < 0
                or nota > evaluacion.calificacion_maxima
            ):
                raise ValueError(
                    f"La calificación de {matricula} debe estar entre 0 y "
                    f"{evaluacion.calificacion_maxima:g}."
                )
            notas_validadas[matricula] = nota

        self.calificaciones[evaluacion_clave].update(notas_validadas)
        return notas_validadas

    def evaluaciones_del_curso(self, curso_clave):
        return [
            evaluacion for evaluacion in self.evaluaciones.values()
            if evaluacion.curso_clave == curso_clave
        ]

    def obtener_calificaciones(self, evaluacion_clave):
        return dict(self.calificaciones.get(evaluacion_clave, {}))
