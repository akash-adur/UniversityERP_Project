package edu.univ.erp.service;

import edu.univ.erp.data.DatabaseFactory;

import java.io.*;
import java.sql.*;
import java.util.Date;

public class BackupService {

    private static final String BACKUP_DIR = "backups";
    private static final String[] TABLES = {"enrollments", "sections", "courses", "students", "instructors", "settings"};

    public void createBackup() {
        File dir = new File(BACKUP_DIR);
        if (!dir.exists()) dir.mkdirs();

        try (Connection conn = DatabaseFactory.getErpDS().getConnection()) {
            for (String table : TABLES) {
                exportTable(conn, table);
            }
            System.out.println("Backup completed at " + new Date());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void restoreBackup() throws Exception {
        File dir = new File(BACKUP_DIR);
        if (!dir.exists() || dir.listFiles() == null || dir.listFiles().length == 0) {
            throw new Exception("No backup files found.");
        }

        Connection conn = null;
        try {
            conn = DatabaseFactory.getErpDS().getConnection();
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();

            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            for (String table : TABLES) {
                stmt.execute("TRUNCATE TABLE " + table);
            }

            for (String table : TABLES) {
                importTable(conn, table);
            }

            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");

            conn.commit();
        } catch (Exception e) {
            if (conn != null) conn.rollback();
            throw new Exception("Restore failed: " + e.getMessage());
        } finally {
            if (conn != null) conn.close();
        }
    }

    private void exportTable(Connection conn, String tableName) throws SQLException, IOException {
        String query = "SELECT * FROM " + tableName;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);
             PrintWriter pw = new PrintWriter(new FileWriter(new File(BACKUP_DIR, tableName + ".csv")))) {

            int colCount = rs.getMetaData().getColumnCount();

            while (rs.next()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= colCount; i++) {
                    String val = rs.getString(i);
                    if (val == null) val = "\\N";
                    if (val.contains(",")) val = "\"" + val + "\"";
                    sb.append(val).append(i == colCount ? "" : ",");
                }
                pw.println(sb);
            }
        }
    }

    private void importTable(Connection conn, String tableName) throws IOException, SQLException {
        File file = new File(BACKUP_DIR, tableName + ".csv");
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM " + tableName + " LIMIT 0");
            int colCount = rs.getMetaData().getColumnCount();

            StringBuilder placeholders = new StringBuilder();
            for(int i=0; i<colCount; i++) placeholders.append(i==0 ? "?" : ",?");

            String sql = "INSERT INTO " + tableName + " VALUES (" + placeholders + ")";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",", -1);

                for (int i = 0; i < colCount; i++) {
                    String val = (i < data.length) ? data[i] : "";
                    if (val.startsWith("\"") && val.endsWith("\"")) val = val.substring(1, val.length()-1);

                    if ("\\N".equals(val)) {
                        pstmt.setObject(i + 1, null);
                    } else {
                        pstmt.setString(i + 1, val);
                    }
                }
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }
}