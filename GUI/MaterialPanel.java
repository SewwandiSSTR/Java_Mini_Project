package ums.gui.panels;

import ums.dao.CourseDAOImpl;
import ums.dao.ICourseDAO;
import ums.dao.IMaterialDAO;
import ums.dao.MaterialDAOImpl;
import ums.gui.UITheme;
import ums.model.Course;
import ums.model.Material;
import ums.roles.Lecturer;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Desktop;
import java.io.File;
import java.util.List;

public class MaterialPanel extends JPanel {

    private final Lecturer      lecturer;
    private final IMaterialDAO  materialDAO = new MaterialDAOImpl();
    private final ICourseDAO    courseDAO   = new CourseDAOImpl();

    private DefaultTableModel tableModel;
    private JTable            table;
    private List<Course>      courseList = new java.util.ArrayList<>();  // backing list for courseBox

    // Form fields
    private JComboBox<String> courseBox;
    private JTextField        titleField;
    private JTextField        descField;
    private JTextField        filePathField;   // display only — filled by file chooser
    private String            selectedFilePath = "";
    private String            selectedFileName = "";

    public MaterialPanel(Lecturer lecturer) {
        this.lecturer = lecturer;
        setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        loadAllMaterials();
    }

    private void buildUI() {
        // ── Title ─────────────────────────────────────────────────────────
        JLabel title = new JLabel("📄  Course Materials  (PDF only)");
        title.setFont(UITheme.TITLE);
        title.setForeground(UITheme.PRIMARY);
        add(title, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Course", "Type", "Title", "Description", "File Name", "Uploaded At"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(UITheme.BODY);
        table.setRowHeight(28);
        table.getTableHeader().setFont(UITheme.HEADING);
        table.getTableHeader().setBackground(UITheme.TABLE_HEADER);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(50);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(200);
        table.getColumnModel().getColumn(5).setPreferredWidth(160);
        table.getColumnModel().getColumn(6).setPreferredWidth(150);

        // Click a row → populate form for editing
        table.getSelectionModel().addListSelectionListener(e -> populateFormFromTable());

        // ── Action buttons row above form ─────────────────────────────────
        JButton openBtn    = UITheme.primaryBtn("📂 Open PDF");
        JButton deleteBtn  = UITheme.dangerBtn("🗑 Delete");
        JButton refreshBtn = UITheme.primaryBtn("↻ Refresh");
        JButton clearBtn   = new JButton("✖ Clear Form");

        openBtn.addActionListener(e -> openSelectedPDF());
        deleteBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> loadAllMaterials());
        clearBtn.addActionListener(e -> clearForm());

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        actionRow.setBackground(UITheme.BG);
        actionRow.add(openBtn); actionRow.add(deleteBtn);
        actionRow.add(refreshBtn); actionRow.add(clearBtn);

        // ── Upload / Update form ──────────────────────────────────────────
        JPanel form = UITheme.sectionPanel("Add / Update Material");
        form.setLayout(new GridLayout(5, 2, 10, 8));

        // Course selector — courses taught by this lecturer.
        // Shows "CODE|TYPE  –  Course Name" so the lecturer can identify each course clearly.
        loadCourseBox();
        titleField    = new JTextField();
        descField     = new JTextField();
        filePathField = new JTextField();
        filePathField.setEditable(false);
        filePathField.setBackground(new Color(245, 247, 250));

        JButton browseBtn = UITheme.primaryBtn("📁 Browse PDF…");
        browseBtn.addActionListener(e -> browsePDF());

        form.add(lbl("Course (Code|Type):")); form.add(courseBox);
        form.add(lbl("Title:"));              form.add(titleField);
        form.add(lbl("Description:"));        form.add(descField);
        form.add(lbl("PDF File:"));           form.add(filePathField);
        form.add(browseBtn);                  form.add(new JLabel("← required for Upload"));

        JButton uploadBtn = UITheme.primaryBtn("⬆ Upload New Material");
        JButton updateBtn = UITheme.primaryBtn("✏ Update Selected");

        uploadBtn.addActionListener(e -> uploadMaterial());
        updateBtn.addActionListener(e -> updateMaterial());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        btnRow.setBackground(UITheme.BG);
        btnRow.add(uploadBtn); btnRow.add(updateBtn);

        JPanel south = new JPanel(new BorderLayout(0, 6));
        south.setBackground(UITheme.BG);
        south.add(actionRow, BorderLayout.NORTH);
        south.add(form,      BorderLayout.CENTER);
        south.add(btnRow,    BorderLayout.SOUTH);

        add(UITheme.scroll(table), BorderLayout.CENTER);
        add(south,                 BorderLayout.SOUTH);
    }

