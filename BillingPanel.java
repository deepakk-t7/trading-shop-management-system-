import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class BillingPanel extends JPanel {

    
    private JComboBox<String> cbCustomers, cbProducts;

    // Input fields
    private JTextField txtQuantity, txtAmountPaid;

    // Grand total label
    private JLabel lblGrandTotal;

    // Cart table (shows items added before checkout)
    private JTable cartTable;
    private DefaultTableModel cartModel;

    // Running total of the current sale
    private double grandTotal = 0.0;

    public BillingPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // TOP: Customer + Product selection 
        JPanel selectionPanel = new JPanel(new GridLayout(2, 4, 10, 8));
        selectionPanel.setBackground(Color.WHITE);
        selectionPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            "  New Sale  "
        ));
        selectionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)), "  New Sale  "),
            BorderFactory.createEmptyBorder(6, 8, 8, 8)
        ));

        // Row 1: Customer & Product dropdowns
        selectionPanel.add(makeLabel("Select Customer:"));
        cbCustomers = new JComboBox<>();
        selectionPanel.add(cbCustomers);

        selectionPanel.add(makeLabel("Select Product:"));
        cbProducts = new JComboBox<>();
        selectionPanel.add(cbProducts);

        // Row 2: Quantity & Add button
        selectionPanel.add(makeLabel("Quantity:"));
        txtQuantity = new JTextField();
        selectionPanel.add(txtQuantity);

        JButton btnAdd = makeButton("+ Add to Cart", new Color(41, 128, 185));
        selectionPanel.add(btnAdd);

        JButton btnRemove = makeButton("Remove Last Item", new Color(127, 140, 141));
        selectionPanel.add(btnRemove);

        add(selectionPanel, BorderLayout.NORTH);

        // CENTER: Cart table 
        String[] cartCols = {"Product ID", "Product Name", "Price (Rs)", "Qty", "Line Total (Rs)"};
        cartModel = new DefaultTableModel(cartCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(26);
        cartTable.setFont(new Font("Arial", Font.PLAIN, 13));
        cartTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        cartTable.getTableHeader().setBackground(new Color(44, 62, 80));
        cartTable.getTableHeader().setForeground(Color.WHITE);
        cartTable.setGridColor(new Color(220, 220, 220));

        // Hide the ID column (index 0) — it's needed internally but ugly to show
        cartTable.getColumnModel().getColumn(0).setMinWidth(0);
        cartTable.getColumnModel().getColumn(0).setMaxWidth(0);

        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            "  Items in Cart  "
        ));
        add(scrollPane, BorderLayout.CENTER);

        // BOTTOM: Total + Payment + Checkout 
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottomPanel.setBackground(new Color(245, 246, 250));
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(189, 195, 199)));

        lblGrandTotal = new JLabel("Grand Total:  Rs 0.00");
        lblGrandTotal.setFont(new Font("Arial", Font.BOLD, 18));
        lblGrandTotal.setForeground(new Color(39, 174, 96));
        bottomPanel.add(lblGrandTotal);

        bottomPanel.add(makeLabel("  Amount Paid by Customer:  Rs"));
        txtAmountPaid = new JTextField("0", 8);
        txtAmountPaid.setFont(new Font("Arial", Font.PLAIN, 14));
        bottomPanel.add(txtAmountPaid);

        JButton btnCheckout = makeButton("Complete Sale + Print Bill", new Color(39, 174, 96));
        btnCheckout.setFont(new Font("Arial", Font.BOLD, 14));
        bottomPanel.add(btnCheckout);

        add(bottomPanel, BorderLayout.SOUTH);

        // WIRE UP 
        refreshDropdowns();
        btnAdd.addActionListener(e    -> addToCart());
        btnRemove.addActionListener(e -> removeLastFromCart());
        btnCheckout.addActionListener(e -> processCheckout());
    }

 
    public void refreshDropdowns() {
        cbCustomers.removeAllItems();
        cbProducts.removeAllItems();
        try (Connection conn = DBConnect.getConnection();
             Statement stmt = conn.createStatement()) {

            // Load all customers
            ResultSet rsCust = stmt.executeQuery(
                "SELECT Customer_ID, Name FROM Customers ORDER BY Name");
            while (rsCust.next()) {
                cbCustomers.addItem(rsCust.getInt(1) + " - " + rsCust.getString(2));
            }

            // Load only products that are in stock (stock > 0)
            ResultSet rsProd = stmt.executeQuery(
                "SELECT Product_ID, Name, Price_Per_Unit, Current_Stock " +
                "FROM Products WHERE Current_Stock > 0 ORDER BY Name");
            while (rsProd.next()) {
                cbProducts.addItem(
                    rsProd.getInt(1) + " | " +
                    rsProd.getString(2) + " | Rs " +
                    rsProd.getDouble(3) + " | Stock: " + rsProd.getInt(4)
                );
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading dropdowns: " + e.getMessage());
        }
    }

    /** Add the selected product+qty to the cart table */
    private void addToCart() {
        if (cbProducts.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "No products in stock. Please add stock in Inventory.");
            return;
        }
        try {
            // Parse the product dropdown string: "ID | Name | Rs price | Stock: n"
            String selected = cbProducts.getSelectedItem().toString();
            String[] parts  = selected.split(" \\| ");
            int    productId = Integer.parseInt(parts[0].trim());
            String prodName  = parts[1].trim();
            double price     = Double.parseDouble(parts[2].replace("Rs", "").trim());
            int    maxStock  = Integer.parseInt(parts[3].replace("Stock:", "").trim());

            int qty = Integer.parseInt(txtQuantity.getText().trim());

            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.");
                return;
            }
            if (qty > maxStock) {
                JOptionPane.showMessageDialog(this,
                    "Not enough stock! Available: " + maxStock + " only.");
                return;
            }

            double lineTotal = price * qty;
            cartModel.addRow(new Object[]{productId, prodName, price, qty, lineTotal});

            grandTotal += lineTotal;
            lblGrandTotal.setText(String.format("Grand Total:  Rs %.2f", grandTotal));
            txtQuantity.setText("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid whole number for quantity.");
        }
    }

    /** Remove the last row added to the cart */
    private void removeLastFromCart() {
        int rows = cartModel.getRowCount();
        if (rows == 0) return;
        double lastTotal = (double) cartModel.getValueAt(rows - 1, 4);
        grandTotal -= lastTotal;
        cartModel.removeRow(rows - 1);
        lblGrandTotal.setText(String.format("Grand Total:  Rs %.2f", grandTotal));
    }


    private void processCheckout() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty. Add items before checking out.");
            return;
        }
        if (cbCustomers.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "No customers found. Please add a customer first.");
            return;
        }

        double paid;
        try {
            paid = Double.parseDouble(txtAmountPaid.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount paid.");
            return;
        }

        if (paid > grandTotal) {
            JOptionPane.showMessageDialog(this, "Amount paid cannot be more than the grand total.");
            return;
        }

        // Parse customer
        String custStr    = cbCustomers.getSelectedItem().toString();
        int    customerId = Integer.parseInt(custStr.split(" - ")[0].trim());
        String custName   = custStr.split(" - ")[1].trim();
        double due        = grandTotal - paid;

        Connection conn = DBConnect.getConnection();

        try {
            conn.setAutoCommit(false); // ← Start transaction block

            // ── Step 1: Insert the invoice header ─────────────────────
            PreparedStatement psInv = conn.prepareStatement(
                "INSERT INTO Sales_Invoice (Customer_ID, Total_Amount, Paid_Amount) VALUES (?, ?, ?)",
                new String[]{"INVOICE_ID"}
            );
            psInv.setInt(1, customerId);
            psInv.setDouble(2, grandTotal);
            psInv.setDouble(3, paid);
            psInv.executeUpdate();

            // Get the auto-generated Invoice ID back from Oracle
            ResultSet rsKeys = psInv.getGeneratedKeys();
            rsKeys.next();
            int invoiceId = rsKeys.getInt(1);

            // Step 2: Insert each cart item + reduce stock 
            PreparedStatement psItem = conn.prepareStatement(
                "INSERT INTO Sales_Items (Invoice_ID, Product_ID, Quantity, Total_Price) VALUES (?, ?, ?, ?)");
            PreparedStatement psStock = conn.prepareStatement(
                "UPDATE Products SET Current_Stock = Current_Stock - ? WHERE Product_ID = ?");

            for (int i = 0; i < cartModel.getRowCount(); i++) {
                int    pId      = (int)    cartModel.getValueAt(i, 0);
                int    qty      = (int)    cartModel.getValueAt(i, 3);
                double tPrice   = (double) cartModel.getValueAt(i, 4);

                psItem.setInt(1, invoiceId);
                psItem.setInt(2, pId);
                psItem.setInt(3, qty);
                psItem.setDouble(4, tPrice);
                psItem.executeUpdate();

                psStock.setInt(1, qty);
                psStock.setInt(2, pId);
                psStock.executeUpdate();
            }

            // Step 3: Add unpaid amount to customer's balance 
            if (due > 0) {
                PreparedStatement psCust = conn.prepareStatement(
                    "UPDATE Customers SET Outstanding_Balance = Outstanding_Balance + ? WHERE Customer_ID = ?");
                psCust.setDouble(1, due);
                psCust.setInt(2, customerId);
                psCust.executeUpdate();
            }

            conn.commit(); // ← Everything worked — save it all

            //  FIX: Show receipt FIRST while cart data is still available
            showReceipt(invoiceId, custName, grandTotal, paid, due);

            // THEN reset the billing screen
            cartModel.setRowCount(0);
            grandTotal = 0.0;
            lblGrandTotal.setText("Grand Total:  Rs 0.00");
            txtAmountPaid.setText("0");
            refreshDropdowns();

        } catch (Exception ex) {
            try { conn.rollback(); } catch (SQLException ignored) {} // Undo everything on error
            JOptionPane.showMessageDialog(this,
                "Sale failed and was cancelled.\nReason: " + ex.getMessage(),
                "Transaction Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            // Always restore autoCommit, even if an error occurred
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    /** Build and display a formatted text receipt, with option to print */
    private void showReceipt(int invoiceId, String custName, double total, double paid, double due) {
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());

        StringBuilder sb = new StringBuilder();
        sb.append("==========================================\n");
        sb.append("     CONSTRUCTION MATERIAL TRADERS        \n");
        sb.append("==========================================\n");
        sb.append(String.format("Invoice #  : %d\n", invoiceId));
        sb.append(String.format("Date       : %s\n", date));
        sb.append(String.format("Customer   : %s\n", custName));
        sb.append("------------------------------------------\n");
        sb.append(String.format("%-22s %5s  %10s\n", "Item", "Qty", "Amount"));
        sb.append("------------------------------------------\n");

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            String name  = cartModel.getValueAt(i, 1).toString();
            if (name.length() > 20) name = name.substring(0, 20);
            int    qty   = (int)    cartModel.getValueAt(i, 3);
            double price = (double) cartModel.getValueAt(i, 4);
            sb.append(String.format("%-22s %5d  %10.2f\n", name, qty, price));
        }

        sb.append("------------------------------------------\n");
        sb.append(String.format("%-28s %10.2f\n", "Grand Total (Rs):", total));
        sb.append(String.format("%-28s %10.2f\n", "Paid (Rs):", paid));
        sb.append(String.format("%-28s %10.2f\n", "Balance Due (Rs):", due));
        sb.append("==========================================\n");
        sb.append("      Thank you for your business!        \n");
        sb.append("==========================================\n");

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        textArea.setEditable(false);
        textArea.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(450, 400));

        int option = JOptionPane.showConfirmDialog(this, scrollPane,
            "Sale Complete — Print Receipt?",
            JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            try {
                textArea.print();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Printer error: " + e.getMessage());
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────

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
}