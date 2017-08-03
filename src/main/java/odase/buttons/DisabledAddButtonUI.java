package odase.buttons;

import java.awt.*;

/**
 * Created by vblagodarov on 12-07-17.
 */
public class DisabledAddButtonUI extends AddButtonUI {
    public DisabledAddButtonUI(Dimension dimension) {
        super(dimension);
    }

    public DisabledAddButtonUI() {
        super();
    }

    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
