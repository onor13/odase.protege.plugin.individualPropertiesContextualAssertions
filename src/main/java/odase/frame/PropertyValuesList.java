package odase.frame;

import odase.editor.IPropertyValueAssertionEditor;
import odase.providers.IPropertyEditorProvider;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.core.ui.list.MListItem;
import org.protege.editor.core.ui.util.InputVerificationStatusChangedListener;
import org.protege.editor.core.ui.util.VerifyingOptionPane;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.util.OWLAxiomInstance;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.axiom.AxiomAnnotationPanel;
import org.protege.editor.owl.ui.framelist.AxiomAnnotationButton;
import org.protege.editor.owl.ui.framelist.OWLFrameList;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicListUI;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.*;

/**
 * Created by vblagodarov on 27-06-17.
 */
public class PropertyValuesList extends MList {

    private OWLEditorKit editorKit;
    private PropertyValueCellRenderer cellRenderer;
    private AxiomAnnotationButton axiomAnnotationButton;
    private IPropertyEditorProvider provider;

    public PropertyValuesList(OWLEditorKit owlEditorKit, IPropertyEditorProvider valuesProvider) {
        super();
        editorKit = owlEditorKit;
        provider = valuesProvider;
        cellRenderer = new PropertyValueCellRenderer(owlEditorKit);
        setCellRenderer(cellRenderer);
        axiomAnnotationButton = new AxiomAnnotationButton(event -> invokeAxiomAnnotationHandler());
        setFixedCellWidth(0); //If not set to 0, the list becomes extremely wide
        setUI(new OWLFrameListUI());
    }

    public PropertyValueItem create(OWLModelManager modelManager, OWLPropertyAssertionAxiom axiom, boolean isInferred) {
        return new PropertyValueItem(modelManager, axiom, isInferred);
    }

