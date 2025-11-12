package edu.univ.erp.auth;

import edu.univ.erp.data.DatabaseFactory;
import edu.univ.erp.domain.UserSession;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles the business logic for authentication.
 */
public class AuthService {

    /**
     * Attempts to log in a user with their username and password.
     *
     * @param username The user's provided username.
     * @param password The user's provided (plaintext) password.
     * @return A UserSession object if login is successful.
     * @throws AuthException if login fails (e.g., user not found, wrong password).
     */
    public UserSession login(String username, String password) throws AuthException {
        // SQL to find the user's hash, role, and ID from the AuthDB
        String sql = "SELECT user_id, password_hash, role FROM AuthDB.users_auth WHERE username = ?";

        // Use try-with-resources to auto-close the connection and statement
        try (Connection conn = DatabaseFactory.getAuthDS().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // User found, get their data
                    String storedHash = rs.getString("password_hash");

                    // Use jBCrypt to check the plain password against the stored hash
                    if (BCrypt.checkpw(password, storedHash)) {
                        // Password is correct!
                        // Create and return a new session for the user.
                        return new UserSession(
                                rs.getInt("user_id"),
                                username,
                                rs.getString("role")
                        );
                    } else {
                        // User found, but password was wrong
                        throw new AuthException("Incorrect username or password.");
                    }
                } else {
                    // No user with that username was found
                    throw new AuthException("Incorrect username or password.");
                }
            }
        } catch (SQLException e) {
            // A database error occurred
            e.printStackTrace();
            throw new AuthException("Database error. Please try again later.");
        }
    }

    /**
     * A custom exception for handling login failures.
     */
    public static class AuthException extends Exception {
        public AuthException(String message) {
            super(message);
        }
    }
}