package edu.univ.erp.service;

import edu.univ.erp.data.DatabaseFactory;
import edu.univ.erp.domain.Section;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InstructorService {

    // 1. Get all sections assigned to this instructor
    public List<Section> getSectionsForInstructor(int instructorId) throws SQLException {
        List<Section> sections = new ArrayList<>();
        String sql = "SELECT s.section_id, s.course_id, s.day_time, s.room, s.capacity, " +
                "c.code as courseCode, c.title as courseTitle " +
                "FROM ERPDB.sections s " +
                "JOIN ERPDB.courses c ON s.course_id = c.course_id " +
                "WHERE s.instructor_id = ?";

        try (Connection conn = DatabaseFactory.getErpDS().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, instructorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Section s = new Section(
                            rs.getInt("section_id"),
                            rs.getInt("course_id"),
                            rs.getString("day_time"),
                            rs.getString("room"),
                            rs.getInt("capacity")
                    );
                    // Reuse the Section domain object, setting extra fields
                    s.setCourseCode(rs.getString("courseCode") + " - " + rs.getString("courseTitle"));
                    sections.add(s);
                }
            }
        }
        return sections;
    }

    // 2. Get Gradebook Data (Student + Grades) for a specific section
    // We will use a simple inner class DTO for this to avoid creating a new file
    public List<GradeRecord> getGradebook(int sectionId) throws SQLException {
        List<GradeRecord> records = new ArrayList<>();
        String sql = "SELECT e.enrollment_id, s.roll_no, u.username, " +
                "e.score_quiz, e.score_midterm, e.score_final, e.final_grade " +
                "FROM ERPDB.enrollments e " +
                "JOIN ERPDB.students s ON e.student_id = s.user_id " +
                "JOIN AuthDB.users_auth u ON s.user_id = u.user_id " +
                "WHERE e.section_id = ?";

        try (Connection conn = DatabaseFactory.getErpDS().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(new GradeRecord(
                            rs.getInt("enrollment_id"),
                            rs.getString("roll_no"),
                            rs.getString("username"),
                            rs.getDouble("score_quiz"),
                            rs.getDouble("score_midterm"),
                            rs.getDouble("score_final"),
                            rs.getString("final_grade")
                    ));
                }
            }
        }
        return records;
    }

    // 3. Update Grades
    public void updateGrades(int enrollmentId, double quiz, double midterm, double finalScore, String letterGrade) throws SQLException {
        String sql = "UPDATE ERPDB.enrollments SET score_quiz=?, score_midterm=?, score_final=?, final_grade=? WHERE enrollment_id=?";
        try (Connection conn = DatabaseFactory.getErpDS().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, quiz);
            stmt.setDouble(2, midterm);
            stmt.setDouble(3, finalScore);
            stmt.setString(4, letterGrade);
            stmt.setInt(5, enrollmentId);
            stmt.executeUpdate();
        }
    }

    // Simple DTO for the Table
    public static class GradeRecord {
        public int enrollmentId;
        public String rollNo;
        public String name;
        public double quiz;
        public double midterm;
        public double finals;
        public String letterGrade;

        public GradeRecord(int enrollmentId, String rollNo, String name, double quiz, double midterm, double finals, String letterGrade) {
            this.enrollmentId = enrollmentId;
            this.rollNo = rollNo;
            this.name = name;
            this.quiz = quiz;
            this.midterm = midterm;
            this.finals = finals;
            this.letterGrade = letterGrade;
        }
    }
}