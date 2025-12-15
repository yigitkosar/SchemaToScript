package edu.rit.g2.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.rit.g2.controller.SchemaController;
import edu.rit.g2.model.SchemaModel;
import edu.rit.g2.parser.DbmsType;
import edu.rit.g2.view.SchemaSwingView;

/**
 * Main Swing-based application window for the Schema2Script tool.  
 * This frame provides a graphical interface for selecting schema files,
 * previewing their structure, and generating SQL output based on the chosen DBMS.
 *
 * <p>The frame manages layout construction, resource loading, user interactions,
 * and communication with controller and parser components of the application.</p>
 *
 * @author Nik Kaučić
 */
public class SchemaFrame extends JFrame {

    /**
     * Directory containing example schema files bundled with the application.
     */
    private static final String RESOURCES_DIR = "src/main/resources";
    /**
     * Dropdown for selecting a schema file to load.
     */
    private final JComboBox<File> fileCombo = new JComboBox<>();
    /**
     * Dropdown for selecting the target DBMS for SQL generation.
     */
    private final JComboBox<String> dbmsCombo = new JComboBox<>(
        new String[]{"MySQL", "PostgreSQL", "Oracle"}
    );
    /**
     * Button that triggers SQL generation once a valid file is selected.
     */
    private final JButton generateBtn = new JButton("Generate");
    /**
     * Text area used to display the generated SQL script.
     */
    private final JTextArea sqlArea = new JTextArea();
    /**
     * Hidden status label used by the Swing view for messaging.
     */
    private final JLabel hiddenStatusLabel = new JLabel();
    /**
     * Panel that renders a visual preview of the parsed schema.
     */
    private final SchemaPreviewPanel previewPanel = new SchemaPreviewPanel();
    private final SchemaController controller =
        new SchemaController(new SchemaModel(), new SchemaSwingView(sqlArea, hiddenStatusLabel));

    /**
     * Creates and initializes the main application window.  
     * Builds the UI layout, loads available schema files, and wires component actions.
     */
    public SchemaFrame() {
        super("Schema2Script — Generator");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        previewPanel.setController(controller);
        JPanel header = buildHeaderPanel();
        JPanel controls = buildFullWidthControls();
        JScrollPane sqlViewer = buildSqlViewer();

        JPanel north = new JPanel(new BorderLayout());
        north.add(header, BorderLayout.NORTH);
        north.add(controls, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, previewPanel, sqlViewer);
        split.setResizeWeight(0.35);
        split.setContinuousLayout(true);
        split.setDividerSize(0);
        split.setBorder(new EmptyBorder(0, 0, 0, 0));

        setLayout(new BorderLayout());
        add(north, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);

        loadResourceFiles();
        wireActions();
    }

