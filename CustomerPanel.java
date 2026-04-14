import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;


public class CustomerPanel extends JPanel {

    private JTable customerTable;
    private DefaultTableModel tableModel;

    // Input fields
    private JTextField txtName, txtPhone, txtAddress, txtBalance;

    // Buttons
    private JButton btnSave, btnUpdate, btnDelete, btnClear;

    // Tracks which customer is currently selected (-1 = nothing)
    private int selectedCustomerId = -1;

    public CustomerPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // TOP: Input form 
        JPanel formPanel = new JPanel(new BorderLayout(8, 8));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            "  Customer Details  "
        ));

        JPanel fieldsGrid = new JPanel(new GridLayout(2, 4, 8, 8));
        fieldsGrid.setBackground(Color.WHITE);
        fieldsGrid.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Row 1
        fieldsGrid.add(makeLabel("Customer Name:"));
        txtName = new JTextField();
        fieldsGrid.add(txtName);

        fieldsGrid.add(makeLabel("Phone Number:"));
        txtPhone = new JTextField();
        fieldsGrid.add(txtPhone);

        // Row 2
        fieldsGrid.add(makeLabel("Address:"));
        txtAddress = new JTextField();
        fieldsGrid.add(txtAddress);

        fieldsGrid.add(makeLabel("Outstanding Balance (Rs):"));
        txtBalance = new JTextField("0.00");
        fieldsGrid.add(txtBalance);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        btnRow.setBackground(Color.WHITE);

        
 //cutomer btns
        btnSave   = makeButton("Save Customer",    new Color(39, 174, 96));
        btnUpdate = makeButton("Update Selected",  new Color(41, 128, 185));
        btnDelete = makeButton("Delete Selected",  new Color(192, 57, 43));
        btnClear  = makeButton("Clear Form",       new Color(127, 140, 141));

        btnRow.add(btnSave);
        btnRow.add(btnUpdate);
        btnRow.add(btnDelete);
        btnRow.add(btnClear);

        formPanel.add(fieldsGrid, BorderLayout.CENTER);
        formPanel.add(btnRow, BorderLayout.SOUTH);
        add(formPanel, BorderLayout.NORTH);

        //  CENTER: Customer Table 
        String[] columns = {"ID", "Name", "Phone", "Address", "Outstanding Balance (Rs)"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        customerTable = new JTable(tableModel);
        customerTable.setRowHeight(26);
        customerTable.setFont(new Font("Arial", Font.PLAIN, 13));
        customerTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        customerTable.getTableHeader().setBackground(new Color(44, 62, 80));
        customerTable.getTableHeader().setForeground(Color.WHITE);
        customerTable.setSelectionBackground(new Color(174, 214, 241));
        customerTable.setGridColor(new Color(220, 220, 220));

        // Column widths
        customerTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        customerTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        customerTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        customerTable.getColumnModel().getColumn(3).setPreferredWidth(220);
        customerTable.getColumnModel().getColumn(4).setPreferredWidth(160);

        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            "  All Customers  "
        ));
        add(scrollPane, BorderLayout.CENTER);

        // WIRE UP 
        loadCustomers();

        btnSave.addActionListener(e   -> saveCustomer());
        btnUpdate.addActionListener(e -> updateCustomer());
        btnDelete.addActionListener(e -> deleteCustomer());
        btnClear.addActionListener(e  -> clearForm());

        // Click a row → fill the form
        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && customerTable.getSelectedRow() != -1) {
                int row = customerTable.getSelectedRow();
                selectedCustomerId = (int) tableModel.getValueAt(row, 0);
                txtName.setText(tableModel.getValueAt(row, 1).toString());
                txtPhone.setText(tableModel.getValueAt(row, 2).toString());
                txtAddress.setText(tableModel.getValueAt(row, 3).toString());
                txtBalance.setText(tableModel.getValueAt(row, 4).toString());
            }
        });
    }

    //  Database Methods 

    private void loadCustomers() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT Customer_ID, Name, Phone, Address, Outstanding_Balance FROM Customers ORDER BY Customer_ID");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("Customer_ID"),
                    rs.getString("Name"),
                    rs.getString("Phone"),
                    rs.getString("Address"),
                    rs.getDouble("Outstanding_Balance")
                });
            }
        } catch (SQLException e) {
            showError("Error loading customers: " + e.getMessage());
        }
    }

    private void saveCustomer() {
        if (txtName.getText().trim().isEmpty()) {
            showError("Customer Name is required.");
            return;
        }
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO Customers (Name, Phone, Address, Outstanding_Balance) VALUES (?, ?, ?, ?)")) {

            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtPhone.getText().trim());
            ps.setString(3, txtAddress.getText().trim());
            ps.setDouble(4, Double.parseDouble(txtBalance.getText().trim()));
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Customer saved successfully!");
            clearForm();
            loadCustomers();

        } catch (NumberFormatException e) {
            showError("Balance must be a valid number (e.g. 0.00).");
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private void updateCustomer() {
        if (selectedCustomerId == -1) {
            showError("Please click on a customer in the table first.");
            return;
        }
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE Customers SET Name=?, Phone=?, Address=?, Outstanding_Balance=? WHERE Customer_ID=?")) {

            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtPhone.getText().trim());
            ps.setString(3, txtAddress.getText().trim());
            ps.setDouble(4, Double.parseDouble(txtBalance.getText().trim()));
            ps.setInt(5,    selectedCustomerId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Customer updated successfully!");
            clearForm();
            loadCustomers();

        } catch (NumberFormatException e) {
            showError("Balance must be a valid number.");
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private void deleteCustomer() {
        if (selectedCustomerId == -1) {
            showError("Please click on a customer in the table first.");
            return;
        }

        try (Connection conn = DBConnect.getConnection()) {

            // Count how many invoices this customer has
            PreparedStatement check = conn.prepareStatement(
                "SELECT COUNT(*) FROM Sales_Invoice WHERE Customer_ID = ?");
            check.setInt(1, selectedCustomerId);
            ResultSet rs = check.executeQuery();
            rs.next();
            int invoiceCount = rs.getInt(1);

            if (invoiceCount > 0) {
                // Ask if they want to delete the customer AND all their invoice history
                int choice = JOptionPane.showConfirmDialog(this,
                    "This customer has " + invoiceCount + " invoice(s) on record.\n\n" +
                    "To delete the customer, ALL their invoice history will also be deleted.\n" +
                    "This cannot be undone.\n\n" +
                    "Do you want to delete everything?",
                    "Customer Has Invoice History", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (choice != JOptionPane.YES_OPTION) return;

                // Delete in order: Sales_Items → Sales_Invoice → Customer
                conn.setAutoCommit(false);
                try {




                    // subquery
                    // Step 1: Delete all sales items for this customer's invoices
                    PreparedStatement delItems = conn.prepareStatement(
                        "DELETE FROM Sales_Items WHERE Invoice_ID IN " +
                        "(SELECT Invoice_ID FROM Sales_Invoice WHERE Customer_ID = ?)");
                    delItems.setInt(1, selectedCustomerId);
                    delItems.executeUpdate();

                    // Step 2: Delete the invoices themselves
                    PreparedStatement delInv = conn.prepareStatement(
                        "DELETE FROM Sales_Invoice WHERE Customer_ID = ?");
                    delInv.setInt(1, selectedCustomerId);
                    delInv.executeUpdate();

                    // Step 3: Now delete the customer
                    PreparedStatement delCust = conn.prepareStatement(
                        "DELETE FROM Customers WHERE Customer_ID = ?");
                    delCust.setInt(1, selectedCustomerId);
                    delCust.executeUpdate();

                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Customer and all their invoices deleted.");

                } catch (SQLException ex) {
                    conn.rollback(); // Undo everything if any step fails
                    showError("Delete failed and was rolled back: " + ex.getMessage());
                } finally {
                    conn.setAutoCommit(true); // Always restore this
                }

            } else {
                // No invoices — simple delete
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete this customer? This cannot be undone.",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (confirm != JOptionPane.YES_OPTION) return;

                PreparedStatement del = conn.prepareStatement(
                    "DELETE FROM Customers WHERE Customer_ID = ?");
                del.setInt(1, selectedCustomerId);
                del.executeUpdate();

                JOptionPane.showMessageDialog(this, "Customer deleted.");
            }

            clearForm();
            loadCustomers();

        } catch (SQLException e) {
            showError("Delete failed: " + e.getMessage());
        }
    }

    private void clearForm() {
        txtName.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
        txtBalance.setText("0.00");
        selectedCustomerId = -1;
        customerTable.clearSelection();
    }

    // Helpers 

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.PLAIN, 13));
        return lbl;
    }

    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}