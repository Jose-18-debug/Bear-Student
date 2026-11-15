"""Pestaña de promedios: ranking de estudiantes y consulta de calificaciones."""

import tkinter as tk
from tkinter import ttk

from bear_student.domain.averages import CALIFICACION_APROBATORIA, estado_de
from bear_student.ui.theme import C, NOTA, TEXTO
from bear_student.ui.widgets import PestanaDesplazable

TODOS_LOS_CURSOS = "Todos los cursos (promedio final)"


def formato(promedio):
    return "—" if promedio is None else f"{promedio:.2f}"


class PromediosView:
    def __init__(self, notebook, sistema_escolar, servicio_promedios, fuente=None):
        self.sistema = sistema_escolar
        self.promedios = servicio_promedios
        self.fuente = fuente or ("Arial", TEXTO)

        self.pestana = PestanaDesplazable(notebook, "Promedios")
        self.frame = self.pestana.interior
        self._crear_interfaz()
        self.actualizar()

    # ----- Construcción ----------------------------------------------------

    def _crear_interfaz(self):
        panel, cuerpo = self._crear_panel(self.frame, "Ranking por promedio")
        panel.pack(fill="x")
        barra = ttk.Frame(cuerpo, style="Panel.TFrame")
        barra.pack(fill="x", pady=(0, 8))
        ttk.Label(barra, text="Ver", style="Panel.TLabel").pack(side="left", padx=(0, 8))
        self.combo_curso = ttk.Combobox(barra, state="readonly", width=40,
                                        style="Campo.TCombobox", font=self.fuente)
        self.combo_curso.pack(side="left")
        self.combo_curso.bind("<<ComboboxSelected>>", lambda _e: self._llenar_ranking())
        self.resumen_ranking = ttk.Label(barra, style="Panel.TLabel")
        self.resumen_ranking.pack(side="right")

        self.tabla_ranking = self._crear_tabla(cuerpo, (
            ("posicion", "#", "center", 50),
            ("matricula", "Matrícula", "w", 110),
            ("nombre", "Nombre", "w", 240),
            ("grupo", "Grupo", "w", 80),
            ("promedio", "Promedio", "e", 100),
            ("estado", "Estado", "w", 150),
        ), alto=8)
        self.tabla_ranking.bind("<<TreeviewSelect>>", self._consultar_desde_ranking)
        ttk.Label(
            cuerpo,
            text=f"Escala de 0 a 10 · se aprueba con {CALIFICACION_APROBATORIA:g} · "
                 "las evaluaciones sin calificar no cuentan.\n"
                 "Selecciona un estudiante para ver sus notas abajo.",
            justify="left",
            style="Panel.TLabel", foreground=C.GRIS_PIZARRA, font=(self.fuente[0], NOTA),
        ).pack(anchor="w", pady=(8, 0))

        panel, cuerpo = self._crear_panel(self.frame, "Consultar calificaciones de un estudiante")
        panel.pack(fill="x", pady=(16, 0))
        barra = ttk.Frame(cuerpo, style="Panel.TFrame")
        barra.pack(fill="x", pady=(0, 8))
        ttk.Label(barra, text="Estudiante", style="Panel.TLabel").pack(side="left", padx=(0, 8))
        self.combo_estudiante = ttk.Combobox(barra, state="readonly", width=40,
                                             style="Campo.TCombobox", font=self.fuente)
        self.combo_estudiante.pack(side="left")
        self.combo_estudiante.bind("<<ComboboxSelected>>", lambda _e: self._llenar_consulta())

        self.resumen_estudiante = tk.Label(
            cuerpo, anchor="w", justify="left", font=(self.fuente[0], TEXTO, "bold"),
            padx=12, pady=8, bg=C.SIN_CALIFICACIONES_FONDO, fg=C.SIN_CALIFICACIONES_TEXTO,
        )
        self.resumen_estudiante.pack(fill="x", pady=(0, 8))
        self.tabla_consulta = self._crear_tabla(cuerpo, (
            ("curso", "Curso", "w", 200),
            ("evaluacion", "Evaluación", "w", 220),
            ("nota", "Calificación", "e", 130),
            ("escala", "Sobre 10", "e", 100),
        ), alto=6)

    def _crear_panel(self, parent, titulo):
        panel = tk.Frame(parent, bg=C.BLANCO, highlightbackground=C.AZUL_ESCARCHA,
                         highlightthickness=1)
        ttk.Label(panel, text=titulo, style="Titulo.TLabel").pack(anchor="w", padx=16, pady=(14, 2))
        cuerpo = ttk.Frame(panel, style="Panel.TFrame", padding=(16, 8, 16, 14))
        cuerpo.pack(fill="both", expand=True)
        return panel, cuerpo

    def _crear_tabla(self, parent, columnas, alto):
        marco = ttk.Frame(parent, style="Panel.TFrame")
        marco.pack(fill="x")
        tabla = ttk.Treeview(marco, columns=[c[0] for c in columnas], show="headings",
                             height=alto, selectmode="browse")
        for clave, titulo, anclaje, ancho in columnas:
            tabla.heading(clave, text=titulo, anchor=anclaje)
            tabla.column(clave, anchor=anclaje, width=ancho, stretch=True)
        tabla.tag_configure("par", background=C.BLANCO)
        tabla.tag_configure("impar", background=C.AZUL_NIEBLA)
        tabla.tag_configure("Aprobado", foreground=C.APROBADO_TEXTO)
        tabla.tag_configure("Reprobado", foreground=C.REPROBADO_TEXTO)
        tabla.tag_configure("Sin calificaciones", foreground=C.SIN_CALIFICACIONES_TEXTO)
        barra = ttk.Scrollbar(marco, orient="vertical", command=tabla.yview)
        tabla.configure(yscrollcommand=barra.set)
        tabla.pack(side="left", fill="x", expand=True)
        barra.pack(side="right", fill="y")
        tabla.aviso_vacio = tk.Label(tabla, bg=C.BLANCO, fg=C.GRIS_PIZARRA, font=self.fuente)
        return tabla

    # ----- Datos -----------------------------------------------------------

    @staticmethod
    def _clave(texto):
        return texto.split(" | ", 1)[0] if " | " in texto else ""

    @staticmethod
    def _mantener_seleccion(combo, opciones, anterior, por_defecto=""):
        combo["values"] = opciones
        if anterior in opciones:
            combo.set(anterior)
        elif por_defecto:
            combo.set(por_defecto)
        else:
            combo.set(opciones[0] if opciones else "")

    def actualizar(self):
        cursos = sorted(self.sistema.cursos.values(), key=lambda c: c.nombre.casefold())
        self._mantener_seleccion(
            self.combo_curso,
            [TODOS_LOS_CURSOS] + [f"{c.clave} | {c.nombre}" for c in cursos],
            self.combo_curso.get(), TODOS_LOS_CURSOS,
        )
        estudiantes = sorted(self.sistema.estudiantes.values(), key=lambda e: e.nombre.casefold())
        self._mantener_seleccion(
            self.combo_estudiante,
            [f"{e.matricula} | {e.nombre} ({e.grupo})" for e in estudiantes],
            self.combo_estudiante.get(),
        )
        self._llenar_ranking()
        self._llenar_consulta()

    def _llenar(self, tabla, filas, vacio):
        tabla.delete(*tabla.get_children())
        for i, (valores, estado) in enumerate(filas):
            tabla.insert("", "end", values=valores, tags=(("par", "impar")[i % 2], estado))
        if filas:
            tabla.aviso_vacio.place_forget()
        else:
            tabla.aviso_vacio.configure(text=vacio)
            tabla.aviso_vacio.place(relx=0.5, rely=0.55, anchor="center")

    def _llenar_ranking(self):
        curso_clave = self._clave(self.combo_curso.get()) or None
        ranking = self.promedios.ranking(curso_clave)
        filas = []
        for posicion, fila in enumerate(ranking, start=1):
            filas.append(((
                posicion if fila.promedio is not None else "",
                fila.matricula, fila.nombre, fila.grupo,
                formato(fila.promedio), fila.estado,
            ), fila.estado))
        vacio = ("Aún no hay estudiantes registrados." if curso_clave is None
                 else "Este curso aún no tiene estudiantes inscritos.")
        self._llenar(self.tabla_ranking, filas, vacio)

        if curso_clave is None:
            con_notas = [f.promedio for f in ranking if f.promedio is not None]
            general = sum(con_notas) / len(con_notas) if con_notas else None
            self.resumen_ranking.configure(text=f"Promedio general: {formato(general)}")
        else:
            curso = self.promedios.promedio_del_curso(curso_clave)
            self.resumen_ranking.configure(text=f"Promedio del curso: {formato(curso)}")

    def _consultar_desde_ranking(self, _evento=None):
        seleccion = self.tabla_ranking.selection()
        if not seleccion:
            return
        matricula = str(self.tabla_ranking.item(seleccion[0], "values")[1])
        for opcion in self.combo_estudiante["values"]:
            if self._clave(opcion) == matricula:
                self.combo_estudiante.set(opcion)
                self._llenar_consulta()
                return

    def _llenar_consulta(self):
        matricula = self._clave(self.combo_estudiante.get())
        if not matricula:
            self._pintar_resumen("Registra un estudiante para consultar sus calificaciones.", None)
            self._llenar(self.tabla_consulta, [], "")
            return

        notas = self.promedios.calificaciones_de_estudiante(matricula)
        por_curso = self.promedios.promedios_por_curso_de_estudiante(matricula)
        final = self.promedios.promedio_final(matricula)
        if final is None:
            texto = "Promedio final: —   ·   Sin calificaciones todavía"
        else:
            detalle = "   ·   ".join(
                f"{self.sistema.cursos[clave].nombre}: {formato(promedio)}"
                for clave, promedio in sorted(
                    por_curso.items(), key=lambda par: self.sistema.cursos[par[0]].nombre.casefold()
                )
            )
            texto = f"Promedio final: {formato(final)}  ({estado_de(final)})\n{detalle}"
        self._pintar_resumen(texto, final)

        filas = [((
            n.curso_nombre, n.evaluacion_nombre,
            f"{n.nota:g} / {n.maxima:g}", f"{n.en_escala:.2f}",
        ), estado_de(n.en_escala)) for n in notas]
        self._llenar(self.tabla_consulta, filas,
                     "Este estudiante aún no tiene calificaciones capturadas.")

    def _pintar_resumen(self, texto, promedio):
        estado = estado_de(promedio)
        colores = {
            "Aprobado": (C.APROBADO_FONDO, C.APROBADO_TEXTO),
            "Reprobado": (C.REPROBADO_FONDO, C.REPROBADO_TEXTO),
        }.get(estado, (C.SIN_CALIFICACIONES_FONDO, C.SIN_CALIFICACIONES_TEXTO))
        self.resumen_estudiante.configure(text=texto, bg=colores[0], fg=colores[1])
