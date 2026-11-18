"""Pruebas de la lógica de Bear Student (sin interfaz gráfica).

Ejecutar desde la carpeta del proyecto:
    python -m unittest discover -s tests -v
"""

from pathlib import Path
import sys
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "src"))

from bear_student.domain.averages import ServicioPromedios, estado_de  # noqa: E402
from bear_student.domain.evaluations import ServicioEvaluaciones  # noqa: E402
from bear_student.domain.students_courses import SistemaEscolar  # noqa: E402


def crear_escuela():
    sistema = SistemaEscolar()
    evaluaciones = ServicioEvaluaciones(
        lambda clave: clave in sistema.cursos, sistema.estudiantes_inscritos
    )
    return sistema, evaluaciones, ServicioPromedios(sistema, evaluaciones)


class EstudiantesYCursosTest(unittest.TestCase):
    def setUp(self):
        self.sistema, _, _ = crear_escuela()

    def test_registra_e_inscribe(self):
        self.sistema.registrar_estudiante(" A1 ", "Ana", "1A")
        self.sistema.registrar_curso("MAT", "Matemáticas")
        self.sistema.inscribir("A1", "MAT")
        self.assertEqual([e.matricula for e in self.sistema.estudiantes_inscritos("MAT")], ["A1"])

    def test_rechaza_duplicados_y_campos_vacios(self):
        self.sistema.registrar_estudiante("A1", "Ana", "1A")
        self.sistema.registrar_curso("MAT", "Matemáticas")
        self.sistema.inscribir("A1", "MAT")
        with self.assertRaises(ValueError):
            self.sistema.registrar_estudiante("A1", "Otra", "1B")
        with self.assertRaises(ValueError):
            self.sistema.registrar_estudiante("B2", "", "1A")
        with self.assertRaises(ValueError):
            self.sistema.registrar_curso("MAT", "Otra")
        with self.assertRaises(ValueError):
            self.sistema.inscribir("A1", "MAT")
        with self.assertRaises(ValueError):
            self.sistema.inscribir("NO", "MAT")


class EvaluacionesTest(unittest.TestCase):
    def setUp(self):
        self.sistema, self.evaluaciones, _ = crear_escuela()
        self.sistema.registrar_estudiante("A1", "Ana", "1A")
        self.sistema.registrar_estudiante("B2", "Beto", "1A")
        self.sistema.registrar_curso("MAT", "Matemáticas")
        self.sistema.inscribir("A1", "MAT")
        self.sistema.inscribir("B2", "MAT")
        self.parcial = self.evaluaciones.registrar_evaluacion("MAT", "Parcial 1", 10)

    def test_guarda_varias_calificaciones(self):
        self.evaluaciones.registrar_varias_calificaciones(self.parcial.clave, {"A1": 9, "B2": "7.5"})
        self.assertEqual(self.evaluaciones.obtener_calificaciones(self.parcial.clave),
                         {"A1": 9.0, "B2": 7.5})

    def test_acepta_coma_decimal(self):
        self.evaluaciones.registrar_calificacion(self.parcial.clave, "A1", "8,5")
        self.assertEqual(self.evaluaciones.obtener_calificaciones(self.parcial.clave)["A1"], 8.5)
        tarea = self.evaluaciones.registrar_evaluacion("MAT", "Tarea", "20,0")
        self.assertEqual(tarea.calificacion_maxima, 20.0)

    def test_no_guarda_nada_si_una_nota_es_invalida(self):
        with self.assertRaises(ValueError):
            self.evaluaciones.registrar_varias_calificaciones(self.parcial.clave, {"A1": 9, "B2": 11})
        self.assertEqual(self.evaluaciones.obtener_calificaciones(self.parcial.clave), {})

    def test_rechaza_estudiante_no_inscrito_y_texto(self):
        self.sistema.registrar_estudiante("C3", "Caro", "1B")
        with self.assertRaises(ValueError):
            self.evaluaciones.registrar_calificacion(self.parcial.clave, "C3", 8)
        with self.assertRaises(ValueError):
            self.evaluaciones.registrar_calificacion(self.parcial.clave, "A1", "ocho")
        with self.assertRaises(ValueError):
            self.evaluaciones.registrar_calificacion(self.parcial.clave, "A1", True)


