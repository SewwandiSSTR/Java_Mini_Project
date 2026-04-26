package ums.gui.panels;

import ums.dao.IMedicalDAO;
import ums.dao.MedicalDAOImpl;
import ums.gui.UITheme;
import ums.model.MedicalRecord;
import ums.roles.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.List;

public class MedicalPanel extends JPanel {

    private final AbstractUser user;
    private final IMedicalDAO medDAO = new MedicalDAOImpl();
    private DefaultTableModel tableModel;

    public MedicalPanel(AbstractUser user) {
        this.user = user;
        setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("🏥  Medical Records");
        title.setFont(UITheme.TITLE);
        title.setForeground(UITheme.PRIMARY);
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.BODY);
        tabs.addTab("Attendance Medical", buildViewTab("attendance"));
        tabs.addTab("Mid Exam Medical",   buildViewTab("mid"));
        tabs.addTab("End Exam Medical",   buildViewTab("end"));
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildViewTab(String type) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UITheme.BG);
        p.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Medical ID","Regno","Course","Type","Status","Submitted","Start","End"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(UITheme.BODY); table.setRowHeight(26);
        table.getTableHeader().setFont(UITheme.HEADING);
        table.getTableHeader().setBackground(UITheme.TABLE_HEADER);

        JButton loadBtn = UITheme.primaryBtn("Load All");
        loadBtn.addActionListener(e -> {
            model.setRowCount(0);
            for (MedicalRecord r : medDAO.getAll(type)) {
                model.addRow(new Object[]{r.getMedical_Id(), r.getRegno(), r.getC_code(), r.getC_type(),
                        r.getMedical_status(), r.getSubmitted_Date(), r.getMedical_Start_Date(), r.getMedical_End_Date()});
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        btnRow.setBackground(UITheme.BG);
        btnRow.add(loadBtn);

        // Lecturer can approve/reject
        if (user instanceof Lecturer lec) {
            JButton approveBtn = UITheme.successBtn("✔ Approve Selected");
            JButton rejectBtn  = UITheme.dangerBtn("✖ Reject Selected");
            approveBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) return;
                String id = safe(model.getValueAt(row, 0));
                if (lec.approveMedical(id, type)) { model.setValueAt("Approved", row, 4); }
            });
            rejectBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) return;
                String id = safe(model.getValueAt(row, 0));
                if (lec.rejectMedical(id, type)) { model.setValueAt("Not-approved", row, 4); }
            });
            btnRow.add(approveBtn); btnRow.add(rejectBtn);
        }

        // Technical Officer can submit medical
        if (user instanceof TechnicalOfficer to) {
            JPanel form = UITheme.sectionPanel("Submit Medical Record");
            form.setLayout(new GridBagLayout());
            GridBagConstraints gc2 = new GridBagConstraints();
            gc2.insets = new Insets(6,8,6,8);
            gc2.fill = GridBagConstraints.HORIZONTAL;
            gc2.anchor = GridBagConstraints.WEST;

            JTextField codeF  = new JTextField(10);
            JTextField ctypeF = new JTextField(4);
            JTextField regnoF = new JTextField(14);
            JTextField subF   = new JTextField(12);
            JTextField startF = new JTextField(12);
            JTextField endF2  = new JTextField(12);
            // Auto-generate Medical ID (UUID stripped, 20 chars)
            String autoId = java.util.UUID.randomUUID().toString().replace("-","").substring(0,20);

            // Default submitted date to today
            subF.setText(new java.sql.Date(System.currentTimeMillis()).toString());

            // Row 0: Course | Type | Reg No
            gc2.gridy=0; gc2.gridx=0; gc2.weightx=0; form.add(lbl("Course Code:"),gc2);
            gc2.gridx=1; gc2.weightx=1; form.add(codeF,gc2);
            gc2.gridx=2; gc2.weightx=0; form.add(lbl("Type (T/P):"),gc2);
            gc2.gridx=3; gc2.weightx=1; form.add(ctypeF,gc2);
            gc2.gridx=4; gc2.weightx=0; form.add(lbl("Reg No:"),gc2);
            gc2.gridx=5; gc2.weightx=1; form.add(regnoF,gc2);
            // Row 1: Dates
            gc2.gridy=1; gc2.gridx=0; gc2.weightx=0; form.add(lbl("Submitted (yyyy-mm-dd):"),gc2);
            gc2.gridx=1; gc2.weightx=1; form.add(subF,gc2);
            gc2.gridx=2; gc2.weightx=0; form.add(lbl("Start Date:"),gc2);
            gc2.gridx=3; gc2.weightx=1; form.add(startF,gc2);
            gc2.gridx=4; gc2.weightx=0; form.add(lbl("End Date:"),gc2);
            gc2.gridx=5; gc2.weightx=1; form.add(endF2,gc2);

            JButton submitBtn = UITheme.primaryBtn("Submit Medical");
            submitBtn.addActionListener(e -> {
                try {
                    String sub = subF.getText().trim(), st = startF.getText().trim(), en = endF2.getText().trim();
                    String code = codeF.getText().trim(), ctype = ctypeF.getText().trim().toUpperCase(), regno = regnoF.getText().trim();
                    if (code.isEmpty()||ctype.isEmpty()||regno.isEmpty()||sub.isEmpty()||st.isEmpty()||en.isEmpty()) {
                        JOptionPane.showMessageDialog(p,"All fields are required."); return;
                    }
                    MedicalRecord r = new MedicalRecord();
                    r.setMedical_Id(java.util.UUID.randomUUID().toString().replace("-","").substring(0,20));
                    r.setC_code(code); r.setC_type(ctype);
                    r.setRegno(regno); r.setMedi_type(type);
                    r.setSubmitted_Date(Date.valueOf(sub));
                    r.setMedical_Start_Date(Date.valueOf(st));
                    r.setMedical_End_Date(Date.valueOf(en));
                    if (to.submitMedical(r)) {
                        JOptionPane.showMessageDialog(p,"Medical submitted. Status: Pending.");
                        codeF.setText(""); ctypeF.setText(""); regnoF.setText("");
                        startF.setText(""); endF2.setText("");
                        subF.setText(new java.sql.Date(System.currentTimeMillis()).toString());
                    } else JOptionPane.showMessageDialog(p,"Submission failed.","Error",JOptionPane.ERROR_MESSAGE);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(p,"Invalid date format. Use yyyy-mm-dd.","Error",JOptionPane.ERROR_MESSAGE);
                }
            });
            btnRow.add(submitBtn);
            JPanel south = new JPanel(new BorderLayout());
            south.setBackground(UITheme.BG);
            south.add(form, BorderLayout.NORTH);
            south.add(btnRow, BorderLayout.SOUTH);
            p.add(UITheme.scroll(table), BorderLayout.CENTER);
            p.add(south, BorderLayout.SOUTH);
            return p;
        }

        p.add(btnRow, BorderLayout.NORTH);
        p.add(UITheme.scroll(table), BorderLayout.CENTER);
        return p;
    }

    private JLabel lbl(String t) { JLabel l = new JLabel(t); l.setFont(UITheme.BODY); return l; }
    private String safe(Object o) { return o == null ? "" : o.toString(); }
}
