import os
import tkinter as tk
from tkinter import ttk, messagebox
from tkinter import font as tkfont
from dataclasses import dataclass


class C:
    AZUL_HIELO = "#A8D8F0"
    AZUL_ESCARCHA = "#D4E9F7"
    AZUL_NIEBLA = "#EAF4FB"
    AZUL_GLACIAR = "#5FA8D3"
    AZUL_INVIERNO = "#2E6FA7"
    AZUL_PROFUNDO = "#255C8E"
    AZUL_NOCHE = "#1B4568"
    NIEVE = "#F7FAFC"
    BLANCO = "#FFFFFF"
    GRIS_ESCARCHA = "#EDF1F5"
    GRIS_BORDE = "#D5DDE5"
    GRIS_ACERO = "#8A99A8"
    GRIS_PIZARRA = "#5C6B7A"
    GRIS_CARBON = "#1F2D3A"
    ERROR = "#D9534F"


TITULO = 18
SUBTITULO = 14
TEXTO = 11
BOTON = 11
ENC_TABLA = 10
CELDA = 10
NOTA = 9

ALTO_LOGO = 64


def elegir_familia():
    disponibles = set(tkfont.families())
    for nombre in ("Nunito", "Segoe UI", "Arial"):
        if nombre in disponibles:
            return nombre
    return "Arial"


@dataclass
class Estudiante:
    matricula: str
    nombre: str
    grupo: str

@dataclass
class Curso:
    clave: str
    nombre: str

class SistemaEscolar:
    def __init__(self):
        self.estudiantes: dict[str, Estudiante] = {}
        self.cursos: dict[str, Curso] = {}
        self.inscripciones: dict[str, set[str]] = {}

    def registrar_estudiante(self, matricula, nombre, grupo):
        matricula, nombre, grupo = matricula.strip(), nombre.strip(), grupo.strip()
        if not all((matricula, nombre, grupo)):
            raise ValueError("Completa matrícula, nombre y grupo.")
        if matricula in self.estudiantes:
            raise ValueError("Esa matrícula ya está registrada.")
        self.estudiantes[matricula] = Estudiante(matricula, nombre, grupo)
        self.inscripciones[matricula] = set()

    def registrar_curso(self, clave, nombre):
        clave, nombre = clave.strip(), nombre.strip()
        if not clave or not nombre:
            raise ValueError("Completa clave y nombre del curso.")
        if clave in self.cursos:
            raise ValueError("Esa clave de curso ya está registrada.")
        self.cursos[clave] = Curso(clave, nombre)

    def inscribir(self, matricula, clave):
        if matricula not in self.estudiantes:
            raise ValueError("Selecciona un estudiante válido.")
        if clave not in self.cursos:
            raise ValueError("Selecciona un curso válido.")
        if clave in self.inscripciones[matricula]:
            raise ValueError("El estudiante ya está inscrito en ese curso.")
        self.inscripciones[matricula].add(clave)


