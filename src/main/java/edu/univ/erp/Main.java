package edu.univ.erp;

import com.formdev.flatlaf.FlatLightLaf;
import edu.univ.erp.ui.LoginFrame; // You will create this next

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set the modern Look and Feel (FlatLaf)
        // This should be the very first thing you do
        FlatLightLaf.setup();

        // Run the UI creation on the AWT Event Dispatch Thread
        // This is the standard, safe way to start a Swing application
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}