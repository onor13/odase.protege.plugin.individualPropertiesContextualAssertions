package odase.providers;

import odase.tools.OWLDataInDomainRange;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by vblagodarov on 11-07-17.
 */
public class InRangePossibleObjectPropertyValuesProvider implements IOWLIndividualsProvider {

    OWLEditorKit ek;

    public InRangePossibleObjectPropertyValuesProvider(OWLEditorKit editorKit) {
        ek = editorKit;
    }

    public HashMap<OWLClass, Set<OWLNamedIndividual>> getPossibleRange(OWLObjectProperty property) {
        OWLReasoner reasoner = OWLDataInDomainRange.getReasoner(ek);
        if (!reasoner.isConsistent()) {
            return new HashMap<>();
        }
        OWLReasoner structuralReasoner = OWLDataInDomainRange.getStructuralReasoner(ek);

        Set<OWLObjectPropertyRangeAxiom> propertyRanges = ek.getModelManager().getActiveOntology().getAxioms(AxiomType.OBJECT_PROPERTY_RANGE, Imports.INCLUDED).
                stream().filter(opera -> opera.getProperty().equals(property)).collect(Collectors.toSet());

        Set<OWLClassExpression> cnf = new HashSet<>();
        for (OWLObjectPropertyRangeAxiom opra : propertyRanges) {
            cnf.add(opra.getRange());
        }
        OWLObjectIntersectionOf range = ek.getOWLModelManager().getOWLDataFactory().getOWLObjectIntersectionOf(cnf);
        if (range.isOWLThing()) {
            return AllOWLIndividualsProvider.getAll(ek);
        }
        HashMap<OWLClass, Set<OWLNamedIndividual>> resultMap = new HashMap<>();

        for (OWLClassExpression ce : range.asDisjunctSet()) {
            //TODO: fix it: ce.getClassesInSignature() is not correct, but don't have a better solution
            for (OWLClass owlClass : ce.getClassesInSignature()) {
                //A non structural reasoner might have a delay in synchronization
                Set<OWLNamedIndividual> inds = structuralReasoner.getInstances(owlClass, false).getFlattened();
                inds.addAll(reasoner.getInstances(owlClass, false).getFlattened());

                resultMap.put(owlClass, inds);
            }
        }
        return resultMap;
    }
}
