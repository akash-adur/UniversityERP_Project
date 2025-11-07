package edu.univ.erp;
import com.formdev.flatlaf.FlatLightLaf;
import org.mindrot.jbcrypt.BCrypt;

public class Main {
    public static void main(String[] args) {
        // Test 1: Can we use the FlatLaf library?
        System.out.println("Setting up Look and Feel...");
        FlatLightLaf.setup();
        System.out.println("FlatLaf is working!");

        // Test 2: Can we use the BCrypt library?
        String hash = BCrypt.hashpw("testpassword", BCrypt.gensalt());
        System.out.println("Generated password hash: " + hash);
    }
}