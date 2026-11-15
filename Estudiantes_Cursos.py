"""Lanzador compatible para abrir Bear Student desde la carpeta del proyecto."""

from pathlib import Path
import sys


RUTA_SRC = Path(__file__).resolve().parent / "src"
sys.path.insert(0, str(RUTA_SRC))

from bear_student.main import Aplicacion


if __name__ == "__main__":
    Aplicacion().mainloop()
