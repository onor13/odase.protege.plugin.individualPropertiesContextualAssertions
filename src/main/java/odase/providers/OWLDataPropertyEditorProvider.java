package odase.providers;

import odase.editor.IPropertyValueAssertionEditor;
import odase.editor.OWLDataPropertyAssertion;
import odase.frame.IOWLPropertiesStackBoxLine;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.*;

/**
 * Created by vblagodarov on 04-07-17.
 */
public class OWLDataPropertyEditorProvider extends OWLBaseDataPropertyEditorProvider implements IPropertyEditorProvider<OWLDataProperty, OWLLiteral> {

    public OWLDataPropertyEditorProvider(OWLEditorKit owlEditorKit) {
        super(owlEditorKit);
    }

    @Override
    public IPropertyValueAssertionEditor<OWLLiteral> getNewEditor(OWLNamedIndividual subjectIndividual,
                                                                  OWLDataProperty property) {
        return new OWLDataPropertyAssertion(editorKit, subjectIndividual, property);
    }

    @Override
    public IPropertyValueAssertionEditor<OWLLiteral> getNewEditor(OWLNamedIndividual subjectIndividual,
                                                                  OWLDataProperty property,
                                                                  OWLLiteral object) {
        return new OWLDataPropertyAssertion(editorKit, subjectIndividual, property, object);
    }

    @Override
    public void updateInnerComponent(IOWLPropertiesStackBoxLine innerComponent,
                                     OWLNamedIndividual subject,
                                     OWLDataProperty property) {
        super.updateInnerComponent(innerComponent, subject, property, AxiomType.DATA_PROPERTY_ASSERTION);
    }


    @Override
    public String getTitle(OWLNamedIndividual subject, OWLDataProperty property) {
        return editorKit.getOWLModelManager().getRendering(subject) + ": Data property " + editorKit.getOWLModelManager().getRendering(property) + " assertion";
    }
}
