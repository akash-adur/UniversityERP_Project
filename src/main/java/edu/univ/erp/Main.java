package edu.univ.erp;

import com.formdev.flatlaf.FlatLightLaf;
import edu.univ.erp.ui.LoginFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set the modern Look and Feel (FlatLaf)
        FlatLightLaf.setup();

        // Run the UI on the AWT Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}