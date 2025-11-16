package edu.univ.erp.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * A simple utility to generate BCrypt password hashes for your seed data.
 * Run this class's main method and copy-paste the output hashes into your SQL.
 */
public class PasswordHasher {
    public static void main(String[] args) {
        String plainPassword = "password123";

        String adminHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        String instHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        String stu1Hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        String stu2Hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

        System.out.println("--- COPY-PASTE THESE HASHES into your SQL seed script ---");
        System.out.println("admin1 ('password123') Hash: " + adminHash);
        System.out.println("inst1 ('password123') Hash:  " + instHash);
        System.out.println("stu1 ('password123') Hash:   " + stu1Hash);
        System.out.println("stu2 ('password123') Hash:   " + stu2Hash);
        System.out.println("-------------------------------------------------------");
    }
}