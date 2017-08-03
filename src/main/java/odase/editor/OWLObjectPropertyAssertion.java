package odase.editor;

import odase.frame.ComponentAndButton;
import odase.providers.AllOWLIndividualsProvider;
import odase.providers.IOWLIndividualsProvider;
import odase.providers.InRangePossibleObjectPropertyValuesProvider;
import odase.tools.OWLDataInDomainRange;
import odase.tools.OWLObjectToStringConverter;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.protege.editor.core.ui.util.InputVerificationStatusChangedListener;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.axiom.FreshActionStrategySelector;
import org.protege.editor.owl.model.axiom.FreshAxiomLocationPreferences;
import org.protege.editor.owl.model.axiom.FreshAxiomLocationStrategy;
import org.protege.editor.owl.model.entity.OWLEntityCreationSet;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProviderListener;
import org.protege.editor.owl.ui.renderer.OWLCellRendererSimple;
import org.protege.editor.owl.ui.tree.OWLModelManagerTree;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.protege.editor.owl.ui.tree.OWLObjectTreeCellRenderer;
import org.protege.editor.owl.ui.tree.OWLObjectTreeNode;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;

import static org.protege.editor.owl.model.selection.OWLClassAssertionSelectionModel.logger;

/**
 * Created by vblagodarov on 21-06-17.
 */
public class OWLObjectPropertyAssertion implements IPropertyValueAssertionEditor<OWLNamedIndividual> {
    private JComponent mainPanel;
    protected OWLEditorKit editorKit;
    protected OWLNamedIndividual subject;
    protected OWLObjectProperty property;
    private JComboBox<OWLNamedIndividual> inputField;
    private DefaultComboBoxModel<OWLNamedIndividual> inputFieldModel = new DefaultComboBoxModel<>();
    private OWLIndividualsList individualsToInsert;
    private JPanel leftPanel;
    private JLabel errorLabel = new JLabel(" ");
    private InputVerificationStatusChangedListener verificationStatusChangedListener = newState -> {
    };

    private OWLObjectTree<OWLObject> tree;
    private TreeSelectionListener listener = e -> transmitSelection();
    private TreeClickListener mouseListener;
    private HierarchyProviderWrapper hierarchyProvider;

    public OWLObjectPropertyAssertion(OWLEditorKit owlEditorKit, OWLNamedIndividual subjectIndividual, OWLObjectProperty objectProperty) {
        editorKit = owlEditorKit;
        subject = subjectIndividual;
        property = objectProperty;
        individualsToInsert = new OWLIndividualsList(editorKit);
    }

    public OWLObjectPropertyAssertion(OWLEditorKit owlEditorKit,
                                      OWLNamedIndividual subjectIndividual,
                                      OWLObjectProperty objectProperty, OWLIndividual object) {
        this(owlEditorKit, subjectIndividual, objectProperty);
        initialize();
        inputField.setSelectedItem(object);
    }

