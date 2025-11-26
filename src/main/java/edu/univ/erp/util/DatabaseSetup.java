package edu.univ.erp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseSetup {

    // !!! CONFIGURE THESE !!!
    private static final String DB_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "prakhar7896";

    public static void main(String[] args) {
        System.out.println("... Starting Database Setup ...");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            System.out.println("✅ Connected to MySQL Server.");

            // 1. SETUP AuthDB
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS AuthDB");
            stmt.executeUpdate("USE AuthDB");

            // UPDATED: Added failed_attempts column
            String createAuthTable = "CREATE TABLE IF NOT EXISTS users_auth (" +
                    "user_id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "username VARCHAR(50) NOT NULL UNIQUE, " +
                    "role VARCHAR(20) NOT NULL, " +
                    "password_hash VARCHAR(100) NOT NULL, " +
                    "status VARCHAR(20) DEFAULT 'active', " +
                    "failed_attempts INT DEFAULT 0, " +
                    "last_login TIMESTAMP NULL" +
                    ")";
            stmt.executeUpdate(createAuthTable);

            String defaultHash = "$2a$10$rRiAi4DLdyb9.9wpMWaMze/NLsoZeNtJ5KPI.WajjuObbOxKV/KOW";

            // Added failed_attempts to INSERT (defaults to 0 anyway, but good for clarity if needed)
            String insertUsers = "INSERT IGNORE INTO users_auth (user_id, username, role, password_hash, status, failed_attempts) VALUES " +
                    "(1, 'admin1', 'Admin',      '" + defaultHash + "', 'active', 0), " +
                    "(2, 'inst1',  'Instructor', '" + defaultHash + "', 'active', 0), " +
                    "(3, 'stu1',   'Student',    '" + defaultHash + "', 'active', 0), " +
                    "(4, 'stu2',   'Student',    '" + defaultHash + "', 'active', 0)";
            stmt.executeUpdate(insertUsers);

            // 2. SETUP ERPDB
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS ERPDB");
            stmt.executeUpdate("USE ERPDB");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS students (" +
                    "user_id INT PRIMARY KEY, " +
                    "roll_no VARCHAR(20) NOT NULL UNIQUE, " +
                    "program VARCHAR(50), " +
                    "year INT, " +
                    "FOREIGN KEY (user_id) REFERENCES AuthDB.users_auth(user_id))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS instructors (" +
                    "user_id INT PRIMARY KEY, " +
                    "department VARCHAR(50), " +
                    "title VARCHAR(50), " +
                    "FOREIGN KEY (user_id) REFERENCES AuthDB.users_auth(user_id))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS settings (" +
                    "setting_key VARCHAR(50) PRIMARY KEY, " +
                    "setting_value VARCHAR(100))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS courses (" +
                    "course_id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "code VARCHAR(20) NOT NULL UNIQUE, " +
                    "title VARCHAR(100) NOT NULL, " +
                    "credits INT NOT NULL)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS sections (" +
                    "section_id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "course_id INT NOT NULL, " +
                    "instructor_id INT, " +
                    "day_time VARCHAR(50), " +
                    "room VARCHAR(20), " +
                    "capacity INT NOT NULL, " +
                    "semester VARCHAR(20) NOT NULL, " +
                    "year INT NOT NULL, " +
                    "section_name VARCHAR(10) DEFAULT 'N/A', " +
                    "FOREIGN KEY (course_id) REFERENCES courses(course_id), " +
                    "FOREIGN KEY (instructor_id) REFERENCES instructors(user_id))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS enrollments (" +
                    "enrollment_id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "student_id INT NOT NULL, " +
                    "section_id INT NOT NULL, " +
                    "status VARCHAR(20) DEFAULT 'enrolled', " +
                    "score_quiz DECIMAL(5,2) DEFAULT 0.00, " +
                    "score_midterm DECIMAL(5,2) DEFAULT 0.00, " +
                    "score_final DECIMAL(5,2) DEFAULT 0.00, " +
                    "final_grade VARCHAR(5) DEFAULT 'N/A', " +
                    "UNIQUE(student_id, section_id), " +
                    "FOREIGN KEY (student_id) REFERENCES students(user_id), " +
                    "FOREIGN KEY (section_id) REFERENCES sections(section_id))");

            // --- SEED DATA ---
            stmt.executeUpdate("INSERT IGNORE INTO instructors (user_id, department, title) VALUES (2, 'Computer Science', 'Professor')");
            stmt.executeUpdate("INSERT IGNORE INTO students (user_id, roll_no, program, year) VALUES (3, 'S101', 'B.Tech CS', 2), (4, 'S102', 'B.Tech ECE', 2)");

            stmt.executeUpdate("INSERT IGNORE INTO settings (setting_key, setting_value) VALUES ('maintenance_mode', 'false')");
            stmt.executeUpdate("INSERT IGNORE INTO settings (setting_key, setting_value) VALUES ('drop_deadline', '2025-11-30')");
            stmt.executeUpdate("INSERT IGNORE INTO settings (setting_key, setting_value) VALUES ('add_deadline', '2025-08-30')");

            stmt.executeUpdate("INSERT IGNORE INTO courses (code, title, credits) VALUES ('CS101', 'Intro to Programming', 4)");
            stmt.executeUpdate("INSERT IGNORE INTO courses (code, title, credits) VALUES ('MATH201', 'Calculus II', 4)");

            stmt.executeUpdate("INSERT IGNORE INTO sections (section_id, course_id, instructor_id, day_time, room, capacity, semester, year) " +
                    "VALUES (1, 1, 2, 'Mon/Wed 10:00', 'Room 101', 60, 'Monsoon', 2025)");

            stmt.executeUpdate("INSERT IGNORE INTO sections (section_id, course_id, instructor_id, day_time, room, capacity, semester, year) " +
                    "VALUES (2, 2, NULL, 'Tue/Thu 09:00', 'Room 202', 40, 'Monsoon', 2025)");

            System.out.println("✅ Database Setup Complete!");

        } catch (SQLException e) {
            System.err.println("❌ Database Setup Failed!");
            e.printStackTrace();
        }
    }
}