class PromediosTest(unittest.TestCase):
    """Escenario: dos cursos, tres estudiantes, evaluaciones con distinta máxima."""

    def setUp(self):
        self.sistema, self.evaluaciones, self.promedios = crear_escuela()
        s, ev = self.sistema, self.evaluaciones
        s.registrar_estudiante("A1", "Ana", "1A")
        s.registrar_estudiante("B2", "Beto", "1A")
        s.registrar_estudiante("C3", "Carla", "1B")
        s.registrar_estudiante("D4", "Dani", "1B")  # sin notas
        s.registrar_curso("MAT", "Matemáticas")
        s.registrar_curso("HIS", "Historia")
        for matricula in ("A1", "B2", "C3", "D4"):
            s.inscribir(matricula, "MAT")
        s.inscribir("A1", "HIS")
        s.inscribir("C3", "HIS")

        p1 = ev.registrar_evaluacion("MAT", "Parcial 1", 10)
        p2 = ev.registrar_evaluacion("MAT", "Proyecto", 20)
        h1 = ev.registrar_evaluacion("HIS", "Ensayo", 100)
        ev.registrar_varias_calificaciones(p1.clave, {"A1": 8, "B2": 6, "C3": 9})
        ev.registrar_varias_calificaciones(p2.clave, {"A1": 20, "B2": 10})  # C3 sin calificar
        ev.registrar_varias_calificaciones(h1.clave, {"A1": 50, "C3": 70})

    def test_promedio_en_curso_normaliza_a_escala_10(self):
        # Ana: 8/10 -> 8 y 20/20 -> 10 => 9
        self.assertAlmostEqual(self.promedios.promedio_en_curso("A1", "MAT"), 9.0)
        # Beto: 6 y 10/20 -> 5 => 5.5
        self.assertAlmostEqual(self.promedios.promedio_en_curso("B2", "MAT"), 5.5)

    def test_evaluacion_sin_calificar_no_cuenta_como_cero(self):
        self.assertAlmostEqual(self.promedios.promedio_en_curso("C3", "MAT"), 9.0)

    def test_sin_notas_devuelve_none(self):
        self.assertIsNone(self.promedios.promedio_en_curso("D4", "MAT"))
        self.assertIsNone(self.promedios.promedio_final("D4"))

    def test_promedio_final_pesa_igual_cada_curso(self):
        # Ana: MAT 9 y HIS 5 => 7 ; Carla: MAT 9 y HIS 7 => 8
        self.assertAlmostEqual(self.promedios.promedio_final("A1"), 7.0)
        self.assertAlmostEqual(self.promedios.promedio_final("C3"), 8.0)
        self.assertAlmostEqual(self.promedios.promedio_final("B2"), 5.5)

    def test_promedio_por_curso(self):
        # MAT: (9 + 5.5 + 9) / 3 ; Dani no cuenta porque no tiene notas
        self.assertAlmostEqual(self.promedios.promedio_del_curso("MAT"), 23.5 / 3)
        self.assertAlmostEqual(self.promedios.promedio_del_curso("HIS"), 6.0)
        resumen = {c.clave: c for c in self.promedios.promedios_de_cursos()}
        self.assertEqual((resumen["MAT"].estudiantes_con_notas, resumen["MAT"].inscritos), (3, 4))
        self.assertEqual([c.nombre for c in self.promedios.promedios_de_cursos()],
                         ["Historia", "Matemáticas"])

    def test_curso_sin_evaluaciones(self):
        self.sistema.registrar_curso("ART", "Arte")
        self.assertIsNone(self.promedios.promedio_del_curso("ART"))
        self.assertEqual(self.promedios.ranking("ART"), [])

    def test_consulta_calificaciones_de_estudiante(self):
        notas = self.promedios.calificaciones_de_estudiante("A1")
        self.assertEqual([(n.curso_clave, n.evaluacion_nombre) for n in notas],
                         [("HIS", "Ensayo"), ("MAT", "Parcial 1"), ("MAT", "Proyecto")])
        self.assertAlmostEqual(notas[0].en_escala, 5.0)
        self.assertEqual(self.promedios.calificaciones_de_estudiante("D4"), [])
        with self.assertRaises(ValueError):
            self.promedios.calificaciones_de_estudiante("ZZ")

    def test_ranking_general(self):
        orden = [(f.nombre, f.promedio) for f in self.promedios.ranking()]
        self.assertEqual([nombre for nombre, _ in orden], ["Carla", "Ana", "Beto", "Dani"])
        self.assertIsNone(orden[-1][1])

    def test_ranking_por_curso_desempata_por_nombre(self):
        # En MAT Ana y Carla empatan con 9: va primero Ana por orden alfabético.
        orden = [f.nombre for f in self.promedios.ranking("MAT")]
        self.assertEqual(orden, ["Ana", "Carla", "Beto", "Dani"])
        self.assertEqual([f.nombre for f in self.promedios.ranking("HIS")], ["Carla", "Ana"])

    def test_estado(self):
        self.assertEqual(estado_de(None), "Sin calificaciones")
        self.assertEqual(estado_de(5.999), "Aprobado")  # se redondea a 6.00 al mostrarse
        self.assertEqual(estado_de(5.5), "Reprobado")
        self.assertEqual(self.promedios.ranking()[0].estado, "Aprobado")


if __name__ == "__main__":
    unittest.main()
