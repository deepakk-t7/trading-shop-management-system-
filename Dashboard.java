import javax.swing.*;
import java.awt.*;


public class Dashboard extends JFrame {

    // CardLayout lets us swap between panels like switching TV channels
    private CardLayout cardLayout;
    private JPanel mainContentPanel;

    public Dashboard() {

        // Window Setup 
        setTitle("Trading Shop Management System");
        setSize(1100, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window on screen
        setLayout(new BorderLayout());

        // SIDEBAR (Left panel with navigation buttons) 
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(33, 47, 61));   // Dark navy
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // App title inside the sidebar
        JLabel appTitle = new JLabel("TRADING SHOP");
        appTitle.setFont(new Font("Arial", Font.BOLD, 15));
        appTitle.setForeground(new Color(174, 214, 241));
        appTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(appTitle);
        sidebar.add(Box.createVerticalStrut(5));

        JLabel appSubtitle = new JLabel("Management System");
        appSubtitle.setFont(new Font("Arial", Font.PLAIN, 11));
        appSubtitle.setForeground(new Color(127, 140, 141));
        appSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(appSubtitle);
        sidebar.add(Box.createVerticalStrut(25));

        // Divider line
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(190, 1));
        sep.setForeground(new Color(52, 73, 94));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(20));

        // Navigation buttons
        JButton btnInventory = createSidebarButton("Inventory");
        JButton btnCustomers = createSidebarButton("Customers");
        JButton btnBilling   = createSidebarButton("Billing / POS");

        sidebar.add(btnInventory);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnCustomers);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnBilling);

        // Push everything to the top
        sidebar.add(Box.createVerticalGlue());

        //  CONTENT AREA (Right panel — the "rooms") 
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(Color.WHITE);

        // Create each panel/room once
        InventoryPanel inventoryPanel = new InventoryPanel();
        CustomerPanel  customerPanel  = new CustomerPanel();
        BillingPanel   billingPanel   = new BillingPanel();

        // Add them to the card deck with a name
        mainContentPanel.add(inventoryPanel, "Inventory");
        mainContentPanel.add(customerPanel,  "Customers");
        mainContentPanel.add(billingPanel,   "Billing");

        // BUTTON LISTENERS (wire buttons to switch rooms) 
        // FIX: Each button has exactly ONE listener — no duplicates
        btnInventory.addActionListener(e -> cardLayout.show(mainContentPanel, "Inventory"));

        btnCustomers.addActionListener(e -> cardLayout.show(mainContentPanel, "Customers"));

        btnBilling.addActionListener(e -> {
            billingPanel.refreshDropdowns(); // Always reload fresh product/customer lists
            cardLayout.show(mainContentPanel, "Billing");
        });

        //  Add sidebar and content to the window 
        add(sidebar, BorderLayout.WEST);
        add(mainContentPanel, BorderLayout.CENTER);
    }

   
    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(52, 73, 94));
        button.setFocusPainted(false);         // Remove ugly focus border
        button.setBorderPainted(false);        // Remove border
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45)); // Full width, fixed height
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Hover effect: lighten on mouse over
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(74, 105, 132));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(52, 73, 94));
            }
        });
        return button;
    }

    // Entry point — run this to start the app
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Dashboard().setVisible(true));
    }
}