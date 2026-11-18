"""Pruebas de aprobación y estadísticas (Persona 4).

Ejecutar desde la carpeta del proyecto:
    python -m unittest discover -s tests -v
"""

from pathlib import Path
import sys
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "src"))

from bear_student.domain.approval import (  # noqa: E402
    APROBADO, REPROBADO, SIN_CALIFICACIONES, ServicioAprobacion, clasificar,
)
from bear_student.domain.averages import ServicioPromedios  # noqa: E402
from bear_student.domain.evaluations import ServicioEvaluaciones  # noqa: E402
from bear_student.domain.students_courses import SistemaEscolar  # noqa: E402


def crear_escuela():
    """Ana 9, Beto 7.2, Caro 6, Dani sin notas, Eli (MAT 10 y ESP 5 = 7.5)."""
    sistema = SistemaEscolar()
    evaluaciones = ServicioEvaluaciones(
        lambda clave: clave in sistema.cursos, sistema.estudiantes_inscritos
    )
    promedios = ServicioPromedios(sistema, evaluaciones)
    for matricula, nombre in (("A1", "Ana"), ("B2", "Beto"), ("C3", "Caro"),
                              ("D4", "Dani"), ("E5", "Eli")):
        sistema.registrar_estudiante(matricula, nombre, "1A")
    sistema.registrar_curso("MAT", "Matemáticas")
    sistema.registrar_curso("ESP", "Español")
    for matricula in ("A1", "B2", "C3", "D4", "E5"):
        sistema.inscribir(matricula, "MAT")
    sistema.inscribir("E5", "ESP")
    mat = evaluaciones.registrar_evaluacion("MAT", "Parcial", 10)
    evaluaciones.registrar_varias_calificaciones(
        mat.clave, {"A1": 9, "B2": 7.2, "C3": 6, "E5": 10})
    esp = evaluaciones.registrar_evaluacion("ESP", "Parcial", 10)
    evaluaciones.registrar_calificacion(esp.clave, "E5", 5)
    return sistema, evaluaciones, promedios


class ClasificarTest(unittest.TestCase):
    def test_umbral_de_siete(self):
        self.assertEqual(clasificar(7.0), APROBADO)
        self.assertEqual(clasificar(6.99), REPROBADO)
        self.assertEqual(clasificar(None), SIN_CALIFICACIONES)

    def test_usa_el_mismo_redondeo_que_la_pantalla(self):
        self.assertEqual(clasificar(6.996), APROBADO)  # se muestra como 7.00
        self.assertEqual(clasificar(6.994), REPROBADO)  # se muestra como 6.99


