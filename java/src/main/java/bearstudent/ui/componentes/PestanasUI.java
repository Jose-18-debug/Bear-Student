package bearstudent.ui.componentes;

import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import static bearstudent.ui.componentes.Estilo.*;

/** Pestañas planas: la activa en blanco con línea inferior azul invierno de 3 px, las demás azul niebla. */
public class PestanasUI extends BasicTabbedPaneUI {
    @Override
    protected void installDefaults() {
        super.installDefaults();
        tabInsets = new Insets(8, 16, 8, 16);
        selectedTabPadInsets = new Insets(0, 0, 0, 0);
        tabAreaInsets = new Insets(0, 0, 0, 0);
        contentBorderInsets = new Insets(1, 1, 1, 1);
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                      int x, int y, int w, int h, boolean isSelected) {
        g.setColor(isSelected ? BLANCO : AZUL_NIEBLA);
        g.fillRect(x, y, w, h);
        if (isSelected) {
            g.setColor(AZUL_INVIERNO);
            g.fillRect(x, y + h - 3, w, 3);
        }
    }

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                  int x, int y, int w, int h, boolean isSelected) {
        g.setColor(AZUL_ESCARCHA);
        g.drawRect(x, y, w - 1, h - 1);
    }

    @Override
    protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                             int tabIndex, String title, Rectangle textRect, boolean isSelected) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(font);
        g.setColor(isSelected ? AZUL_INVIERNO : GRIS_PIZARRA);
        g.drawString(title, textRect.x, textRect.y + metrics.getAscent());
    }

    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        Insets ins = tabPane.getInsets();
        int y = ins.top + calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
        g.setColor(AZUL_ESCARCHA);
        g.drawRect(ins.left, y - 1, tabPane.getWidth() - ins.left - ins.right - 1,
                tabPane.getHeight() - y - ins.bottom);
    }

    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex,
                                       Rectangle iconRect, Rectangle textRect, boolean isSelected) {
        // sin rectángulo punteado de foco
    }
}
