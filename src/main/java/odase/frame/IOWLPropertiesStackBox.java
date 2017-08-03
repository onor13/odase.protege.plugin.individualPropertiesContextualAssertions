package odase.frame;

import odase.providers.IPropertyEditorProvider;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLProperty;

import javax.swing.*;

/**
 * Created by vblagodarov on 03-07-17.
 */
public interface IOWLPropertiesStackBox<T extends OWLProperty> {
    void addItem(OWLNamedIndividual subject, T property);

    void setNewPropertyValuesProvider(IPropertyEditorProvider provider);

    void removeAll();

    void dispose();

    void collapseAll();

    void expandAll();

    JComponent getComponent();
}