    private void initialize() {
        hierarchyProvider = new HierarchyProviderWrapper(editorKit, property,
                new InRangePossibleObjectPropertyValuesProvider(editorKit));
        tree = new OWLModelManagerTree<>(editorKit, hierarchyProvider);
        tree.setCellRenderer(new OWLObjectTreeCellRenderer(editorKit));
        tree.expandRow(0);
        tree.addTreeSelectionListener(listener);
        mouseListener = new TreeClickListener(editorKit);
        tree.addMouseListener(mouseListener);


        Set<OWLNamedIndividual> possibleValues = new HashSet<>();
        hierarchyProvider.getPossibleRange(property).values().forEach(set -> possibleValues.addAll(set));
        OWLObjectToStringConverter<OWLNamedIndividual> converter = new OWLObjectToStringConverter(editorKit.getModelManager());

        inputFieldModel = new DefaultComboBoxModel<>();
        possibleValues.forEach(individual -> inputFieldModel.addElement(individual));
        inputField = new JComboBox<>(inputFieldModel);
        inputField.setRenderer(converter);
        inputField.setEditable(true);
        inputField.addActionListener(e -> updateAndValidateSelection());
        AutoCompleteDecorator.decorate(inputField, converter);

        final String switchToAll = "Switch to all values";
        JToggleButton inDomainAllSwitch = new JToggleButton(switchToAll);
        inDomainAllSwitch.setSelected(false);
        //TODO: update the size of frame, based on the modification of the tree size.
        inDomainAllSwitch.addActionListener(e -> {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            if (abstractButton.getModel().isSelected()) {
                inDomainAllSwitch.setText("Switch to values limited by range");
                IOWLIndividualsProvider provider = new AllOWLIndividualsProvider(editorKit);
                hierarchyProvider.setNewIndividualsProvider(provider);
                updateView(property, provider);
            } else {
                inDomainAllSwitch.setText(switchToAll);
                IOWLIndividualsProvider provider = new InRangePossibleObjectPropertyValuesProvider(editorKit);
                hierarchyProvider.setNewIndividualsProvider(provider);
                updateView(property, provider);
            }
        });

        errorLabel.setForeground(Color.RED);
        leftPanel = new JPanel(new VerticalLayout());
        leftPanel.setPreferredSize(new Dimension(300, 300));
        leftPanel.add(inDomainAllSwitch);

        JButton addButton = new JButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object selected = inputField.getSelectedItem();
                if (selected == null || !(selected instanceof OWLNamedIndividual)) {
                    return;
                }
                if (individualsToInsert.addElement((OWLNamedIndividual) selected)) {
                    leftPanel.updateUI();
                }
            }
        });

        addButton.setText("Add");

        leftPanel.add(ComponentAndButton.get(inputField, addButton));

        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setPreferredSize(new Dimension(200, 15));
        leftPanel.add(separator);
        leftPanel.add(errorLabel);
        leftPanel.add(new JScrollPane(individualsToInsert.getComponent()));
        JLabel usageInfo = new JLabel(UIManager.getIcon("OptionPane.informationIcon"));
        usageInfo.setToolTipText("<html> The object property assertion possibilities: <ul>" +
                "<li>Double click on one or multiple individuals in the displayed tree</li>" +
                "<li>Enter the name of the individual</li>" +
                " </ul>" +
                "Double click on the individual in the list will remove it. </br>" +
                "If the individual you would like to insert is not created yet, You can do it by using a right click on one of the classes in the displayed in the tree</html>");
        leftPanel.add(usageInfo);
        updateAndValidateSelection();
        mainPanel = new JPanel(new BorderLayout());
        //Without this extra panel, the empty space will not be filled.
        mainPanel.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, new JScrollPane(tree)));
    }

    private void updateView(OWLObjectProperty property,
                            IOWLIndividualsProvider provider) {
        Set<OWLNamedIndividual> possibleValues = new HashSet<>();
        provider.getPossibleRange(property).values().forEach(set -> possibleValues.addAll(set));
        inputFieldModel.removeAllElements();
        possibleValues.forEach(individual -> inputFieldModel.addElement(individual));
        if (!possibleValues.isEmpty()) {
            inputField.setSelectedItem(possibleValues.stream().findFirst().get());
        }

        tree.reload();
    }

    private void transmitSelection() {
        OWLObject obj = tree.getSelectedOWLObject();
        if (obj != null && obj instanceof OWLNamedIndividual && !inputField.getSelectedItem().equals(obj)) {
            inputField.setSelectedItem(obj);
        }
    }

    private void updateAndValidateSelection() {
        if (subject == null || property == null) {
            verificationStatusChangedListener.verifiedStatusChanged(false);
            return;
        }

        Object selectedItem = inputField.getSelectedItem();
        if (selectedItem == null) {
            errorLabel.setText("Invalid individual name");
        } else {
            errorLabel.setText(" ");
            if (selectedItem instanceof OWLObject && !selectedItem.equals(tree.getSelectedOWLObject())) {
                tree.setSelectedOWLObject((OWLObject) selectedItem);
            }
        }

        if (individualsToInsert.getSize() == 0) {
            errorLabel.setText("List of individuals to insert is empty");
            verificationStatusChangedListener.verifiedStatusChanged(false);
            return;
        }

        verificationStatusChangedListener.verifiedStatusChanged(true);
    }

    public OWLIndividualAxiom getAxiom(OWLNamedIndividual individual) {
        return editorKit.getModelManager().getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(property,
                subject,
                individual);
    }

    @Override
    public boolean isValidObject() {
        return getOWLObjectValues().size() > 0;
    }


    @Override
    public OWLNamedIndividual getSubject() {
        return subject;
    }

    @Override
    public OWLProperty getProperty() {
        return property;
    }

    public JComponent getEditorComponent() {
        if (mainPanel == null) {
            initialize();
        }
        return mainPanel;
    }

    public void addStatusChangedListener(InputVerificationStatusChangedListener listener) {
        if (listener != null) {
            this.verificationStatusChangedListener = listener;
        } else {
            verificationStatusChangedListener = (newState) -> {
            };
        }
        updateAndValidateSelection();
    }

    public void removeStatusChangedListener(InputVerificationStatusChangedListener listener) {
        this.verificationStatusChangedListener = (newState) -> {
        };
    }

    public void dispose() {
        tree.removeTreeSelectionListener(listener);
        tree.removeMouseListener(mouseListener);
        tree.dispose();
    }

    public void handleEditingFinished(OWLPropertyAssertionAxiom previousAxiom) {
        Collection<OWLNamedIndividual> individuals = getOWLObjectValues();
        for (OWLNamedIndividual individual : individuals) {
            final OWLIndividualAxiom axiom = getAxiom(individual);
            FreshAxiomLocationPreferences prefs = FreshAxiomLocationPreferences.getPreferences();
            FreshActionStrategySelector strategySelector = new FreshActionStrategySelector(prefs, editorKit);
            FreshAxiomLocationStrategy strategy = strategySelector.getFreshAxiomLocationStrategy();
            OWLOntology ontology = strategy.getFreshAxiomLocation(axiom, editorKit.getModelManager());

            editorKit.getOWLModelManager().applyChange(new AddAxiom(ontology, axiom));
            if (previousAxiom != null) {
                editorKit.getOWLModelManager().applyChange(new RemoveAxiom(ontology, previousAxiom));
            }
            if (!editorKit.getOWLModelManager().getActiveOntology().containsAxiom(axiom)) {
                logger.warn("Editing of an axiom finished, but the axiom was not added to the active ontology. Axiom: {}.", axiom);
            }
        }
    }

    @Override
    public Collection<OWLNamedIndividual> getOWLObjectValues() {
        return individualsToInsert.getAll();
    }

    public OWLEditorKit getEditorKit() {
        return editorKit;
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof IPropertyValueAssertionEditor) {
            IPropertyValueAssertionEditor editor = ((IPropertyValueAssertionEditor) o);
            return (editor.getSubject().equals(this.getSubject()) &&
                    editor.getProperty().equals(this.getProperty()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getSubject().getIRI().hashCode() ^ getProperty().getIRI().hashCode();
    }

    public class OWLIndividualsList {
        private JList<OWLNamedIndividual> individuals = new JList<>();
        private DefaultListModel<OWLNamedIndividual> individualsListModel = new DefaultListModel<>();

        public OWLIndividualsList(OWLEditorKit owlEditorKit) {
            super();
            editorKit = owlEditorKit;

            individuals.setCellRenderer(new OWLCellRendererSimple(owlEditorKit));
            individuals.setModel(individualsListModel);
            individuals.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        JList list = (JList) e.getSource();
                        int index = list.locationToIndex(e.getPoint());
                        if (index >= 0 && index < individualsListModel.size()) {
                            individualsListModel.remove(index);
                        }
                    }
                }
            });

            individualsListModel.addListDataListener(new ListDataListener() {
                @Override
                public void intervalAdded(ListDataEvent e) {
                    updateAndValidateSelection();
                }

                @Override
                public void intervalRemoved(ListDataEvent e) {
                    updateAndValidateSelection();
                }

                @Override
                public void contentsChanged(ListDataEvent e) {
                    updateAndValidateSelection();
                }
            });
            individuals.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }


        public boolean addElement(OWLNamedIndividual individual) {
            if (individual != null && !individualsListModel.contains(individual)) {
                individualsListModel.addElement(individual);
                return true;
            }
            return false;
        }

        public Collection<OWLNamedIndividual> getAll() {
            Set<OWLNamedIndividual> set = new HashSet<>();
            for (int i = 0; i < individualsListModel.getSize(); i++) {
                set.add(individualsListModel.get(i));
            }
            return set;
        }

        public int getSize() {
            return individualsListModel.getSize();
        }

        public JComponent getComponent() {
            return individuals;
        }

    }

    private class HierarchyProviderWrapper implements OWLObjectHierarchyProvider<OWLObject>, IOWLIndividualsProvider {

        private final List<OWLObjectHierarchyProviderListener<OWLObject>> listeners = new ArrayList<>();
        private OWLObjectProperty property;
        private OWLEditorKit editorKit;
        private IOWLIndividualsProvider provider;
        private OWLDataInDomainRange logicProvider;
        private Set<OWLObject> roots;
        private HashMap<OWLClass, Set<OWLNamedIndividual>> classToInd;
        private HashMap<OWLIndividual, OWLClass> indToClass;

        private OWLOntology ontology;

        //For some reason only getRoots and getChildren is used
        public HierarchyProviderWrapper(OWLEditorKit owlEditorKit, OWLObjectProperty objectProperty, IOWLIndividualsProvider individualsProvider) {
            logicProvider = new OWLDataInDomainRange(owlEditorKit);
            editorKit = owlEditorKit;
            property = objectProperty;
            provider = individualsProvider;
            ontology = editorKit.getOWLModelManager().getActiveOntology();
            initialize();
        }

        public void setNewIndividualsProvider(IOWLIndividualsProvider individualsProvider) {
            provider = individualsProvider;
            initialize();
        }

        public void reload() {
            initialize();
        }

        private void initialize() {
            roots = new HashSet<>();
            indToClass = new HashMap<>();
            classToInd = provider.getPossibleRange(property);
            for (Map.Entry<OWLClass, Set<OWLNamedIndividual>> pair : classToInd.entrySet()) {
                for (OWLIndividual individual : pair.getValue()) {
                    indToClass.put(individual, pair.getKey());
                }
            }

            Set<OWLClass> classSet = classToInd.keySet();
            roots = new HashSet<>();
            roots.addAll(classSet);

            for (OWLClass c : classSet) {
                for (OWLClass subClass : logicProvider.getChildren(c)) {
                    if (classSet.contains(subClass)) {
                        roots.remove(subClass);
                    }
                }
            }
        }

        @Override
        public void setOntologies(Set<OWLOntology> ontologies) {

        }

        @Override
        public Set<OWLObject> getRoots() {
            return roots;
        }

        @Override
        public Set<OWLObject> getChildren(OWLObject object) {
            Set<OWLObject> results = new HashSet<>();
            if (object instanceof OWLClass) {
                OWLClass c = (OWLClass) object;
                Set<OWLClass> classes = logicProvider.getChildren(c);
                if (classes != null) {
                    for (OWLClass owlClass : classes) {
                        if (!owlClass.isOWLNothing()) {
                            results.add(owlClass);
                        }
                    }
                }
                Set<OWLNamedIndividual> ind = classToInd.get(object);
                if (ind != null) {
                    results.addAll(ind);
                }
            }
            return results;
        }

        @Override
        public Set<OWLObject> getDescendants(OWLObject object) {
            Set<OWLObject> results = new HashSet<>();
            if (object instanceof OWLClass) {
                OWLClass c = (OWLClass) object;
                Set<OWLClass> classes = logicProvider.getDescendents(c);
                for (OWLClass owlClass : classes) {
                    if (!owlClass.isOWLNothing()) {
                        results.add(owlClass);
                        Set<OWLNamedIndividual> indInClass = classToInd.get(owlClass);
                        if (indInClass != null) {
                            results.addAll(indInClass);
                        }
                    }
                }
                Set<OWLNamedIndividual> ind = classToInd.get(object);
                if (ind != null) {
                    results.addAll(ind);
                }
            }
            return results;
        }

        @Override
        public Set<OWLObject> getParents(OWLObject object) {
            Set<OWLObject> result = new HashSet<>();
            if (object instanceof OWLNamedIndividual) {
                OWLClass c = indToClass.get(object);
                if (c != null) {
                    result.add(c);
                }
            } else if (object instanceof OWLClass) {
                Collection<OWLClassExpression> superClasses = EntitySearcher.getSuperClasses((OWLClass) object, ontology);
                result.addAll(superClasses);
            }
            return result;
        }

        @Override
        public Set<OWLObject> getAncestors(OWLObject object) {
            return new HashSet<>();
        }

        @Override
        public Set<OWLObject> getEquivalents(OWLObject object) {
            return new HashSet<>();
        }

        @Override
        public Set<List<OWLObject>> getPathsToRoot(OWLObject object) {
            return new HashSet<>();
        }

        @Override
        public boolean containsReference(OWLObject object) {
            if (object instanceof OWLClass) {
                return editorKit.getOWLModelManager().getOWLHierarchyManager().getOWLClassHierarchyProvider().containsReference((OWLClass) object);
            }
            return editorKit.getOWLModelManager().getOWLHierarchyManager().getOWLIndividualsByTypeHierarchyProvider().containsReference(object);
        }

        @Override
        public void addListener(OWLObjectHierarchyProviderListener<OWLObject> listener) {
            listeners.add(listener);
        }

        @Override
        public void removeListener(OWLObjectHierarchyProviderListener<OWLObject> listener) {
            listeners.remove(listener);
        }

        @Override
        public void dispose() {
            listeners.clear();
        }

        @Override
        public HashMap<OWLClass, Set<OWLNamedIndividual>> getPossibleRange(OWLObjectProperty property) {
            return classToInd;
        }
    }

    private class TreeClickListener implements MouseListener {

        OWLEditorKit ek;

        public TreeClickListener(OWLEditorKit editorKit) {
            ek = editorKit;
        }

        private void popupMenu(MouseEvent e) {
            OWLObject selected = getSelected(e);
            if (selected == null || !(selected instanceof OWLClass)) {
                return;
            }

            OWLClass selectedClass = (OWLClass) selected;
            JPopupMenu menu = new JPopupMenu();
            JMenuItem menuItem = new JMenuItem("Add individual to " + ek.getModelManager().getRendering(selectedClass));
            menuItem.addActionListener(e1 -> {
                OWLEntityCreationSet<OWLNamedIndividual> set = editorKit.getOWLWorkspace().createOWLIndividual();
                if (set == null) {
                    return;
                }
                List<OWLOntologyChange> changes = new ArrayList<>();
                changes.addAll(set.getOntologyChanges());
                if (!selectedClass.isOWLThing()) {
                    OWLAxiom typeAxiom = editorKit.getOWLModelManager().getOWLDataFactory().
                            getOWLClassAssertionAxiom(selectedClass, set.getOWLEntity());
                    changes.add(new AddAxiom(editorKit.getOWLModelManager().getActiveOntology(), typeAxiom));
                }
                editorKit.getOWLModelManager().applyChanges(changes);
                hierarchyProvider.reload();
                tree.reload();
                inputFieldModel.addElement(set.getOWLEntity());
                inputField.setSelectedItem(set.getOWLEntity());
            });
            menu.add(menuItem);
            menu.show(e.getComponent(), e.getX(), e.getY());
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() != 2) {
                return;
            }
            OWLObject selected = getSelected(e);
            if (selected != null && selected instanceof OWLNamedIndividual) {
                individualsToInsert.addElement(((OWLNamedIndividual) selected));
                leftPanel.updateUI();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                popupMenu(e);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        private OWLObject getSelected(MouseEvent e) {
            OWLObjectTree<OWLObject> sourceTree = (OWLObjectTree<OWLObject>) e.getSource();
            TreePath path = sourceTree.getPathForLocation(e.getX(), e.getY());
            return path == null ? null : ((OWLObjectTreeNode) path.getLastPathComponent()).getOWLObject();
        }
    }
}
