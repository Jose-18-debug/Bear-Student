package bearstudent.ui.componentes;

import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static bearstudent.ui.componentes.Estilo.*;

/** Campo de texto con borde gris, azul glaciar al enfocarlo y rojo si es inválido. */
public class Campo extends JTextField {
    private boolean invalido;
    private boolean enfocado;

    public Campo() {
        super(18);
        setFont(fuente(TEXTO, false));
        setForeground(GRIS_CARBON);
        setBackground(BLANCO);
        setCaretColor(GRIS_CARBON);
        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { enfocado = true; pintarBorde(); }
            @Override public void focusLost(FocusEvent e) { enfocado = false; pintarBorde(); }
        });
        addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) { if (invalido) setInvalido(false); }
        });
        pintarBorde();
    }

    /** true pone el borde rojo; se quita solo en cuanto la persona escribe. */
    public void setInvalido(boolean valor) {
        invalido = valor;
        pintarBorde();
    }

    private void pintarBorde() {
        Color color = invalido ? ROJO_ERROR : (enfocado ? AZUL_GLACIAR : GRIS_BORDE);
        setBorder(new CompoundBorder(new LineBorder(color, 1), new EmptyBorder(5, 8, 5, 8)));
    }
}
