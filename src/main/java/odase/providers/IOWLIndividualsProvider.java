package odase.providers;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by vblagodarov on 11-07-17.
 */
public interface IOWLIndividualsProvider {
    HashMap<OWLClass, Set<OWLNamedIndividual>> getPossibleRange(OWLObjectProperty property);
}
