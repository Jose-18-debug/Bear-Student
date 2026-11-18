"""Colores y tipografía compartidos por las ventanas Bear Student."""

import tkinter.font as tkfont


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
    APROBADO_FONDO = "#DDF1E8"
    APROBADO_TEXTO = "#1F6B50"
    APROBADO_BORDE = "#3C9D7B"
    REPROBADO_FONDO = "#FBE3E1"
    REPROBADO_TEXTO = "#A12622"
    REPROBADO_BORDE = "#D9534F"
    RIESGO_FONDO = "#FFF1D6"
    RIESGO_TEXTO = "#8A5A00"
    RIESGO_BORDE = "#F2B84B"
    INFO_FONDO = "#D4E9F7"
    INFO_TEXTO = "#1B4568"
    INFO_BORDE = "#5FA8D3"
    SIN_CALIFICACIONES_FONDO = "#EDF1F5"
    SIN_CALIFICACIONES_TEXTO = "#5C6B7A"
    SIN_CALIFICACIONES_BORDE = "#D5DDE5"
    GRAFICA_JAVA = "#2E6FA7"
    GRAFICA_PYTHON = "#E3A72F"
    GRAFICA_JAVASCRIPT = "#3AA6A0"


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
