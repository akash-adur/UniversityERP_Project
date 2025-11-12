package edu.univ.erp.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * A simple utility to generate BCrypt password hashes for your seed data.
 * Run this class's main method and copy-paste the output hashes into your SQL.
 */
public class PasswordHasher {
    public static void main(String[] args) {
        // We'll use "password123" as the default password for all test accounts.
        String plainPassword = "password123";

        // Generate hashes for all 4 required users.
        // IMPORTANT: Your hashes will be different from anyone else's.
        String adminHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        String instHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        String stu1Hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        String stu2Hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

        System.out.println("--- COPY-PASTE THESE HASHES ---");
        System.out.println("admin1 ('password123') Hash: " + adminHash);
        System.out.println("inst1 ('password123') Hash:  " + instHash);
        System.out.println("stu1 ('password123') Hash:   " + stu1Hash);
        System.out.println("stu2 ('password123') Hash:   " + stu2Hash);
        System.out.println("---------------------------------");
    }
}

/*
-- --- 1. Populate AuthDB ---
-- !! REPLACE THE HASHES BELOW with the ones from your console output !!
USE AuthDB;

INSERT INTO users_auth (user_id, username, role, password_hash, status)
VALUES
(1, 'admin1', 'Admin',      '$2a$10$miw2jD81C3XtOBtSv0Y9nOcQUeFHX86wS7JtMy2VZA25VPEUmguB6',NULL),
(2, 'inst1',  'Instructor', '$2a$10$ZolX0m.30Mw0rGQeYwjwpeumXGSCcILfQcH8zEHilrweWQUoGmf4a',NULL),
(3, 'stu1',   'Student',    '$2a$10$K5hhZP45BI4qKDDLeTPV8O/V6cAgXDMY8ZIJCUD2OdlyrmlVQvEL2',NULL),
(4, 'stu2',   'Student',    '$2a$10$g3WeFCLCAqnx0hacTErI5eTrX3ZsqQ/VPOil4o77TBwCKK8oFmnWK',NULL);


-- --- 2. Populate ERPDB ---
USE ERPDB;

-- Create the profiles. The user_id MUST match the AuthDB
INSERT INTO instructors (user_id, department, title)
VALUES (2, 'Computer Science', 'Professor');

INSERT INTO students (user_id, roll_no, program, year)
VALUES
(3, 'B23101', 'B.Tech CS', 2),
(4, 'B23102', 'B.Tech ECE', 2);

-- Add the default maintenance mode setting (your TestConnection needs this)
INSERT INTO settings (setting_key, setting_value)
VALUES ('maintenance_mode', 'false')
ON DUPLICATE KEY UPDATE setting_value = 'false';
 */