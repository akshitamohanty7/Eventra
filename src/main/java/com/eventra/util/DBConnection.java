package com.eventra.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton database connection manager for Eventra.
 * Loads database configuration from classpath 'db.properties'
 * and provides JDBC Connection instances to DAOs.
 */
public class DBConnection {

    private static volatile DBConnection instance;

    private String driver;
    private String url;
    private String username;
    private String password;

    private DBConnection() {
        loadProperties();
        loadDriver();
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }

    private void loadProperties() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
                this.driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
                this.url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/eventra_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
                this.username = props.getProperty("db.username", "root");
                this.password = props.getProperty("db.password", "password");
            } else {
                // Fallback to default local configuration if db.properties is absent
                this.driver = "com.mysql.cj.jdbc.Driver";
                this.url = "jdbc:mysql://localhost:3306/eventra_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
                this.username = "root";
                this.password = "password";
            }
        } catch (IOException e) {
            System.err.println("[Eventra-DBConnection] Error reading db.properties: " + e.getMessage());
            this.driver = "com.mysql.cj.jdbc.Driver";
            this.url = "jdbc:mysql://localhost:3306/eventra_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            this.username = "root";
            this.password = "password";
        }
    }

    private void loadDriver() {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found on classpath: " + driver, e);
        }
    }

    /**
     * Obtains a new active database Connection.
     * The caller is responsible for closing the connection (e.g., via try-with-resources).
     *
     * @return active java.sql.Connection
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
