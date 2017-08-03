package odase.buttons;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by vblagodarov on 06-07-17.
 */
public abstract class AddButtonUI extends BasicButtonUI {
    Dimension buttonSize;
    Color color = Color.LIGHT_GRAY;

    public AddButtonUI(Dimension dimension) {
        buttonSize = dimension;

    }

    public AddButtonUI() {
    }

    public abstract Color getColor();

    public abstract boolean isEnabled();

    @Override
    public void paint(Graphics g, JComponent c) {
        JButton button = (JButton) c;
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                color = Color.DARK_GRAY;
                button.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                color = Color.LIGHT_GRAY;
                button.repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                color = Color.GREEN.darker();
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                color = Color.LIGHT_GRAY;
                button.repaint();
            }
        });
        button.setOpaque(true);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setEnabled(isEnabled());
        if (buttonSize == null) {
            buttonSize = button.getPreferredSize();
        }

        button.setPreferredSize(buttonSize);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillOval(0, 0, buttonSize.height, buttonSize.width);
        g2.setColor(getColor());
        int height = buttonSize.height;
        int thickness = (Math.round(height / 8.0f) / 2) * 2;

        int insetX = height / 4;
        int insetY = height / 4;
        int insetHeight = height / 2;
        int insetWidth = height / 2;
        g2.fillRect(height / 2 - thickness / 2, insetY, thickness, insetHeight);
        g2.fillRect(insetX, height / 2 - thickness / 2, insetWidth, thickness);
    }
}