class BotonRedondo(tk.Canvas):
    COLORES = {
        True: {
            "normal": (C.AZUL_INVIERNO, C.AZUL_INVIERNO, C.BLANCO),
            "hover": (C.AZUL_PROFUNDO, C.AZUL_PROFUNDO, C.BLANCO),
            "pressed": (C.AZUL_NOCHE, C.AZUL_NOCHE, C.BLANCO),
            "disabled": (C.GRIS_BORDE, C.GRIS_BORDE, C.GRIS_ACERO),
        },
        False: {
            "normal": (C.BLANCO, C.AZUL_INVIERNO, C.AZUL_INVIERNO),
            "hover": (C.AZUL_NIEBLA, C.AZUL_INVIERNO, C.AZUL_INVIERNO),
            "pressed": (C.AZUL_ESCARCHA, C.AZUL_INVIERNO, C.AZUL_INVIERNO),
            "disabled": (C.BLANCO, C.GRIS_BORDE, C.GRIS_ACERO),
        },
    }

    def __init__(self, parent, texto, comando, fuente, primario=True, fondo=C.BLANCO):
        medida = tkfont.Font(font=fuente)
        self._ancho = medida.measure(texto) + 48
        self._alto = 36
        super().__init__(parent, width=self._ancho, height=self._alto, bg=fondo,
                         highlightthickness=0, bd=0, cursor="hand2", takefocus=1)
        self._texto = texto
        self._comando = comando
        self._fuente = fuente
        self._primario = primario
        self._estado = "normal"
        self._habilitado = True
        self._foco = False
        self._dibujar()
        self.bind("<Enter>", lambda e: self._poner("hover"))
        self.bind("<Leave>", lambda e: self._poner("normal"))
        self.bind("<ButtonPress-1>", lambda e: self._poner("pressed"))
        self.bind("<ButtonRelease-1>", self._soltar)
        self.bind("<FocusIn>", lambda e: self._cambiar_foco(True))
        self.bind("<FocusOut>", lambda e: self._cambiar_foco(False))
        self.bind("<space>", lambda e: self._invocar())
        self.bind("<Return>", lambda e: self._invocar())

    def _pastilla(self, x0, y0, x1, y1, color):
        r = (y1 - y0) / 2
        self.create_oval(x0, y0, x0 + 2 * r, y1, fill=color, outline=color)
        self.create_oval(x1 - 2 * r, y0, x1, y1, fill=color, outline=color)
        self.create_rectangle(x0 + r, y0, x1 - r, y1, fill=color, outline=color)

    def _dibujar(self):
        relleno, borde, texto = self.COLORES[self._primario][self._estado]
        self.delete("all")
        w, h = self._ancho - 1, self._alto - 1
        if self._foco and self._habilitado:
            self._pastilla(0, 0, w, h, C.AZUL_GLACIAR)
            self._pastilla(2, 2, w - 2, h - 2, borde)
            self._pastilla(4, 4, w - 4, h - 4, relleno)
        else:
            self._pastilla(0, 0, w, h, borde)
            if borde != relleno:
                self._pastilla(2, 2, w - 2, h - 2, relleno)
        self.create_text(self._ancho / 2, self._alto / 2, text=self._texto, fill=texto, font=self._fuente)

    def _poner(self, estado):
        if not self._habilitado:
            return
        self._estado = estado
        self._dibujar()

    def _cambiar_foco(self, valor):
        self._foco = valor
        self._dibujar()

    def _soltar(self, evento):
        if not self._habilitado:
            return
        dentro = 0 <= evento.x <= self._ancho and 0 <= evento.y <= self._alto
        self._poner("hover" if dentro else "normal")
        if dentro:
            self._comando()

    def _invocar(self):
        if self._habilitado:
            self._comando()

    def habilitar(self, valor):
        self._habilitado = valor
        self._estado = "normal" if valor else "disabled"
        self.configure(cursor="hand2" if valor else "arrow")
        self._dibujar()


