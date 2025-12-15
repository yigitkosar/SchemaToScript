package edu.rit.g2.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import edu.rit.g2.controller.SchemaController;
import edu.rit.g2.model.SchemaModel;

/**
 * Swing component that displays a visual preview of the parsed schema.  
 * Each table is rendered as a titled card containing its column definitions.
 *
 * <p>This panel is refreshed whenever a new {@link SchemaModel} is loaded,
 * providing users with a structured overview of tables and their attributes.</p>
 *
 * @author Frane Gobin
 */
public class SchemaPreviewPanel extends JPanel {

    private SchemaController controller;

public void setController(SchemaController c) {
    this.controller = c;
}
    /**
     * Inner container that holds dynamically generated table preview cards.
     */
    private final JPanel content = new JPanel();

    /**
     * Creates the schema preview panel and initializes its layout, scroll container,
     * and default "no schema loaded" state.
     */
    public SchemaPreviewPanel() {
        super(new BorderLayout());
        setBorder(new EmptyBorder(0, 16, 16, 16));

        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel title = new JLabel("Classes / Tables");
        title.setBorder(new EmptyBorder(12, 12, 8, 12));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));

        add(title, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        render(null);
    }

    /**
     * Renders the schema preview based on the provided {@link SchemaModel}.  
     * If the model is null or contains no tables, a placeholder message is shown.
     *
     * @param model the parsed schema model to visualize, or {@code null} to clear the preview
     */
    public void render(SchemaModel model) {
    content.removeAll();

    if (model == null || model.getTables() == null || model.getTables().isEmpty()) {
        JLabel empty = new JLabel("No schema loaded.");
        empty.setBorder(new EmptyBorder(12, 12, 12, 12));
        content.add(empty);
        revalidate();
        repaint();
        return;
    }

    List<Map<String, Object>> tables = model.getTables();

    for (Map<String, Object> table : tables) {
        String tableName = String.valueOf(table.get("tableName"));

        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(new TitledBorder(tableName));
        card.setOpaque(true);

        DefaultListModel<String> lm = new DefaultListModel<>();

        @SuppressWarnings("unchecked")
        List<Map<String, String>> cols =
                (List<Map<String, String>>) table.getOrDefault("columns", List.of());

        for (Map<String, String> c : cols) {
            String n = c.getOrDefault("name", "?");
            String t = c.getOrDefault("type", "VARCHAR(255)");
            lm.addElement(n + " : " + t);
        }

        JList<String> list = new JList<>(lm);
        list.setVisibleRowCount(Math.min(8, lm.size()));
        JScrollPane sc = new JScrollPane(list);
        sc.setBorder(new EmptyBorder(4, 8, 8, 8));

        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {

                    int idx = list.locationToIndex(e.getPoint());
                    if (idx < 0) return;

                    String oldValue = lm.get(idx);

                    String input = JOptionPane.showInputDialog(
                            SchemaPreviewPanel.this,
                            "Edit column (format: name : type)",
                            oldValue
                    );

                    if (input == null) return;
                    if (!input.contains(":")) {
                        JOptionPane.showMessageDialog(
                                SchemaPreviewPanel.this,
                                "Format must be: name : type",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }

                    String[] parts = input.split(":");
                    String newName = parts[0].trim();
                    String newType = parts[1].trim();

                    lm.set(idx, newName + " : " + newType);

                    if (controller != null) {
                        controller.handleColumnEdit(tableName, idx, newName, newType);
                    }
                }
            }
        });

        card.add(sc, BorderLayout.CENTER);
        content.add(card);
        content.add(Box.createVerticalStrut(8));
    }

    revalidate();
    repaint();
}
}