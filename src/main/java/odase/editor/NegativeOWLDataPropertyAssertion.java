package odase.editor;

import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.*;

/**
 * Created by vblagodarov on 03-07-17.
 */
public class NegativeOWLDataPropertyAssertion extends OWLDataPropertyAssertion {
    public NegativeOWLDataPropertyAssertion(OWLEditorKit owlEditorKit,
                                            OWLNamedIndividual subjectIndividual,
                                            OWLDataProperty dataProperty) {
        super(owlEditorKit, subjectIndividual, dataProperty);
    }

    public NegativeOWLDataPropertyAssertion(OWLEditorKit owlEditorKit,
                                            OWLNamedIndividual subjectIndividual,
                                            OWLDataProperty dataProperty,
                                            OWLLiteral object) {
        super(owlEditorKit, subjectIndividual, dataProperty, object);
    }

    public OWLIndividualAxiom getAxiom(OWLLiteral individual) {
        return editorKit.getModelManager().getOWLDataFactory().getOWLNegativeDataPropertyAssertionAxiom(property,
                subject,
                individual);
    }
}
