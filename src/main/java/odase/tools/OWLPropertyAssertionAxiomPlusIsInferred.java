package odase.tools;

import org.semanticweb.owlapi.model.OWLPropertyAssertionAxiom;

/**
 * Created by vblagodarov on 10-07-17.
 */
public class OWLPropertyAssertionAxiomPlusIsInferred {
    private OWLPropertyAssertionAxiom axiom;
    private boolean isInf;

    public OWLPropertyAssertionAxiomPlusIsInferred(OWLPropertyAssertionAxiom assertionAxiom) {
        this(assertionAxiom, false);
    }

    public OWLPropertyAssertionAxiomPlusIsInferred(OWLPropertyAssertionAxiom assertionAxiom, boolean isInferred) {
        axiom = assertionAxiom;
        isInf = isInferred;
    }

    public OWLPropertyAssertionAxiom getAxiom() {
        return axiom;
    }

    public boolean isInferred() {
        return isInf;
    }
}
