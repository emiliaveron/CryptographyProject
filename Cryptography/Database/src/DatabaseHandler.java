import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseHandler {
    private Connection conn;
    private static final Logger logger = LoggerFactory.getLogger(DatabaseHandler.class);

    // Constructor to initialize the database connection
    public DatabaseHandler(String dbName) {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);
            logger.info("Connected to the database");
        } catch (SQLException e) {
            logger.error("Error connecting to the database", e);
        }
    }

    // Method to insert a new user record
    public void insertUser(String username, String password) {
        try {
            // Hash the password before inserting it into the database
            String hashedPassword = hashPassword(password);
            if (hashedPassword != null) {
                String insertUserSQL = "INSERT INTO User (Username, Password) VALUES (?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertUserSQL);
                pstmt.setString(1, username);
                pstmt.setString(2, hashedPassword);
                pstmt.executeUpdate();
                logger.info("User inserted successfully");
            } else {
                logger.error("Error hashing password");
            }
        } catch (SQLException e) {
            logger.error("Error inserting user", e);
        }
    }

    // Method to check if a user exists
    public boolean userExists(String username) {
        try {
            String checkUserSQL = "SELECT COUNT(*) AS Count FROM User WHERE Username = ?";
            PreparedStatement pstmt = conn.prepareStatement(checkUserSQL);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            int count = rs.getInt("Count");
            return count > 0;
        } catch (SQLException e) {
            logger.error("Error checking if user exists", e);
            return false;
        }
    }

    // Method to verify user password
    public boolean verifyPassword(String username, String password) {
        try {
            String verifyPasswordSQL = "SELECT Password FROM User WHERE Username = ?";
            PreparedStatement pstmt = conn.prepareStatement(verifyPasswordSQL);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("Password");
                String hashedInputPassword = hashPassword(password);
                return hashedInputPassword != null && hashedInputPassword.equals(storedPassword);
            }
            return false; // User not found
        } catch (SQLException e) {
            logger.error("Error verifying password", e);
            return false;
        }
    }

    // Method to hash a password using SHA-256 algorithm
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error hashing password", e);
            return null;
        }
    }

    // Method to close the database connection
    public void closeConnection() {
        try {
            if (conn != null) {
                conn.close();
                System.out.println("Disconnected from the database.");
            }
        } catch (SQLException e) {
            logger.error("Error closing connection", e);
        }
    }


}
