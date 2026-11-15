"""Prueba de humo de la ventana: se construye y la pestaña Promedios muestra datos."""

from pathlib import Path
import sys
import tkinter as tk
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "src"))


class PestanaPromediosTest(unittest.TestCase):
    def setUp(self):
        try:
            from bear_student.main import Aplicacion
            self.app = Aplicacion()
        except tk.TclError as error:  # equipo sin pantalla
            self.skipTest(f"No hay pantalla disponible: {error}")
        self.app.withdraw()

    def tearDown(self):
        self.app.destroy()

    def test_ranking_consulta_y_columna_de_cursos(self):
        app = self.app
        s, ev = app.sistema, app.servicio_evaluaciones
        s.registrar_estudiante("A1", "Ana", "1A")
        s.registrar_estudiante("B2", "Beto", "1A")
        s.registrar_curso("MAT", "Matemáticas")
        s.inscribir("A1", "MAT")
        s.inscribir("B2", "MAT")
        parcial = ev.registrar_evaluacion("MAT", "Parcial", 10)
        ev.registrar_varias_calificaciones(parcial.clave, {"A1": 6, "B2": 9})
        app.actualizar()

        pestanas = [app.pestanas.tab(t, "text") for t in app.pestanas.tabs()]
        self.assertIn("Promedios", pestanas)

        vista = app.vista_promedios
        ranking = [vista.tabla_ranking.item(i, "values") for i in vista.tabla_ranking.get_children()]
        self.assertEqual([fila[2] for fila in ranking], ["Beto", "Ana"])
        self.assertEqual(ranking[0][4], "9.00")

        # Al seleccionar en el ranking se carga la consulta de ese estudiante.
        # (Con la ventana oculta Tk no entrega <<TreeviewSelect>>, así que se
        # llama directamente al manejador que se ejecuta al seleccionar.)
        vista.tabla_ranking.selection_set(vista.tabla_ranking.get_children()[0])
        vista._consultar_desde_ranking()
        self.assertTrue(vista.combo_estudiante.get().startswith("B2 |"))
        self.assertIn("Promedio final: 9.00", vista.resumen_estudiante.cget("text"))

        cursos = [app.tabla_cur.item(i, "values") for i in app.tabla_cur.get_children()]
        self.assertEqual(cursos[0][3], "7.50")

    def test_notas_nuevas_se_ven_al_cambiar_de_pestana(self):
        app = self.app
        s, ev = app.sistema, app.servicio_evaluaciones
        s.registrar_estudiante("A1", "Ana", "1A")
        s.registrar_curso("MAT", "Matemáticas")
        s.inscribir("A1", "MAT")
        app.actualizar()
        parcial = ev.registrar_evaluacion("MAT", "Parcial", 10)
        ev.registrar_calificacion(parcial.clave, "A1", 8)  # como si se guardara en Evaluaciones

        app.pestanas.select(len(app.pestanas.tabs()) - 1)
        app.update()
        fila = app.vista_promedios.tabla_ranking.get_children()[0]
        self.assertEqual(app.vista_promedios.tabla_ranking.item(fila, "values")[4], "8.00")


if __name__ == "__main__":
    unittest.main()
