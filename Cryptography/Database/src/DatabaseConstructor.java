import java.sql.*;

public class DatabaseConstructor {
    public static void main(String[] args) {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            // Connect to the SQLite database (change the database name here)
            conn = DriverManager.getConnection("jdbc:sqlite:users.db");
            System.out.println("Connected to the database.");

            // Create a Statement object for executing SQL queries
            Statement stmt = conn.createStatement();

            Statement statement = conn.createStatement();

            // Define the SQL statement to create a new table
            String sql  ="CREATE TABLE IF NOT EXISTS User (" +
                    "UserID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "Username TEXT NOT NULL," +
                    "Password TEXT NOT NULL)";
            // Execute the SQL statement
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    System.out.println("Disconnected from the database.");
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}


