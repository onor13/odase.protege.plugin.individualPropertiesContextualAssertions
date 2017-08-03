package odase.providers;

import odase.buttons.DataPropertyToggleButtonUI;
import odase.buttons.DisabledAddButtonUI;
import odase.buttons.EnabledAddButtonUI;
import odase.buttons.ToggleButtonUI;
import odase.frame.PropertyValuesList;
import odase.frame.IOWLPropertiesStackBoxLine;
import odase.tools.OWLDataInDomainRange;
import odase.tools.OWLPropertyAssertionAxiomPlusIsInferred;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import javax.swing.*;

/**
 * Created by vblagodarov on 10-07-17.
 */
public class OWLBaseDataPropertyEditorProvider {

    protected OWLEditorKit editorKit;
    protected OWLDataInDomainRange logicProvider;

    public OWLBaseDataPropertyEditorProvider(OWLEditorKit owlEditorKit) {
        editorKit = owlEditorKit;
        logicProvider = new OWLDataInDomainRange(owlEditorKit);
    }

    public boolean canAddToInnerList(OWLNamedIndividual subjectIndividual, OWLDataProperty property) {
        return logicProvider.canAddMore(subjectIndividual, property);
    }

    public ToggleButtonUI getToggleButtonUI(String title) {
        return new DataPropertyToggleButtonUI(title);
    }

    public void updateInnerComponent(IOWLPropertiesStackBoxLine innerComponent,
                                     OWLNamedIndividual subject,
                                     OWLDataProperty property, AxiomType axiomType) {
        DefaultListModel<PropertyValuesList.PropertyValueItem> listModel = new DefaultListModel<>();
        for (OWLPropertyAssertionAxiomPlusIsInferred item : logicProvider.getLiteralObjects(subject,
                property,
                axiomType)) {
            listModel.addElement(innerComponent.getList().create(editorKit.getModelManager(), item.getAxiom(), item.isInferred()));
        }
        if (canAddToInnerList(subject, property)) {
            innerComponent.getButton().setUI(new EnabledAddButtonUI());
        } else {
            innerComponent.getButton().setUI(new DisabledAddButtonUI());
        }

        innerComponent.getButton().setEnabled(canAddToInnerList(subject, property));
        innerComponent.getList().setModel(listModel);
        innerComponent.setEnableToggleAction(!innerComponent.getList().isEmpty());
    }
}
