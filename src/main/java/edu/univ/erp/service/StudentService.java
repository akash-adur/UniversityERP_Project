package edu.univ.erp.service;

import edu.univ.erp.data.DatabaseFactory;
import edu.univ.erp.domain.EnrollmentDetails;
import edu.univ.erp.domain.Section;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class StudentService {

    // We need an AdminService to read settings
    private final AdminService adminService;

    public StudentService() {
        this.adminService = new AdminService();
    }

    // --- NEW HELPER METHOD FOR MAINTENANCE MODE ---
    private void checkMaintenanceMode() throws Exception {
        String mode = adminService.getSetting("maintenance_mode");
        if ("true".equalsIgnoreCase(mode)) {
            throw new Exception("⚠️ System is under maintenance. Actions are currently disabled.");
        }
    }

    /**
     * Fetches all sections for a specific course.
     */
    public List<Section> getSectionsForCourse(int courseId) throws SQLException {
        List<Section> sections = new ArrayList<>();
        String sql = "SELECT s.section_id, s.day_time, s.room, s.capacity, " +
                "COALESCE(i.title, 'TBD') as instructorName " +
                "FROM ERPDB.sections s " +
                "LEFT JOIN ERPDB.instructors i ON s.instructor_id = i.user_id " +
                "WHERE s.course_id = ?";
        try (Connection conn = DatabaseFactory.getErpDS().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Section section = new Section(
                            rs.getInt("section_id"),
                            courseId,
                            rs.getString("day_time"),
                            rs.getString("room"),
                            rs.getInt("capacity")
                    );
                    section.setInstructorName(rs.getString("instructorName"));
                    sections.add(section);
                }
            }
        }
        return sections;
    }

    /**
     * Attempts to register a student for a section.
     */
    public void registerForSection(int studentId, int sectionId) throws Exception {
        // --- MAINTENANCE CHECK ---
        checkMaintenanceMode();

        Connection conn = null;
        try {
            conn = DatabaseFactory.getErpDS().getConnection();
            conn.setAutoCommit(false);

            // --- Check 1: Is the section full? ---
            String capacitySql = "SELECT capacity, (SELECT COUNT(*) FROM enrollments WHERE section_id = ?) as enrolled FROM sections WHERE section_id = ?";
            int capacity = 0;
            int enrolled = 0;
            try (PreparedStatement stmt = conn.prepareStatement(capacitySql)) {
                stmt.setInt(1, sectionId);
                stmt.setInt(2, sectionId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        capacity = rs.getInt("capacity");
                        enrolled = rs.getInt("enrolled");
                    } else {
                        throw new Exception("Section not found.");
                    }
                }
            }
            if (enrolled >= capacity) {
                throw new Exception("Section is full.");
            }

            // --- Step 3: Insert into enrollments ---
            String insertSql = "INSERT INTO ERPDB.enrollments (student_id, section_id, status) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setInt(1, studentId);
                stmt.setInt(2, sectionId);
                stmt.setString(3, "enrolled");
                stmt.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            if (e.getSQLState().equals("23000")) {
                throw new Exception("You are already enrolled in this section.");
            }
            throw new Exception("Database error: " + e.getMessage());
        } finally {
            if (conn != null) conn.close();
        }
    }

    /**
     * Fetches all enrollments for a specific student.
     */
    /**
     * Fetches all enrollments for a specific student (UPDATED TO FETCH GRADES).
     */
    public List<EnrollmentDetails> getEnrollmentsForStudent(int studentId) throws SQLException {
        List<EnrollmentDetails> details = new ArrayList<>();
        // Added 'e.final_grade' to the SELECT list
        String sql = "SELECT e.enrollment_id, e.status, e.final_grade, " +
                "s.section_id, s.day_time, s.room, " +
                "c.code, c.title, " +
                "COALESCE(i.title, 'TBD') as instructorName " +
                "FROM ERPDB.enrollments e " +
                "JOIN ERPDB.sections s ON e.section_id = s.section_id " +
                "JOIN ERPDB.courses c ON s.course_id = c.course_id " +
                "LEFT JOIN ERPDB.instructors i ON s.instructor_id = i.user_id " +
                "WHERE e.student_id = ?";

        try (Connection conn = DatabaseFactory.getErpDS().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Handle null grades gracefully
                    String grade = rs.getString("final_grade");
                    if (grade == null) grade = "N/A";

                    details.add(new EnrollmentDetails(
                            rs.getInt("enrollment_id"),
                            rs.getInt("section_id"),
                            rs.getString("code"),
                            rs.getString("title"),
                            rs.getString("day_time"),
                            rs.getString("room"),
                            rs.getString("instructorName"),
                            rs.getString("status"),
                            grade // <--- PASS THE REAL GRADE HERE
                    ));
                }
            }
        }
        return details;
    }

    /**
     * Gets the drop deadline as a LocalDate object.
     */
    public LocalDate getDropDeadline() throws Exception {
        try {
            String deadlineString = adminService.getSetting("drop_deadline");
            if (deadlineString == null) {
                // Fallback: If not set, make deadline "yesterday" so drops are blocked
                return LocalDate.now().minusDays(1);
            }
            return LocalDate.parse(deadlineString); // Assumes "YYYY-MM-DD"
        } catch (DateTimeParseException e) {
            throw new Exception("Invalid deadline format in database.");
        }
    }

    /**
     * Drops a student from a section.
     */
    public void dropSection(int enrollmentId) throws Exception {
        // --- MAINTENANCE CHECK ---
        checkMaintenanceMode();

        // --- DEADLINE CHECK ---
        LocalDate deadline = getDropDeadline();
        if (LocalDate.now().isAfter(deadline)) {
            throw new Exception("Drop failed: The drop deadline (" + deadline + ") has passed.");
        }

        String sql = "DELETE FROM ERPDB.enrollments WHERE enrollment_id = ?";
        try (Connection conn = DatabaseFactory.getErpDS().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new Exception("Enrollment not found or already dropped.");
            }
        } catch (SQLException e) {
            throw new Exception("Database error: " + e.getMessage());
        }
    }
}