class Aplicacion(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("Bear Student · Administración de estudiantes y cursos")
        self.geometry("900x800")
        self.minsize(800, 700)
        self.configure(bg=C.NIEVE)
        self.familia = elegir_familia()
        self.sistema = SistemaEscolar()
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
        ruta = os.path.join(os.path.dirname(os.path.abspath(__file__)), "logo.png")
        if not os.path.exists(ruta):
            return None, C.BLANCO
        try:
            from PIL import Image, ImageTk
            pil = Image.open(ruta).convert("RGBA")
            w, h = pil.size
            pil = pil.crop((int(w * 0.05), int(h * 0.24), int(w * 0.95), int(h * 0.78)))
            base = Image.new("RGBA", pil.size, (255, 255, 255, 255))
            base.alpha_composite(pil)
            pil = base.convert("RGB")
            ancho = round(pil.width * ALTO_LOGO / pil.height)
            pil = pil.resize((ancho, ALTO_LOGO), Image.LANCZOS)
            r, g, b = pil.getpixel((0, 0))
            return ImageTk.PhotoImage(pil), "#%02x%02x%02x" % (r, g, b)
        except Exception:
            pass
        try:
            img = tk.PhotoImage(file=ruta)
            w, h = img.width(), img.height()
            recorte = tk.PhotoImage()
            recorte.tk.call(str(recorte), "copy", str(img), "-from",
                            int(w * 0.05), int(h * 0.24), int(w * 0.95), int(h * 0.78))
            try:
                transparente = bool(img.transparency_get(2, 2))
            except tk.TclError:
                transparente = False
            if transparente:
                r, g, b = 255, 255, 255
            else:
                px = img.get(2, 2)
                if isinstance(px, str):
                    px = tuple(int(v) for v in px.split())
                r, g, b = px[:3]
            factor = max(1, recorte.height() // ALTO_LOGO)
            return recorte.subsample(factor, factor), "#%02x%02x%02x" % (r, g, b)
        except Exception:
            return None, C.BLANCO

    def _crear_interfaz(self):
        self.logo, fondo_logo = self._preparar_logo()

        barra = tk.Frame(self, bg=fondo_logo)
        barra.pack(fill="x", side="top")
        if self.logo is not None:
            tk.Label(barra, image=self.logo, bg=fondo_logo).pack(side="left", padx=24, pady=16)
        else:
            tk.Label(barra, text="Bear Student", bg=fondo_logo, fg=C.AZUL_INVIERNO,
                     font=self._f(TITULO, True)).pack(side="left", padx=24, pady=24)
        tk.Label(barra, text="Registro de estudiantes,\ncursos e inscripciones", bg=fondo_logo,
                 fg=C.GRIS_PIZARRA, font=self._f(TEXTO), justify="right").pack(side="right", padx=24)
        tk.Frame(self, bg=C.AZUL_HIELO, height=3).pack(fill="x", side="top")

        self.estado = tk.Label(self, anchor="w", bg=C.GRIS_ESCARCHA, fg=C.GRIS_PIZARRA,
                               font=self._f(NOTA), padx=16, pady=8)
        self.estado.pack(fill="x", side="bottom")

        cont = ttk.Frame(self)
        cont.pack(fill="x", padx=16, pady=(16, 8))

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

        marco_ins, ins = self._panel(self, "Inscribir estudiante en un curso")
        marco_ins.pack(fill="x", padx=16, pady=8)
        ttk.Label(ins, text="Estudiante", style="Panel.TLabel").grid(row=0, column=0, sticky="w")
        ttk.Label(ins, text="Curso", style="Panel.TLabel").grid(row=0, column=1, sticky="w", padx=(8, 0))
        self.combo_est = ttk.Combobox(ins, state="readonly", width=30, style="Campo.TCombobox",
                                      font=self._f(TEXTO))
        self.combo_curso = ttk.Combobox(ins, state="readonly", width=30, style="Campo.TCombobox",
                                        font=self._f(TEXTO))
        self.combo_est.grid(row=1, column=0, sticky="ew", pady=(4, 0))
        self.combo_curso.grid(row=1, column=1, sticky="ew", padx=(8, 0), pady=(4, 0))
        self.btn_inscribir = BotonRedondo(ins, "Inscribir", self.inscribir, self._f(BOTON, True))
        self.btn_inscribir.grid(row=1, column=2, padx=(16, 0), pady=(4, 0))
        ins.columnconfigure((0, 1), weight=1)

        self.pestanas = ttk.Notebook(self)
        self.pestanas.pack(fill="both", expand=True, padx=16, pady=(8, 16))
        self.tabla_est = self._crear_tabla(
            "Estudiantes",
            [("matricula", "Matrícula", 140, "w"), ("nombre", "Nombre", 300, "w"),
             ("grupo", "Grupo", 100, "w"), ("cursos", "Cursos", 90, "e")],
            "Aún no hay estudiantes. Regístralos con el formulario de arriba.")
        self.tabla_cur = self._crear_tabla(
            "Cursos",
            [("clave", "Clave", 140, "w"), ("nombre", "Nombre del curso", 300, "w"),
             ("inscritos", "Inscritos", 90, "e")],
            "Aún no hay cursos. Registra el primero con el formulario de arriba.")
        self.tabla_ins = self._crear_tabla(
            "Inscripciones",
            [("estudiante", "Estudiante", 300, "w"), ("curso", "Curso", 300, "w")],
            "Todavía no hay inscripciones. Registra un estudiante y un curso para inscribirlo.")
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
        BotonRedondo(marco, texto, comando, self._f(BOTON, True)).pack(side="left")
        BotonRedondo(marco, "Limpiar", lambda: self._limpiar(campos), self._f(BOTON, True),
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
        self._llenar(self.tabla_cur, [(c.clave, c.nombre,
                                       sum(c.clave in cl for cl in s.inscripciones.values()))
                                      for c in s.cursos.values()])
        self._llenar(self.tabla_ins, [(s.estudiantes[m].nombre, s.cursos[k].nombre)
                                      for m, claves in s.inscripciones.items() for k in sorted(claves)])

        self.btn_inscribir.habilitar(bool(s.estudiantes) and bool(s.cursos))

        total_ins = sum(len(v) for v in s.inscripciones.values())
        self.estado.configure(
            text=f"Estudiantes: {len(s.estudiantes)}     Cursos: {len(s.cursos)}     Inscripciones: {total_ins}")


if __name__ == "__main__":
    Aplicacion().mainloop()