package odase.providers;

import odase.buttons.ToggleButtonUI;
import odase.editor.IPropertyValueAssertionEditor;
import odase.frame.IOWLPropertiesStackBoxLine;
import org.semanticweb.owlapi.model.*;

/**
 * Created by vblagodarov on 04-07-17.
 */
public interface IPropertyEditorProvider<TProp extends OWLPropertyExpression, TObj extends OWLObject> {

    IPropertyValueAssertionEditor<TObj> getNewEditor(OWLNamedIndividual subjectIndividual, TProp property);

    IPropertyValueAssertionEditor<TObj> getNewEditor(OWLNamedIndividual subjectIndividual, TProp property, TObj object);

    void updateInnerComponent(IOWLPropertiesStackBoxLine innerComponent, OWLNamedIndividual subject, TProp property);

    boolean canAddToInnerList(OWLNamedIndividual subjectIndividual, TProp property);

    ToggleButtonUI getToggleButtonUI(String title);

    String getTitle(OWLNamedIndividual subject, TProp property);
}
