package odase.providers;

import odase.buttons.DisabledAddButtonUI;
import odase.buttons.EnabledAddButtonUI;
import odase.buttons.ObjectPropertyToggleButtonUI;
import odase.buttons.ToggleButtonUI;
import odase.frame.PropertyValuesList;
import odase.frame.IOWLPropertiesStackBoxLine;
import odase.tools.OWLDataInDomainRange;
import odase.tools.OWLPropertyAssertionAxiomPlusIsInferred;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;

/**
 * Created by vblagodarov on 10-07-17.
 */
public abstract class OWLBaseObjectPropertyEditorProvider {

    protected OWLEditorKit editorKit;
    protected OWLDataInDomainRange logicProvider;

    protected OWLBaseObjectPropertyEditorProvider(OWLEditorKit owlEditorKit) {
        editorKit = owlEditorKit;
        logicProvider = new OWLDataInDomainRange(owlEditorKit);
    }

    public ToggleButtonUI getToggleButtonUI(String title) {
        return new ObjectPropertyToggleButtonUI(title);
    }

    public boolean canAddToInnerList(OWLNamedIndividual subjectIndividual, OWLObjectProperty property) {
        return logicProvider.canAddMore(subjectIndividual, property);
    }

    public void updateInnerComponent(IOWLPropertiesStackBoxLine innerComponent,
                                     OWLNamedIndividual subject,
                                     OWLObjectProperty property,
                                     AxiomType axiomType) {
        DefaultListModel<PropertyValuesList.PropertyValueItem> listModel = new DefaultListModel<>();
        for (OWLPropertyAssertionAxiomPlusIsInferred item : logicProvider.getIndividualObjects(subject,
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
