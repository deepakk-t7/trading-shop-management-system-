import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class InventoryPanel extends JPanel {

    // The table that shows all products
    private JTable productTable;
    private DefaultTableModel tableModel;

    // Input fields at the top
    private JTextField txtName, txtCategory, txtPrice, txtStock;
    private JComboBox<String> cbUnit;

    // Buttons
    private JButton btnSave, btnUpdate, btnDelete, btnClear;

    // Tracks which product row is currently selected (-1 = nothing selected)
    private int selectedProductId = -1;

    public InventoryPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ── TOP: Input form
        JPanel formPanel = new JPanel(new BorderLayout(8, 8));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            "  Product Details  "
        ));

        // 5 columns: label + field pairs laid out in a grid
        JPanel fieldsGrid = new JPanel(new GridLayout(2, 6, 8, 8));
        fieldsGrid.setBackground(Color.WHITE);
        fieldsGrid.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Row 1
        fieldsGrid.add(makeLabel("Product Name:"));
        txtName = new JTextField();
        fieldsGrid.add(txtName);

        fieldsGrid.add(makeLabel("Category:"));
        txtCategory = new JTextField();
        fieldsGrid.add(txtCategory);

        fieldsGrid.add(makeLabel("Unit:"));
        cbUnit = new JComboBox<>(new String[]{"Bags", "Tons", "SqFt", "Pieces", "Kg", "Litres"});
        fieldsGrid.add(cbUnit);

        // Row 2
        fieldsGrid.add(makeLabel("Price Per Unit (Rs):"));
        txtPrice = new JTextField();
        fieldsGrid.add(txtPrice);

        fieldsGrid.add(makeLabel("Stock Quantity:"));
        txtStock = new JTextField("0");
        fieldsGrid.add(txtStock);

        fieldsGrid.add(new JLabel()); // empty filler cell
        fieldsGrid.add(new JLabel()); // empty filler cell

        // Buttons row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        btnRow.setBackground(Color.WHITE);

        btnSave   = makeButton("Save New Product",  new Color(39, 174, 96));
        btnUpdate = makeButton("Update Selected",   new Color(41, 128, 185));
        btnDelete = makeButton("Delete Selected",   new Color(192, 57, 43));
        btnClear  = makeButton("Clear Form",        new Color(127, 140, 141));

        btnRow.add(btnSave);
        btnRow.add(btnUpdate);
        btnRow.add(btnDelete);
        btnRow.add(btnClear);

        formPanel.add(fieldsGrid, BorderLayout.CENTER);
        formPanel.add(btnRow, BorderLayout.SOUTH);
        add(formPanel, BorderLayout.NORTH);

        // CENTER: Product Table
        String[] columns = {"ID", "Product Name", "Category", "Unit", "Price (Rs)", "Stock"};
        tableModel = new DefaultTableModel(columns, 0) {
            // Make cells non-editable directly in table — use the form above
            public boolean isCellEditable(int row, int col) { return false; }
        };
        productTable = new JTable(tableModel);
        productTable.setRowHeight(26);
        productTable.setFont(new Font("Arial", Font.PLAIN, 13));
        productTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        productTable.getTableHeader().setBackground(new Color(44, 62, 80));
        productTable.getTableHeader().setForeground(Color.WHITE);
        productTable.setSelectionBackground(new Color(174, 214, 241));
        productTable.setGridColor(new Color(220, 220, 220));

        // Set column widths
        productTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        productTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        productTable.getColumnModel().getColumn(5).setPreferredWidth(70);

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            "  Current Inventory  "
        ));
        add(scrollPane, BorderLayout.CENTER);

        // WIRE UP: Buttons and table click
        loadProducts();



 // buttons logic

        btnSave.addActionListener(e   -> saveProduct());
        btnUpdate.addActionListener(e -> updateProduct());
        btnDelete.addActionListener(e -> deleteProduct());
        btnClear.addActionListener(e  -> clearForm());

        // When a row is clicked → fill the form with that row's data
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && productTable.getSelectedRow() != -1) {
                int row = productTable.getSelectedRow();
                selectedProductId = (int) tableModel.getValueAt(row, 0);
                txtName.setText(tableModel.getValueAt(row, 1).toString());
                txtCategory.setText(tableModel.getValueAt(row, 2).toString());
                cbUnit.setSelectedItem(tableModel.getValueAt(row, 3).toString());
                txtPrice.setText(tableModel.getValueAt(row, 4).toString());
                txtStock.setText(tableModel.getValueAt(row, 5).toString());
            }
        });
    }

    //  Database Methods

   
    private void loadProducts() {
        tableModel.setRowCount(0); // Clear table first
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT Product_ID, Name, Category, Unit, Price_Per_Unit, Current_Stock FROM Products ORDER BY Product_ID");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("Product_ID"),
                    rs.getString("Name"),
                    rs.getString("Category"),
                    rs.getString("Unit"),
                    rs.getDouble("Price_Per_Unit"),
                    rs.getInt("Current_Stock")
                });
            }
        } catch (SQLException e) {
            showError("Error loading products: " + e.getMessage());
        }
    }


    private void saveProduct() {
        if (txtName.getText().trim().isEmpty() || txtPrice.getText().trim().isEmpty()) {
            showError("Product Name and Price are required.");
            return;
        }
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO Products (Name, Category, Unit, Price_Per_Unit, Current_Stock) VALUES (?, ?, ?, ?, ?)")) {

            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtCategory.getText().trim());
            ps.setString(3, cbUnit.getSelectedItem().toString());
            ps.setDouble(4, Double.parseDouble(txtPrice.getText().trim()));
            ps.setInt(5,    Integer.parseInt(txtStock.getText().trim()));
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Product saved successfully!");
            clearForm();
            loadProducts();

        } catch (NumberFormatException e) {
            showError("Price and Stock must be valid numbers.");
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

 
    private void updateProduct() {
        if (selectedProductId == -1) {
            showError("Please click on a product in the table first.");
            return;
        }
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE Products SET Name=?, Category=?, Unit=?, Price_Per_Unit=?, Current_Stock=? WHERE Product_ID=?")) {

            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtCategory.getText().trim());
            ps.setString(3, cbUnit.getSelectedItem().toString());
            ps.setDouble(4, Double.parseDouble(txtPrice.getText().trim()));
            ps.setInt(5,    Integer.parseInt(txtStock.getText().trim()));
            ps.setInt(6,    selectedProductId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Product updated successfully!");
            clearForm();
            loadProducts();

        } catch (NumberFormatException e) {
            showError("Price and Stock must be valid numbers.");
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }


    private void deleteProduct() {
        if (selectedProductId == -1) {
            showError("Please click on a product in the table first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this product?\nThis cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBConnect.getConnection()) {

            // Check if this product has been used in any sale
            PreparedStatement check = conn.prepareStatement(
                "SELECT COUNT(*) FROM Sales_Items WHERE Product_ID = ?");
            check.setInt(1, selectedProductId);
            ResultSet rs = check.executeQuery();
            rs.next();
            int usageCount = rs.getInt(1);

            if (usageCount > 0) {
                // Cannot delete — product is tied to sales history
                JOptionPane.showMessageDialog(this,
                    "Cannot delete this product.\n" +
                    "It appears in " + usageCount + " past sale(s).\n\n" +
                    "Tip: Set its stock to 0 instead so it won't appear in new bills.",
                    "Cannot Delete", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Safe to delete
            PreparedStatement del = conn.prepareStatement(
                "DELETE FROM Products WHERE Product_ID = ?");
            del.setInt(1, selectedProductId);
            del.executeUpdate();

            JOptionPane.showMessageDialog(this, "Product deleted.");
            clearForm();
            loadProducts();

        } catch (SQLException e) {
            showError("Delete failed: " + e.getMessage());
        }
    }

  
    private void clearForm() {
        txtName.setText("");
        txtCategory.setText("");
        txtPrice.setText("");
        txtStock.setText("0");
        cbUnit.setSelectedIndex(0);
        selectedProductId = -1;
        productTable.clearSelection();
    }

    // Helper Builders 

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