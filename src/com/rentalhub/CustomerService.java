package com.rentalhub;

import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class CustomerService {

    private Scanner sc = new Scanner(System.in);
    private int loggedInCustomerId = -1;
    private String loggedInCustomerName = "";

    // ── 1. REGISTER CUSTOMER ─────────────────────────────────────────
    public void registerCustomer() {
        System.out.println("\n  ── Register New Customer ────────────────────────");

        // Name validation
        String name = "";
        while (true) {
            System.out.print("  Full name     : ");
            name = sc.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("  ✘ Name cannot be empty.");
            } else if (name.length() < 3) {
                System.out.println("  ✘ Name must be at least 3 characters.");
            } else {
                break;
            }
        }

        // Phone validation
        String phone = "";
        while (true) {
            System.out.print("  Phone number  : ");
            phone = sc.nextLine().trim();
            if (!phone.matches("\\d{10}")) {
                System.out.println("  ✘ Phone must be exactly 10 digits.");
            } else {
                // Check if phone already exists
                if (isPhoneRegistered(phone)) {
                    System.out.println("  ✘ This phone number is already registered.");
                } else {
                    break;
                }
            }
        }

        // Email (optional)
        System.out.print("  Email (optional, press Enter to skip) : ");
        String email = sc.nextLine().trim();
        if (email.isEmpty()) email = null;

        // Password validation
        String password = "";
        while (true) {
            System.out.print("  Password      : ");
            password = sc.nextLine().trim();
            if (password.length() < 4) {
                System.out.println("  ✘ Password must be at least 4 characters.");
            } else {
                break;
            }
        }

        // Insert into DB
        String sql = "INSERT INTO customers(name, phone, email, password, registered_date) " +
                     "VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, email);
            pstmt.setString(4, password);
            pstmt.setDate(5, Date.valueOf(LocalDate.now()));
            pstmt.executeUpdate();

            System.out.println("\n  ✔ Registration successful! Welcome, " + name + "!");
            System.out.println("  You can now login with your phone and password.");

        } catch (SQLException e) {
            System.out.println("  ✘ Error registering: " + e.getMessage());
        }
    }

    // ── 2. LOGIN ─────────────────────────────────────────────────────
    public boolean login() {
        System.out.println("\n  ── Customer Login ───────────────────────────────");

        System.out.print("  Phone number : ");
        String phone = sc.nextLine().trim();
        System.out.print("  Password     : ");
        String password = sc.nextLine().trim();

        String sql = "SELECT customer_id, name FROM customers " +
                     "WHERE phone = ? AND password = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, phone);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                loggedInCustomerId   = rs.getInt("customer_id");
                loggedInCustomerName = rs.getString("name");
                System.out.println("\n  ✔ Login successful! Welcome back, "
                                   + loggedInCustomerName + "!");
                return true;
            } else {
                System.out.println("\n  ✘ Invalid phone or password. Try again.");
                return false;
            }

        } catch (SQLException e) {
            System.out.println("  ✘ Login error: " + e.getMessage());
            return false;
        }
    }

    // ── 3. VIEW MY PROFILE ───────────────────────────────────────────
    public void viewProfile() {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, loggedInCustomerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("\n  ── My Profile ───────────────────────────────────");
                System.out.println("  Customer ID   : " + rs.getInt("customer_id"));
                System.out.println("  Name          : " + rs.getString("name"));
                System.out.println("  Phone         : " + rs.getString("phone"));
                System.out.println("  Email         : " + (rs.getString("email") != null
                                                           ? rs.getString("email") : "N/A"));
                System.out.println("  Registered On : " + rs.getDate("registered_date"));
                System.out.println("  " + "─".repeat(48));
            }

        } catch (SQLException e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    // ── 4. HELPER — CHECK PHONE EXISTS ──────────────────────────────
    private boolean isPhoneRegistered(String phone) {
        String sql = "SELECT customer_id FROM customers WHERE phone = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            return false;
        }
    }

    // ── 5. GETTERS ───────────────────────────────────────────────────
    public int getLoggedInCustomerId() {
        return loggedInCustomerId;
    }

    public String getLoggedInCustomerName() {
        return loggedInCustomerName;
    }
}