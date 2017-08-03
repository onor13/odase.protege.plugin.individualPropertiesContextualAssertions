package odase.frame;

import odase.buttons.DisabledAddButtonUI;
import odase.buttons.EnabledAddButtonUI;
import odase.editor.IPropertyValueAssertionDetails;
import odase.providers.IPropertyEditorProvider;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.VerticalLayout;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Created by vblagodarov on 19-06-17.
 */
public class OWLPropertiesStackedBox extends JPanel implements Scrollable, ActionListener, IOWLPropertiesStackBox {
    private Color separatorColor;
    private Border separatorBorder;
    protected OWLEditorKit editorKit;
    private OWLModelManager modelManager;
    private final HashMap<IPropertyValueAssertionDetails, StackedBoxLineSaved> rows = new HashMap<>();
    private OWLOntologyChangeListener ontologyChangeListener;
    private OWLModelManagerListener modelManagerListener;
    private IPropertyEditorProvider innerComponentProvider;


    public OWLPropertiesStackedBox(OWLEditorKit owlEditorKit, IPropertyEditorProvider provider) {
        setLayout(new VerticalLayout());
        setOpaque(true);
        setBackground(Color.WHITE);
        editorKit = owlEditorKit;
        modelManager = editorKit.getModelManager();

        separatorBorder = new SeparatorBorder();
        setSeparatorColor(new Color(214, 223, 247));
        ontologyChangeListener = changes -> {
            refreshView(changes);
        };
        modelManager.addOntologyChangeListener(ontologyChangeListener);
        modelManagerListener = event -> {
            if (event.isType(EventType.REASONER_CHANGED) || event.isType(EventType.ONTOLOGY_RELOADED) ||
                    event.isType(EventType.ONTOLOGY_CLASSIFIED)) {
                refreshView();
            }
        };
        modelManager.addListener(modelManagerListener);

        innerComponentProvider = provider;
    }

    protected Color getSeparatorColor() {
        return separatorColor;
    }

    protected void setSeparatorColor(Color separatorColor) {
        this.separatorColor = separatorColor;
    }

    public void setNewPropertyValuesProvider(IPropertyEditorProvider provider) {
        innerComponentProvider = provider;
        for (Map.Entry<IPropertyValueAssertionDetails, StackedBoxLineSaved> mapEntry : rows.entrySet()) {
            IPropertyValueAssertionDetails e = mapEntry.getKey();
            StackedBoxLineSaved cl = mapEntry.getValue();
            //  if(!cl.getCollapsible().isCollapsed()){
            innerComponentProvider.updateInnerComponent(mapEntry.getValue(), e.getSubject(), e.getProperty());

        }
    }

