package edu.univ.erp.service;

import edu.univ.erp.data.DatabaseFactory;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.PreparedStatement; // Added import
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.*;

class AdminServiceTest {

    private AdminService adminService;
    private final int COURSE_ID = 1; // CS101
    private final String SEC_NAME = "Z"; // Unique test name

    @BeforeEach
    void setUp() {
        adminService = new AdminService();
        // Ensure the parent Course exists so FK constraints don't fail
        try (Connection conn = DatabaseFactory.getErpDS().getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT IGNORE INTO courses (course_id, code, title, credits) VALUES (1, 'CS101', 'Intro to Programming', 4)")) {
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Ensure clean state before start
        cleanupSection();
    }

    @AfterEach
    void tearDown() {
        // Ensure cleanup happens even if test fails
        cleanupSection();
    }

    @Test
    void testCreateSection_Fail_IfNameDuplicate() {
        String term = "Monsoon";
        int year = 2025;

        // 1. Action: Create "Section Z" for the first time -> Should Pass
        assertDoesNotThrow(() ->
                adminService.createSection(COURSE_ID, "Mon 10:00", "Room 1", 50, term, year, SEC_NAME)
        );

        // 2. Action: Create "Section Z" AGAIN for same course/term -> Should Fail
        Exception exception = assertThrows(Exception.class, () -> {
            adminService.createSection(COURSE_ID, "Tue 10:00", "Room 2", 50, term, year, SEC_NAME);
        });

        // 3. Verify Error Message
        assertTrue(exception.getMessage().contains("already exists"),
                "Should throw error about duplicate section name");
    }

    private void cleanupSection() {
        try (Connection conn = DatabaseFactory.getErpDS().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM sections WHERE course_id=" + COURSE_ID + " AND section_name='" + SEC_NAME + "'");
        } catch (Exception ignored) {}
    }
}