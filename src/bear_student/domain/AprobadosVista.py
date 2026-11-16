"""Pestaña de estadísticas: aprobados, reprobados, riesgo y resumen general."""

import tkinter as tk
from tkinter import ttk

from bear_student.domain.approval import APROBADO, REPROBADO, SIN_CALIFICACIONES
from bear_student.ui.averages_view import TODOS_LOS_CURSOS, formato
from bear_student.ui.theme import C, NOTA, TEXTO
from bear_student.ui.widgets import PestanaDesplazable

MOSTRAR_TODOS = "Todos"
MOSTRAR_APROBADOS = "Aprobados"
MOSTRAR_REPROBADOS = "Reprobados"
MOSTRAR_RIESGO = "En riesgo"
OPCIONES_MOSTRAR = [MOSTRAR_TODOS, MOSTRAR_APROBADOS, MOSTRAR_REPROBADOS, MOSTRAR_RIESGO]

ETIQUETA_RIESGO = "Riesgo"


def _etiqueta(estudiante):
    """Etiqueta de color de la fila: un aprobado en riesgo se pinta en ámbar."""
    if estudiante.estado == APROBADO and estudiante.en_riesgo:
        return ETIQUETA_RIESGO
    return estudiante.estado


def _quien(estudiante):
    if estudiante is None:
        return ""
    return f"{estudiante.nombre} ({estudiante.matricula})"


