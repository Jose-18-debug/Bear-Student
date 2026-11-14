import tkinter as tk
from tkinter import ttk, messagebox
from dataclasses import dataclass

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

class Aplicacion(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("Administración de estudiantes y cursos")
        self.geometry("720x540")
        self.sistema = SistemaEscolar()
        self._crear_interfaz()

    def _crear_interfaz(self):
        ttk.Label(self, text="Bear Student", font=("Segoe UI", 18, "bold")).pack(pady=(14, 4))
        ttk.Label(self, text="Registro de estudiantes, cursos e inscripciones").pack(pady=(0, 12))
        cont = ttk.Frame(self, padding=12)
        cont.pack(fill="x")

        est = ttk.LabelFrame(cont, text="Registrar estudiante", padding=10)
        est.grid(row=0, column=0, sticky="nsew", padx=(0, 8))
        self.matricula = self._campo(est, "Matrícula", 0)
        self.nombre = self._campo(est, "Nombre", 1)
        self.grupo = self._campo(est, "Grupo", 2)
        ttk.Button(est, text="Guardar estudiante", command=self.registrar_estudiante).grid(row=3, column=0, columnspan=2, sticky="ew", pady=(8, 0))

        cur = ttk.LabelFrame(cont, text="Registrar curso", padding=10)
        cur.grid(row=0, column=1, sticky="nsew", padx=(8, 0))
        self.clave = self._campo(cur, "Clave", 0)
        self.nombre_curso = self._campo(cur, "Nombre del curso", 1)
        ttk.Button(cur, text="Guardar curso", command=self.registrar_curso).grid(row=2, column=0, columnspan=2, sticky="ew", pady=(8, 0))
        cont.columnconfigure((0, 1), weight=1)

        ins = ttk.LabelFrame(self, text="Inscribir estudiante en un curso", padding=10)
        ins.pack(fill="x", padx=12, pady=4)
        self.combo_est = ttk.Combobox(ins, state="readonly", width=30)
        self.combo_curso = ttk.Combobox(ins, state="readonly", width=30)
        ttk.Label(ins, text="Estudiante").grid(row=0, column=0, sticky="w")
        self.combo_est.grid(row=1, column=0, padx=(0, 8), sticky="ew")
        ttk.Label(ins, text="Curso").grid(row=0, column=1, sticky="w")
        self.combo_curso.grid(row=1, column=1, padx=8, sticky="ew")
        ttk.Button(ins, text="Inscribir", command=self.inscribir).grid(row=1, column=2, padx=(8, 0))
        ins.columnconfigure((0, 1), weight=1)

        lista = ttk.LabelFrame(self, text="Registros e inscripciones", padding=8)
        lista.pack(fill="both", expand=True, padx=12, pady=(8, 12))
        self.texto = tk.Text(lista, height=11, wrap="word", state="disabled", font=("Consolas", 10))
        self.texto.pack(fill="both", expand=True)
        self.actualizar()

    @staticmethod
    def _campo(parent, etiqueta, fila):
        ttk.Label(parent, text=etiqueta).grid(row=fila, column=0, sticky="w", pady=3)
        entrada = ttk.Entry(parent, width=25)
        entrada.grid(row=fila, column=1, sticky="ew", pady=3)
        parent.columnconfigure(1, weight=1)
        return entrada

    def registrar_estudiante(self):
        try:
            self.sistema.registrar_estudiante(self.matricula.get(), self.nombre.get(), self.grupo.get())
            for e in (self.matricula, self.nombre, self.grupo): e.delete(0, tk.END)
            self.actualizar(); messagebox.showinfo("Listo", "Estudiante registrado.")
        except ValueError as e: messagebox.showerror("No se pudo registrar", str(e))

    def registrar_curso(self):
        try:
            self.sistema.registrar_curso(self.clave.get(), self.nombre_curso.get())
            self.clave.delete(0, tk.END); self.nombre_curso.delete(0, tk.END)
            self.actualizar(); messagebox.showinfo("Listo", "Curso registrado.")
        except ValueError as e: messagebox.showerror("No se pudo registrar", str(e))

    def inscribir(self):
        try:
            matricula = self.combo_est.get().split(" | ", 1)[0]
            clave = self.combo_curso.get().split(" | ", 1)[0]
            self.sistema.inscribir(matricula, clave)
            self.actualizar(); messagebox.showinfo("Listo", "Inscripción realizada.")
        except ValueError as e: messagebox.showerror("No se pudo inscribir", str(e))

    def actualizar(self):
        self.combo_est["values"] = [f"{e.matricula} | {e.nombre} ({e.grupo})" for e in self.sistema.estudiantes.values()]
        self.combo_curso["values"] = [f"{c.clave} | {c.nombre}" for c in self.sistema.cursos.values()]
        lineas = ["ESTUDIANTES"]
        lineas += [f"• {e.matricula} - {e.nombre} | Grupo: {e.grupo}" for e in self.sistema.estudiantes.values()] or ["(ninguno)"]
        lineas += ["", "CURSOS"]
        lineas += [f"• {c.clave} - {c.nombre}" for c in self.sistema.cursos.values()] or ["(ninguno)"]
        lineas += ["", "INSCRIPCIONES"]
        for m, claves in self.sistema.inscripciones.items():
            for k in sorted(claves): lineas.append(f"• {self.sistema.estudiantes[m].nombre} -> {self.sistema.cursos[k].nombre}")
        if not any(self.sistema.inscripciones.values()): lineas.append("(ninguna)")
        self.texto.configure(state="normal"); self.texto.delete("1.0", tk.END)
        self.texto.insert("1.0", "\n".join(lineas)); self.texto.configure(state="disabled")

if __name__ == "__main__":
    Aplicacion().mainloop()
