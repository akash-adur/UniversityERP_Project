package edu.univ.erp;

import edu.univ.erp.data.DatabaseFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestConnection {
    public static void main(String[] args) {
        System.out.println("Attempting to connect to databases...");

        // Test 1: ERP Database
        try (Connection conn = DatabaseFactory.getErpDS().getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT setting_value FROM settings WHERE setting_key = 'maintenance_mode'");
            if (rs.next()) {
                System.out.println("✅ SUCCESS: Connected to ERP DB! Maintenance Mode is: " + rs.getString(1));
            }

        } catch (SQLException e) {
            System.err.println("❌ FAILED: Could not connect to ERP DB.");
            e.printStackTrace();
        }

        // Test 2: Auth Database
        try (Connection conn = DatabaseFactory.getAuthDS().getConnection();
             Statement stmt = conn.createStatement()) {

            // Just a simple ping query since this DB might be empty right now
            stmt.execute("SELECT 1");
            System.out.println("✅ SUCCESS: Connected to Auth DB!");

        } catch (SQLException e) {
            System.err.println("❌ FAILED: Could not connect to Auth DB.");
            e.printStackTrace();
        }
    }
}