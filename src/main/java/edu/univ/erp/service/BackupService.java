package edu.univ.erp.service;

import com.opencsv.*;
import edu.univ.erp.data.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.*;
import java.util.Date;

public class BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);
    private static final String BACKUP_DIR = "backups";
    private static final String[] TABLES = {"enrollments", "sections", "courses", "students", "instructors", "settings"};

    public void createBackup() {
        File dir = new File(BACKUP_DIR);
        if (!dir.exists()) dir.mkdirs();

        try (Connection conn = DatabaseFactory.getErpDS().getConnection()) {
            for (String table : TABLES) {
                exportTable(conn, table);
            }
            logger.info("Backup completed at {}", new Date());
        } catch (Exception e) {
            logger.error("Backup creation failed", e);
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
            logger.info("Database restore completed successfully.");
        } catch (Exception e) {
            if (conn != null) conn.rollback();
            logger.error("Restore failed, rolling back", e);
            throw new Exception("Restore failed: " + e.getMessage());
        } finally {
            if (conn != null) conn.close();
        }
    }

    private void exportTable(Connection conn, String tableName) throws SQLException, IOException {
        String query = "SELECT * FROM " + tableName;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);
             CSVWriter writer = new CSVWriter(new FileWriter(new File(BACKUP_DIR, tableName + ".csv")),
                     CSVWriter.DEFAULT_SEPARATOR,
                     CSVWriter.DEFAULT_QUOTE_CHARACTER,
                     ICSVWriter.NO_ESCAPE_CHARACTER,
                     CSVWriter.DEFAULT_LINE_END)) {

            int colCount = rs.getMetaData().getColumnCount();

            while (rs.next()) {
                String[] row = new String[colCount];
                for (int i = 0; i < colCount; i++) {
                    String val = rs.getString(i + 1);
                    row[i] = (val == null) ? "\\N" : val;
                }
                writer.writeNext(row);
            }
        }
    }

    private void importTable(Connection conn, String tableName) throws IOException, SQLException {
        File file = new File(BACKUP_DIR, tableName + ".csv");
        if (!file.exists()) return;

        CSVParser parser = new CSVParserBuilder()
                .withSeparator(',')
                .withQuoteChar('"')
                .withEscapeChar(ICSVWriter.NO_ESCAPE_CHARACTER)
                .build();

        try (CSVReader reader = new CSVReaderBuilder(new FileReader(file))
                .withCSVParser(parser)
                .build()) {

            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM " + tableName + " LIMIT 0");
            int colCount = rs.getMetaData().getColumnCount();

            StringBuilder placeholders = new StringBuilder();
            for(int i=0; i<colCount; i++) placeholders.append(i==0 ? "?" : ",?");

            String sql = "INSERT INTO " + tableName + " VALUES (" + placeholders + ")";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            String[] data;
            while ((data = reader.readNext()) != null) {
                for (int i = 0; i < colCount; i++) {
                    String val = (i < data.length) ? data[i] : "";

                    if ("\\N".equals(val)) {
                        pstmt.setObject(i + 1, null);
                    } else {
                        pstmt.setString(i + 1, val);
                    }
                }
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (Exception e) {
            throw new IOException("CSV Parse Error: " + e.getMessage());
        }
    }
}