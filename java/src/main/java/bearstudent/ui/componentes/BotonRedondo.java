package bearstudent.ui.componentes;

import javax.swing.ButtonModel;
import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

import static bearstudent.ui.componentes.Estilo.*;

/** Botón en forma de píldora: primario (relleno azul) o secundario (solo borde). */
public class BotonRedondo extends JButton {
    private final boolean primario;

    public BotonRedondo(String texto, boolean primario, Runnable accion) {
        super(texto);
        this.primario = primario;
        setFont(fuente(BOTON, true));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        FontMetrics fm = getFontMetrics(getFont());
        setPreferredSize(new Dimension(fm.stringWidth(texto) + 48, 32));
        addActionListener(e -> accion.run());
        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { repaint(); }
            @Override public void focusLost(FocusEvent e) { repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        ButtonModel m = getModel();
        Color relleno, borde, texto;
        if (!isEnabled()) {
            relleno = primario ? GRIS_BORDE : BLANCO; borde = GRIS_BORDE; texto = GRIS_ACERO;
        } else if (m.isPressed()) {
            relleno = primario ? AZUL_NOCHE : AZUL_ESCARCHA;
            borde = primario ? AZUL_NOCHE : AZUL_INVIERNO;
            texto = primario ? BLANCO : AZUL_INVIERNO;
        } else if (m.isRollover()) {
            relleno = primario ? AZUL_PROFUNDO : AZUL_NIEBLA;
            borde = primario ? AZUL_PROFUNDO : AZUL_INVIERNO;
            texto = primario ? BLANCO : AZUL_INVIERNO;
        } else {
            relleno = primario ? AZUL_INVIERNO : BLANCO;
            borde = AZUL_INVIERNO;
            texto = primario ? BLANCO : AZUL_INVIERNO;
        }
        int w = getWidth(), h = getHeight();
        g2.setColor(borde);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
        if (!borde.equals(relleno)) {
            g2.setColor(relleno);
            g2.fill(new RoundRectangle2D.Float(2, 2, w - 4, h - 4, h - 4, h - 4));
        }
        if (isFocusOwner() && isEnabled()) {      // aro azul glaciar para navegar con teclado
            g2.setColor(AZUL_GLACIAR);
            g2.setStroke(new BasicStroke(2));
            g2.draw(new RoundRectangle2D.Float(1, 1, w - 3, h - 3, h - 2, h - 2));
        }
        g2.setFont(getFont());
        g2.setColor(texto);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