class AprobacionView:
    def __init__(self, notebook, sistema_escolar, servicio_aprobacion, fuente=None):
        self.sistema = sistema_escolar
        self.aprobacion = servicio_aprobacion
        self.fuente = fuente or ("Arial", TEXTO)

        self.pestana = PestanaDesplazable(notebook, "Estadísticas")
        self.frame = self.pestana.interior
        self._crear_interfaz()
        self.actualizar()

    # ----- Construcción ----------------------------------------------------

    def _crear_interfaz(self):
        panel, cuerpo = self._crear_panel(self.frame, "Estadísticas generales")
        panel.pack(fill="x")
        barra = ttk.Frame(cuerpo, style="Panel.TFrame")
        barra.pack(fill="x", pady=(0, 8))
        ttk.Label(barra, text="Ver", style="Panel.TLabel").pack(side="left", padx=(0, 8))
        self.combo_curso = ttk.Combobox(barra, state="readonly", width=40,
                                        style="Campo.TCombobox", font=self.fuente)
        self.combo_curso.pack(side="left")
        self.combo_curso.bind("<<ComboboxSelected>>", lambda _e: self._llenar())

        tarjetas = ttk.Frame(cuerpo, style="Panel.TFrame")
        tarjetas.pack(fill="x")
        tarjetas.columnconfigure((0, 1, 2, 3), weight=1, uniform="tarjetas")
        self.t_promedio = self._crear_tarjeta(tarjetas, "Promedio general", 0)
        self.t_maxima = self._crear_tarjeta(tarjetas, "Calificación máxima", 1)
        self.t_minima = self._crear_tarjeta(tarjetas, "Calificación mínima", 2)
        self.t_aprobacion = self._crear_tarjeta(tarjetas, "Porcentaje de aprobación", 3)

        self.conteos = tk.Label(
            cuerpo, anchor="w", justify="left", font=(self.fuente[0], TEXTO, "bold"),
            padx=12, pady=8, bg=C.INFO_FONDO, fg=C.INFO_TEXTO,
        )
        self.conteos.pack(fill="x", pady=(12, 0))

        panel, cuerpo = self._crear_panel(self.frame, "Aprobados, reprobados y riesgo académico")
        panel.pack(fill="x", pady=(16, 0))
        barra = ttk.Frame(cuerpo, style="Panel.TFrame")
        barra.pack(fill="x", pady=(0, 8))
        ttk.Label(barra, text="Mostrar", style="Panel.TLabel").pack(side="left", padx=(0, 8))
        self.combo_mostrar = ttk.Combobox(barra, state="readonly", width=20, values=OPCIONES_MOSTRAR,
                                          style="Campo.TCombobox", font=self.fuente)
        self.combo_mostrar.set(MOSTRAR_TODOS)
        self.combo_mostrar.pack(side="left")
        self.combo_mostrar.bind("<<ComboboxSelected>>", lambda _e: self._llenar())

        self.tabla = self._crear_tabla(cuerpo, (
            ("matricula", "Matrícula", "w", 100),
            ("nombre", "Nombre", "w", 200),
            ("grupo", "Grupo", "w", 70),
            ("promedio", "Promedio", "e", 90),
            ("estado", "Estado", "w", 130),
            ("riesgo", "Riesgo académico", "w", 300),
        ), alto=9)
        ttk.Label(
            cuerpo,
            text=(f"Escala de 0 a 10 · se aprueba con {self.aprobacion.nota_aprobatoria:g} · "
                  f"riesgo: reprobado, aprobado con menos de "
                  f"{self.aprobacion.nota_aprobatoria + self.aprobacion.margen_riesgo:g} "
                  "o con algún curso reprobado.\n"
                  "El porcentaje de aprobación solo cuenta a quienes ya tienen calificaciones."),
            justify="left",
            style="Panel.TLabel", foreground=C.GRIS_PIZARRA, font=(self.fuente[0], NOTA),
        ).pack(anchor="w", pady=(8, 0))

    def _crear_panel(self, parent, titulo):
        panel = tk.Frame(parent, bg=C.BLANCO, highlightbackground=C.AZUL_ESCARCHA,
                         highlightthickness=1)
        ttk.Label(panel, text=titulo, style="Titulo.TLabel").pack(anchor="w", padx=16, pady=(14, 2))
        cuerpo = ttk.Frame(panel, style="Panel.TFrame", padding=(16, 8, 16, 14))
        cuerpo.pack(fill="both", expand=True)
        return panel, cuerpo

    def _crear_tarjeta(self, parent, titulo, columna):
        marco = tk.Frame(parent, bg=C.AZUL_NIEBLA, highlightbackground=C.AZUL_ESCARCHA,
                         highlightthickness=1)
        marco.grid(row=0, column=columna, sticky="nsew", padx=4)
        tk.Label(marco, text=titulo, anchor="w", bg=C.AZUL_NIEBLA, fg=C.GRIS_PIZARRA,
                 font=(self.fuente[0], NOTA)).pack(fill="x", padx=12, pady=(10, 0))
        valor = tk.Label(marco, text="—", anchor="w", bg=C.AZUL_NIEBLA, fg=C.AZUL_NOCHE,
                         font=(self.fuente[0], 18, "bold"))
        valor.pack(fill="x", padx=12)
        detalle = tk.Label(marco, text="", anchor="w", justify="left", bg=C.AZUL_NIEBLA,
                           fg=C.GRIS_PIZARRA, font=(self.fuente[0], NOTA), wraplength=170)
        detalle.pack(fill="x", padx=12, pady=(0, 10))
        return valor, detalle

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
        tabla.tag_configure(APROBADO, foreground=C.APROBADO_TEXTO)
        tabla.tag_configure(REPROBADO, foreground=C.REPROBADO_TEXTO)
        tabla.tag_configure(ETIQUETA_RIESGO, foreground=C.RIESGO_TEXTO)
        tabla.tag_configure(SIN_CALIFICACIONES, foreground=C.SIN_CALIFICACIONES_TEXTO)
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

    def actualizar(self):
        cursos = sorted(self.sistema.cursos.values(), key=lambda c: c.nombre.casefold())
        opciones = [TODOS_LOS_CURSOS] + [f"{c.clave} | {c.nombre}" for c in cursos]
        anterior = self.combo_curso.get()
        self.combo_curso["values"] = opciones
        self.combo_curso.set(anterior if anterior in opciones else TODOS_LOS_CURSOS)
        self._llenar()

    def _llenar(self):
        curso_clave = self._clave(self.combo_curso.get()) or None
        resumen = self.aprobacion.resumen(curso_clave)
        self._llenar_tarjetas(resumen)
        self._llenar_tabla(self.aprobacion.clasificar_estudiantes(curso_clave), curso_clave)

    def _llenar_tarjetas(self, r):
        valor, detalle = self.t_promedio
        valor.configure(text=formato(r.promedio_general))
        detalle.configure(text=f"{r.con_calificaciones} de {r.total_estudiantes} estudiantes con notas")

        valor, detalle = self.t_maxima
        valor.configure(text=formato(r.maxima.promedio if r.maxima else None))
        detalle.configure(text=_quien(r.maxima))

        valor, detalle = self.t_minima
        valor.configure(text=formato(r.minima.promedio if r.minima else None))
        detalle.configure(text=_quien(r.minima))

        valor, detalle = self.t_aprobacion
        valor.configure(text="—" if r.porcentaje_aprobacion is None
                        else f"{r.porcentaje_aprobacion:.1f}%")
        detalle.configure(text=f"{r.aprobados} aprobados de {r.con_calificaciones}"
                          if r.con_calificaciones else "")

        self.conteos.configure(text=(
            f"Aprobados: {r.aprobados}   ·   Reprobados: {r.reprobados}   ·   "
            f"En riesgo: {r.en_riesgo}   ·   Sin calificaciones: {r.sin_calificaciones}"
        ))

    def _llenar_tabla(self, estudiantes, curso_clave):
        filtro = self.combo_mostrar.get()
        if filtro == MOSTRAR_APROBADOS:
            estudiantes = [e for e in estudiantes if e.estado == APROBADO]
        elif filtro == MOSTRAR_REPROBADOS:
            estudiantes = [e for e in estudiantes if e.estado == REPROBADO]
        elif filtro == MOSTRAR_RIESGO:
            estudiantes = [e for e in estudiantes if e.en_riesgo]

        self.tabla.delete(*self.tabla.get_children())
        for i, e in enumerate(estudiantes):
            self.tabla.insert("", "end", tags=(("par", "impar")[i % 2], _etiqueta(e)), values=(
                e.matricula, e.nombre, e.grupo, formato(e.promedio), e.estado,
                e.motivo_riesgo if e.en_riesgo else "",
            ))
        if estudiantes:
            self.tabla.aviso_vacio.place_forget()
        else:
            if self.sistema.estudiantes and filtro == MOSTRAR_TODOS and curso_clave is not None:
                texto = "Este curso aún no tiene estudiantes inscritos."
            elif self.sistema.estudiantes:
                texto = "Ningún estudiante cumple con este filtro."
            else:
                texto = "Aún no hay estudiantes registrados."
            self.tabla.aviso_vacio.configure(text=texto)
            self.tabla.aviso_vacio.place(relx=0.5, rely=0.55, anchor="center")