"""Pantalla para registrar evaluaciones y capturar notas individuales o por lote."""

import tkinter as tk
from tkinter import messagebox, ttk

from bear_student.domain.evaluations import ServicioEvaluaciones
from bear_student.ui.theme import (
    BOTON,
    C,
    TEXTO,
)
from bear_student.ui.widgets import BotonInvierno, PestanaDesplazable


class EvaluacionesView:
    def __init__(self, notebook, sistema_escolar, servicio=None, fuente=None):
        self.sistema_escolar = sistema_escolar
        self.servicio = servicio or ServicioEvaluaciones(
            lambda clave: clave in sistema_escolar.cursos,
            sistema_escolar.estudiantes_inscritos,
        )
        self.fuente = fuente or ("Arial", TEXTO)
        self.entradas_notas = {}
        self.clave_evaluacion_activa = ""

        # La pestaña es más alta que la ventana: se envuelve en un área con scroll.
        self.pestana = PestanaDesplazable(notebook, "Evaluaciones")
        self.frame = self.pestana.interior
        self._crear_interfaz()
        self.actualizar()

    def _crear_interfaz(self):
        formularios = ttk.Frame(self.frame)
        formularios.pack(fill="x")
        formularios.columnconfigure((0, 1), weight=1, uniform="evaluaciones")

        panel_registro, cuerpo_registro = self._crear_panel(
            formularios, "Registrar evaluación"
        )
        panel_registro.grid(row=0, column=0, sticky="nsew", padx=(0, 8))
        ttk.Label(cuerpo_registro, text="Curso", style="Panel.TLabel").grid(
            row=0, column=0, sticky="w", pady=6
        )
        self.combo_curso = ttk.Combobox(
            cuerpo_registro, state="readonly", style="Campo.TCombobox", font=self.fuente
        )
        self.combo_curso.grid(row=1, column=0, sticky="ew", pady=(0, 8))
        ttk.Label(cuerpo_registro, text="Nombre de la evaluación", style="Panel.TLabel").grid(
            row=2, column=0, sticky="w", pady=6
        )
        self.entrada_nombre = ttk.Entry(
            cuerpo_registro, style="Campo.TEntry", font=self.fuente
        )
        self.entrada_nombre.grid(row=3, column=0, sticky="ew", pady=(0, 8))
        ttk.Label(cuerpo_registro, text="Calificación máxima", style="Panel.TLabel").grid(
            row=4, column=0, sticky="w", pady=6
        )
        self.entrada_maxima = ttk.Entry(
            cuerpo_registro, style="Campo.TEntry", font=self.fuente
        )
        self.entrada_maxima.insert(0, "10")
        self.entrada_maxima.grid(row=5, column=0, sticky="ew", pady=(0, 10))
        self._boton(
            cuerpo_registro,
            "Guardar evaluación",
            self.registrar_evaluacion,
        ).grid(row=6, column=0, sticky="w")
        cuerpo_registro.columnconfigure(0, weight=1)

        panel_captura, cuerpo_captura = self._crear_panel(
            formularios, "Capturar calificaciones"
        )
        panel_captura.grid(row=0, column=1, sticky="nsew", padx=(8, 0))
        ttk.Label(cuerpo_captura, text="Evaluación", style="Panel.TLabel").pack(
            anchor="w", pady=(0, 6)
        )
        self.combo_evaluacion = ttk.Combobox(
            cuerpo_captura,
            state="readonly",
            style="Campo.TCombobox",
            font=self.fuente,
        )
        self.combo_evaluacion.pack(fill="x")
        self.combo_evaluacion.bind("<<ComboboxSelected>>", self._cambiar_evaluacion)
        self.etiqueta_curso_activo = ttk.Label(
            cuerpo_captura, text="Selecciona una evaluación para ver sus estudiantes.",
            style="Panel.TLabel", wraplength=340,
        )
        self.etiqueta_curso_activo.pack(anchor="w", pady=(12, 4))

        self.panel_estudiantes, cuerpo_estudiantes = self._crear_panel(
            self.frame, "Notas por estudiante"
        )
        self.panel_estudiantes.pack(fill="x", pady=(16, 8))
        self.cuerpo_estudiantes = cuerpo_estudiantes
        self.marco_filas = ttk.Frame(cuerpo_estudiantes, style="Panel.TFrame")
        self.canvas_filas = tk.Canvas(
            self.marco_filas,
            height=160,
            bg=C.BLANCO,
            highlightthickness=0,
            borderwidth=0,
        )
        self.barra_filas = ttk.Scrollbar(
            self.marco_filas, orient="vertical", command=self.canvas_filas.yview
        )
        self.canvas_filas.configure(yscrollcommand=self.barra_filas.set)
        self.canvas_filas.pack(side="left", fill="both", expand=True)
        self.barra_filas.pack(side="right", fill="y")
        self.contenedor_filas = ttk.Frame(self.canvas_filas, style="Panel.TFrame")
        self.ventana_filas = self.canvas_filas.create_window(
            (0, 0), window=self.contenedor_filas, anchor="nw"
        )
        self.contenedor_filas.bind("<Configure>", self._ajustar_region_filas)
        self.canvas_filas.bind("<Configure>", self._ajustar_ancho_filas)
        self.mensaje_vacio = ttk.Label(
            cuerpo_estudiantes,
            text="Registra una evaluación y asegúrate de que haya estudiantes inscritos.",
            style="Panel.TLabel",
        )
        self.mensaje_vacio.pack(anchor="w", pady=4)
        self.boton_guardar_lote = self._boton(
            cuerpo_estudiantes, "Guardar calificaciones", self.guardar_calificaciones
        )
        self.boton_guardar_lote.pack(anchor="w", pady=(12, 0))

        panel_lista, cuerpo_lista = self._crear_panel(self.frame, "Calificaciones registradas")
        panel_lista.pack(fill="both", expand=True, pady=(8, 0))
        marco_tabla = ttk.Frame(cuerpo_lista, style="Panel.TFrame")
        marco_tabla.pack(fill="both", expand=True)
        self.tabla = ttk.Treeview(
            marco_tabla,
            columns=("curso", "evaluacion", "estudiante", "calificacion"),
            show="headings",
            height=7,
        )
        encabezados = (
            ("curso", "Curso", "w", 170),
            ("evaluacion", "Evaluación", "w", 220),
            ("estudiante", "Estudiante", "w", 240),
            ("calificacion", "Calificación", "e", 120),
        )
        for clave, titulo, anclaje, ancho in encabezados:
            self.tabla.heading(clave, text=titulo, anchor=anclaje)
            self.tabla.column(clave, anchor=anclaje, width=ancho, stretch=True)
        self.tabla.tag_configure("par", background=C.BLANCO)
        self.tabla.tag_configure("impar", background=C.AZUL_NIEBLA)
        barra_tabla = ttk.Scrollbar(marco_tabla, orient="vertical", command=self.tabla.yview)
        self.tabla.configure(yscrollcommand=barra_tabla.set)
        self.tabla.pack(side="left", fill="both", expand=True)
        barra_tabla.pack(side="right", fill="y")

    def _crear_panel(self, parent, titulo):
        panel = tk.Frame(
            parent,
            bg=C.BLANCO,
            highlightbackground=C.AZUL_ESCARCHA,
            highlightthickness=1,
        )
        ttk.Label(panel, text=titulo, style="Titulo.TLabel").pack(
            anchor="w", padx=16, pady=(14, 2)
        )
        cuerpo = ttk.Frame(panel, style="Panel.TFrame", padding=(16, 8, 16, 14))
        cuerpo.pack(fill="both", expand=True)
        return panel, cuerpo

    def _boton(self, parent, texto, comando):
        return BotonInvierno(parent, texto, comando, (self.fuente[0], BOTON, "bold"))

    def actualizar(self):
        clave_curso_actual = self._clave_seleccion(self.combo_curso.get())
        cursos = self.sistema_escolar.cursos.values()
        opciones_curso = [f"{curso.clave} | {curso.nombre}" for curso in cursos]
        self.combo_curso["values"] = opciones_curso
        self._seleccionar_opcion(self.combo_curso, opciones_curso, clave_curso_actual)

        clave_anterior = self.clave_evaluacion_activa
        evaluaciones = list(self.servicio.evaluaciones.values())
        opciones_evaluacion = [
            f"{evaluacion.clave} | {evaluacion.nombre}"
            for evaluacion in evaluaciones
        ]
        self.combo_evaluacion["values"] = opciones_evaluacion
        self._seleccionar_opcion(self.combo_evaluacion, opciones_evaluacion, clave_anterior)
        self.clave_evaluacion_activa = self._clave_seleccion(self.combo_evaluacion.get())
        self._mostrar_estudiantes()
        self._actualizar_tabla()

    @staticmethod
    def _clave_seleccion(texto):
        return texto.split(" | ", 1)[0] if " | " in texto else ""

    def _seleccionar_opcion(self, combo, opciones, clave):
        for opcion in opciones:
            if self._clave_seleccion(opcion) == clave:
                combo.set(opcion)
                return
        if opciones:
            combo.current(0)
        else:
            combo.set("")

    def registrar_evaluacion(self):
        curso_clave = self._clave_seleccion(self.combo_curso.get())
        try:
            evaluacion = self.servicio.registrar_evaluacion(
                curso_clave,
                self.entrada_nombre.get(),
                self.entrada_maxima.get(),
            )
        except ValueError as error:
            messagebox.showerror("No se pudo guardar", str(error), parent=self.frame)
            return

        self.entrada_nombre.delete(0, tk.END)
        self.actualizar()
        self.combo_evaluacion.set(f"{evaluacion.clave} | {evaluacion.nombre}")
        self.clave_evaluacion_activa = evaluacion.clave
        self._mostrar_estudiantes()

    def _cambiar_evaluacion(self, _evento=None):
        self.clave_evaluacion_activa = self._clave_seleccion(self.combo_evaluacion.get())
        self._mostrar_estudiantes()

    def _mostrar_estudiantes(self):
        for fila in self.contenedor_filas.winfo_children():
            fila.destroy()
        self.entradas_notas.clear()
        self.marco_filas.pack_forget()

        evaluacion = self.servicio.evaluaciones.get(self.clave_evaluacion_activa)
        if evaluacion is None:
            self.etiqueta_curso_activo.configure(
                text="Selecciona una evaluación para ver sus estudiantes."
            )
            self.mensaje_vacio.pack(anchor="w", pady=4)
            self.boton_guardar_lote.habilitar(False)
            return

        curso = self.sistema_escolar.cursos[evaluacion.curso_clave]
        self.etiqueta_curso_activo.configure(
            text=f"{curso.nombre} · calificación máxima: "
            f"{evaluacion.calificacion_maxima:g}"
        )
        estudiantes = self.sistema_escolar.estudiantes_inscritos(evaluacion.curso_clave)
        if not estudiantes:
            self.mensaje_vacio.configure(text="Este curso aún no tiene estudiantes inscritos.")
            self.mensaje_vacio.pack(anchor="w", pady=4)
            self.boton_guardar_lote.habilitar(False)
            return

        self.mensaje_vacio.pack_forget()
        self.marco_filas.pack(fill="x")
        ttk.Label(
            self.contenedor_filas, text="Estudiante", style="Panel.TLabel"
        ).grid(row=0, column=0, sticky="w", pady=(0, 6))
        ttk.Label(
            self.contenedor_filas, text="Calificación", style="Panel.TLabel"
        ).grid(row=0, column=1, sticky="e", padx=(12, 0), pady=(0, 6))
        notas_guardadas = self.servicio.obtener_calificaciones(evaluacion.clave)
        for fila, estudiante in enumerate(estudiantes, start=1):
            ttk.Label(
                self.contenedor_filas,
                text=f"{estudiante.nombre} · {estudiante.matricula}",
                style="Panel.TLabel",
            ).grid(row=fila, column=0, sticky="w", pady=3)
            entrada = ttk.Entry(
                self.contenedor_filas,
                width=12,
                justify="right",
                style="Campo.TEntry",
                font=self.fuente,
            )
            if estudiante.matricula in notas_guardadas:
                entrada.insert(0, f"{notas_guardadas[estudiante.matricula]:g}")
            entrada.grid(row=fila, column=1, sticky="e", padx=(12, 0), pady=3)
            self.entradas_notas[estudiante.matricula] = entrada
        self.contenedor_filas.columnconfigure(0, weight=1)
        self.boton_guardar_lote.pack_forget()
        self.boton_guardar_lote.pack(anchor="w", pady=(12, 0))
        self.boton_guardar_lote.habilitar(True)

    def _ajustar_region_filas(self, _evento=None):
        self.canvas_filas.configure(scrollregion=self.canvas_filas.bbox("all"))

    def _ajustar_ancho_filas(self, evento):
        self.canvas_filas.itemconfigure(self.ventana_filas, width=evento.width)

    def guardar_calificaciones(self):
        notas = {
            matricula: entrada.get().strip()
            for matricula, entrada in self.entradas_notas.items()
            if entrada.get().strip()
        }
        try:
            self.servicio.registrar_varias_calificaciones(
                self.clave_evaluacion_activa, notas
            )
        except ValueError as error:
            messagebox.showerror("No se guardaron las calificaciones", str(error), parent=self.frame)
            return

        self._actualizar_tabla()
        messagebox.showinfo(
            "Listo",
            f"Se guardaron {len(notas)} calificación(es).",
            parent=self.frame,
        )

    def _actualizar_tabla(self):
        self.tabla.delete(*self.tabla.get_children())
        numero_fila = 0
        for clave, evaluacion in self.servicio.evaluaciones.items():
            curso = self.sistema_escolar.cursos.get(evaluacion.curso_clave)
            if curso is None:
                continue
            notas = self.servicio.obtener_calificaciones(clave)
            for matricula, nota in notas.items():
                estudiante = self.sistema_escolar.estudiantes.get(matricula)
                if estudiante is None:
                    continue
                self.tabla.insert(
                    "",
                    "end",
                    values=(
                        curso.nombre,
                        evaluacion.nombre,
                        estudiante.nombre,
                        f"{nota:.2f} / {evaluacion.calificacion_maxima:.2f}",
                    ),
                    tags=(("par", "impar")[numero_fila % 2],),
                )
                numero_fila += 1
