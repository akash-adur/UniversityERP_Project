package edu.univ.erp.service;

import edu.univ.erp.data.DatabaseFactory;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StudentServiceTest {

    private StudentService studentService;
    private AdminService adminService;

    // IDs for temporary test data (using 999 to avoid seed data 1-4)
    private final int TEST_USER_ID = 999;
    private final int TEST_COURSE_ID = 999;
    private final int TEST_SECTION_ID = 999;

    @BeforeAll
    void setupSuite() {
        studentService = new StudentService();
        adminService = new AdminService();
    }

    @BeforeEach
    void setupTestData() throws Exception {
        // 1. Reset deadlines to "normal"
        adminService.setSetting("add_deadline", LocalDate.now().plusDays(30).toString());

        // 2. Create Fresh Test Data (User, Student, Course, Section)
        try (Connection conn = DatabaseFactory.getErpDS().getConnection()) {
            // Create User in AuthDB (Required for FK)
            try (PreparedStatement u = conn.prepareStatement(
                    "INSERT IGNORE INTO AuthDB.users_auth (user_id, username, role, password_hash) VALUES (?, 'test_student', 'Student', 'dummy_hash')")) {
                u.setInt(1, TEST_USER_ID);
                u.executeUpdate();
            }

            // Create Student Profile
            try (PreparedStatement s = conn.prepareStatement(
                    "INSERT IGNORE INTO ERPDB.students (user_id, roll_no, program, year) VALUES (?, 'TEST999', 'Test Prog', 1)")) {
                s.setInt(1, TEST_USER_ID);
                s.executeUpdate();
            }

            // Create Test Course
            try (PreparedStatement c = conn.prepareStatement(
                    "INSERT IGNORE INTO ERPDB.courses (course_id, code, title, credits) VALUES (?, 'TEST101', 'Test Course', 3)")) {
                c.setInt(1, TEST_COURSE_ID);
                c.executeUpdate();
            }

            // Create Test Section
            try (PreparedStatement sec = conn.prepareStatement(
                    "INSERT IGNORE INTO ERPDB.sections (section_id, course_id, day_time, room, capacity, semester, year) VALUES (?, ?, 'Mon 9:00', 'TestRoom', 50, 'Monsoon', 2025)")) {
                sec.setInt(1, TEST_SECTION_ID);
                sec.setInt(2, TEST_COURSE_ID);
                sec.executeUpdate();
            }

            // 3. Ensure no prior enrollments exist for this test user
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM enrollments WHERE student_id = ?")) {
                del.setInt(1, TEST_USER_ID);
                del.executeUpdate();
            }
        }
    }

    @AfterEach
    void cleanupTestData() throws Exception {
        // Delete all data created for the test to leave the DB clean
        try (Connection conn = DatabaseFactory.getErpDS().getConnection()) {
            // Delete Enrollment
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM enrollments WHERE student_id = ?")) {
                del.setInt(1, TEST_USER_ID);
                del.executeUpdate();
            }
            // Delete Section
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM sections WHERE section_id = ?")) {
                del.setInt(1, TEST_SECTION_ID);
                del.executeUpdate();
            }
            // Delete Course
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM courses WHERE course_id = ?")) {
                del.setInt(1, TEST_COURSE_ID);
                del.executeUpdate();
            }
            // Delete Student
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM students WHERE user_id = ?")) {
                del.setInt(1, TEST_USER_ID);
                del.executeUpdate();
            }
            // Delete User Auth
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM AuthDB.users_auth WHERE user_id = ?")) {
                del.setInt(1, TEST_USER_ID);
                del.executeUpdate();
            }
        }
    }

    @Test
    void testRegister_Success_WhenDeadlineIsFuture() {
        // 1. Action: Try to register using the temp user
        assertDoesNotThrow(() -> studentService.registerForSection(TEST_USER_ID, TEST_SECTION_ID),
                "Should register successfully when deadline is in the future");
    }

    @Test
    void testRegister_Fail_WhenDeadlinePassed() throws Exception {
        // 1. Setup: Set deadline to Yesterday
        String yesterday = LocalDate.now().minusDays(1).toString();
        adminService.setSetting("add_deadline", yesterday);

        // 2. Action & Assert: Expect an Exception
        Exception exception = assertThrows(Exception.class, () -> {
            studentService.registerForSection(TEST_USER_ID, TEST_SECTION_ID);
        });

        // 3. Verify Message
        assertTrue(exception.getMessage().contains("deadline"),
                "Exception message should mention the deadline");
    }

    @Test
    void testRegister_Fail_DuplicateEnrollment() throws Exception {
        // 1. Setup: Register once successfully
        studentService.registerForSection(TEST_USER_ID, TEST_SECTION_ID);

        // 2. Action: Try to register AGAIN for the same section
        Exception exception = assertThrows(Exception.class, () -> {
            studentService.registerForSection(TEST_USER_ID, TEST_SECTION_ID);
        });

        // 3. Verify: Should fail with duplicate error
        // Note: The service checks for "Course" duplication first (same course in same term)
        assertTrue(exception.getMessage().contains("already registered for this course"),
                "Should fail with duplicate course registration error.");
    }
}