package odase.providers;

import odase.tools.OWLDataInDomainRange;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import java.util.Set;

/**
 * Created by vblagodarov on 03-07-17.
 */
public class InDomainPropertiesProvider implements IPropertiesProvider {

    OWLDataInDomainRange logicProvider;

    public InDomainPropertiesProvider(OWLEditorKit owlEditorKit) {
        logicProvider = new OWLDataInDomainRange(owlEditorKit);
    }

    @Override
    public Set<OWLObjectProperty> getObjectProperties(OWLNamedIndividual individual) {
        return logicProvider.getObjectPropertiesInDomain(individual);
    }

    @Override
    public Set<OWLDataProperty> getDataProperties(OWLNamedIndividual individual) {
        return logicProvider.getDataPropertiesInDomain(individual);
    }
}
