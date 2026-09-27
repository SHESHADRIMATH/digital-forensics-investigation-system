package com.digitalforensics.util;

import com.digitalforensics.exception.DataAccessException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gives every DAO a JDBC connection to MySQL.
 * Settings are read once from db.properties (WEB-INF/classes) so the
 * password is not hard-coded in Java source.
 */
public final class DBConnection {

    private static final Properties CONFIG = new Properties();

    // static block runs once, when the class is first used
    static {
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("db.properties not found on the classpath");
            }
            CONFIG.load(in);
            Class.forName(CONFIG.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Could not initialise database settings", e);
        }
    }

    private DBConnection() {
    }

    /** Callers must close the connection - always use try-with-resources. */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(
                    CONFIG.getProperty("db.url"),
                    CONFIG.getProperty("db.user"),
                    CONFIG.getProperty("db.password"));
        } catch (SQLException e) {
            throw new DataAccessException("Cannot connect to MySQL. Is the MySQL service running?", e);
        }
    }

    public static String getSetting(String key, String defaultValue) {
        return CONFIG.getProperty(key, defaultValue);
    }
}
