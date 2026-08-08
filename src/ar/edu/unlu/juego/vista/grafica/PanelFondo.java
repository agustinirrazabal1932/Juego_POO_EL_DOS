package ar.edu.unlu.juego.vista.grafica;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class PanelFondo extends JPanel {
    private Image fondo;   // null si no hay archivo

    public PanelFondo(String ruta) {
        try {
            if (new File(ruta).exists()) fondo = new ImageIcon(ruta).getImage();
        } catch (Exception e) { /* sin imagen, usa gradiente */ }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (fondo != null) {
            g.drawImage(fondo, 0, 0, getWidth(), getHeight(), this);
        } else {
            // fallback: degradé oscuro (por si todavía no pusiste imagen)
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(new GradientPaint(0, 0, new Color(20, 30, 60),
                    0, getHeight(), new Color(10, 10, 20)));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
