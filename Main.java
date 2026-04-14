public class Main {
    public static void main(String[] args) {

        System.out.println("Testing database connection...");
        java.sql.Connection conn = DBConnect.getConnection();

        if (conn == null) {
            System.out.println("FAILED — Could not connect to Oracle. Check DBConnect.java settings.");
            return;
        }
        System.out.println("SUCCESS — Java is talking to Oracle!");

        // FIX: LoginPanel's constructor calls setVisible(true) internally,
        // so we just instantiate it on the EDT — no extra .setVisible() needed.
        javax.swing.SwingUtilities.invokeLater(LoginPanel::new);
    }
}