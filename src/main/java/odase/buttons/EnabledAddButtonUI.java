package odase.buttons;

import java.awt.*;

/**
 * Created by vblagodarov on 12-07-17.
 */
public class EnabledAddButtonUI extends AddButtonUI {

    public EnabledAddButtonUI(Dimension dimension) {
        super(dimension);
    }

    public EnabledAddButtonUI() {
        super();
    }

    @Override
    public Color getColor() {
        return Color.WHITE;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
