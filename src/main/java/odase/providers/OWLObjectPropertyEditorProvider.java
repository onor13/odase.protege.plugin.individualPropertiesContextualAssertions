package odase.providers;

import odase.editor.IPropertyValueAssertionEditor;
import odase.editor.OWLObjectPropertyAssertion;
import odase.frame.IOWLPropertiesStackBoxLine;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.*;

/**
 * Created by vblagodarov on 04-07-17.
 */
public class OWLObjectPropertyEditorProvider extends OWLBaseObjectPropertyEditorProvider implements IPropertyEditorProvider<OWLObjectProperty, OWLNamedIndividual> {

    public OWLObjectPropertyEditorProvider(OWLEditorKit owlEditorKit) {
        super(owlEditorKit);
    }

    @Override
    public IPropertyValueAssertionEditor<OWLNamedIndividual> getNewEditor(OWLNamedIndividual subjectIndividual,
                                                                          OWLObjectProperty property) {
        return new OWLObjectPropertyAssertion(editorKit, subjectIndividual, property);
    }

    @Override
    public IPropertyValueAssertionEditor<OWLNamedIndividual> getNewEditor(OWLNamedIndividual subjectIndividual,
                                                                          OWLObjectProperty property,
                                                                          OWLNamedIndividual object) {
        return new OWLObjectPropertyAssertion(editorKit, subjectIndividual, property, object);
    }

    @Override
    public void updateInnerComponent(IOWLPropertiesStackBoxLine innerComponent,
                                     OWLNamedIndividual subject,
                                     OWLObjectProperty property) {
        super.updateInnerComponent(innerComponent, subject, property, AxiomType.OBJECT_PROPERTY_ASSERTION);
    }

    @Override
    public String getTitle(OWLNamedIndividual subject, OWLObjectProperty property) {
        return editorKit.getOWLModelManager().getRendering(subject) + ": Object property " + editorKit.getOWLModelManager().getRendering(property) + " assertion";
    }
}
