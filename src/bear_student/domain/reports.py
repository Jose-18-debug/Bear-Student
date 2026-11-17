"""Generación de reportes académicos por grupo y por asignatura."""

import csv
import json
from pathlib import Path

from bear_student.domain.approval import clasificar


class ServicioReportes:
    """Construye reportes usando los servicios de promedios y aprobación."""

    def __init__(self, sistema, servicio_promedios, servicio_aprobacion):
        self.sistema = sistema
        self.promedios = servicio_promedios
        self.aprobacion = servicio_aprobacion

    def grupos_disponibles(self):
        return sorted({e.grupo for e in self.sistema.estudiantes.values()},
                      key=str.casefold)

    def reporte_grupo(self, grupo):
        grupo = (grupo or "").strip()
        if not grupo:
            raise ValueError("Selecciona un grupo.")
        estudiantes = sorted(
            (e for e in self.sistema.estudiantes.values() if e.grupo == grupo),
            key=lambda e: e.nombre.casefold(),
        )
        if not estudiantes:
            raise ValueError(f"No hay estudiantes registrados en el grupo {grupo}.")

        filas = []
        for estudiante in estudiantes:
            por_curso = self.promedios.promedios_por_curso_de_estudiante(
                estudiante.matricula
            )
            cursos = []
            cursos_inscritos = self.sistema.inscripciones.get(
                estudiante.matricula, set()
            )
            for clave in sorted(
                cursos_inscritos,
                key=lambda k: self.sistema.cursos[k].nombre.casefold()
            ):
                curso = self.sistema.cursos[clave]
                notas = self.promedios.calificaciones_de_estudiante(
                    estudiante.matricula, clave
                )
                promedio = por_curso.get(clave)
                cursos.append({
                    "clave_curso": clave,
                    "curso": curso.nombre,
                    "promedio": round(promedio, 2) if promedio is not None else None,
                    "estado": clasificar(
                        promedio, self.aprobacion.nota_aprobatoria
                    ),
                    "evaluaciones": [
                        {
                            "clave": n.evaluacion_clave,
                            "nombre": n.evaluacion_nombre,
                            "calificacion": n.nota,
                            "calificacion_maxima": n.maxima,
                            "escala_10": round(n.en_escala, 2),
                        }
                        for n in notas
                    ],
                })
            promedio_final = self.promedios.promedio_final(estudiante.matricula)
            filas.append({
                "matricula": estudiante.matricula,
                "nombre": estudiante.nombre,
                "grupo": estudiante.grupo,
                "promedio_final": (
                    round(promedio_final, 2) if promedio_final is not None else None
                ),
                "estado": clasificar(
                    promedio_final, self.aprobacion.nota_aprobatoria
                ),
                "cursos": cursos,
            })

        return {
            "tipo_reporte": "grupo",
            "grupo": grupo,
            "total_estudiantes": len(filas),
            "nota_aprobatoria": self.aprobacion.nota_aprobatoria,
            "estudiantes": filas,
        }

    def reporte_curso(self, curso_clave):
        curso = self.sistema.cursos.get((curso_clave or "").strip())
        if curso is None:
            raise ValueError("Selecciona una asignatura válida.")

        estudiantes = self.sistema.estudiantes_inscritos(curso.clave)
        filas = []
        for estudiante in estudiantes:
            promedio = self.promedios.promedio_en_curso(
                estudiante.matricula, curso.clave
            )
            notas = self.promedios.calificaciones_de_estudiante(
                estudiante.matricula, curso.clave
            )
            filas.append({
                "matricula": estudiante.matricula,
                "nombre": estudiante.nombre,
                "grupo": estudiante.grupo,
                "promedio": round(promedio, 2) if promedio is not None else None,
                "estado": clasificar(
                    promedio, self.aprobacion.nota_aprobatoria
                ),
                "evaluaciones": [
                    {
                        "clave": n.evaluacion_clave,
                        "nombre": n.evaluacion_nombre,
                        "calificacion": n.nota,
                        "calificacion_maxima": n.maxima,
                        "escala_10": round(n.en_escala, 2),
                    }
                    for n in notas
                ],
            })

        promedio_curso = self.promedios.promedio_del_curso(curso.clave)
        return {
            "tipo_reporte": "asignatura",
            "clave_curso": curso.clave,
            "asignatura": curso.nombre,
            "total_inscritos": len(filas),
            "promedio_grupal": (
                round(promedio_curso, 2) if promedio_curso is not None else None
            ),
            "nota_aprobatoria": self.aprobacion.nota_aprobatoria,
            "estudiantes": filas,
        }

    @staticmethod
    def exportar_json(reporte, ruta):
        ruta = Path(ruta)
        ruta.parent.mkdir(parents=True, exist_ok=True)
        with ruta.open("w", encoding="utf-8") as archivo:
            json.dump(reporte, archivo, ensure_ascii=False, indent=4)
        return ruta

    @staticmethod
    def exportar_csv(reporte, ruta):
        """Exporta una fila por estudiante-evaluación; conserva también filas sin notas."""
        ruta = Path(ruta)
        ruta.parent.mkdir(parents=True, exist_ok=True)
        filas = []

        for estudiante in reporte.get("estudiantes", []):
            cursos = estudiante.get("cursos")
            if cursos is None:
                cursos = [{
                    "clave_curso": reporte.get("clave_curso", ""),
                    "curso": reporte.get("asignatura", ""),
                    "promedio": estudiante.get("promedio"),
                    "estado": estudiante.get("estado", ""),
                    "evaluaciones": estudiante.get("evaluaciones", []),
                }]
            if not cursos:
                cursos = [{
                    "clave_curso": "", "curso": "", "promedio":
                    estudiante.get("promedio_final"), "estado":
                    estudiante.get("estado", ""), "evaluaciones": []
                }]

            for curso in cursos:
                evaluaciones = curso.get("evaluaciones", [])
                if not evaluaciones:
                    evaluaciones = [{}]
                for evaluacion in evaluaciones:
                    filas.append({
                        "tipo_reporte": reporte.get("tipo_reporte", ""),
                        "grupo_reporte": reporte.get("grupo", estudiante.get("grupo", "")),
                        "clave_curso": curso.get("clave_curso", ""),
                        "asignatura": curso.get("curso", ""),
                        "matricula": estudiante.get("matricula", ""),
                        "nombre": estudiante.get("nombre", ""),
                        "grupo": estudiante.get("grupo", ""),
                        "promedio": curso.get(
                            "promedio", estudiante.get("promedio_final")
                        ),
                        "estado": curso.get("estado", estudiante.get("estado", "")),
                        "evaluacion": evaluacion.get("nombre", ""),
                        "calificacion": evaluacion.get("calificacion", ""),
                        "calificacion_maxima": evaluacion.get(
                            "calificacion_maxima", ""
                        ),
                        "escala_10": evaluacion.get("escala_10", ""),
                    })

        columnas = [
            "tipo_reporte", "grupo_reporte", "clave_curso", "asignatura",
            "matricula", "nombre", "grupo", "promedio", "estado",
            "evaluacion", "calificacion", "calificacion_maxima", "escala_10",
        ]
        with ruta.open("w", newline="", encoding="utf-8-sig") as archivo:
            escritor = csv.DictWriter(archivo, fieldnames=columnas)
            escritor.writeheader()
            escritor.writerows(filas)
        return ruta
