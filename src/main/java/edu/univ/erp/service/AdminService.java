package edu.univ.erp.service;

import edu.univ.erp.data.DatabaseFactory;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AdminService {

    public String getSetting(String settingKey) throws SQLException {
        String sql = "SELECT setting_value FROM ERPDB.settings WHERE setting_key = ?";
        try (Connection conn = DatabaseFactory.getErpDS().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, settingKey);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("setting_value");
            }
        }
        return null;
    }

    public void setSetting(String settingKey, String settingValue) throws SQLException {
        String sql = "UPDATE ERPDB.settings SET setting_value = ? WHERE setting_key = ?";
        try (Connection conn = DatabaseFactory.getErpDS().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, settingValue);
            stmt.setString(2, settingKey);
            stmt.executeUpdate();
        }
    }

    public void createUser(String username, String password, String role, String department) throws SQLException {
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        Connection authConn = null;
        Connection erpConn = null;
        try {
            authConn = DatabaseFactory.getAuthDS().getConnection();
            erpConn = DatabaseFactory.getErpDS().getConnection();

            String authSql = "INSERT INTO AuthDB.users_auth (username, role, password_hash) VALUES (?, ?, ?)";
            int newUserId;
            try (PreparedStatement authStmt = authConn.prepareStatement(authSql, Statement.RETURN_GENERATED_KEYS)) {
                authStmt.setString(1, username);
                authStmt.setString(2, role);
                authStmt.setString(3, passwordHash);
                authStmt.executeUpdate();
                try (ResultSet rs = authStmt.getGeneratedKeys()) {
                    if (rs.next()) newUserId = rs.getInt(1);
                    else throw new SQLException("Creating user failed, no ID obtained.");
                }
            }

            String erpSql;
            if ("Student".equalsIgnoreCase(role)) {
                erpSql = "INSERT INTO ERPDB.students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";
            } else if ("Instructor".equalsIgnoreCase(role)) {
                erpSql = "INSERT INTO ERPDB.instructors (user_id, department, title) VALUES (?, ?, ?)";
            } else {
                return;
            }
            try (PreparedStatement erpStmt = erpConn.prepareStatement(erpSql)) {
                erpStmt.setInt(1, newUserId);
                if ("Student".equalsIgnoreCase(role)) {
                    erpStmt.setString(2, "S" + newUserId);
                    erpStmt.setString(3, "B.Tech");
                    erpStmt.setInt(4, 1);
                } else {
                    String deptToSave = (department != null && !department.isEmpty()) ? department : "General";
                    erpStmt.setString(2, deptToSave);
                    erpStmt.setString(3, "Professor");
                }
                erpStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            if (authConn != null) authConn.close();
            if (erpConn != null) erpConn.close();
        }
    }

    public void createCourse(String code, String title, int credits) throws SQLException {
        String sql = "INSERT INTO ERPDB.courses (code, title, credits) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseFactory.getErpDS().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            stmt.setString(2, title);
            stmt.setInt(3, credits);
            stmt.executeUpdate();
        }
    }

    public void createSection(int courseId, String dayTime, String room, int capacity, String semester, int year) throws SQLException {
        String sql = "INSERT INTO ERPDB.sections (course_id, day_time, room, capacity, semester, year, instructor_id) VALUES (?, ?, ?, ?, ?, ?, NULL)";
        try (Connection conn = DatabaseFactory.getErpDS().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            stmt.setString(2, dayTime);
            stmt.setString(3, room);
            stmt.setInt(4, capacity);
            stmt.setString(5, semester);
            stmt.setInt(6, year);
            stmt.executeUpdate();
        }
    }

    public List<Instructor> getAllInstructors() throws SQLException {
        List<Instructor> instructors = new ArrayList<>();
        String sql = "SELECT i.user_id, i.department, i.title, u.username " +
                "FROM ERPDB.instructors i " +
                "JOIN AuthDB.users_auth u ON i.user_id = u.user_id";
        try (Connection conn = DatabaseFactory.getErpDS().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                instructors.add(new Instructor(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("department"),
                        rs.getString("title")
                ));
            }
        }
        return instructors;
    }

    public List<Section> getAllSections() throws SQLException {
        List<Section> sections = new ArrayList<>();
        // Updated to select semester and year
        String sql = "SELECT s.section_id, s.course_id, s.day_time, s.room, s.capacity, s.semester, s.year, " +
                "c.code as courseCode, " +
                "COALESCE(u.username, 'TBD') as instructorName " +
                "FROM ERPDB.sections s " +
                "JOIN ERPDB.courses c ON s.course_id = c.course_id " +
                "LEFT JOIN ERPDB.instructors i ON s.instructor_id = i.user_id " +
                "LEFT JOIN AuthDB.users_auth u ON i.user_id = u.user_id " +
                "ORDER BY s.year DESC, s.semester, s.section_id";

        try (Connection conn = DatabaseFactory.getErpDS().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Section s = new Section(
                        rs.getInt("section_id"),
                        rs.getInt("course_id"),
                        rs.getString("day_time"),
                        rs.getString("room"),
                        rs.getInt("capacity"),
                        rs.getString("semester"),
                        rs.getInt("year")
                );
                s.setCourseCode(rs.getString("courseCode"));
                s.setInstructorName(rs.getString("instructorName"));
                sections.add(s);
            }
        }
        return sections;
    }

    public void assignInstructor(int sectionId, int instructorId) throws SQLException {
        String sql = "UPDATE ERPDB.sections SET instructor_id = ? WHERE section_id = ?";
        try (Connection conn = DatabaseFactory.getErpDS().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, instructorId);
            stmt.setInt(2, sectionId);
            stmt.executeUpdate();
        }
    }
}