class ServicioAprobacionTest(unittest.TestCase):
    def setUp(self):
        _, _, promedios = crear_escuela()
        self.servicio = ServicioAprobacion(promedios)

    def test_clasifica_aprobados_y_reprobados(self):
        self.assertEqual([e.nombre for e in self.servicio.aprobados()], ["Ana", "Eli", "Beto"])
        self.assertEqual([e.nombre for e in self.servicio.reprobados()], ["Caro"])

    def test_sin_calificaciones_no_es_aprobado_ni_reprobado(self):
        dani = [e for e in self.servicio.clasificar_estudiantes() if e.nombre == "Dani"][0]
        self.assertEqual(dani.estado, SIN_CALIFICACIONES)
        self.assertFalse(dani.en_riesgo)

    def test_detecta_riesgo(self):
        riesgo = {e.nombre: e.motivo_riesgo for e in self.servicio.en_riesgo()}
        self.assertEqual(set(riesgo), {"Beto", "Caro", "Eli"})
        self.assertIn("por poco", riesgo["Beto"])          # 7.2 < 7.5
        self.assertIn("menor a 7", riesgo["Caro"])          # 6 < 7
        self.assertIn("Español", riesgo["Eli"])             # 7.5 final, pero ESP = 5
        self.assertNotIn("por poco", riesgo["Eli"])         # 7.5 ya no es "por poco"

    def test_estadisticas_generales(self):
        r = self.servicio.resumen()
        self.assertEqual((r.total_estudiantes, r.aprobados, r.reprobados, r.sin_calificaciones),
                         (5, 3, 1, 1))
        self.assertEqual(r.en_riesgo, 3)
        self.assertAlmostEqual(r.promedio_general, (9 + 7.2 + 6 + 7.5) / 4)
        self.assertEqual((r.maxima.nombre, r.maxima.promedio), ("Ana", 9.0))
        self.assertEqual((r.minima.nombre, r.minima.promedio), ("Caro", 6.0))
        self.assertAlmostEqual(r.porcentaje_aprobacion, 75.0)  # 3 de 4 con notas

    def test_estadisticas_de_un_curso(self):
        r = self.servicio.resumen("MAT")
        self.assertEqual((r.total_estudiantes, r.aprobados, r.reprobados, r.sin_calificaciones),
                         (5, 3, 1, 1))
        self.assertAlmostEqual(r.promedio_general, (9 + 7.2 + 6 + 10) / 4)
        self.assertEqual(r.maxima.nombre, "Eli")
        # En un curso no se revisan "otros cursos reprobados".
        riesgo = {e.nombre for e in self.servicio.en_riesgo("MAT")}
        self.assertEqual(riesgo, {"Beto", "Caro"})

    def test_sin_datos(self):
        sistema = SistemaEscolar()
        evaluaciones = ServicioEvaluaciones(
            lambda clave: clave in sistema.cursos, sistema.estudiantes_inscritos)
        r = ServicioAprobacion(ServicioPromedios(sistema, evaluaciones)).resumen()
        self.assertEqual((r.total_estudiantes, r.aprobados, r.reprobados), (0, 0, 0))
        self.assertIsNone(r.promedio_general)
        self.assertIsNone(r.maxima)
        self.assertIsNone(r.minima)
        self.assertIsNone(r.porcentaje_aprobacion)

    def test_curso_invalido_y_parametros_invalidos(self):
        with self.assertRaises(ValueError):
            self.servicio.resumen("NO")
        _, _, promedios = crear_escuela()
        with self.assertRaises(ValueError):
            ServicioAprobacion(promedios, nota_aprobatoria=0)
        with self.assertRaises(ValueError):
            ServicioAprobacion(promedios, margen_riesgo=-1)


class PestanaEstadisticasTest(unittest.TestCase):
    def setUp(self):
        try:
            import tkinter as tk
            from bear_student.main import Aplicacion
            self.app = Aplicacion()
        except ImportError as error:  # Python sin Tkinter
            self.skipTest(f"Tkinter no está disponible: {error}")
        except tk.TclError as error:  # equipo sin pantalla
            self.skipTest(f"No hay pantalla disponible: {error}")
        self.app.withdraw()

    def tearDown(self):
        self.app.destroy()

    def test_tarjetas_y_tabla(self):
        app = self.app
        s, ev = app.sistema, app.servicio_evaluaciones
        s.registrar_estudiante("A1", "Ana", "1A")
        s.registrar_estudiante("B2", "Beto", "1A")
        s.registrar_curso("MAT", "Matemáticas")
        s.inscribir("A1", "MAT")
        s.inscribir("B2", "MAT")
        parcial = ev.registrar_evaluacion("MAT", "Parcial", 10)
        ev.registrar_varias_calificaciones(parcial.clave, {"A1": 9, "B2": 6})
        app.actualizar()

        pestanas = [app.pestanas.tab(t, "text") for t in app.pestanas.tabs()]
        self.assertIn("Estadísticas", pestanas)

        vista = app.vista_estadisticas
        self.assertEqual(vista.t_promedio[0].cget("text"), "7.50")
        self.assertEqual(vista.t_maxima[0].cget("text"), "9.00")
        self.assertEqual(vista.t_minima[0].cget("text"), "6.00")
        self.assertEqual(vista.t_aprobacion[0].cget("text"), "50.0%")

        filas = [vista.tabla.item(i, "values") for i in vista.tabla.get_children()]
        self.assertEqual([f[1] for f in filas], ["Ana", "Beto"])
        self.assertEqual([f[4] for f in filas], ["Aprobado", "Reprobado"])

        vista.combo_mostrar.set("Reprobados")
        vista._llenar()
        filas = [vista.tabla.item(i, "values") for i in vista.tabla.get_children()]
        self.assertEqual([f[1] for f in filas], ["Beto"])


if __name__ == "__main__":
    unittest.main()