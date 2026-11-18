"""Pestaña para consultar y exportar reportes académicos."""

import tkinter as tk
from tkinter import filedialog, messagebox, ttk

from bear_student.ui.theme import C, TEXTO
from bear_student.ui.widgets import BotonInvierno, PestanaDesplazable


class ReportesView:
    def __init__(self, notebook, sistema, servicio_reportes, fuente=None):
        self.sistema = sistema
        self.reportes = servicio_reportes
        self.fuente = fuente or ("Arial", TEXTO)
        self.reporte_actual = None
        self.pestana = PestanaDesplazable(notebook, "Reportes")
        self.frame = self.pestana.interior
        self._crear_interfaz()
        self.actualizar()

    def _crear_interfaz(self):
        panel = tk.Frame(
            self.frame, bg=C.BLANCO,
            highlightbackground=C.AZUL_ESCARCHA, highlightthickness=1
        )
        panel.pack(fill="x")
        ttk.Label(
            panel, text="Generación de reportes", style="Titulo.TLabel"
        ).pack(anchor="w", padx=16, pady=(14, 4))
        cuerpo = ttk.Frame(panel, style="Panel.TFrame", padding=16)
        cuerpo.pack(fill="x")

        ttk.Label(cuerpo, text="Tipo de reporte", style="Panel.TLabel").grid(
            row=0, column=0, sticky="w", padx=(0, 8)
        )
        self.combo_tipo = ttk.Combobox(
            cuerpo, state="readonly", values=("Por grupo", "Por asignatura"),
            width=20, font=self.fuente
        )
        self.combo_tipo.set("Por grupo")
        self.combo_tipo.grid(row=1, column=0, sticky="ew", padx=(0, 12))
        self.combo_tipo.bind("<<ComboboxSelected>>", self._cambiar_tipo)

        ttk.Label(cuerpo, text="Grupo o asignatura", style="Panel.TLabel").grid(
            row=0, column=1, sticky="w"
        )
        self.combo_filtro = ttk.Combobox(
            cuerpo, state="readonly", width=35, font=self.fuente
        )
        self.combo_filtro.grid(row=1, column=1, sticky="ew")
        cuerpo.columnconfigure(1, weight=1)

        self.boton_generar = BotonInvierno(
            cuerpo, "Generar reporte", self.generar, self.fuente
        )
        self.boton_generar.grid(row=1, column=2, padx=(12, 0))

        barra = ttk.Frame(cuerpo, style="Panel.TFrame")
        barra.grid(row=2, column=0, columnspan=3, sticky="w", pady=(14, 0))
        self.boton_json = BotonInvierno(
            barra, "Exportar JSON", lambda: self.exportar("json"),
            self.fuente, primario=True
        )
        self.boton_json.pack(side="left")
        self.boton_csv = BotonInvierno(
            barra, "Exportar CSV", lambda: self.exportar("csv"),
            self.fuente, primario=True
        )
        self.boton_csv.pack(side="left", padx=(8, 0))
        self._habilitar_exportacion(False)

        self.resumen = tk.Label(
            cuerpo, text="Selecciona un grupo o asignatura y genera el reporte.",
            bg=C.INFO_FONDO, fg=C.INFO_TEXTO, anchor="w", justify="left",
            padx=12, pady=10, font=self.fuente, wraplength=800
        )
        self.resumen.grid(row=3, column=0, columnspan=3, sticky="ew", pady=(14, 0))

        panel_tabla = tk.Frame(
            self.frame, bg=C.BLANCO,
            highlightbackground=C.AZUL_ESCARCHA, highlightthickness=1
        )
        panel_tabla.pack(fill="both", expand=True, pady=(14, 0))
        ttk.Label(
            panel_tabla, text="Detalle del reporte", style="Titulo.TLabel"
        ).pack(anchor="w", padx=16, pady=(14, 8))
        marco = ttk.Frame(panel_tabla, style="Panel.TFrame", padding=(12, 0, 12, 12))
        marco.pack(fill="both", expand=True)
        columnas = (
            ("matricula", "Matrícula", 110, "w"),
            ("nombre", "Estudiante", 190, "w"),
            ("grupo", "Grupo", 75, "w"),
            ("curso", "Asignatura", 180, "w"),
            ("promedio", "Promedio", 90, "e"),
            ("estado", "Estado", 130, "w"),
            ("evaluacion", "Evaluación", 160, "w"),
            ("nota", "Calificación", 110, "e"),
        )
        self.tabla = ttk.Treeview(
            marco, columns=[c[0] for c in columnas], show="headings", height=12
        )
        for clave, titulo, ancho, anclaje in columnas:
            self.tabla.heading(clave, text=titulo, anchor=anclaje)
            self.tabla.column(clave, width=ancho, anchor=anclaje, stretch=True)
        self.tabla.tag_configure("par", background=C.BLANCO)
        self.tabla.tag_configure("impar", background=C.AZUL_NIEBLA)
        scroll = ttk.Scrollbar(marco, orient="vertical", command=self.tabla.yview)
        self.tabla.configure(yscrollcommand=scroll.set)
        self.tabla.pack(side="left", fill="both", expand=True)
        scroll.pack(side="right", fill="y")

    def _habilitar_exportacion(self, habilitar):
        estado = "normal" if habilitar else "disabled"
        self.boton_json.configure(state=estado)
        self.boton_csv.configure(state=estado)

    def _cambiar_tipo(self, _evento=None):
        self.reporte_actual = None
        self._habilitar_exportacion(False)
        self._llenar_opciones()
        self.tabla.delete(*self.tabla.get_children())

    def _llenar_opciones(self):
        if self.combo_tipo.get() == "Por grupo":
            opciones = self.reportes.grupos_disponibles()
        else:
            cursos = sorted(
                self.sistema.cursos.values(), key=lambda c: c.nombre.casefold()
            )
            opciones = [f"{c.clave} | {c.nombre}" for c in cursos]
        self.combo_filtro["values"] = opciones
        self.combo_filtro.set(opciones[0] if opciones else "")

    def actualizar(self):
        anterior = self.combo_filtro.get()
        self._llenar_opciones()
        opciones = list(self.combo_filtro["values"])
        if anterior in opciones:
            self.combo_filtro.set(anterior)
        self.reporte_actual = None
        self._habilitar_exportacion(False)

    def generar(self):
        try:
            seleccion = self.combo_filtro.get().strip()
            if self.combo_tipo.get() == "Por grupo":
                self.reporte_actual = self.reportes.reporte_grupo(seleccion)
                self.resumen.configure(
                    text=f"Reporte del grupo {seleccion} · "
                         f"{self.reporte_actual['total_estudiantes']} estudiante(s)"
                )
            else:
                clave = seleccion.split(" | ", 1)[0]
                self.reporte_actual = self.reportes.reporte_curso(clave)
                self.resumen.configure(
                    text=f"Reporte de {self.reporte_actual['asignatura']} · "
                         f"{self.reporte_actual['total_inscritos']} estudiante(s) inscrito(s) · "
                         f"Promedio del curso: {self.reporte_actual['promedio_grupal']}"
                )
            self._mostrar_filas()
            self._habilitar_exportacion(True)
        except ValueError as error:
            self.reporte_actual = None
            self._habilitar_exportacion(False)
            messagebox.showwarning("No se pudo generar el reporte", str(error), parent=self.frame)

    def _mostrar_filas(self):
        self.tabla.delete(*self.tabla.get_children())
        numero = 0
        for estudiante in self.reporte_actual["estudiantes"]:
            cursos = estudiante.get("cursos")
            if cursos is None:
                cursos = [{
                    "curso": self.reporte_actual.get("asignatura", ""),
                    "promedio": estudiante.get("promedio"),
                    "estado": estudiante.get("estado", ""),
                    "evaluaciones": estudiante.get("evaluaciones", []),
                }]
            if not cursos:
                cursos = [{"curso": "", "promedio": estudiante.get("promedio_final"),
                           "estado": estudiante.get("estado", ""), "evaluaciones": []}]
            for curso in cursos:
                evaluaciones = curso.get("evaluaciones", []) or [{}]
                for evaluacion in evaluaciones:
                    self.tabla.insert(
                        "", "end",
                        values=(
                            estudiante.get("matricula", ""),
                            estudiante.get("nombre", ""),
                            estudiante.get("grupo", ""),
                            curso.get("curso", ""),
                            "—" if curso.get("promedio") is None else f"{curso['promedio']:.2f}",
                            curso.get("estado", estudiante.get("estado", "")),
                            evaluacion.get("nombre", "Sin calificaciones"),
                            (
                                f"{evaluacion['calificacion']:g} / "
                                f"{evaluacion['calificacion_maxima']:g}"
                                if evaluacion.get("calificacion") is not None else "—"
                            ),
                        ),
                        tags=(("par", "impar")[numero % 2],)
                    )
                    numero += 1

    def exportar(self, formato):
        if not self.reporte_actual:
            return
        if formato == "json":
            extension = ".json"
            tipos = [("Archivo JSON", "*.json")]
            nombre = "reporte_academico.json"
        else:
            extension = ".csv"
            tipos = [("Archivo CSV", "*.csv")]
            nombre = "reporte_academico.csv"
        ruta = filedialog.asksaveasfilename(
            parent=self.frame, defaultextension=extension, initialfile=nombre,
            filetypes=tipos, title=f"Guardar reporte {formato.upper()}"
        )
        if not ruta:
            return
        try:
            if formato == "json":
                self.reportes.exportar_json(self.reporte_actual, ruta)
            else:
                self.reportes.exportar_csv(self.reporte_actual, ruta)
            messagebox.showinfo(
                "Exportación completada",
                f"El reporte se guardó correctamente en:\n{ruta}",
                parent=self.frame
            )
        except OSError as error:
            messagebox.showerror("Error al exportar", str(error), parent=self.frame)