    // ── Browse PDF file ───────────────────────────────────────────────────
    private void browsePDF() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select PDF File");
        fc.setFileFilter(new FileNameExtensionFilter("PDF files (*.pdf)", "pdf"));
        fc.setAcceptAllFileFilterUsed(false);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            selectedFilePath = f.getAbsolutePath();
            selectedFileName = f.getName();
            filePathField.setText(selectedFileName);  // show name only
        }
    }

    // ── Upload NEW material ───────────────────────────────────────────────
    private void uploadMaterial() {
        if (!validateForm(true)) return;

        String[] parts = parseCourse();
        Material m = new Material();
        m.setC_code(parts[0]);
        m.setC_type(parts[1]);
        m.setLec_id(lecturer.getUserId());
        m.setTitle(titleField.getText().trim());
        m.setDescription(descField.getText().trim());
        m.setFilePath(selectedFilePath);
        m.setFileName(selectedFileName);

        if (materialDAO.addMaterial(m)) {
            JOptionPane.showMessageDialog(this,
                    "Material '" + m.getTitle() + "' uploaded successfully.");
            clearForm();
            loadAllMaterials();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Upload failed. Make sure the Material table exists in the database.\n" +
                            "Run the CREATE TABLE SQL from MaterialDAOImpl.java first.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── UPDATE selected material ──────────────────────────────────────────
    private void updateMaterial() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a material row first."); return; }
        if (!validateForm(false)) return;

        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        Material m = materialDAO.getById(id);
        if (m == null) return;

        m.setTitle(titleField.getText().trim());
        m.setDescription(descField.getText().trim());

        // Only replace file if a new one was selected
        if (!selectedFilePath.isBlank()) {
            m.setFilePath(selectedFilePath);
            m.setFileName(selectedFileName);
        }

        if (materialDAO.updateMaterial(m)) {
            JOptionPane.showMessageDialog(this, "Material updated successfully.");
            clearForm();
            loadAllMaterials();
        } else {
            JOptionPane.showMessageDialog(this, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Delete selected ───────────────────────────────────────────────────
    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a material first."); return; }
        String title = tableModel.getValueAt(row, 3).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete material: \"" + title + "\"?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        if (materialDAO.deleteMaterial(id)) {
            loadAllMaterials();
            clearForm();
        }
    }

    // ── Open PDF in system viewer ─────────────────────────────────────────
    private void openSelectedPDF() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a material first."); return; }

        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        Material m = materialDAO.getById(id);
        if (m == null) return;

        File f = new File(m.getFilePath());
        if (!f.exists()) {
            JOptionPane.showMessageDialog(this,
                    "File not found:\n" + m.getFilePath() +
                            "\n\nThe file may have been moved or deleted.",
                    "File Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Desktop.getDesktop().open(f);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Cannot open file: " + ex.getMessage() +
                            "\n\nMake sure a PDF viewer is installed.",
                    "Open Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Populate form when row selected ──────────────────────────────────
    private void populateFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        String code = tableModel.getValueAt(row, 1).toString();
        String type = tableModel.getValueAt(row, 2).toString();
        String keyPrefix = code + "|" + type;  // match start of combo item "CODE|TYPE  – Name"

        // Select matching course in combo
        for (int i = 0; i < courseBox.getItemCount(); i++) {
            if (courseBox.getItemAt(i).startsWith(keyPrefix)) {
                courseBox.setSelectedIndex(i); break;
            }
        }
        titleField.setText(tableModel.getValueAt(row, 3).toString());
        Object desc = tableModel.getValueAt(row, 4);
        descField.setText(desc != null ? desc.toString() : "");
        filePathField.setText(tableModel.getValueAt(row, 5).toString());
        selectedFilePath = "";   // force new selection for file replacement
        selectedFileName = "";
    }

    // ── Load all materials for this lecturer ──────────────────────────────
    private void loadAllMaterials() {
        tableModel.setRowCount(0);
        List<Material> list = materialDAO.getByLecturer(lecturer.getUserId());
        for (Material m : list) {
            tableModel.addRow(new Object[]{
                    m.getMaterialId(),
                    m.getC_code(),
                    m.getC_type(),
                    m.getTitle(),
                    m.getDescription(),
                    m.getFileName(),
                    m.getUploadedAt() != null ? m.getUploadedAt().toString().substring(0, 16) : ""
            });
        }
    }

    // ── Validation ────────────────────────────────────────────────────────
    private boolean validateForm(boolean requireFile) {
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title is required."); return false;
        }
        if (requireFile && selectedFilePath.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please select a PDF file."); return false;
        }
        return true;
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /**
     * Populates (or repopulates) the courseBox from the lecturer's assigned courses.
     * Each item is displayed as "CODE|TYPE  –  Course Name" for clarity.
     * The backing courseList is kept in sync for parseCourse() lookups.
     */
    private void loadCourseBox() {
        courseList = courseDAO.getByLecturer(lecturer.getUserId());
        if (courseBox == null) {
            courseBox = new JComboBox<>();
        } else {
            courseBox.removeAllItems();
        }
        if (courseList.isEmpty()) {
            courseBox.addItem("No courses assigned");
        } else {
            for (Course c : courseList) {
                courseBox.addItem(c.getCourseCode() + "|" + c.getCourseType()
                        + "  \u2013  " + c.getCourseName());
            }
        }
    }

    /**
     * Extracts [C_code, C_type] from the selected combo item.
     * Items are "CODE|TYPE  – Name", so we split on "|" and trim the type part.
     */
    private String[] parseCourse() {
        String sel = (String) courseBox.getSelectedItem();
        if (sel != null && sel.contains("|")) {
            // sel = "ICT2142|T  –  Object Oriented..."
            // Split on "|" → ["ICT2142", "T  –  Object Oriented..."]
            String[] parts = sel.split("\\|", 2);
            String code = parts[0].trim();
            // Type is the first token before any whitespace/dash in parts[1]
            String type = parts[1].trim().split("\\s")[0];
            return new String[]{code, type};
        }
        return new String[]{"", "T"};
    }

    private void clearForm() {
        titleField.setText("");
        descField.setText("");
        filePathField.setText("");
        selectedFilePath = "";
        selectedFileName = "";
        table.clearSelection();
        if (courseBox.getItemCount() > 0) courseBox.setSelectedIndex(0);
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(UITheme.BODY);
        return l;
    }
}