    /**
     * Builds the top header section of the window containing the title and subtitle.
     *
     * @return a configured header panel
     */
    private JPanel buildHeaderPanel() {
        JLabel title = new JLabel("Schema2Script");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        JLabel subtitle = new JLabel("Generate SQL from your schema");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 13.5f));
        subtitle.setForeground(new Color(90, 90, 90));

        JPanel headerText = new JPanel();
        headerText.setOpaque(false);
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.add(title);
        headerText.add(Box.createVerticalStrut(2));
        headerText.add(subtitle);

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(14, 16, 6, 16));
        header.add(headerText, BorderLayout.WEST);
        return header;
    }

    /**
     * Builds the control panel containing file selection, DBMS selection,
     * and the generate button.
     *
     * @return the fully configured control panel
     */
    private JPanel buildFullWidthControls() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(8, 16, 16, 16));
        panel.setBackground(Color.WHITE);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 10, 10, 10);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel fileLbl = styledLabel("File");
        JLabel dbLbl = styledLabel("Choose Database");

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        panel.add(fileLbl, gc);

        gc.gridx = 1; gc.gridy = 0; gc.weightx = 1;
        panel.add(fileCombo, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        panel.add(dbLbl, gc);

        gc.gridx = 1; gc.gridy = 1; gc.weightx = 0;
        panel.add(dbmsCombo, gc);

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.NONE;
        stylePrimaryButton(generateBtn);
        panel.add(generateBtn, gc);

        return panel;
    }

    /**
     * Constructs the scrollable SQL viewer component.
     *
     * @return a scroll pane containing the SQL output area
     */
    private JScrollPane buildSqlViewer() {
        sqlArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        sqlArea.setEditable(false);
        sqlArea.setLineWrap(false);
        sqlArea.setMargin(new Insets(12, 14, 12, 14));
        JScrollPane scroll = new JScrollPane(sqlArea);
        scroll.setBorder(new EmptyBorder(0, 16, 16, 16));
        return scroll;
    }

    /**
     * Creates a label with consistent styling used throughout the UI.
     *
     * @param text the label text
     * @return a styled {@link JLabel}
     */
    private JLabel styledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 13.5f));
        return l;
    }

    /**
     * Applies primary-button styling to the given button, including size,
     * colors, hover behavior, and disabled-state appearance.
     *
     * @param b the button to style
     */
    private void stylePrimaryButton(JButton b) {
        b.setPreferredSize(new Dimension(160, 42));
        b.setBackground(new Color(0, 120, 215));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 14f));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setEnabled(false);

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (b.isEnabled()) b.setBackground(new Color(0, 100, 190));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (b.isEnabled()) b.setBackground(new Color(0, 120, 215));
            }
        });
    }

    /**
     * Loads schema files from the resources directory into the file selector.  
     * If no valid files are found, the preview is cleared and a placeholder model is used.
     */
    private void loadResourceFiles() {
        File dir = new File(RESOURCES_DIR);
        fileCombo.removeAllItems();

        if (!dir.exists() || !dir.isDirectory()) {
            configureComboPlaceholder();
            previewPanel.render(null);
            return;
        }

        File[] files = dir.listFiles(f -> f.isFile() && f.getName().toLowerCase().endsWith(".json"));
        if (files == null || files.length == 0) {
            configureComboPlaceholder();
            previewPanel.render(null);
            return;
        }

        Arrays.sort(files, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        for (File f : files) fileCombo.addItem(f);

        fileCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("Choose a file");
                    setForeground(Color.GRAY);
                    setToolTipText(null);
                } else if (value instanceof File file) {
                    setText(file.getName());
                    setToolTipText(file.getAbsolutePath());
                }
                return this;
            }
        });

        fileCombo.setSelectedItem(null);
        generateBtn.setEnabled(false);
        previewPanel.render(null);
    }

    /**
     * Configures the file selection combo box with a placeholder entry
     * indicating that no files are available.
     */
    private void configureComboPlaceholder() {
        fileCombo.setModel(new DefaultComboBoxModel<>());
        fileCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText("Choose a file");
                setForeground(Color.GRAY);
                return this;
            }
        });
        fileCombo.setSelectedItem(null);
    }

    /**
     * Attaches listeners to interactive UI components such as the file selector
     * and the generate button.
     */
    private void wireActions() {
        generateBtn.addActionListener(e -> onGenerate());
        fileCombo.addActionListener(e -> {
            File sel = (File) fileCombo.getSelectedItem();
            generateBtn.setEnabled(sel != null);
            if (sel != null) generateBtn.setBackground(new Color(0, 120, 215));
            else generateBtn.setBackground(new Color(160, 160, 160));
            updatePreviewForSelected();
        });
    }

    /**
     * Handles the SQL generation sequence triggered by the user.  
     * Loads the selected schema file, updates the controller, and displays the generated SQL.
     */
    private void onGenerate() {

        SchemaModel edited = controller.getModel().getSchema();
        if (edited == null) {
            JOptionPane.showMessageDialog(
                this,
                "No schema loaded.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
    
        String dbChoice = (String) dbmsCombo.getSelectedItem();
    
        DbmsType chosenDb;
        switch (dbChoice) {
            case "MySQL" -> chosenDb = DbmsType.MYSQL;
            case "PostgreSQL" -> chosenDb = DbmsType.POSTGRES;
            case "Oracle" -> chosenDb = DbmsType.ORACLE;
            default -> {
                JOptionPane.showMessageDialog(this, "Unknown DBMS: " + dbChoice);
                return;
            }
        }
    
        controller.handleGenerateSqlFromModel(edited, chosenDb);
    }

    /**
     * Updates the schema preview panel based on the currently selected file.  
     * If parsing fails, the preview is cleared.
     */
    private void updatePreviewForSelected() {
        File selected = (File) fileCombo.getSelectedItem();
        if (selected == null) {
            previewPanel.render(null);
            return;
        }
        try {
            controller.handleSchemaUpload(selected, "json");
            previewPanel.render(controller.getModel().getSchema());
        } catch (Exception ex) {
            previewPanel.render(null);
        }
    }
}