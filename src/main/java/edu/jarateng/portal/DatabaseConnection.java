package edu.jarateng.portal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;

public final class DatabaseConnection {
    private DatabaseConnection() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DatabaseConnection::testConnection);
    }

    private static void testConnection() {
        String username = DatabaseStore.configuredUsername();
        String password = System.getenv("MYSQL_PASSWORD");

        if (password == null || password.isEmpty()) {
            JPasswordField passwordInput = new CopyablePasswordField(24);
            int answer = JOptionPane.showConfirmDialog(null, passwordInput,
                    "MySQL password for " + username, JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) {
                return;
            }
            password = new String(passwordInput.getPassword());
        }

        Properties credentials = new Properties();
        credentials.setProperty("user", username);
        credentials.setProperty("password", password);

        try (Connection connection = DriverManager.getConnection(
                DatabaseStore.configuredUrl(), credentials);
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT DATABASE()")) {
            String database = result.next() ? result.getString(1) : "(unknown)";
            JOptionPane.showMessageDialog(null,
                    "Connected successfully to database: " + database,
                    "MySQL connection successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException error) {
            System.err.println("MySQL connection failed: " + error.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Could not connect to MySQL.\n\n" + error.getMessage()
                            + "\n\nCheck that MySQL is running, the password is correct, "
                            + "and this user has access to the configured database.",
                    "MySQL connection failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
