package odase.providers;

import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by vblagodarov on 11-07-17.
 */
public class AllOWLIndividualsProvider implements IOWLIndividualsProvider {

    OWLEditorKit ek;

    public AllOWLIndividualsProvider(OWLEditorKit editorKit) {
        ek = editorKit;
    }

    public HashMap<OWLClass, Set<OWLNamedIndividual>> getPossibleRange(OWLObjectProperty property) {
        return getAll(ek);
    }

    public static HashMap<OWLClass, Set<OWLNamedIndividual>> getAll(OWLEditorKit editorKit) {
        HashMap<OWLClass, Set<OWLNamedIndividual>> resultMap = new HashMap<>();
        for (OWLClass owlClass : editorKit.getModelManager().getActiveOntology().getClassesInSignature(Imports.INCLUDED)) {
            Set<OWLNamedIndividual> set = new HashSet<>();
            for (OWLClassAssertionAxiom axiom : editorKit.getModelManager().getActiveOntology().getAxioms(AxiomType.CLASS_ASSERTION, Imports.INCLUDED)) {
                if (axiom.getClassExpression().equals(owlClass)) {
                    OWLNamedIndividual namedIndividual = axiom.getIndividual().asOWLNamedIndividual();
                    if (namedIndividual != null) {
                        set.add(namedIndividual);
                    }
                }
            }
            resultMap.put(owlClass, set);
        }
        return resultMap;
    }
}
