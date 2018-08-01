package odase.tools;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.inference.ReasonerPreferences;
import org.protege.editor.owl.model.inference.ReasonerStatus;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by vblagodarov on 13-06-17.
 */
public class OWLDataInDomainRange {
    OWLEditorKit editorKit;

    public OWLDataInDomainRange(OWLEditorKit owlEditorKit) {
        editorKit = owlEditorKit;
    }

    public static OWLReasoner getReasoner(OWLEditorKit editorKit) {
        ReasonerStatus status = editorKit.getOWLModelManager().getOWLReasonerManager().getReasonerStatus();
        if (!status.equals(ReasonerStatus.REASONER_NOT_INITIALIZED) &&
                !status.equals(ReasonerStatus.NO_REASONER_FACTORY_CHOSEN)) {
            return editorKit.getModelManager().getOWLReasonerManager().getCurrentReasoner();
        } else {
            return new StructuralReasoner(editorKit.getModelManager().getActiveOntology(), new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
        }
    }

    public static OWLReasoner getStructuralReasoner(OWLEditorKit editorKit) {
        return new StructuralReasoner(editorKit.getModelManager().getActiveOntology(), new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
    }

    private OWLReasoner getReasoner() {
        return getReasoner(editorKit);
    }

    public Set<OWLNamedIndividual> getSameTypeInstances(OWLClass indClassType) {
        return getReasoner().getInstances(indClassType, true).getFlattened();

    }

    public Set<OWLClass> getIndividualType(OWLNamedIndividual individual) {
        Set<OWLClass> classes = new HashSet<>();
        for (OWLOntology ont : editorKit.getModelManager().getActiveOntologies()) {
            Set<OWLIndividualAxiom> axioms = ont.getAxioms(individual, Imports.INCLUDED).stream().
                    filter(a -> a.getAxiomType() == AxiomType.CLASS_ASSERTION).collect(Collectors.toSet());
            axioms.forEach(a -> classes.addAll(((OWLClassAssertionAxiom) a).getClassExpression().getClassesInSignature()));
        }
        return classes;
    }

    public Collection<OWLPropertyAssertionAxiomPlusIsInferred> getIndividualObjects(OWLNamedIndividual subject, OWLObjectProperty property, AxiomType filterType) {
        //Comparison between Axioms doesn't work, so need to be creative. If not we'll end up with duplicates like in Protégé 5.2
        HashMap<OWLPropertyAssertionObject, OWLPropertyAssertionAxiomPlusIsInferred> resultSet = new HashMap<>();
        if (subject == null || property == null) {
            return new HashSet<>();
        }
        Set<OWLIndividualAxiom> axioms = new HashSet<>();
        for (OWLOntology ont : editorKit.getModelManager().getActiveOntologies()) {
            axioms.addAll(ont.getAxioms(subject, Imports.INCLUDED));
        }

        for (OWLIndividualAxiom axiom : axioms) {
            if (axiom.getAxiomType() == filterType) {
                OWLPropertyAssertionAxiom propAssertAxiom = (OWLPropertyAssertionAxiom) axiom;
                if (propAssertAxiom.getProperty().equals(property)) {
                    resultSet.put(propAssertAxiom.getObject(), new OWLPropertyAssertionAxiomPlusIsInferred(propAssertAxiom));
                }
            }
        }

        Set<OWLPropertyAssertionObject> assertedSet = resultSet.keySet();
        editorKit.getOWLModelManager().getReasonerPreferences().executeTask(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_OBJECT_PROPERTY_ASSERTIONS, () -> {
            if (!editorKit.getOWLModelManager().getReasoner().isConsistent()) {
                return;
            }
            NodeSet<OWLNamedIndividual> values = getReasoner().getObjectPropertyValues(subject, property);
            for (OWLNamedIndividual ind : values.getFlattened()) {
                if (!assertedSet.contains(ind)) {
                    OWLObjectPropertyAssertionAxiom ax = editorKit.getModelManager().getOWLDataFactory().
                            getOWLObjectPropertyAssertionAxiom(property,
                                    subject,
                                    ind);
                    resultSet.put(ax.getObject(), new OWLPropertyAssertionAxiomPlusIsInferred(ax, true));
                }
            }
        });

        return resultSet.values();
    }

    public Collection<OWLPropertyAssertionAxiomPlusIsInferred> getLiteralObjects(OWLNamedIndividual subject,
                                                                                 OWLDataProperty property,
                                                                                 AxiomType filterType) {
        //Comparison between Axioms doesn't work, so need to be creative .If not we'll end up with duplicates like in Protégé 5.2
        HashMap<OWLPropertyAssertionObject, OWLPropertyAssertionAxiomPlusIsInferred> resultSet = new HashMap<>();
        if (subject == null || property == null) {
            return new HashSet<>();
        }
        Set<OWLIndividualAxiom> axioms = editorKit.getModelManager().getActiveOntology().getAxioms(subject, Imports.INCLUDED);
        for (OWLIndividualAxiom axiom : axioms) {
            if (axiom.getAxiomType() == filterType) {
                OWLPropertyAssertionAxiom propAssertAxiom = (OWLDataPropertyAssertionAxiom) axiom;
                if (propAssertAxiom.getProperty().equals(property)) {
                    resultSet.put(propAssertAxiom.getObject(), new OWLPropertyAssertionAxiomPlusIsInferred(propAssertAxiom));
                }
            }
        }

        Set<OWLPropertyAssertionObject> assertedSet = resultSet.keySet();
        editorKit.getOWLModelManager().getReasonerPreferences().executeTask(ReasonerPreferences.OptionalInferenceTask.SHOW_INFERRED_DATA_PROPERTY_ASSERTIONS, () -> {
            if (!editorKit.getOWLModelManager().getReasoner().isConsistent()) {
                return;
            }
            Set<OWLLiteral> values = getReasoner().getDataPropertyValues(subject, property);
            for (OWLLiteral object : values) {
                if (!assertedSet.contains(object)) {
                    OWLDataPropertyAssertionAxiom ax = editorKit.getModelManager().getOWLDataFactory().
                            getOWLDataPropertyAssertionAxiom(property,
                                    subject,
                                    object);
                    resultSet.put(ax.getObject(), new OWLPropertyAssertionAxiomPlusIsInferred(ax, true));
                }
            }
        });

        return resultSet.values();
    }

    public Set<OWLObjectProperty> getObjectPropertiesInDomain(OWLNamedIndividual individual) {
        Set<OWLObjectProperty> propertiesToDisplay = new HashSet<>();
        if (individual == null) {
            return propertiesToDisplay;
        }
        propertiesToDisplay.addAll(getIndividualObjectProperties(individual));

        return propertiesToDisplay;
    }

    public Set<OWLClass> getChildren(OWLClass owlClass) {
        OWLReasoner reasoner = getReasoner();
        if (!reasoner.isConsistent()) {
            return new HashSet<>();
        }
        return reasoner.getSubClasses(owlClass, true).getFlattened();
    }

    public Set<OWLClass> getDescendents(OWLClass owlClass) {
        OWLReasoner reasoner = getReasoner();
        if (!reasoner.isConsistent()) {
            return new HashSet<>();
        }
        return reasoner.getSubClasses(owlClass, false).getFlattened();
    }

    public Set<OWLDataProperty> getDataPropertiesInDomain(OWLNamedIndividual individual) {
        Set<OWLDataProperty> propertiesToDisplay = new HashSet<>();
        if (individual == null) {
            return propertiesToDisplay;
        }
        propertiesToDisplay.addAll(getIndividualDataProperties(individual));
        return propertiesToDisplay;
    }

    public boolean canAddMore(OWLNamedIndividual subjectIndividual, OWLObjectProperty objectProperty) {
        if (subjectIndividual == null || objectProperty == null) {
            return true;
        }
        //TODO: test MaxCardinality
        OWLReasoner reasoner = getReasoner();
        if (!reasoner.isConsistent()) {
            return true;
        }
        for (OWLOntology ont : editorKit.getModelManager().getActiveOntologies()) {
            if (ont.getAxioms(objectProperty, Imports.INCLUDED).stream().anyMatch(a -> a.getAxiomType() == AxiomType.FUNCTIONAL_OBJECT_PROPERTY) &&
                    reasoner.getObjectPropertyValues(subjectIndividual, objectProperty).getFlattened().size() > 0) {
                return false;
            }
        }
        return true;
    }

    public boolean canAddMore(OWLNamedIndividual individual, OWLDataProperty dataProperty) {
        if (individual == null || dataProperty == null) {
            return true;
        }
        //TODO: test MaxCardinality
        OWLReasoner reasoner = getReasoner();
        if (!reasoner.isConsistent()) {
            return true;
        }
        for (OWLOntology ont : editorKit.getModelManager().getActiveOntologies()) {
            if (ont.getAxioms(dataProperty, Imports.INCLUDED).stream().anyMatch(
                    a -> a.getAxiomType() == AxiomType.FUNCTIONAL_DATA_PROPERTY) &&
                    reasoner.getDataPropertyValues(individual, dataProperty).size() > 0) {
                return false;
            }
        }
        return true;
    }

    public Set<OWLObjectProperty> getIndividualObjectProperties(OWLNamedIndividual individual) {
        Set<OWLObjectProperty> resultSet = new HashSet<>();
        Set<OWLClass> indClassExps = getIndividualType(individual);
        Map<OWLPropertyExpression, OWLClassExpression> domains = new HashMap<>();

        for (OWLPropertyDomainAxiom axiom : editorKit.getModelManager().getActiveOntology().
                getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN, Imports.INCLUDED)) {
            OWLPropertyExpression pe = axiom.getProperty();
            if (!domains.containsKey(pe)) {
                domains.put(pe, axiom.getDomain());
            } else {
                Set<OWLClassExpression> set = new HashSet<>(domains.get(pe).asConjunctSet());
                set.add(axiom.getDomain());
                domains.put(pe, editorKit.getOWLModelManager().getOWLDataFactory().getOWLObjectIntersectionOf(set));
            }
        }

        OWLReasoner reasoner = getReasoner();
        if (!reasoner.isConsistent()) {
            return resultSet;
        }

        for(OWLClass indClassExp : indClassExps){
            for (OWLPropertyExpression p : domains.keySet()) {
                if ((reasoner.getSubClasses(domains.get(p), false).containsEntity(indClassExp) ||
                        reasoner.getEquivalentClasses(domains.get(p)).contains(indClassExp)) && p.isObjectPropertyExpression()) {
                    resultSet.addAll(p.getObjectPropertiesInSignature());
                }
            }
        }

        return resultSet;
    }

    public Set<OWLDataProperty> getIndividualDataProperties(OWLNamedIndividual individual) {
        Set<OWLDataProperty> resultSet = new HashSet<>();
        Set<OWLClass> indClassExps = getIndividualType(individual);
        Map<OWLPropertyExpression, OWLClassExpression> domains = new HashMap<>();


        for (OWLPropertyDomainAxiom axiom : editorKit.getModelManager().getActiveOntology().
                getAxioms(AxiomType.DATA_PROPERTY_DOMAIN, Imports.INCLUDED)) {
            OWLPropertyExpression pe = axiom.getProperty();
            if (!domains.containsKey(pe)) {
                domains.put(pe, axiom.getDomain());
            } else {
                Set<OWLClassExpression> set = new HashSet<>(domains.get(pe).asConjunctSet());
                set.add(axiom.getDomain());
                domains.put(pe, editorKit.getOWLModelManager().getOWLDataFactory().getOWLObjectIntersectionOf(set));
            }
        }


        OWLReasoner reasoner = getReasoner();
        if (!reasoner.isConsistent()) {
            return resultSet;
        }

        for(OWLClass indClassExp : indClassExps){
            for (OWLPropertyExpression p : domains.keySet()) {
                if ((reasoner.getSubClasses(domains.get(p), false).containsEntity(indClassExp) ||
                        reasoner.getEquivalentClasses(domains.get(p)).contains(indClassExp)) && p.isDataPropertyExpression()) {
                    resultSet.addAll(p.getDataPropertiesInSignature());
                }
            }
        }

        return resultSet;
    }
}
