package odase.editor;

import odase.LiteralChecker;
import odase.tools.OWLDataTypeFormatInfo;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;
import org.protege.editor.core.ui.util.InputVerificationStatusChangedListener;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.axiom.FreshActionStrategySelector;
import org.protege.editor.owl.model.axiom.FreshAxiomLocationPreferences;
import org.protege.editor.owl.model.axiom.FreshAxiomLocationStrategy;
import org.protege.editor.owl.model.parser.OWLLiteralParser;
import org.protege.editor.owl.model.util.OWLDataTypeUtils;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.editor.OWLConstantEditor;
import org.protege.editor.owl.ui.renderer.OWLCellRendererSimple;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.*;
import java.util.List;

import static org.protege.editor.owl.model.selection.OWLClassAssertionSelectionModel.logger;

/**
 * Created by vblagodarov on 26-06-17.
 */
public class OWLDataPropertyAssertion implements IPropertyValueAssertionEditor<OWLLiteral> {

    protected OWLEditorKit editorKit;
    private JPanel mainPanel;
    private OWLConstantEditor constantEditorComponent;
    private final JTextArea annotationContent = new JTextArea(8, 40);
    private final JComboBox<String> langComboBox;
    private final JComboBox<OWLDatatype> datatypeComboBox;
    private final JLabel langLabel = new JLabel("Lang");
    private final OWLDataFactory dataFactory;
    private final OWLDataTypeFormatInfo formatInfo;
    private final JLabel messageLabel = new JLabel();
    private final JLabel formatDetails = new JLabel(UIManager.getIcon("OptionPane.informationIcon"));
    private JPanel descrPanel;
    protected OWLNamedIndividual subject;
    protected OWLDataProperty property;
    private InputVerificationStatusChangedListener verificationStatusChangedListener = newState -> {
    };

    public OWLDataPropertyAssertion(OWLEditorKit owlEditorKit, OWLNamedIndividual subjectIndividual, OWLDataProperty dataProperty) {
        editorKit = owlEditorKit;
        property = dataProperty;
        subject = subjectIndividual;
        final Border paddingBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        editorKit = owlEditorKit;
        dataFactory = owlEditorKit.getModelManager().getOWLDataFactory();
        formatInfo = new OWLDataTypeFormatInfo(dataFactory);
        annotationContent.setWrapStyleWord(true);
        annotationContent.setLineWrap(true);
        annotationContent.setBorder(null);

        constantEditorComponent = new OWLConstantEditor(owlEditorKit);
        constantEditorComponent.setBorder(paddingBorder);

        final UIHelper uiHelper = new UIHelper(owlEditorKit);
        langComboBox = uiHelper.getLanguageSelector();
        datatypeComboBox = datatypeSelectorPanel();
        if (subject != null && property != null) {
            OWLDatatype bestMatch = getBestMatch(property);
            datatypeComboBox.setSelectedItem(bestMatch);
        }
        datatypeComboBox.addActionListener(e -> {
            Object selected = ((JComboBox) e.getSource()).getSelectedItem();
            if (selected != null && selected instanceof OWLDatatype) {
                OWLDatatype sd = (OWLDatatype) selected;
                formatDetails.setToolTipText(formatInfo.getDatatypeFormatInfo(sd));
            }
            updateLang();
        });

        annotationContent.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validateContent();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validateContent();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        formatDetails.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(formatDetails);
        descrPanel = new JPanel(new HorizontalLayout());
        descrPanel.add(datatypeComboBox);
        descrPanel.add(langComboBox);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, annotationContent, descrPanel);
        splitPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(splitPane);

