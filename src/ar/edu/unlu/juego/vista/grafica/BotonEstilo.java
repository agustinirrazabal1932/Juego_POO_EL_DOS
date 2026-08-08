package ar.edu.unlu.juego.vista.grafica;

import javax.swing.*;
import java.awt.*;

public class BotonEstilo extends JButton {
    private final Color base;

    public BotonEstilo(String texto, Color base) {
        super(texto);
        this.base = base;
        setForeground(Color.WHITE);                       // texto blanco
        setFont(new Font("Arial", Font.BOLD, 16));
        setContentAreaFilled(false);                      // NO uses el fondo del L&F: pinto yo
        setFocusPainted(false);                           // sin el recuadro de foco
        setBorderPainted(false);
        setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22)); // "padding" interno
        setCursor(new Cursor(Cursor.HAND_CURSOR));        // manito al pasar por encima
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color c = base;
        if (getModel().isPressed())       c = c.darker();     // más oscuro al apretar
        else if (getModel().isRollover()) c = c.brighter();   // más claro al pasar el mouse
        g2.setColor(c);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24); // rectángulo redondeado
        g2.dispose();
        super.paintComponent(g);   // dibuja el texto ENCIMA del fondo que pinté
    }
}
