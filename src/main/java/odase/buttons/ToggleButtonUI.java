package odase.buttons;

import org.jdesktop.swingx.icon.EmptyIcon;

import javax.swing.*;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;

/**
 * Created by vblagodarov on 03-07-17.
 */
public abstract class ToggleButtonUI extends BasicToggleButtonUI {
    private String text;
    private static Icon emptyIcon = new EmptyIcon();

    public ToggleButtonUI(String label) {
        text = label;
    }

    protected abstract Color getPropertyColor();

    @Override
    public void paint(Graphics g, JComponent c) {
        JToggleButton button = (JToggleButton) c;
        button.setText(text);
        button.setFont(c.getFont().deriveFont(Font.BOLD));
        button.setBackground(Color.WHITE);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setDisabledIcon(emptyIcon);
        g.setColor(getPropertyColor());
        //TODO: getX and getY always 0 ???
        g.fillRect(button.getX(), button.getY(), 10, button.getHeight());

        super.paint(g, c);
    }
}
