package odase.providers;

import odase.tools.OWLDataInDomainRange;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

/**
 * Created by vblagodarov on 03-07-17.
 */
public class AllPropertiesProvider implements IPropertiesProvider {

    OWLEditorKit ek;

    public AllPropertiesProvider(OWLEditorKit owlEditorKit) {
        ek = owlEditorKit;
    }

    @Override
    public Set<OWLObjectProperty> getObjectProperties(OWLNamedIndividual individual) {
        return ek.getOWLModelManager().getActiveOntology().getObjectPropertiesInSignature();
    }

    @Override
    public Set<OWLDataProperty> getDataProperties(OWLNamedIndividual individual) {
        return ek.getOWLModelManager().getActiveOntology().getDataPropertiesInSignature();
    }
}
