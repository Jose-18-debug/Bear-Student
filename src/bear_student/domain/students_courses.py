"""Datos y operaciones básicas de estudiantes, cursos e inscripciones."""

from dataclasses import dataclass


@dataclass
class Estudiante:
    matricula: str
    nombre: str
    grupo: str


@dataclass
class Curso:
    clave: str
    nombre: str


class SistemaEscolar:
    def __init__(self):
        self.estudiantes: dict[str, Estudiante] = {}
        self.cursos: dict[str, Curso] = {}
        self.inscripciones: dict[str, set[str]] = {}

    def registrar_estudiante(self, matricula, nombre, grupo):
        matricula, nombre, grupo = matricula.strip(), nombre.strip(), grupo.strip()
        if not all((matricula, nombre, grupo)):
            raise ValueError("Completa matrícula, nombre y grupo.")
        if matricula in self.estudiantes:
            raise ValueError("Esa matrícula ya está registrada.")
        self.estudiantes[matricula] = Estudiante(matricula, nombre, grupo)
        self.inscripciones[matricula] = set()

    def registrar_curso(self, clave, nombre):
        clave, nombre = clave.strip(), nombre.strip()
        if not clave or not nombre:
            raise ValueError("Completa clave y nombre del curso.")
        if clave in self.cursos:
            raise ValueError("Esa clave de curso ya está registrada.")
        self.cursos[clave] = Curso(clave, nombre)

    def inscribir(self, matricula, clave):
        if matricula not in self.estudiantes:
            raise ValueError("Selecciona un estudiante válido.")
        if clave not in self.cursos:
            raise ValueError("Selecciona un curso válido.")
        if clave in self.inscripciones[matricula]:
            raise ValueError("El estudiante ya está inscrito en ese curso.")
        self.inscripciones[matricula].add(clave)

    def estudiantes_inscritos(self, clave_curso):
        """Devuelve estudiantes inscritos en el curso, en orden de nombre."""
        matriculas = [
            matricula for matricula, cursos in self.inscripciones.items()
            if clave_curso in cursos
        ]
        return sorted(
            (self.estudiantes[matricula] for matricula in matriculas),
            key=lambda estudiante: estudiante.nombre.casefold(),
        )
