package bearstudent.ui.componentes;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Paleta Invierno y tipografía de la guía de diseño; son los mismos colores que la versión en Python. */
public final class Estilo {

    public static final Color AZUL_HIELO = new Color(0xA8D8F0);
    public static final Color AZUL_ESCARCHA = new Color(0xD4E9F7);
    public static final Color AZUL_NIEBLA = new Color(0xEAF4FB);
    public static final Color AZUL_GLACIAR = new Color(0x5FA8D3);
    public static final Color AZUL_INVIERNO = new Color(0x2E6FA7);
    public static final Color AZUL_PROFUNDO = new Color(0x255C8E);
    public static final Color AZUL_NOCHE = new Color(0x1B4568);
    public static final Color NIEVE = new Color(0xF7FAFC);
    public static final Color BLANCO = Color.WHITE;
    public static final Color GRIS_ESCARCHA = new Color(0xEDF1F5);
    public static final Color GRIS_BORDE = new Color(0xD5DDE5);
    public static final Color GRIS_ACERO = new Color(0x8A99A8);
    public static final Color GRIS_PIZARRA = new Color(0x5C6B7A);
    public static final Color GRIS_CARBON = new Color(0x1F2D3A);
    public static final Color ROJO_ERROR = new Color(0xD9534F);

    // Swing mide las fuentes en píxeles, por eso se usan los tamaños de la columna "Web"
    // de la guía de diseño (se ven igual que los 11 pt de Tkinter).
    public static final int TITULO = 24, SUBTITULO = 18, TEXTO = 14, BOTON = 14, ENC_TABLA = 13, CELDA = 13, NOTA = 12;
    public static final int ALTO_LOGO = 56;

    public static final String FAMILIA = elegirFamilia();

    private Estilo() { }

    /** Nunito si está instalada; si no, Segoe UI; si no, Arial; y si tampoco, la sans serif del sistema. */
    private static String elegirFamilia() {
        Set<String> disponibles = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String nombre : new String[]{"Nunito", "Segoe UI", "Arial"}) {
            if (disponibles.contains(nombre)) return nombre;
        }
        return Font.SANS_SERIF;
    }

    public static Font fuente(int tam, boolean negrita) {
        return new Font(FAMILIA, negrita ? Font.BOLD : Font.PLAIN, tam);
    }
}
