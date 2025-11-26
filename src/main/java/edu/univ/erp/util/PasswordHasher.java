package edu.univ.erp.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    /**
     * Generates a BCrypt hash for the given password.
     * Can be called by DatabaseSetup or other utilities.
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static void main(String[] args) {
        // You can still keep this for manual testing if you want
        String p = "password123";
        System.out.println("Generated Hash: " + hashPassword(p));
    }
}