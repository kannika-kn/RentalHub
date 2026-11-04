package com.rentalhub;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class DBconnection {

    public static Connection getConnection() {

        try {
            Properties props = new Properties();

            InputStream input = DBconnection.class
                    .getClassLoader()
                    .getResourceAsStream("db.properties");

            if (input == null) {
                System.out.println("Database configuration file not found.");
                return null;
            }

            props.load(input);
            input.close();

            String url = props.getProperty("url");
            String user = props.getProperty("user");
            String password = props.getProperty("password");

            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(url, user, password);

            System.out.println("Database connected successfully.");

            return con;

        } catch (ClassNotFoundException | SQLException | IOException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            return null;
        }
    }
}