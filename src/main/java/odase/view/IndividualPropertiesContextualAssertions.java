package odase.view;


import odase.frame.*;
import odase.providers.*;
import org.jdesktop.swingx.VerticalLayout;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.inference.ReasonerStatus;
import org.protege.editor.owl.ui.view.individual.AbstractOWLIndividualViewComponent;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.Set;

/**
 * Created by vblagodarov on 16-06-17.
 */
public class IndividualPropertiesContextualAssertions extends AbstractOWLIndividualViewComponent {

    private static final long serialVersionUID = -1228370750437540626L;
    private IPropertiesProvider propertiesProvider;
    private IOWLPropertiesStackBox<OWLObjectProperty> objectPropertiesComponent;
    private IOWLPropertiesStackBox<OWLDataProperty> dataPropertiesComponent;
    private JTextField statusBar;
    private JPanel mainPanel;
    private JScrollPane propertiesPane;

    @Override
    public OWLNamedIndividual updateView(OWLNamedIndividual individual) {
        refreshView(individual);
        if (individual != null) {
            updateReasonerInfo();
        }
        return individual;
    }

    @Override
    public void initialiseIndividualsView() throws Exception {
        setLayout(new BorderLayout());
        statusBar = new JTextField();
        statusBar.setEditable(false);
        statusBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, statusBar.getPreferredSize().height));

        OWLEditorKit editorKit = getOWLEditorKit();
        propertiesProvider = new InDomainPropertiesProvider(editorKit);


        objectPropertiesComponent = new OWLPropertiesStackedBox(editorKit, new OWLObjectPropertyEditorProvider(editorKit));
        dataPropertiesComponent = new OWLPropertiesStackedBox(editorKit, new OWLDataPropertyEditorProvider(editorKit));

        refreshView(getSelectedOWLIndividual());

        mainPanel = new JPanel(new VerticalLayout());
        mainPanel.add(objectPropertiesComponent.getComponent());
        mainPanel.add(dataPropertiesComponent.getComponent());

        propertiesPane = new JScrollPane();
        propertiesPane.setViewportView(mainPanel);
        add(createMenu(), BorderLayout.NORTH);
        add(propertiesPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.PAGE_END);
    }

    private void updateReasonerInfo() {
        ReasonerStatus status = getOWLEditorKit().getOWLModelManager().getOWLReasonerManager().getReasonerStatus();
        if (status.equals(ReasonerStatus.INITIALIZED) ||
                status.equals(ReasonerStatus.OUT_OF_SYNC) ||
                status.equals(ReasonerStatus.INCONSISTENT) ||
                status.equals(ReasonerStatus.INITIALIZATION_IN_PROGRESS)) {
            String reasonerName = getReasonerName();
            if (reasonerName == null) {
                statusBar.setText("reasoner is active");
            } else {
                statusBar.setText("reasoner " + reasonerName + " is active");
            }

            statusBar.setForeground(Color.black);
        } else {
            statusBar.setText("using Structural reasoner, the view might be incomplete");
            statusBar.setForeground(Color.red);
        }
    }

    private String getReasonerName() {
        String name = getOWLEditorKit().getOWLModelManager().getOWLReasonerManager().getCurrentReasonerName();
        if (name == null) {
            name = getOWLEditorKit().getOWLModelManager().getOWLReasonerManager().getCurrentReasoner().getReasonerName();
        }
        return name;
    }

    private void drainPropertiesPanel() {
        objectPropertiesComponent.removeAll();
        dataPropertiesComponent.removeAll();
    }


    private void refreshView(OWLNamedIndividual individual) {
        drainPropertiesPanel();
        if (individual == null) {
            return;
        }
        Set<OWLObjectProperty> objectPropertiesToDisplay = propertiesProvider.getObjectProperties(individual);
        Set<OWLDataProperty> dataPropertiesToDisplay = propertiesProvider.getDataProperties(individual);

        objectPropertiesToDisplay.forEach(property -> objectPropertiesComponent.addItem(individual, property));
        dataPropertiesToDisplay.forEach(property -> dataPropertiesComponent.addItem(individual, property));
    }

    private JComponent createMenu() {
        Font font = UIManager.getFont("Label.font");
        if (font == null) {
            font = new Font("Verdana", Font.PLAIN, 11);
        }
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JComboBox<String> displayAllOrInDomain;
        JComboBox<String> axiomType;

        final String inDomainProperties = "In domain properties";
        final String allProperties = "All properties";
        displayAllOrInDomain = new JComboBox<>(new String[]{inDomainProperties, allProperties});
        displayAllOrInDomain.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (e.getItem().equals(inDomainProperties)) {
                    propertiesProvider = new InDomainPropertiesProvider(getOWLEditorKit());
                } else {
                    propertiesProvider = new AllPropertiesProvider(getOWLEditorKit());
                }
                refreshView(getSelectedOWLIndividual());
            }
        });
        displayAllOrInDomain.setFont(font);
        panel.add(displayAllOrInDomain);
        //------------------------------------------------------------

        final String positiveAxiom = "Positive axioms";
        final String negativeAxiom = "Negative axioms";
        axiomType = new JComboBox<>(new String[]{positiveAxiom, negativeAxiom});
        axiomType.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (e.getItem().equals(negativeAxiom)) {
                    objectPropertiesComponent.setNewPropertyValuesProvider(new NegativeOWLObjectPropertyEditorProvider(getOWLEditorKit()));
                    dataPropertiesComponent.setNewPropertyValuesProvider(new NegativeOWLDataPropertyEditorProvider(getOWLEditorKit()));
                } else {
                    objectPropertiesComponent.setNewPropertyValuesProvider(new OWLObjectPropertyEditorProvider(getOWLEditorKit()));
                    dataPropertiesComponent.setNewPropertyValuesProvider(new OWLDataPropertyEditorProvider(getOWLEditorKit()));
                }
            }
        });
        axiomType.setFont(font);

        panel.add(axiomType);
        //--------------------------------------------------------------

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorderPainted(true);


        JMenu displayOptions = new JMenu("Expand/Collapse options");
        displayOptions.setFont(font);
        JMenuItem expandAll = new JMenuItem(new AbstractAction("Expand All") {
            @Override
            public void actionPerformed(ActionEvent e) {
                objectPropertiesComponent.expandAll();
                dataPropertiesComponent.expandAll();
            }
        });

        JMenuItem collapseAll = new JMenuItem(new AbstractAction("Collapse All") {
            @Override
            public void actionPerformed(ActionEvent e) {
                objectPropertiesComponent.collapseAll();
                dataPropertiesComponent.collapseAll();
            }
        });

        JMenuItem expandObjectProps = new JMenuItem(new AbstractAction("Expand Object Properties") {
            @Override
            public void actionPerformed(ActionEvent e) {
                objectPropertiesComponent.expandAll();
            }
        });

        JMenuItem collapseObjectProps = new JMenuItem(new AbstractAction("Collapse Object Properties") {
            @Override
            public void actionPerformed(ActionEvent e) {
                objectPropertiesComponent.collapseAll();
            }
        });

        JMenuItem expandDataProps = new JMenuItem(new AbstractAction("Expand Data Properties") {
            @Override
            public void actionPerformed(ActionEvent e) {
                dataPropertiesComponent.expandAll();
            }
        });

        JMenuItem collapseDataProps = new JMenuItem(new AbstractAction("Collapse Data Properties") {
            @Override
            public void actionPerformed(ActionEvent e) {
                dataPropertiesComponent.collapseAll();
            }
        });


        displayOptions.add(expandAll);
        displayOptions.add(collapseAll);
        displayOptions.add(expandObjectProps);
        displayOptions.add(expandDataProps);
        displayOptions.add(collapseObjectProps);
        displayOptions.add(collapseDataProps);

        menuBar.add(displayOptions);
        menuBar.add(Box.createHorizontalGlue());

        panel.add(menuBar);

        return panel;
    }

    @Override
    public void disposeView() {
        objectPropertiesComponent.dispose();
        dataPropertiesComponent.dispose();
    }

}