        updateLang();
    }

    public OWLDataPropertyAssertion(OWLEditorKit owlEditorKit,
                                    OWLNamedIndividual subjectIndividual,
                                    OWLDataProperty dataProperty,
                                    OWLLiteral object) {
        this(owlEditorKit, subjectIndividual, dataProperty);
        setEditedObject(object);
    }

    private void setEditedObject(OWLLiteral object) {
        if (object != null) {
            annotationContent.setText(object.getLiteral());
            if (!object.isRDFPlainLiteral()) {
                datatypeComboBox.setSelectedItem(object.getDatatype());
            } else {
                langComboBox.setSelectedItem(object.getLang());
            }
        }
    }

    private OWLDatatype getBestMatch(OWLProperty dataProperty) {
        for (OWLDataPropertyRangeAxiom axiom : editorKit.getModelManager().getActiveOntology().
                getAxioms(AxiomType.DATA_PROPERTY_RANGE, Imports.INCLUDED)) {
            OWLPropertyExpression pe = axiom.getProperty();
            if (pe.equals(dataProperty)) {
                OWLDataRange range = axiom.getRange();
                if (range.getDataRangeType() == DataRangeType.DATATYPE) {
                    OWLDatatype dt = range.asOWLDatatype();
                    if (dt != null) {
                        formatDetails.setToolTipText(formatInfo.getDatatypeFormatInfo(dt));
                        return dt;
                    }
                }
                return null;
            }
        }
        return null;
    }

    private void updateLang() {
        OWLDatatype owlDatatype = getSelectedDatatype();
        boolean langEnabled = owlDatatype == null || owlDatatype.isRDFPlainLiteral();
        validateContent();
        setLangEnabled(langEnabled);
    }

    private boolean validateContent() {
        clearErrorMessage();
        if (getLexicalValue().isEmpty()) {
            verificationStatusChangedListener.verifiedStatusChanged(false);
            return false;
        }
        Optional<OWLDatatype> datatype = Optional.ofNullable(getSelectedDatatype());
        Collection<OWLLiteral> values = getOWLObjectValues();
        if (values.isEmpty()) {
            return false;
        }
        boolean isValid = LiteralChecker.isLiteralIsInLexicalSpace(values.iterator().next());
        datatype.ifPresent(d -> {
            if (!isValid) {
                annotationContent.setForeground(Color.RED);
                String message = String.format(
                        "The entered value is not valid for the specified datatype (%s)",
                        editorKit.getOWLModelManager().getRendering(d));
                displayErrorMessage(message);
                verificationStatusChangedListener.verifiedStatusChanged(false);
            } else {
                verificationStatusChangedListener.verifiedStatusChanged(true);
            }
        });
        return isValid;
    }

    private boolean isLangSelected() {
        return langComboBox.getSelectedItem() != null && !langComboBox.getSelectedItem().equals("");
    }

    private boolean isDatatypeSelected() {
        return datatypeComboBox.getSelectedItem() != null;
    }

    private String getSelectedLang() {
        return (String) langComboBox.getSelectedItem();
    }

    private void displayErrorMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setForeground(Color.RED);
        annotationContent.setToolTipText(message);
    }

    private String getLexicalValue() {
        return annotationContent.getText().trim();
    }

    private void setLangEnabled(boolean b) {
        langLabel.setEnabled(b);
        langComboBox.setEnabled(b);
    }

    private void clearErrorMessage() {
        messageLabel.setText("");
        messageLabel.setForeground(null);
        annotationContent.setToolTipText(null);
        annotationContent.setForeground(null);
    }

    private OWLDatatype getSelectedDatatype() {
        return (OWLDatatype) datatypeComboBox.getSelectedItem();
    }

    private JComboBox datatypeSelectorPanel() {
        JComboBox c = new JComboBox(new DefaultComboBoxModel(getDatatypeList().toArray()));
        c.setPreferredSize(new Dimension(200, c.getPreferredSize().height));
        c.setRenderer(new OWLCellRendererSimple(editorKit));
        return c;
    }

    private Collection<OWLDatatype> getDatatypeList() {
        OWLOntologyManager mngr = editorKit.getOWLModelManager().getOWLOntologyManager();
        java.util.List<OWLDatatype> datatypeList = new ArrayList<>(
                new OWLDataTypeUtils(mngr).getKnownDatatypes(editorKit.getOWLModelManager().getActiveOntologies()));
        OWLDataFactory df = mngr.getOWLDataFactory();

        datatypeList.add(df.getOWLDatatype(XSDVocabulary.DATE.getIRI()));
        datatypeList.add(df.getOWLDatatype(XSDVocabulary.TIME.getIRI()));
        datatypeList.add(df.getOWLDatatype(XSDVocabulary.DURATION.getIRI()));

        Collections.sort(datatypeList, editorKit.getOWLModelManager().getOWLObjectComparator());
        return datatypeList;
    }

    public void addStatusChangedListener(InputVerificationStatusChangedListener listener) {
        if (listener != null) {
            this.verificationStatusChangedListener = listener;
        } else {
            verificationStatusChangedListener = (newState) -> {
            };
        }
    }

    public void removeStatusChangedListener(InputVerificationStatusChangedListener listener) {
        this.verificationStatusChangedListener = (newState) -> {
        };
    }

    public JComponent getEditorComponent() {
        return mainPanel;
    }

    public void dispose() {

    }

    public void handleEditingFinished(OWLPropertyAssertionAxiom previousAxiom) {
        Collection<OWLLiteral> values = getOWLObjectValues();
        OWLLiteral value = values.iterator().next();
        if (value == null) {
            return;
        }

        final OWLIndividualAxiom axiom = getAxiom(value);
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

    public OWLIndividualAxiom getAxiom(OWLLiteral individual) {
        return editorKit.getOWLModelManager().getOWLDataFactory().getOWLDataPropertyAssertionAxiom(property,
                subject,
                individual);
    }

    @Override
    public boolean isValidObject() {
        return validateContent();
    }

    @Override
    public OWLNamedIndividual getSubject() {
        return subject;
    }

    @Override
    public OWLProperty getProperty() {
        return property;
    }

    @Override
    public Collection<OWLLiteral> getOWLObjectValues() {
        String value = getLexicalValue();
        List<OWLLiteral> resultList = new ArrayList<>();
        if (isLangSelected()) {
            resultList.add(dataFactory.getOWLLiteral(value, getSelectedLang()));
        } else if (isDatatypeSelected()) {
            resultList.add(dataFactory.getOWLLiteral(value, getSelectedDatatype()));
        } else {
            resultList.add(new OWLLiteralParser(dataFactory).parseLiteral(value));
        }
        return resultList;
    }

    @Override
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
}
