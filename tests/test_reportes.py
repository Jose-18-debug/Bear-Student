"""Pruebas del servicio de reportes de Persona 5."""

import csv
import json
from pathlib import Path
import sys
import tempfile
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "src"))

from bear_student.domain.approval import ServicioAprobacion
from bear_student.domain.averages import ServicioPromedios
from bear_student.domain.evaluations import ServicioEvaluaciones
from bear_student.domain.reports import ServicioReportes
from bear_student.domain.students_courses import SistemaEscolar


class ReportesTest(unittest.TestCase):
    def setUp(self):
        self.sistema = SistemaEscolar()
        self.evaluaciones = ServicioEvaluaciones(
            lambda clave: clave in self.sistema.cursos,
            self.sistema.estudiantes_inscritos,
        )
        self.promedios = ServicioPromedios(self.sistema, self.evaluaciones)
        self.aprobacion = ServicioAprobacion(self.promedios)
        self.servicio = ServicioReportes(
            self.sistema, self.promedios, self.aprobacion
        )

        self.sistema.registrar_estudiante("A1", "Ana", "1A")
        self.sistema.registrar_estudiante("B2", "Beto", "1A")
        self.sistema.registrar_estudiante("C3", "Carla", "2B")
        self.sistema.registrar_curso("MAT", "Matemáticas")
        self.sistema.registrar_curso("HIS", "Historia")
        self.sistema.inscribir("A1", "MAT")
        self.sistema.inscribir("B2", "MAT")
        self.sistema.inscribir("A1", "HIS")
        self.sistema.inscribir("C3", "MAT")
        parcial = self.evaluaciones.registrar_evaluacion("MAT", "Parcial", 10)
        self.evaluaciones.registrar_varias_calificaciones(
            parcial.clave, {"A1": 9, "B2": 6, "C3": 8}
        )
        tarea = self.evaluaciones.registrar_evaluacion("HIS", "Tarea", 20)
        self.evaluaciones.registrar_calificacion(tarea.clave, "A1", 18)

    def test_reporte_por_grupo(self):
        reporte = self.servicio.reporte_grupo("1A")
        self.assertEqual(reporte["total_estudiantes"], 2)
        ana = reporte["estudiantes"][0]
        self.assertEqual(ana["matricula"], "A1")
        self.assertEqual(len(ana["cursos"]), 2)
        self.assertEqual(ana["promedio_final"], 9.0)
        self.assertEqual(reporte["estudiantes"][1]["estado"], "Reprobado")

    def test_reporte_por_asignatura(self):
        reporte = self.servicio.reporte_curso("MAT")
        self.assertEqual(reporte["total_inscritos"], 3)
        self.assertEqual(reporte["promedio_grupal"], 7.67)
        self.assertEqual(reporte["estudiantes"][1]["estado"], "Reprobado")

    def test_exporta_json_y_csv(self):
        reporte = self.servicio.reporte_grupo("1A")
        with tempfile.TemporaryDirectory() as carpeta:
            ruta_json = Path(carpeta) / "reporte.json"
            ruta_csv = Path(carpeta) / "reporte.csv"
            self.servicio.exportar_json(reporte, ruta_json)
            self.servicio.exportar_csv(reporte, ruta_csv)
            with ruta_json.open(encoding="utf-8") as archivo:
                datos = json.load(archivo)
            self.assertEqual(datos["grupo"], "1A")
            with ruta_csv.open(encoding="utf-8-sig", newline="") as archivo:
                filas = list(csv.DictReader(archivo))
            self.assertTrue(filas)
            self.assertEqual(filas[0]["matricula"], "A1")

    def test_rechaza_grupo_y_curso_invalidos(self):
        with self.assertRaises(ValueError):
            self.servicio.reporte_grupo("NO")
        with self.assertRaises(ValueError):
            self.servicio.reporte_curso("NO")


if __name__ == "__main__":
    unittest.main()
