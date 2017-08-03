package odase.editor;

import org.protege.editor.core.ui.util.VerifiedInputEditor;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;

/**
 * Created by vblagodarov on 22-06-17.
 */
public interface IPropertyValueAssertionEditor<T extends OWLObject> extends VerifiedInputEditor, IPropertyValueAssertionDetails<T> {

    JComponent getEditorComponent();

    void dispose();

    boolean isValidObject();

    void handleEditingFinished(OWLPropertyAssertionAxiom previousAxiom);

    OWLEditorKit getEditorKit();
}
