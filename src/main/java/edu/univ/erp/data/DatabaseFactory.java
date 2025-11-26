package edu.univ.erp.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public class DatabaseFactory {
    private static HikariDataSource authDataSource;
    private static HikariDataSource erpDataSource;

    // Static block to initialize pools when app starts
    static {
        initAuthDB();
        initERPDB();
    }

    private static void initAuthDB() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/AuthDB");
        config.setUsername("root");
        config.setPassword("prakhar7896"); // REPLACE THIS
        config.setMaximumPoolSize(5);
        authDataSource = new HikariDataSource(config);
    }

    private static void initERPDB() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/ERPDB");
        config.setUsername("root");
        config.setPassword("prakhar7896"); // REPLACE THIS
        config.setMaximumPoolSize(10);
        erpDataSource = new HikariDataSource(config);
    }

    public static DataSource getAuthDS() { return authDataSource; }
    public static DataSource getErpDS() { return erpDataSource; }
}