package ums.db;

import javax.swing.*;
import java.sql.*;

public class DBConnection {
    private static DBConnection instance;
    private Connection connection;
    private static final String URL  = "jdbc:mysql://localhost:3308/tec_mis";
    private static final String USER = "root";
    private static final String PASS = "1234";

    private DBConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(null, "DB Error: " + e.getMessage());
        }
    }

    // BUG FIX: add synchronized to prevent race condition in multi-threaded Swing context
    public static synchronized DBConnection getInstance() {
        if (instance == null) instance = new DBConnection();
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASS);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Reconnect Error: " + e.getMessage());
        }
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }
}
