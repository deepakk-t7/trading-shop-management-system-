import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginPanel extends JFrame {

    private static final String VALID_USERNAME = "admin";
    private static final String VALID_PASSWORD = "admin123";

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblError;

    public LoginPanel() {
        setTitle("Login — Trading Shop Management System");
        setSize(480, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(new Color(33, 47, 61));
        setContentPane(outerPanel);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(40, 45, 40, 45)
        ));
        card.setPreferredSize(new Dimension(380, 480));
        card.setMaximumSize(new Dimension(380, 480));

        // Title
        JLabel lblTitle = new JLabel("Trading Shop");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(33, 47, 61));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblTitle);

        JLabel lblSubtitle = new JLabel("Management System");
        lblSubtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(127, 140, 141));
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblSubtitle);

        card.add(Box.createVerticalStrut(30));

        // Username
        card.add(makeFieldLabel("Username"));
        card.add(Box.createVerticalStrut(6));
        txtUsername = new JTextField();
        styleTextField(txtUsername);
        card.add(txtUsername);

        card.add(Box.createVerticalStrut(18));

        // Password
        card.add(makeFieldLabel("Password"));
        card.add(Box.createVerticalStrut(6));
        txtPassword = new JPasswordField();
        styleTextField(txtPassword);
        card.add(txtPassword);

        card.add(Box.createVerticalStrut(10));

        // Error label
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Arial", Font.PLAIN, 12));
        lblError.setForeground(new Color(192, 57, 43));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblError);

        card.add(Box.createVerticalStrut(15));

        // Login button
        JButton btnLogin = new JButton("Login");
        btnLogin.setBackground(new Color(33, 47, 61));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 15));
        btnLogin.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setOpaque(true);

        btnLogin.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnLogin.setBackground(new Color(52, 73, 94));
            }
            public void mouseExited(MouseEvent e) {
                btnLogin.setBackground(new Color(33, 47, 61));
            }
        });

        card.add(btnLogin);
        card.add(Box.createVerticalStrut(15));

        // JLabel lblHint = new JLabel("Default: admin / admin123");
        // lblHint.setFont(new Font("Arial", Font.ITALIC, 12));
        // lblHint.setForeground(new Color(149, 165, 166));
        // lblHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        // card.add(lblHint);

        outerPanel.add(card);

        ActionListener loginAction = e -> attemptLogin();
        btnLogin.addActionListener(loginAction);
        txtPassword.addActionListener(loginAction);
        txtUsername.addActionListener(e -> txtPassword.requestFocus());

        setVisible(true);
    }

    private void attemptLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.equals(VALID_USERNAME) && password.equals(VALID_PASSWORD)) {
            dispose();
            SwingUtilities.invokeLater(() -> new Dashboard().setVisible(true));
        } else {
            lblError.setText("Incorrect username or password.");
            txtPassword.setText("");
            txtPassword.requestFocus();
        }
    }

    //  CENTERED LABEL (like button)
    private JLabel makeFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.PLAIN, 14));
        lbl.setForeground(new Color(85, 85, 85));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT); // FIXED
        return lbl;
    }

    //  FULL WIDTH + CENTER FIELD
    private void styleTextField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 15));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45)); // full width
        field.setAlignmentX(Component.CENTER_ALIGNMENT); // FIXED
    }
}