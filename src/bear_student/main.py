import os
import tkinter as tk
from tkinter import ttk, messagebox
from bear_student.domain.approval import ServicioAprobacion
from bear_student.domain.reports import ServicioReportes
from bear_student.domain.averages import ServicioPromedios
from bear_student.domain.evaluations import ServicioEvaluaciones
from bear_student.domain.students_courses import SistemaEscolar
from bear_student.domain.AprobadosVista import AprobacionView
from bear_student.ui.averages_view import PromediosView, formato
from bear_student.ui.evaluations_view import EvaluacionesView
from bear_student.ui.reports_view import ReportesView
from bear_student.ui.widgets import BotonInvierno, PestanaDesplazable
from bear_student.ui.theme import (
    ALTO_LOGO,
    BOTON,
    C,
    CELDA,
    ENC_TABLA,
    NOTA,
    SUBTITULO,
    TEXTO,
    TITULO,
    elegir_familia,
)


class Aplicacion(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("Bear Student · Administración escolar")
        self.geometry("900x800")
        self.minsize(800, 700)
        self.configure(bg=C.NIEVE)
        self.familia = elegir_familia()
        self.sistema = SistemaEscolar()
        self.servicio_evaluaciones = ServicioEvaluaciones(
            lambda clave: clave in self.sistema.cursos,
            self.sistema.estudiantes_inscritos,
        )
        self.servicio_promedios = ServicioPromedios(self.sistema, self.servicio_evaluaciones)
        self.servicio_aprobacion = ServicioAprobacion(self.servicio_promedios)
        self.servicio_reportes = ServicioReportes(
            self.sistema, self.servicio_promedios, self.servicio_aprobacion
        )
        self._configurar_estilos()
        self._crear_interfaz()

    def _f(self, tam, negrita=False):
        return (self.familia, tam, "bold" if negrita else "normal")

    def _configurar_estilos(self):
        s = ttk.Style(self)
        s.theme_use("clam")

        s.configure(".", background=C.NIEVE, foreground=C.GRIS_CARBON, font=self._f(TEXTO))
        s.configure("TFrame", background=C.NIEVE)
        s.configure("Panel.TFrame", background=C.BLANCO)
        s.configure("TLabel", background=C.NIEVE, foreground=C.GRIS_CARBON, font=self._f(TEXTO))
        s.configure("Panel.TLabel", background=C.BLANCO, foreground=C.GRIS_CARBON, font=self._f(TEXTO))

        s.configure("Titulo.TLabel", background=C.BLANCO, foreground=C.AZUL_INVIERNO,
                    font=self._f(SUBTITULO, True))

        s.configure("Campo.TEntry", fieldbackground=C.BLANCO, foreground=C.GRIS_CARBON,
                    bordercolor=C.GRIS_BORDE, lightcolor=C.GRIS_BORDE, darkcolor=C.GRIS_BORDE,
                    insertcolor=C.GRIS_CARBON, padding=6)
        s.map("Campo.TEntry",
              bordercolor=[("invalid", C.ERROR), ("focus", C.AZUL_GLACIAR)],
              lightcolor=[("invalid", C.ERROR), ("focus", C.AZUL_GLACIAR)],
              darkcolor=[("invalid", C.ERROR), ("focus", C.AZUL_GLACIAR)])

        s.configure("Campo.TCombobox", fieldbackground=C.BLANCO, background=C.AZUL_ESCARCHA,
                    foreground=C.GRIS_CARBON, arrowcolor=C.AZUL_INVIERNO,
                    bordercolor=C.GRIS_BORDE, lightcolor=C.GRIS_BORDE, darkcolor=C.GRIS_BORDE,
                    padding=6)
        s.map("Campo.TCombobox",
              fieldbackground=[("readonly", C.BLANCO)],
              foreground=[("readonly", C.GRIS_CARBON)],
              bordercolor=[("focus", C.AZUL_GLACIAR)],
              lightcolor=[("focus", C.AZUL_GLACIAR)],
              darkcolor=[("focus", C.AZUL_GLACIAR)],
              arrowcolor=[("disabled", C.GRIS_ACERO)])
        self.option_add("*TCombobox*Listbox.font", self._f(TEXTO))
        self.option_add("*TCombobox*Listbox.background", C.BLANCO)
        self.option_add("*TCombobox*Listbox.foreground", C.GRIS_CARBON)
        self.option_add("*TCombobox*Listbox.selectBackground", C.AZUL_HIELO)
        self.option_add("*TCombobox*Listbox.selectForeground", C.GRIS_CARBON)

        s.configure("TNotebook", background=C.NIEVE, bordercolor=C.AZUL_ESCARCHA,
                    lightcolor=C.AZUL_ESCARCHA, darkcolor=C.AZUL_ESCARCHA, borderwidth=1,
                    tabmargins=(0, 0, 0, 0))
        s.configure("TNotebook.Tab", background=C.AZUL_NIEBLA, foreground=C.GRIS_PIZARRA,
                    font=self._f(BOTON, True), padding=(16, 8), borderwidth=1,
                    bordercolor=C.AZUL_ESCARCHA, lightcolor=C.AZUL_NIEBLA, darkcolor=C.AZUL_NIEBLA,
                    focuscolor=C.AZUL_NIEBLA)
        s.map("TNotebook.Tab",
              background=[("selected", C.BLANCO), ("active", C.AZUL_ESCARCHA)],
              lightcolor=[("selected", C.BLANCO), ("active", C.AZUL_ESCARCHA)],
              darkcolor=[("selected", C.BLANCO), ("active", C.AZUL_ESCARCHA)],
              foreground=[("selected", C.AZUL_INVIERNO)])

        s.configure("Treeview", background=C.BLANCO, fieldbackground=C.BLANCO, foreground=C.GRIS_CARBON,
                    font=self._f(CELDA), rowheight=32, borderwidth=0,
                    bordercolor=C.AZUL_ESCARCHA, lightcolor=C.AZUL_ESCARCHA, darkcolor=C.AZUL_ESCARCHA)
        s.map("Treeview",
              background=[("selected", C.AZUL_HIELO)],
              foreground=[("selected", C.GRIS_CARBON)])
        s.configure("Treeview.Heading", background=C.AZUL_ESCARCHA, foreground=C.AZUL_NOCHE,
                    font=self._f(ENC_TABLA, True), relief="flat", padding=(8, 8), borderwidth=0)
        s.map("Treeview.Heading", background=[("active", C.AZUL_ESCARCHA)])

        s.configure("Vertical.TScrollbar", background=C.GRIS_BORDE, troughcolor=C.GRIS_ESCARCHA,
                    bordercolor=C.GRIS_ESCARCHA, arrowcolor=C.GRIS_PIZARRA, relief="flat")
        s.map("Vertical.TScrollbar", background=[("active", C.GRIS_ACERO)])

    def _preparar_logo(self):
        ruta = os.path.join(os.path.dirname(os.path.abspath(__file__)), "assets", "logo.png")
        if not os.path.exists(ruta):
            return None, C.BLANCO
        try:
            from PIL import Image, ImageTk
            pil = Image.open(ruta).convert("RGBA")
            w, h = pil.size
            pil = pil.crop((int(w * 0.05), int(h * 0.24), int(w * 0.95), int(h * 0.78)))
            ancho = round(pil.width * ALTO_LOGO / pil.height)
            pil = pil.resize((ancho, ALTO_LOGO), Image.LANCZOS)
            return ImageTk.PhotoImage(pil), C.AZUL_NOCHE
        except Exception:
            pass
        try:
            img = tk.PhotoImage(file=ruta)
            w, h = img.width(), img.height()
            recorte = tk.PhotoImage()
            recorte.tk.call(str(recorte), "copy", str(img), "-from",
                            int(w * 0.05), int(h * 0.24), int(w * 0.95), int(h * 0.78))
            factor = max(1, recorte.height() // ALTO_LOGO)
            return recorte.subsample(factor, factor), C.AZUL_NOCHE
        except Exception:
            return None, C.BLANCO

    def _crear_interfaz(self):
        self.logo, _ = self._preparar_logo()

        barra = tk.Frame(self, bg=C.AZUL_NOCHE)
        barra.pack(fill="x", side="top")
        if self.logo is not None:
            tk.Label(barra, image=self.logo, bg=C.AZUL_NOCHE).pack(side="left", padx=24, pady=16)
        else:
            tk.Label(barra, text="Bear Student", bg=C.AZUL_NOCHE, fg=C.BLANCO,
                     font=self._f(TITULO, True)).pack(side="left", padx=24, pady=24)
        tk.Label(barra, text="Administración escolar", bg=C.AZUL_NOCHE,
                 fg=C.BLANCO, font=self._f(TEXTO), justify="right").pack(side="right", padx=24)
        tk.Frame(self, bg=C.AZUL_HIELO, height=3).pack(fill="x", side="top")

        self.estado = tk.Label(self, anchor="w", bg=C.GRIS_ESCARCHA, fg=C.GRIS_PIZARRA,
                               font=self._f(NOTA), padx=16, pady=8)
        self.estado.pack(fill="x", side="bottom")

        # Los formularios viven en su propia pestaña para que las tablas y la
        # pestaña de evaluaciones tengan todo el alto de la ventana.
        self.pestanas = ttk.Notebook(self)
        self.pestanas.pack(fill="both", expand=True, padx=16, pady=16)
        registro = PestanaDesplazable(self.pestanas, "Registro", padding=(8, 16, 8, 8)).interior

        cont = ttk.Frame(registro)
        cont.pack(fill="x", pady=(0, 8))

        marco_est, est = self._panel(cont, "Registrar estudiante")
        marco_est.grid(row=0, column=0, sticky="nsew", padx=(0, 8))
        self.matricula = self._campo(est, "Matrícula", 0)
        self.nombre = self._campo(est, "Nombre", 1)
        self.grupo = self._campo(est, "Grupo", 2)
        self._botones(est, 3, "Guardar estudiante", self.registrar_estudiante,
                      (self.matricula, self.nombre, self.grupo))

        marco_cur, cur = self._panel(cont, "Registrar curso")
        marco_cur.grid(row=0, column=1, sticky="nsew", padx=(8, 0))
        self.clave = self._campo(cur, "Clave", 0)
        self.nombre_curso = self._campo(cur, "Nombre del curso", 1)
        self._botones(cur, 2, "Guardar curso", self.registrar_curso, (self.clave, self.nombre_curso))
        cont.columnconfigure((0, 1), weight=1, uniform="formularios")

        marco_ins, ins = self._panel(registro, "Inscribir estudiante en un curso")
        marco_ins.pack(fill="x", pady=8)
        ttk.Label(ins, text="Estudiante", style="Panel.TLabel").grid(row=0, column=0, sticky="w")
        ttk.Label(ins, text="Curso", style="Panel.TLabel").grid(row=0, column=1, sticky="w", padx=(8, 0))
        self.combo_est = ttk.Combobox(ins, state="readonly", width=30, style="Campo.TCombobox",
                                      font=self._f(TEXTO))
        self.combo_curso = ttk.Combobox(ins, state="readonly", width=30, style="Campo.TCombobox",
                                        font=self._f(TEXTO))
        self.combo_est.grid(row=1, column=0, sticky="ew", pady=(4, 0))
        self.combo_curso.grid(row=1, column=1, sticky="ew", padx=(8, 0), pady=(4, 0))
        self.btn_inscribir = BotonInvierno(ins, "Inscribir", self.inscribir, self._f(BOTON, True))
        self.btn_inscribir.grid(row=1, column=2, padx=(16, 0), pady=(4, 0))
        ins.columnconfigure((0, 1), weight=1)

        self.tabla_est = self._crear_tabla(
            "Estudiantes",
            [("matricula", "Matrícula", 140, "w"), ("nombre", "Nombre", 300, "w"),
             ("grupo", "Grupo", 100, "w"), ("cursos", "Cursos", 90, "e")],
            "Aún no hay estudiantes. Regístralos con el formulario de arriba.")
        self.tabla_cur = self._crear_tabla(
            "Cursos",
            [("clave", "Clave", 140, "w"), ("nombre", "Nombre del curso", 300, "w"),
             ("inscritos", "Inscritos", 90, "e"), ("promedio", "Promedio", 100, "e")],
            "Aún no hay cursos. Registra el primero con el formulario de arriba.")
        self.tabla_ins = self._crear_tabla(
            "Inscripciones",
            [("estudiante", "Estudiante", 300, "w"), ("curso", "Curso", 300, "w")],
            "Todavía no hay inscripciones. Registra un estudiante y un curso para inscribirlo.")
        self.vista_evaluaciones = EvaluacionesView(
            self.pestanas,
            self.sistema,
            servicio=self.servicio_evaluaciones,
            fuente=self._f(TEXTO),
        )
        self.vista_promedios = PromediosView(
            self.pestanas,
            self.sistema,
            self.servicio_promedios,
            fuente=self._f(TEXTO),
        )
        self.vista_estadisticas = AprobacionView(
            self.pestanas,
            self.sistema,
            self.servicio_aprobacion,
            fuente=self._f(TEXTO),
        )
        self.vista_reportes = ReportesView(
            self.pestanas,
            self.sistema,
            self.servicio_reportes,
            fuente=self._f(TEXTO),
        )
        # Las notas se capturan en otra pestaña: al cambiar de pestaña se
        # recalculan los promedios para que siempre estén al día.
        self.pestanas.bind("<<NotebookTabChanged>>", lambda _e: self._actualizar_promedios())
        self.actualizar()

    def _panel(self, parent, titulo):
        marco = tk.Frame(parent, bg=C.BLANCO, highlightbackground=C.AZUL_ESCARCHA, highlightthickness=1)
        ttk.Label(marco, text=titulo, style="Titulo.TLabel").pack(anchor="w", padx=16, pady=(16, 0))
        cuerpo = ttk.Frame(marco, style="Panel.TFrame", padding=(16, 8, 16, 16))
        cuerpo.pack(fill="both", expand=True)
        return marco, cuerpo

    def _campo(self, parent, etiqueta, fila):
        ttk.Label(parent, text=etiqueta, style="Panel.TLabel").grid(row=fila, column=0, sticky="w", pady=8, padx=(0, 16))
        entrada = ttk.Entry(parent, width=25, style="Campo.TEntry", font=self._f(TEXTO))
        entrada.grid(row=fila, column=1, sticky="ew", pady=8)
        entrada.bind("<Key>", lambda e: e.widget.state(["!invalid"]))
        parent.columnconfigure(1, weight=1)
        return entrada

    def _botones(self, parent, fila, texto, comando, campos):
        marco = ttk.Frame(parent, style="Panel.TFrame")
        marco.grid(row=fila, column=0, columnspan=2, sticky="ew", pady=(8, 0))
        BotonInvierno(marco, texto, comando, self._f(BOTON, True)).pack(side="left")
        BotonInvierno(marco, "Limpiar", lambda: self._limpiar(campos), self._f(BOTON, True),
                      primario=False).pack(side="left", padx=(8, 0))

    def _limpiar(self, campos):
        for c in campos:
            c.delete(0, tk.END)
            c.state(["!invalid"])
        campos[0].focus_set()

    def _marcar_error(self, campos):
        vacios = [c for c in campos if not c.get().strip()]
        for c in (vacios or campos[:1]):
            c.state(["invalid"])
        (vacios or campos)[0].focus_set()

    def _crear_tabla(self, titulo, columnas, vacio):
        marco = ttk.Frame(self.pestanas, style="Panel.TFrame", padding=8)
        self.pestanas.add(marco, text=titulo)
        tree = ttk.Treeview(marco, columns=[c[0] for c in columnas], show="headings", selectmode="browse")
        for id_, texto, ancho, anclaje in columnas:
            tree.heading(id_, text=texto, anchor=anclaje)
            tree.column(id_, width=ancho, anchor=anclaje, stretch=True)
        tree.tag_configure("par", background=C.BLANCO)
        tree.tag_configure("impar", background=C.AZUL_NIEBLA)
        tree.tag_configure("sel", background=C.AZUL_HIELO, foreground=C.GRIS_CARBON)
        tree.bind("<<TreeviewSelect>>", lambda e, t=tree: self._pintar(t))
        barra = ttk.Scrollbar(marco, orient="vertical", command=tree.yview)
        tree.configure(yscrollcommand=barra.set)
        tree.pack(side="left", fill="both", expand=True)
        barra.pack(side="right", fill="y")
        tree.aviso_vacio = tk.Label(tree, text=vacio, bg=C.BLANCO, fg=C.GRIS_PIZARRA,
                                    font=self._f(TEXTO), wraplength=420, justify="center")
        return tree

    @staticmethod
    def _pintar(tree):
        seleccion = set(tree.selection())
        for i, item in enumerate(tree.get_children()):
            tag = "sel" if item in seleccion else ("par", "impar")[i % 2]
            tree.item(item, tags=(tag,))

    def _llenar(self, tree, filas):
        tree.delete(*tree.get_children())
        for fila in filas:
            tree.insert("", "end", values=fila)
        self._pintar(tree)
        if filas:
            tree.aviso_vacio.place_forget()
        else:
            tree.aviso_vacio.place(relx=0.5, rely=0.55, anchor="center")

    def registrar_estudiante(self):
        campos = (self.matricula, self.nombre, self.grupo)
        try:
            self.sistema.registrar_estudiante(self.matricula.get(), self.nombre.get(), self.grupo.get())
            self._limpiar(campos)
            self.actualizar(); messagebox.showinfo("Listo", "Estudiante registrado.")
        except ValueError as e:
            self._marcar_error(campos)
            messagebox.showerror("No se pudo registrar", str(e))

    def registrar_curso(self):
        campos = (self.clave, self.nombre_curso)
        try:
            self.sistema.registrar_curso(self.clave.get(), self.nombre_curso.get())
            self._limpiar(campos)
            self.actualizar(); messagebox.showinfo("Listo", "Curso registrado.")
        except ValueError as e:
            self._marcar_error(campos)
            messagebox.showerror("No se pudo registrar", str(e))

    def inscribir(self):
        try:
            matricula = self.combo_est.get().split(" | ", 1)[0]
            clave = self.combo_curso.get().split(" | ", 1)[0]
            self.sistema.inscribir(matricula, clave)
            self.actualizar(); messagebox.showinfo("Listo", "Inscripción realizada.")
        except ValueError as e:
            messagebox.showerror("No se pudo inscribir", str(e))

    def actualizar(self):
        s = self.sistema
        self.combo_est["values"] = [f"{e.matricula} | {e.nombre} ({e.grupo})" for e in s.estudiantes.values()]
        self.combo_curso["values"] = [f"{c.clave} | {c.nombre}" for c in s.cursos.values()]

        self._llenar(self.tabla_est, [(e.matricula, e.nombre, e.grupo, len(s.inscripciones[e.matricula]))
                                      for e in s.estudiantes.values()])
        self._llenar(self.tabla_ins, [(s.estudiantes[m].nombre, s.cursos[k].nombre)
                                      for m, claves in s.inscripciones.items() for k in sorted(claves)])

        self.btn_inscribir.habilitar(bool(s.estudiantes) and bool(s.cursos))
        self.vista_evaluaciones.actualizar()
        self._actualizar_promedios()

        total_ins = sum(len(v) for v in s.inscripciones.values())
        self.estado.configure(
            text=f"Estudiantes: {len(s.estudiantes)}     Cursos: {len(s.cursos)}     Inscripciones: {total_ins}")


    def _actualizar_promedios(self):
        resumen = {c.clave: c for c in self.servicio_promedios.promedios_de_cursos()}
        self._llenar(self.tabla_cur, [(c.clave, c.nombre, resumen[c.clave].inscritos,
                                       formato(resumen[c.clave].promedio))
                                      for c in self.sistema.cursos.values()])
        self.vista_promedios.actualizar()
        self.vista_estadisticas.actualizar()
        self.vista_reportes.actualizar()


if __name__ == "__main__":
    Aplicacion().mainloop()
