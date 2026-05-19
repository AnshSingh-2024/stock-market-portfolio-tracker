package com.tracker.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class for managing MySQL database connections using JDBC.
 * Ensures only one connection instance exists throughout the application
 * lifecycle.
 */
public class DBConnection {

    // Database configuration constants — update these for your environment
    private static final String URL = "jdbc:mysql://localhost:3306/stock_tracker";
    private static final String USER = "root";
    private static final String PASSWORD = "your_password_here"; // Change to your MySQL password

    // Volatile ensures visibility across threads
    private static volatile DBConnection instance;
    private Connection connection;

    /**
     * Private constructor prevents external instantiation.
     * Loads the MySQL JDBC driver and establishes a connection.
     */
    private DBConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DBConnection] Connected to MySQL successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("[DBConnection] Failed to connect to MySQL.");
            e.printStackTrace();
        }
    }

    /**
     * Returns the singleton instance of DBConnection using double-checked locking.
     * Thread-safe lazy initialization.
     */
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

    /**
     * Returns the active JDBC Connection object.
     * If the connection has been closed or is null, it re-establishes it.
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("[DBConnection] Reconnecting to database...");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("[DBConnection] Reconnection failed.");
            e.printStackTrace();
        }
        return connection;
    }
}
