package edu.univ.erp.auth;

import edu.univ.erp.data.DatabaseFactory;
import edu.univ.erp.domain.UserSession;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public UserSession login(String username, String password) throws AuthException {
        String sql = "SELECT user_id, password_hash, role, status, failed_attempts FROM AuthDB.users_auth WHERE username = ?";

        try (Connection conn = DatabaseFactory.getAuthDS().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String storedHash = rs.getString("password_hash");
                    String role = rs.getString("role");
                    String status = rs.getString("status");
                    int attempts = rs.getInt("failed_attempts");

                    if ("locked".equalsIgnoreCase(status)) {
                        logger.warn("Login attempt on locked account: {}", username);
                        throw new AuthException("Account locked due to too many tries.");
                    }

                    if (BCrypt.checkpw(password, storedHash)) {
                        resetFailedAttempts(conn, userId);
                        logger.info("User logged in successfully: {}", username);
                        return new UserSession(userId, username, role);
                    } else {
                        attempts++;
                        updateFailedAttempts(conn, userId, attempts);
                        logger.warn("Failed login attempt for user: {} (Attempts: {})", username, attempts);

                        if (attempts >= 5) {
                            lockAccount(conn, userId);
                            logger.warn("Account locked for user: {}", username);
                            throw new AuthException("Account locked due to too many tries.");
                        }
                        throw new AuthException("Incorrect username or password.");
                    }
                } else {
                    logger.warn("Login failed: User not found - {}", username);
                    throw new AuthException("Incorrect username or password.");
                }
            }
        } catch (SQLException e) {
            logger.error("Database error during login for user: " + username, e);
            throw new AuthException("Database error. Please try again later.");
        }
    }

    public void changePassword(String username, String oldPassword, String newPassword) throws AuthException {
        String sql = "SELECT user_id, password_hash FROM AuthDB.users_auth WHERE username = ?";

        try (Connection conn = DatabaseFactory.getAuthDS().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String storedHash = rs.getString("password_hash");

                    if (!BCrypt.checkpw(oldPassword, storedHash)) {
                        throw new AuthException("Current password is incorrect.");
                    }

                    String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                    updatePasswordInDB(conn, userId, newHash);
                    logger.info("Password changed successfully for user: {}", username);

                } else {
                    throw new AuthException("User not found.");
                }
            }
        } catch (SQLException e) {
            logger.error("Database error during password change for user: " + username, e);
            throw new AuthException("Database error during password change.");
        }
    }

    private void updatePasswordInDB(Connection conn, int userId, String newHash) throws SQLException {
        String sql = "UPDATE AuthDB.users_auth SET password_hash = ?, failed_attempts = 0, status = 'active' WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newHash);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    private void resetFailedAttempts(Connection conn, int userId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE AuthDB.users_auth SET failed_attempts = 0 WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    private void updateFailedAttempts(Connection conn, int userId, int attempts) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE AuthDB.users_auth SET failed_attempts = ? WHERE user_id = ?")) {
            stmt.setInt(1, attempts);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    private void lockAccount(Connection conn, int userId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE AuthDB.users_auth SET status = 'locked' WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    public static class AuthException extends Exception {
        public AuthException(String message) {
            super(message);
        }
    }
}