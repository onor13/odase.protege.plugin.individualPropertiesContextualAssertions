package odase.editor;

import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.*;

/**
 * Created by vblagodarov on 03-07-17.
 */
public class NegativeOWLObjectPropertyAssertion extends OWLObjectPropertyAssertion {
    public NegativeOWLObjectPropertyAssertion(OWLEditorKit owlEditorKit,
                                              OWLNamedIndividual subjectIndividual,
                                              OWLObjectProperty objectProperty) {
        super(owlEditorKit, subjectIndividual, objectProperty);
    }

    public NegativeOWLObjectPropertyAssertion(OWLEditorKit owlEditorKit,
                                              OWLNamedIndividual subjectIndividual,
                                              OWLObjectProperty objectProperty,
                                              OWLIndividual object) {
        super(owlEditorKit, subjectIndividual, objectProperty, object);
    }

    public OWLIndividualAxiom getAxiom(OWLNamedIndividual individual) {
        return editorKit.getModelManager().getOWLDataFactory().getOWLNegativeObjectPropertyAssertionAxiom(property,
                subject,
                individual);
    }
}
