package odase.buttons;

import org.protege.editor.owl.ui.renderer.OWLSystemColors;

import java.awt.*;

/**
 * Created by vblagodarov on 03-07-17.
 */
public class ObjectPropertyToggleButtonUI extends ToggleButtonUI {

    public ObjectPropertyToggleButtonUI(String label) {
        super(label);
    }

    @Override
    protected Color getPropertyColor() {
        return OWLSystemColors.getOWLObjectPropertyColor();
    }
}
