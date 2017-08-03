package odase.editor;

import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import java.util.Collection;

/**
 * Created by vblagodarov on 30-06-17.
 */
public interface IPropertyValueAssertionDetails<T extends OWLObject> {
    OWLIndividualAxiom getAxiom(T individual);

    OWLNamedIndividual getSubject();

    OWLProperty getProperty();

    Collection<T> getOWLObjectValues();
}