    public void showAssertionDialog(IPropertyValueAssertionEditor editor, OWLPropertyAssertionAxiom previousAxiom) {
        final JComponent editorComponent = editor.getEditorComponent();
        final VerifyingOptionPane optionPane = new VerifyingOptionPane(editorComponent) {
            public void selectInitialValue() {
            }
        };
        optionPane.setOKEnabled(editor.isValidObject());
        final InputVerificationStatusChangedListener verificationListener = (verified) -> {
            optionPane.setOKEnabled(verified);
        };
        editor.addStatusChangedListener(verificationListener);

        Component parent = getDialogParent();

        JDialog dialog = optionPane.createDialog(parent, null);
        dialog.setModal(false);
        dialog.setResizable(true);
        dialog.setMinimumSize(new Dimension(300, 100));
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                Object retVal = optionPane.getValue();
                editorComponent.setPreferredSize(editorComponent.getSize());
                if (retVal != null && retVal.equals(Integer.valueOf(0))) {
                    editor.handleEditingFinished(previousAxiom);
                }
                editor.removeStatusChangedListener(verificationListener);
                editor.dispose();

            }
        });

        dialog.setTitle(provider.getTitle(editor.getSubject(), editor.getProperty()));
        dialog.setVisible(true);
    }

    public boolean isEmpty() {
        return getModel().getSize() <= 0;
    }

    @Override
    protected Color getItemBackgroundColor(MListItem item) {
        if (item instanceof PropertyValueItem) {
            if (((PropertyValueItem) item).isInferred()) {
                return OWLFrameList.INFERRED_BG_COLOR;
            }
        }
        return super.getItemBackgroundColor(item);
    }

    private Component getDialogParent() {
        Preferences prefs = PreferencesManager.getInstance().getApplicationPreferences("org.protege.editor.core.application");
        return prefs.getBoolean("DIALOGS_ALWAYS_CENTRED", false) ? SwingUtilities.getAncestorOfClass(Frame.class, this.getParent()) : this.getParent();
    }

    private boolean isAnnotationPresent(PropertyValueItem item) {
        OWLAxiom ax = item.getAxiom();
        return (!ax.getAnnotations().isEmpty());
    }

    private void invokeAxiomAnnotationHandler() {
        Object obj = getSelectedValue();
        if (!(obj instanceof PropertyValueItem)) {
            return;
        }
        PropertyValueItem row = (PropertyValueItem) obj;
        OWLAxiom ax = row.getAxiom();

        AxiomAnnotationPanel axiomAnnotationPanel;
        axiomAnnotationPanel = new AxiomAnnotationPanel(editorKit);

        OWLOntology ontology = row.getOntology();
        final OWLAxiomInstance axiomInstance;
        if (ontology != null) {
            axiomInstance = new OWLAxiomInstance(ax, ontology);
        } else {
            axiomInstance = new OWLAxiomInstance(ax, editorKit.getOWLModelManager().getActiveOntology());
        }
        axiomAnnotationPanel.setAxiomInstance(axiomInstance);
        new UIHelper(editorKit).showDialog("Annotations for " + ax.getAxiomType().toString(), axiomAnnotationPanel, JOptionPane.CLOSED_OPTION);
        axiomAnnotationPanel.dispose();
    }

    protected java.util.List<MListButton> getButtons(Object value) {
        java.util.List<MListButton> buttons = new ArrayList<>(super.getButtons(value));
        buttons.add(axiomAnnotationButton);
        axiomAnnotationButton.setAnnotationPresent(isAnnotationPresent((PropertyValueItem) value));
        return buttons;
    }


    public class PropertyValueItem implements MListItem {
        private OWLModelManager manager;
        private OWLPropertyAssertionAxiom axiom;
        private boolean isInferredAxiom;
        String tooltip;

        public PropertyValueItem(OWLModelManager modelManager, OWLPropertyAssertionAxiom axiom) {
            this(modelManager, axiom, false);
        }

        public PropertyValueItem(OWLModelManager modelManager, OWLPropertyAssertionAxiom axiom, boolean isInferred) {
            manager = modelManager;
            this.axiom = axiom;
            isInferredAxiom = isInferred;
        }

        public OWLPropertyAssertionAxiom getAxiom() {
            return axiom;
        }

        public OWLOntology getOntology() {
            return manager.getActiveOntology();
        }

        public OWLPropertyAssertionObject getObject() {
            return axiom.getObject();
        }

        public boolean isInferred() {
            return isInferredAxiom;
        }

        @Override
        public boolean isEditable() {
            return !isInferred();
        }

        @Override
        public void handleEdit() {
            IPropertyValueAssertionEditor editor = provider.getNewEditor(
                    axiom.getSubject().asOWLNamedIndividual(),
                    axiom.getProperty(), axiom.getObject());
            showAssertionDialog(editor, axiom);
        }

        @Override
        public boolean isDeleteable() {
            return !isInferred();
        }

        @Override
        public boolean handleDelete() {
            OWLOntologyChange removeChange = new RemoveAxiom(manager.getActiveOntology(), axiom);
            manager.applyChange(removeChange);
            return true;
        }

        @Override
        public String getTooltip() {
            return tooltip;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof PropertyValueItem)) {
                return false;
            }
            OWLPropertyAssertionAxiom otherAxiom = ((PropertyValueItem) object).getAxiom();
            OWLPropertyAssertionAxiom thisAxiom = getAxiom();
            if (otherAxiom == null || thisAxiom == null) {
                return false;
            }
            return thisAxiom.getSubject().equals(otherAxiom.getSubject()) &&
                    thisAxiom.getProperty().equals(otherAxiom.getProperty()) &&
                    thisAxiom.getObject().equals(otherAxiom.getObject());

        }
    }

    public class OWLFrameListUI extends BasicListUI {

        private Point lastMouseDownPoint;

        private int[] cumulativeCellHeight;


        // As BasicListUI is implemented with windows keystrokes, we need to
        // return a mouse listener that ignores the (bad) default toggle behaviour when Ctrl is pressed.
        // This would prevent mac users from using this very common key combination (right-click)
        // instead, add handling for the context menu and double click editing
        // Also must implement discontiguous multi-selection
        protected MouseInputListener createMouseInputListener() {

            return new MouseInputHandler() {

                boolean showingPopup = false;

                public void mousePressed(MouseEvent e) {
                    showingPopup = false;
                    lastMouseDownPoint = e.getPoint();
                    if (e.isPopupTrigger()) {
                        showingPopup = true;
                        showPopupMenu(e);
                    } else if ((e.getModifiersEx() & InputEvent.META_DOWN_MASK) != 0) {
                        int sel = locationToIndex(PropertyValuesList.this, lastMouseDownPoint);
                        handleModifiedSelectionEvent(sel);
                    } else {
                        super.mousePressed(e);
                    }
                }

                public void mouseReleased(MouseEvent e) {
                    super.mouseReleased(e);
                }
            };
        }

        private void handleModifiedSelectionEvent(int index) {
            if (isSelectedIndex(index)) {
                removeSelectionInterval(index, index);
            } else if (getSelectionMode() == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION || getSelectedIndex() == -1) {
                addSelectionInterval(index, index);
            }
        }

        private void showPopupMenu(MouseEvent e) {
            /*for (OWLFrameListPopupMenuAction<?> action : actions) {
                action.updateState();
            }
            popupMenu.show(this, e.getX(), e.getY());*/
        }


        protected void updateLayoutState() {
            cumulativeCellHeight = new int[list.getModel().getSize()];
            /*
                * If both JList fixedCellWidth and fixedCellHeight have been set,
                * then initialize cellWidth and cellHeight, and set cellHeights to
                * null.
                */
            int fixedCellHeight = list.getFixedCellHeight();
            int fixedCellWidth = list.getFixedCellWidth();
            cellWidth = fixedCellWidth != -1 ? fixedCellWidth : -1;
            cellHeight = fixedCellHeight;
            if (fixedCellHeight != -1) {
                cellHeights = null;
            } else {
                cellHeights = new int[list.getModel().getSize()];
            }
            /*
                * If either of JList fixedCellWidth and fixedCellHeight haven't
                * been set, then initialize cellWidth and cellHeights by scanning
                * through the entire model. Note: if the renderer is null, we just
                * set cellWidth and cellHeights[*] to zero, if they're not set
                * already.
                */
            if (fixedCellWidth == -1 || fixedCellHeight == -1) {
                ListModel dataModel = list.getModel();
                int dataModelSize = dataModel.getSize();
                ListCellRenderer renderer = list.getCellRenderer();
                if (renderer != null) {
                    int cumulativeHeight = 0;
                    for (int index = 0; index < dataModelSize; index++) {
                        Object value = dataModel.getElementAt(index);
                        Component c = renderer.getListCellRendererComponent(list, value, index, false, false);
                        rendererPane.add(c);
                        Dimension cellSize = c.getPreferredSize();
                        if (fixedCellWidth == -1) {
                            cellWidth = Math.max(cellSize.width, cellWidth);
                        }
                        if (fixedCellHeight == -1) {
                            cellHeights[index] = cellSize.height;
                        }

                        cumulativeHeight += cellHeights[index];
                        cumulativeCellHeight[index] = cumulativeHeight;
                    }
                } else {
                    if (cellWidth == -1) {
                        cellWidth = 0;
                    }
                    if (cellHeights == null) {
                        cellHeights = new int[dataModelSize];
                    }
                    for (int index = 0; index < dataModelSize; index++) {
                        cellHeights[index] = 0;
                    }
                }
            }
        }


        public Rectangle getCellBounds(JList list, int index1, int index2) {
            maybeUpdateLayoutState();
            int minIndex = Math.min(index1, index2);
            int maxIndex = Math.max(index1, index2);
            if (minIndex >= list.getModel().getSize()) {
                return null;
            }
            Rectangle minBounds = getCellBounds(list, minIndex);
            if (minBounds == null) {
                return null;
            }
            if (minIndex == maxIndex) {
                return minBounds;
            }
            Rectangle maxBounds = getCellBounds(list, maxIndex);
            if (maxBounds != null) {
                if (minBounds.x != maxBounds.x) {
                    // Different columns
                    minBounds.y = 0;
                    minBounds.height = list.getHeight();
                }
                minBounds.add(maxBounds);
            }
            return minBounds;
        }

        /**
         * Gets the bounds of the specified model index, returning the resulting
         * bounds, or null if <code>index</code> is not valid.
         */
        private Rectangle getCellBounds(JList list, int index) {
            if (index < 0) {
                return new Rectangle();
            }
            maybeUpdateLayoutState();
            if (index >= cumulativeCellHeight.length) {
                return null;
            }
            Insets insets = list.getInsets();
            int x;
            int w;
            int y;
            int h;
            x = insets.left;
            if (index >= cellHeights.length) {
                y = 0;
            } else {
                y = cumulativeCellHeight[index] - cellHeights[index];
            }
            w = list.getWidth() - (insets.left + insets.right);
            h = cellHeights[index];
            return new Rectangle(x, y, w, h);
        }

        /**
         * Paint one List cell: compute the relevant state, get the "rubber
         * stamp" cell renderer component, and then use the CellRendererPane to
         * paint it. Subclasses may want to override this method rather than
         * paint().
         *
         * @see #paint
         */

        protected void paintCell(Graphics g, int row, Rectangle rowBounds, ListCellRenderer cellRenderer, ListModel dataModel, ListSelectionModel selModel, int leadIndex) {
            Object value = dataModel.getElementAt(row);
            boolean cellHasFocus = list.hasFocus() && row == leadIndex;
            boolean isSelected = selModel.isSelectedIndex(row);
            Component rendererComponent = cellRenderer.getListCellRendererComponent(list, value, row, isSelected, cellHasFocus);
            int cx = rowBounds.x;
            int cy = rowBounds.y;
            int cw = rowBounds.width;
            int ch = rowBounds.height;
            rendererPane.paintComponent(g, rendererComponent, list, cx, cy, cw, ch, true);
        }
    }
}