    @Override
    public void addItem(OWLNamedIndividual subject, OWLProperty property) {

        PropertyValuesList innerComponent = new PropertyValuesList(editorKit, innerComponentProvider);

        final JXCollapsiblePane collapsible = new JXCollapsiblePane();
        collapsible.getContentPane().setBackground(Color.WHITE);
        collapsible.getContentPane().add(innerComponent);
        collapsible.setBorder(new CompoundBorder(separatorBorder, collapsible
                .getBorder()));

        collapsible.setCollapsed(true);

        JButton button = new JButton();
        button.addActionListener(e -> {
            innerComponent.showAssertionDialog(innerComponentProvider.getNewEditor(subject, property), null);
        });


        Action toggleAction = collapsible.getActionMap().get(
                JXCollapsiblePane.TOGGLE_ACTION);
        toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON, UIManager
                .getIcon("Tree.expandedIcon"));
        toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON, UIManager
                .getIcon("Tree.collapsedIcon"));


        StackedBoxLineSaved lineSaved = new StackedBoxLineSaved(innerComponent, collapsible, button, toggleAction);
        innerComponentProvider.updateInnerComponent(lineSaved, subject, property);
        /*toggleAction.addPropertyChangeListener(evt -> {
            if (!collapsible.isCollapsed()) {
                innerComponentProvider.updateInnerComponent(lineSaved, subject, property);
            }
        });*/

        JToggleButton toggleButton = new JToggleButton(toggleAction);
        toggleButton.setUI(innerComponentProvider.getToggleButtonUI(editorKit.getModelManager().getRendering(property)));

        int height = Math.max(toggleButton.getPreferredSize().height - 3, 20);

        if (innerComponentProvider.canAddToInnerList(subject, property)) {
            button.setUI(new EnabledAddButtonUI(new Dimension(height, height)));
        } else {
            button.setUI(new DisabledAddButtonUI(new Dimension(height, height)));
        }
        add(ComponentAndButton.get(toggleButton, button));
        rows.put(innerComponentProvider.getNewEditor(subject, property), lineSaved);

        add(collapsible);
    }

    public void dispose() {
        modelManager.removeOntologyChangeListener(ontologyChangeListener);
        modelManager.removeListener(modelManagerListener);
    }

    public void expandAll() {
        setCollapsedAll(false);
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    public void collapseAll() {
        setCollapsedAll(true);
    }

    private void setCollapsedAll(boolean value) {
        for (StackedBoxLineSaved cl : rows.values()) {
            cl.getCollapsible().setCollapsed(value);
        }
    }

    private void refreshView() {
        for (Map.Entry<IPropertyValueAssertionDetails, StackedBoxLineSaved> mapEntry : rows.entrySet()) {
            IPropertyValueAssertionDetails details = mapEntry.getKey();
            StackedBoxLineSaved info = mapEntry.getValue();
            //  if (!info.collapsible.isCollapsed()){
            innerComponentProvider.updateInnerComponent(info, details.getSubject(), details.getProperty());
        }
    }

    private void refreshView(List<? extends OWLOntologyChange> changes) {
        changes.forEach(change -> {
            if (change.getOntology().equals(editorKit.getOWLModelManager().getActiveOntology()) &&
                    change.getAxiom() instanceof OWLPropertyAssertionAxiom) {
                OWLPropertyAssertionAxiom propertyAssertionAxiom = (OWLPropertyAssertionAxiom) change.getAxiom();
                for (Map.Entry<IPropertyValueAssertionDetails, StackedBoxLineSaved> mapEntry : rows.entrySet()) {
                    IPropertyValueAssertionDetails details = mapEntry.getKey();
                    StackedBoxLineSaved info = mapEntry.getValue();
                    if (details.getSubject().equals(propertyAssertionAxiom.getSubject())) {
                        //&& !info.collapsible.isCollapsed()){
                        innerComponentProvider.updateInnerComponent(info, details.getSubject(), details.getProperty());
                        //Some of the values are retrieved using Reasoner, which is not always synchronized.
                        //So if we removing the axiom explicitly;
                        if (details.getProperty().equals(propertyAssertionAxiom.getProperty())
                                && change.isRemoveAxiom()) {
                            DefaultListModel lm = (DefaultListModel) info.getList().getModel();
                            lm.removeElement(info.getList().create(editorKit.getModelManager(), propertyAssertionAxiom, false));
                            if (innerComponentProvider.canAddToInnerList(details.getSubject(), details.getProperty())) {
                                info.getButton().setUI(new EnabledAddButtonUI());
                            } else {
                                info.getButton().setUI(new DisabledAddButtonUI());
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * @see Scrollable#getPreferredScrollableViewportSize()
     */
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    /**
     * @see Scrollable#getScrollableBlockIncrement(java.awt.Rectangle, int, int)
     */
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect,
                                           int orientation, int direction) {
        return 10;
    }

    /**
     * @see Scrollable#getScrollableTracksViewportHeight()
     */
    @Override
    public boolean getScrollableTracksViewportHeight() {
        if (getParent() instanceof JViewport) {
            return (((JViewport) getParent()).getHeight() > getPreferredSize().height);
        } else {
            return false;
        }
    }

    /**
     * @see Scrollable#getScrollableTracksViewportWidth()
     */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    /**
     * @see Scrollable#getScrollableUnitIncrement(java.awt.Rectangle, int, int)
     */
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation,
                                          int direction) {
        return 10;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
    }

    /**
     * The border between the stack components. It separates each component with a
     * fine line border.
     */
    class SeparatorBorder implements Border {

        boolean isFirst(Component c) {
            return c.getParent() == null || c.getParent().getComponent(0) == c;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            // if the collapsible is collapsed, we do not want its border to be
            // painted.
            if (c instanceof JXCollapsiblePane) {
                if (((JXCollapsiblePane) c).isCollapsed()) {
                    return new Insets(0, 0, 0,
                            0);
                }
            }
            return new Insets(isFirst(c) ? 4 : 1, 0, 1, 0);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width,
                                int height) {
            g.setColor(getSeparatorColor());
            if (isFirst(c)) {
                g.drawLine(x, y + 2, x + width, y + 2);
            }
            g.drawLine(x, y + height - 1, x + width, y + height - 1);
        }
    }

    //Nicole's modifications!

    /**
     * This method is designed to change the title text of a given collapsible
     * pane.
     *
     * @param title title of the box
     * @param i     the pane for which the title will be changed
     */
    public void setBoxTitle(String title, int i) {
        JXHyperlink link = (JXHyperlink) this.getComponent(i);  //i believe this returns the link for a given pane.
        link.setText(title);

    }

    /**
     * This method is designed to change the title backgroun color of a given
     * collapsible pane.
     *
     * @param i     the pane for which the title will be changed
     * @param color new color to set the background color of the box
     */
    public void setBoxTitleBackgroundColor(int i, Color color) {
        JXHyperlink link = (JXHyperlink) this.getComponent(i);  //i believe this returns the link for a given pane.
        link.setBackground(color);
    }

    private class StackedBoxLineSaved implements IOWLPropertiesStackBoxLine {
        private PropertyValuesList list;
        private JXCollapsiblePane collapsible;
        private JButton button;
        private Action action;

        public StackedBoxLineSaved(PropertyValuesList valuesList,
                                   JXCollapsiblePane collapsiblePane,
                                   JButton addButton,
                                   Action toggleAction) {
            list = valuesList;
            collapsible = collapsiblePane;
            button = addButton;
            action = toggleAction;
        }

        public PropertyValuesList getList() {
            return list;
        }

        public JXCollapsiblePane getCollapsible() {
            return collapsible;
        }

        public JButton getButton() {
            return button;
        }

        public void setEnableToggleAction(boolean enable) {
            action.setEnabled(enable);
        }
    }

}
