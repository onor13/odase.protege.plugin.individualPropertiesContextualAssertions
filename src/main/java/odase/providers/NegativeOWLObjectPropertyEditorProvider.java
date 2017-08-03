package odase.providers;

import odase.editor.*;
import odase.frame.IOWLPropertiesStackBoxLine;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.*;

/**
 * Created by vblagodarov on 04-07-17.
 */
public class NegativeOWLObjectPropertyEditorProvider extends OWLBaseObjectPropertyEditorProvider implements IPropertyEditorProvider<OWLObjectProperty, OWLNamedIndividual> {

    public NegativeOWLObjectPropertyEditorProvider(OWLEditorKit owlEditorKit) {
        super(owlEditorKit);
    }

    @Override
    public IPropertyValueAssertionEditor<OWLNamedIndividual> getNewEditor(OWLNamedIndividual subjectIndividual,
                                                                          OWLObjectProperty property) {
        return new NegativeOWLObjectPropertyAssertion(editorKit, subjectIndividual, property);
    }

    @Override
    public IPropertyValueAssertionEditor<OWLNamedIndividual> getNewEditor(OWLNamedIndividual subjectIndividual,
                                                                          OWLObjectProperty property,
                                                                          OWLNamedIndividual object) {
        return new NegativeOWLObjectPropertyAssertion(editorKit, subjectIndividual, property, object);
    }

    @Override
    public void updateInnerComponent(IOWLPropertiesStackBoxLine innerComponent, OWLNamedIndividual subject,
                                     OWLObjectProperty property) {
        super.updateInnerComponent(innerComponent, subject, property, AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION);
    }

    @Override
    public String getTitle(OWLNamedIndividual subject, OWLObjectProperty property) {
        return editorKit.getOWLModelManager().getRendering(subject) + ": Negative Object property " + editorKit.getOWLModelManager().getRendering(property) + " assertion";
    }
}
