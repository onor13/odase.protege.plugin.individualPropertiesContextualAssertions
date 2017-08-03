package odase.tools;

import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLObject;

import javax.swing.*;
import java.awt.*;

/**
 * Created by vblagodarov on 27-06-17.
 */
public class OWLObjectToStringConverter<T extends OWLObject> extends ObjectToStringConverter implements ListCellRenderer {

    private OWLModelManager manager;

    public OWLObjectToStringConverter(OWLModelManager modelManager) {
        manager = modelManager;
    }

    @Override
    public String getPreferredStringForItem(Object item) {

        if (item != null && item instanceof OWLObject) {
            return manager.getRendering((OWLObject) item);
        }
        return null;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = new JLabel();
        if (value instanceof OWLObject) {
            label.setText(manager.getRendering((OWLObject) value));
        }
        return label;
    }
}
