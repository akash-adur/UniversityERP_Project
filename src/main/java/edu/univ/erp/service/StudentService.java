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

    private final AdminService adminService;

    public StudentService() {
        this.adminService = new AdminService();
    }

    private void checkMaintenanceMode() throws Exception {
        String mode = adminService.getSetting("maintenance_mode");
        if ("true".equalsIgnoreCase(mode)) {
            throw new Exception("⚠️ System is under maintenance. Actions are currently disabled.");
        }
    }

    public List<Section> getSectionsForCourse(int courseId) throws SQLException {
        List<Section> sections = new ArrayList<>();
        // FIX: Added s.section_name to query
        String sql = "SELECT s.section_id, s.day_time, s.room, s.capacity, s.semester, s.year, s.section_name, " +
                "COALESCE(u.username, 'TBD') as instructorName " +
                "FROM ERPDB.sections s " +
                "LEFT JOIN ERPDB.instructors i ON s.instructor_id = i.user_id " +
                "LEFT JOIN AuthDB.users_auth u ON i.user_id = u.user_id " +
                "WHERE s.course_id = ? " +
                "ORDER BY s.year DESC, s.semester";

        try (Connection conn = DatabaseFactory.getErpDS().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // FIX: Passed section_name (8th argument)
                    Section section = new Section(
                            rs.getInt("section_id"),
                            courseId,
                            rs.getString("day_time"),
                            rs.getString("room"),
                            rs.getInt("capacity"),
                            rs.getString("semester"),
                            rs.getInt("year"),
                            rs.getString("section_name") // <--- NEW ARGUMENT
                    );
                    section.setInstructorName(rs.getString("instructorName"));
                    sections.add(section);
                }
            }
        }
        return sections;
    }

    // --- DEADLINE HELPERS ---
    public LocalDate getDropDeadline() throws Exception {
        return getDeadline("drop_deadline");
    }

    public LocalDate getAddDeadline() throws Exception {
        return getDeadline("add_deadline");
    }

    private LocalDate getDeadline(String key) throws Exception {
        try {
            String deadlineString = adminService.getSetting(key);
            if (deadlineString == null) {
                return LocalDate.now().plusDays(30);
            }
            return LocalDate.parse(deadlineString);
        } catch (DateTimeParseException e) {
            throw new Exception("Invalid deadline format in database for " + key);
        }
    }

    public void registerForSection(int studentId, int sectionId) throws Exception {
        checkMaintenanceMode();

        LocalDate deadline = getAddDeadline();
        if (LocalDate.now().isAfter(deadline)) {
            throw new Exception("Registration failed: The Add/Register deadline (" + deadline + ") has passed.");
        }

        Connection conn = null;
        try {
            conn = DatabaseFactory.getErpDS().getConnection();
            conn.setAutoCommit(false);

            int courseId = -1;
            String semester = "";
            int year = 0;

            String getSectionInfoSql = "SELECT course_id, semester, year FROM ERPDB.sections WHERE section_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(getSectionInfoSql)) {
                stmt.setInt(1, sectionId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        courseId = rs.getInt("course_id");
                        semester = rs.getString("semester");
                        year = rs.getInt("year");
                    } else {
                        throw new Exception("Section not found.");
                    }
                }
            }

            String dupCheckSql = "SELECT count(*) FROM ERPDB.enrollments e " +
                    "JOIN ERPDB.sections s ON e.section_id = s.section_id " +
                    "WHERE e.student_id = ? AND s.course_id = ? AND s.semester = ? AND s.year = ?";

            try (PreparedStatement stmt = conn.prepareStatement(dupCheckSql)) {
                stmt.setInt(1, studentId);
                stmt.setInt(2, courseId);
                stmt.setString(3, semester);
                stmt.setInt(4, year);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new Exception("You are already registered for this course in the " + semester + " " + year + " term.");
                    }
                }
            }

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
                    }
                }
            }
            if (enrolled >= capacity) {
                throw new Exception("Section is full.");
            }

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
                throw new Exception("You are already enrolled in this exact section.");
            }
            throw new Exception("Database error: " + e.getMessage());
        } finally {
            if (conn != null) conn.close();
        }
    }

    public List<EnrollmentDetails> getEnrollmentsForStudent(int studentId) throws SQLException {
        List<EnrollmentDetails> details = new ArrayList<>();
        // FIX: Added s.section_name to query
        String sql = "SELECT e.enrollment_id, e.status, e.final_grade, " +
                "e.score_quiz, e.score_midterm, e.score_final, " +
                "s.section_id, s.day_time, s.room, s.semester, s.year, s.section_name, " + // <--- Added here
                "c.code, c.title, c.credits, " +
                "COALESCE(u.username, 'TBD') as instructorName " +
                "FROM ERPDB.enrollments e " +
                "JOIN ERPDB.sections s ON e.section_id = s.section_id " +
                "JOIN ERPDB.courses c ON s.course_id = c.course_id " +
                "LEFT JOIN ERPDB.instructors i ON s.instructor_id = i.user_id " +
                "LEFT JOIN AuthDB.users_auth u ON i.user_id = u.user_id " +
                "WHERE e.student_id = ?";

        try (Connection conn = DatabaseFactory.getErpDS().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
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
                            grade,
                            rs.getInt("credits"),
                            rs.getString("semester"),
                            rs.getInt("year"),
                            rs.getDouble("score_quiz"),
                            rs.getDouble("score_midterm"),
                            rs.getDouble("score_final"),
                            rs.getString("section_name") // <--- Pass new argument
                    ));
                }
            }
        }
        return details;
    }

    public void dropSection(int enrollmentId) throws Exception {
        checkMaintenanceMode();
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