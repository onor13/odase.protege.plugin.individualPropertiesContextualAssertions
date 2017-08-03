package odase.providers;

import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import java.util.Set;

/**
 * Created by vblagodarov on 03-07-17.
 */
public interface IPropertiesProvider {
    Set<OWLObjectProperty> getObjectProperties(OWLNamedIndividual individual);

    Set<OWLDataProperty> getDataProperties(OWLNamedIndividual individual);
}
