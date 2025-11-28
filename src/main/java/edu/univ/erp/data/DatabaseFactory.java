package edu.univ.erp.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseFactory {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseFactory.class);
    private static HikariDataSource authDataSource;
    private static HikariDataSource erpDataSource;

    // Static block to initialize pools when app starts
    static {
        Properties props = loadProperties();
        if (props != null) {
            initAuthDB(props);
            initERPDB(props);
        }
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = DatabaseFactory.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                logger.error("Sorry, unable to find application.properties");
                return null;
            }
            props.load(input);
        } catch (IOException ex) {
            logger.error("Error loading configuration properties", ex);
            return null;
        }
        return props;
    }

    private static void initAuthDB(Properties props) {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url.auth"));
            config.setUsername(props.getProperty("db.user"));
            config.setPassword(props.getProperty("db.password"));
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.size.auth", "5")));
            authDataSource = new HikariDataSource(config);
            logger.info("AuthDB Connection Pool initialized successfully.");
        } catch (Exception e) {
            logger.error("Failed to initialize AuthDB pool", e);
        }
    }

    private static void initERPDB(Properties props) {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url.erp"));
            config.setUsername(props.getProperty("db.user"));
            config.setPassword(props.getProperty("db.password"));
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.size.erp", "10")));
            erpDataSource = new HikariDataSource(config);
            logger.info("ERPDB Connection Pool initialized successfully.");
        } catch (Exception e) {
            logger.error("Failed to initialize ERPDB pool", e);
        }
    }

    public static DataSource getAuthDS() { return authDataSource; }
    public static DataSource getErpDS() { return erpDataSource; }
}