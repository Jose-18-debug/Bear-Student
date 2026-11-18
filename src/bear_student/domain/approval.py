"""Aprobación y estadísticas
Reutiliza los promedios de Persona 3

"""

import math
from dataclasses import dataclass

from bear_student.domain.averages import media

NOTA_APROBATORIA = 7.0
MARGEN_RIESGO = 0.5

APROBADO = "Aprobado"
REPROBADO = "Reprobado"
SIN_CALIFICACIONES = "Sin calificaciones"


def clasificar(promedio, nota_aprobatoria=NOTA_APROBATORIA):
    """Devuelve Aprobado, Reprobado o Sin calificaciones para un promedio.

    Se redondea a 2 decimales igual que lo que ve el usuario en pantalla,
    así que 6.996 (que se muestra como 7.00) cuenta como aprobado.
    """
    if promedio is None:
        return SIN_CALIFICACIONES
    return APROBADO if round(promedio, 2) >= nota_aprobatoria else REPROBADO


@dataclass(frozen=True)
class EstudianteClasificado:
    matricula: str
    nombre: str
    grupo: str
    promedio: float | None
    estado: str
    en_riesgo: bool
    motivo_riesgo: str


@dataclass(frozen=True)
class ResumenEstadistico:
    total_estudiantes: int
    aprobados: int
    reprobados: int
    sin_calificaciones: int
    en_riesgo: int
    promedio_general: float | None
    maxima: EstudianteClasificado | None
    minima: EstudianteClasificado | None
    porcentaje_aprobacion: float | None

    @property
    def con_calificaciones(self):
        return self.aprobados + self.reprobados


class ServicioAprobacion:
    """Clasifica estudiantes y calcula estadísticas sin tocar la interfaz."""

    def __init__(self, servicio_promedios, nota_aprobatoria=NOTA_APROBATORIA,
                 margen_riesgo=MARGEN_RIESGO):
        if not math.isfinite(nota_aprobatoria) or nota_aprobatoria <= 0:
            raise ValueError("La nota aprobatoria debe ser mayor que cero.")
        if not math.isfinite(margen_riesgo) or margen_riesgo < 0:
            raise ValueError("El margen de riesgo no puede ser negativo.")
        self.promedios = servicio_promedios
        self.nota_aprobatoria = nota_aprobatoria
        self.margen_riesgo = margen_riesgo

    # ----- Clasificación ---------------------------------------------------

    def clasificar_estudiantes(self, curso_clave=None):
        """Todos los estudiantes (o los inscritos en un curso) clasificados.

        Van de mayor a menor promedio (el orden del ranking de Persona 3);
        quienes no tienen calificaciones quedan al final.
        """
        clasificados = []
        for fila in self.promedios.ranking(curso_clave):
            estado = clasificar(fila.promedio, self.nota_aprobatoria)
            motivos = self._motivos_de_riesgo(fila, estado, curso_clave)
            clasificados.append(EstudianteClasificado(
                fila.matricula, fila.nombre, fila.grupo, fila.promedio,
                estado, bool(motivos), "; ".join(motivos),
            ))
        return clasificados

    def aprobados(self, curso_clave=None):
        return [e for e in self.clasificar_estudiantes(curso_clave) if e.estado == APROBADO]

    def reprobados(self, curso_clave=None):
        return [e for e in self.clasificar_estudiantes(curso_clave) if e.estado == REPROBADO]

    def en_riesgo(self, curso_clave=None):
        return [e for e in self.clasificar_estudiantes(curso_clave) if e.en_riesgo]

    def _motivos_de_riesgo(self, fila, estado, curso_clave):
        if fila.promedio is None:
            return []
        motivos = []
        limite = self.nota_aprobatoria + self.margen_riesgo
        if estado == REPROBADO:
            motivos.append(f"Promedio menor a {self.nota_aprobatoria:g}")
        elif round(fila.promedio, 2) < limite:
            motivos.append(f"Aprobó por poco (menos de {limite:g})")
        if curso_clave is None:
            por_curso = self.promedios.promedios_por_curso_de_estudiante(fila.matricula)
            reprobados = sorted(
                self.promedios.sistema.cursos[clave].nombre
                for clave, promedio in por_curso.items()
                if clasificar(promedio, self.nota_aprobatoria) == REPROBADO
            )
            if reprobados:
                motivos.append("Curso(s) reprobado(s): " + ", ".join(reprobados))
        return motivos

    # ----- Estadísticas ----------------------------------------------------

    def resumen(self, curso_clave=None):
        clasificados = self.clasificar_estudiantes(curso_clave)
        con_notas = [e for e in clasificados if e.promedio is not None]
        aprobados = sum(1 for e in con_notas if e.estado == APROBADO)
        reprobados = len(con_notas) - aprobados
        return ResumenEstadistico(
            total_estudiantes=len(clasificados),
            aprobados=aprobados,
            reprobados=reprobados,
            sin_calificaciones=len(clasificados) - len(con_notas),
            en_riesgo=sum(1 for e in clasificados if e.en_riesgo),
            promedio_general=media(e.promedio for e in con_notas),
            maxima=max(con_notas, key=lambda e: e.promedio, default=None),
            minima=min(con_notas, key=lambda e: e.promedio, default=None),
            porcentaje_aprobacion=(aprobados / len(con_notas) * 100) if con_notas else None,
        )