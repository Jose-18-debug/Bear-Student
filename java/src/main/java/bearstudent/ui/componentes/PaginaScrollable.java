package bearstudent.ui.componentes;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;

/** Panel que se ajusta al ancho de la ventana y, si la ventana es baja, se puede deslizar hacia abajo. */
class PaginaScrollable extends JPanel implements Scrollable {
    PaginaScrollable() {
        super(new BorderLayout());
        setOpaque(false);
    }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle area, int orientacion, int direccion) { return 24; }
    @Override public int getScrollableBlockIncrement(Rectangle area, int orientacion, int direccion) {
        return orientacion == SwingConstants.VERTICAL ? area.height : area.width;
    }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }

    /** Si la ventana es más alta que el contenido, lo estira; si es más baja, deja que se deslice. */
    @Override public boolean getScrollableTracksViewportHeight() {
        return getParent() instanceof JViewport vp && vp.getHeight() > getPreferredSize().height;
    }
}
