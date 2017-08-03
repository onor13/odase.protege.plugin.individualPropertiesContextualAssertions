package odase.frame;

import javax.swing.*;
import java.awt.*;

/**
 * Created by vblagodarov on 19-07-17.
 */
public class ComponentAndButton {

    public static JPanel get(JComponent component, JButton button) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.weightx = 0.9;
        c.ipadx = 30;
        panel.add(component, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_END;
        panel.add(button);
        return panel;
    }
}
