package odase.frame;

import odase.frame.PropertyValuesList;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.*;

import javax.swing.*;
import java.awt.*;

/**
 * Created by vblagodarov on 27-06-17.
 */
public class PropertyValueCellRenderer implements ListCellRenderer<PropertyValuesList.PropertyValueItem> {

    private OWLCellRenderer owlCellRenderer;
    private OWLEditorKit editorKit;

    public PropertyValueCellRenderer(OWLEditorKit owlEditorKit) {
        editorKit = owlEditorKit;
        owlCellRenderer = new OWLCellRenderer(owlEditorKit, true, true);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends PropertyValuesList.PropertyValueItem> list, PropertyValuesList.PropertyValueItem value, int index, boolean isSelected, boolean cellHasFocus) {
        owlCellRenderer.setOntology(editorKit.getOWLModelManager().getActiveOntology());
        owlCellRenderer.setHighlightKeywords(true);
        owlCellRenderer.setWrap(false);
        return owlCellRenderer.getListCellRendererComponent(list, value.getObject(), index, isSelected, cellHasFocus);
    }
}
