package edu.univ.erp.service; // Fixed package to match directory structure if needed, or keep edu.univ.erp.auth

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.data.DatabaseFactory; // Added import
import edu.univ.erp.util.PasswordHasher; // Added import
import org.junit.jupiter.api.BeforeEach; // Added import
import org.junit.jupiter.api.Test;

import java.sql.Connection; // Added import
import java.sql.PreparedStatement; // Added import

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    @BeforeEach
    void resetTestUser() {
        // Force-reset stu1 password to ensure valid credentials for testing
        // This fixes failure if the database has a changed password
        try (Connection conn = DatabaseFactory.getAuthDS().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE users_auth SET password_hash = ?, status = 'active', failed_attempts = 0 WHERE username = ?")) {
            stmt.setString(1, PasswordHasher.hashPassword("password123"));
            stmt.setString(2, "stu1");
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testLogin_Success_ValidCredentials() {
        AuthService auth = new AuthService();
        // Uses the seed data: stu1 / password123
        assertDoesNotThrow(() -> {
            auth.login("stu1", "password123");
        });
    }

    @Test
    void testLogin_Fail_InvalidPassword() {
        AuthService auth = new AuthService();

        Exception exception = assertThrows(AuthService.AuthException.class, () -> {
            auth.login("stu1", "WRONG_PASSWORD");
        });

        assertEquals("Incorrect username or password.", exception.getMessage());
    }
}