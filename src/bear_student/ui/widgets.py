"""Widgets compartidos por todas las pantallas de Bear Student."""

import tkinter as tk
from tkinter import ttk

from bear_student.ui.theme import C


class BotonInvierno(tk.Button):
    """Botón Tkinter plano con estados de la paleta Bear Student."""

    def __init__(self, parent, texto, comando, fuente, primario=True):
        self.primario = primario
        self.color_normal = C.AZUL_INVIERNO if primario else C.BLANCO
        self.color_hover = C.AZUL_PROFUNDO if primario else C.AZUL_NIEBLA
        self.color_presionado = C.AZUL_NOCHE if primario else C.AZUL_ESCARCHA
        self.color_texto = C.BLANCO if primario else C.AZUL_INVIERNO
        super().__init__(
            parent,
            text=texto,
            command=comando,
            font=fuente,
            bg=self.color_normal,
            fg=self.color_texto,
            activebackground=self.color_presionado,
            activeforeground=self.color_texto,
            disabledforeground=C.GRIS_ACERO,
            relief="flat",
            borderwidth=0,
            padx=16,
            pady=9,
            cursor="hand2",
            takefocus=True,
        )
        self.bind("<Enter>", lambda _evento: self._color(self.color_hover))
        self.bind("<Leave>", lambda _evento: self._color(self.color_normal))
        self.bind("<ButtonPress-1>", lambda _evento: self._color(self.color_presionado))
        self.bind("<ButtonRelease-1>", lambda _evento: self._color(self.color_hover))

    def _color(self, color):
        if str(self["state"]) != "disabled":
            self.configure(bg=color)

    def habilitar(self, valor):
        self.configure(state="normal" if valor else "disabled")
        self.configure(
            bg=self.color_normal if valor else C.GRIS_BORDE,
            cursor="hand2" if valor else "arrow",
        )


class PestanaDesplazable(ttk.Frame):
    """Pestaña con barra de desplazamiento vertical.

    El contenido se coloca dentro de ``self.interior``. Si cabe completo, la
    barra no se puede mover; si no cabe, se puede bajar con la rueda del ratón.
    """

    def __init__(self, notebook, titulo, fondo=C.NIEVE, padding=16):
        super().__init__(notebook)
        notebook.add(self, text=titulo)

        self.canvas = tk.Canvas(self, bg=fondo, highlightthickness=0, borderwidth=0)
        self.barra = ttk.Scrollbar(self, orient="vertical", command=self.canvas.yview)
        self.canvas.configure(yscrollcommand=self.barra.set)
        self.barra.pack(side="right", fill="y")
        self.canvas.pack(side="left", fill="both", expand=True)

        self.interior = ttk.Frame(self.canvas, padding=padding)
        self._ventana = self.canvas.create_window((0, 0), window=self.interior, anchor="nw")
        self.interior.bind("<Configure>", self._ajustar_region)
        self.canvas.bind("<Configure>", self._ajustar_ancho)
        # Un único manejador global: desplaza la pestaña que está bajo el ratón.
        raiz = self._root()
        if not getattr(raiz, "_rueda_bear_student", False):
            self.bind_all("<MouseWheel>", PestanaDesplazable._rueda, add="+")
            raiz._rueda_bear_student = True

    def _ajustar_region(self, _evento=None):
        self.canvas.configure(scrollregion=self.canvas.bbox("all"))

    def _ajustar_ancho(self, evento):
        self.canvas.itemconfigure(self._ventana, width=evento.width)

    def desplazar(self, pasos):
        if self.interior.winfo_reqheight() > self.canvas.winfo_height():
            self.canvas.yview_scroll(pasos, "units")

    @staticmethod
    def _rueda(evento):
        try:
            widget = evento.widget.winfo_containing(evento.x_root, evento.y_root)
        except (AttributeError, KeyError, tk.TclError):
            return  # p. ej. la lista desplegable de un Combobox
        while widget is not None and not isinstance(widget, PestanaDesplazable):
            widget = widget.master
        if widget is not None and evento.delta:
            widget.desplazar(-1 if evento.delta > 0 else 1)
