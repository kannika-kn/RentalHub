package com.rentalhub;

import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class AdminService {

    private Scanner sc = new Scanner(System.in);

    // ── 1. LOGIN FROM DATABASE ───────────────────────────────────────
    public boolean login() {
        System.out.print("  Enter admin username : ");
        String user = sc.nextLine().trim();
        System.out.print("  Enter admin password : ");
        String pass = sc.nextLine().trim();

        String sql = "SELECT * FROM admin WHERE username = ? AND password = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user);
            pstmt.setString(2, pass);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("\n  ✔ Admin login successful. Welcome, " + user + "!");
                return true;
            } else {
                System.out.println("\n  ✘ Invalid username or password.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("  ✘ Login error: " + e.getMessage());
            return false;
        }
    }

    // ── 2. ADD ITEM ──────────────────────────────────────────────────
    public void addItem() {
        System.out.println("\n  ── Add New Item ─────────────────────────────");

        System.out.print("  Item name        : ");
        String name = sc.nextLine().trim();

        System.out.println("  Categories: Electronics, Photography, Sports, Camping, Gaming, Music, Tools");
        System.out.print("  Category         : ");
        String category = sc.nextLine().trim();

        System.out.print("  Description      : ");
        String description = sc.nextLine().trim();

        double price = 0;
        while (true) {
            try {
                System.out.print("  Price per day(₹) : ");
                price = Double.parseDouble(sc.nextLine().trim());
                if (price <= 0) { System.out.println("  ✘ Price must be greater than 0."); continue; }
                break;
            } catch (NumberFormatException e) {
                System.out.println("  ✘ Invalid input. Enter a valid number.");
            }
        }

        int qty = 0;
        while (true) {
            try {
                System.out.print("  Quantity         : ");
                qty = Integer.parseInt(sc.nextLine().trim());
                if (qty <= 0) { System.out.println("  ✘ Quantity must be at least 1."); continue; }
                break;
            } catch (NumberFormatException e) {
                System.out.println("  ✘ Invalid input. Enter a valid number.");
            }
        }

        String sql = "INSERT INTO items(name, category, price_per_day, quantity, condition_status, description, added_date) " +
                     "VALUES(?, ?, ?, ?, 'GOOD', ?, ?)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, qty);
            pstmt.setString(5, description);
            pstmt.setDate(6, Date.valueOf(LocalDate.now()));
            pstmt.executeUpdate();
            System.out.println("\n  ✔ Item '" + name + "' added successfully!");

        } catch (SQLException e) {
            System.out.println("  ✘ Error adding item: " + e.getMessage());
        }
    }

    // ── 3. UPDATE ITEM QUANTITY ──────────────────────────────────────
    public void updateItemQuantity() {
        viewAllItems();
        int id = 0;
        while (true) {
            try {
                System.out.print("\n  Enter Item ID to update : ");
                id = Integer.parseInt(sc.nextLine().trim());
                break;
            } catch (NumberFormatException e) {
                System.out.println("  ✘ Invalid input. Enter a valid number.");
            }
        }

        int newQty = 0;
        while (true) {
            try {
                System.out.print("  Enter new quantity      : ");
                newQty = Integer.parseInt(sc.nextLine().trim());
                if (newQty < 0) { System.out.println("  ✘ Quantity cannot be negative."); continue; }
                break;
            } catch (NumberFormatException e) {
                System.out.println("  ✘ Invalid input. Enter a valid number.");
            }
        }

        String sql = "UPDATE items SET quantity = ? WHERE item_id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newQty);
            pstmt.setInt(2, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0)
                System.out.println("  ✔ Quantity updated successfully!");
            else
                System.out.println("  ✘ Item ID not found.");

        } catch (SQLException e) {
            System.out.println("  ✘ Error updating item: " + e.getMessage());
        }
    }

    // ── 4. DELETE ITEM ───────────────────────────────────────────────
    public void deleteItem() {
        viewAllItems();
        int id = 0;
        while (true) {
            try {
                System.out.print("\n  Enter Item ID to delete : ");
                id = Integer.parseInt(sc.nextLine().trim());
                break;
            } catch (NumberFormatException e) {
                System.out.println("  ✘ Invalid input. Enter a valid number.");
            }
        }

        System.out.print("  Are you sure? (yes/no) : ");
        String confirm = sc.nextLine().trim();
        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("  Delete cancelled.");
            return;
        }

        String sql = "DELETE FROM items WHERE item_id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0)
                System.out.println("  ✔ Item deleted successfully!");
            else
                System.out.println("  ✘ Item ID not found.");

        } catch (SQLException e) {
            System.out.println("  ✘ Error: Cannot delete item that has active rentals.");
        }
    }

    // ── 5. VIEW ALL ITEMS (ADMIN) ────────────────────────────────────
    public void viewAllItems() {
        String sql = "SELECT * FROM items ORDER BY category, name";
        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n  ── All Items ────────────────────────────────────────────────────────────────");
            System.out.printf("  %-4s %-22s %-14s %-10s %-5s %-10s%n",
                    "ID", "Name", "Category", "Price/Day", "Qty", "Condition");
            System.out.println("  " + "─".repeat(75));

            while (rs.next()) {
                System.out.printf("  %-4d %-22s %-14s %-10.2f %-5d %-10s%n",
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price_per_day"),
                        rs.getInt("quantity"),
                        rs.getString("condition_status"));
            }
            System.out.println("  " + "─".repeat(75));

        } catch (SQLException e) {
            System.out.println("  ✘ Error viewing items: " + e.getMessage());
        }
    }

    // ── 6. VIEW ALL CUSTOMERS ────────────────────────────────────────
    public void viewCustomers() {
        String sql = "SELECT * FROM customers ORDER BY registered_date DESC";
        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n  ── All Customers ───────────────────────────────────────────────────────────");
            System.out.printf("  %-4s %-20s %-15s %-25s %-12s%n",
                    "ID", "Name", "Phone", "Email", "Registered");
            System.out.println("  " + "─".repeat(80));

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("  %-4d %-20s %-15s %-25s %-12s%n",
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email") != null ? rs.getString("email") : "N/A",
                        rs.getDate("registered_date"));
            }
            System.out.println("  " + "─".repeat(80));
            System.out.println("  Total customers: " + count);

        } catch (SQLException e) {
            System.out.println("  ✘ Error viewing customers: " + e.getMessage());
        }
    }

    // ── 7. VIEW ALL RENTALS ──────────────────────────────────────────
    public void viewRentals() {
        String sql = "SELECT r.rental_id, c.name AS customer, i.name AS item, " +
                     "r.rent_date, r.due_date, r.return_date, r.rental_days, " +
                     "r.late_fee, r.total_amount, r.status " +
                     "FROM rentals r " +
                     "JOIN customers c ON r.customer_id = c.customer_id " +
                     "JOIN items i ON r.item_id = i.item_id " +
                     "ORDER BY r.rent_date DESC";
        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n  ── All Rental Transactions ─────────────────────────────────────────────────");
            System.out.printf("  %-4s %-18s %-20s %-12s %-12s %-12s %-5s %-10s %-10s %-8s%n",
                    "ID", "Customer", "Item", "RentDate", "DueDate", "ReturnDate",
                    "Days", "LateFee", "Total", "Status");
            System.out.println("  " + "─".repeat(115));

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("  %-4d %-18s %-20s %-12s %-12s %-12s %-5d %-10.2f %-10.2f %-8s%n",
                        rs.getInt("rental_id"),
                        rs.getString("customer"),
                        rs.getString("item"),
                        rs.getDate("rent_date"),
                        rs.getDate("due_date"),
                        rs.getDate("return_date") != null ? rs.getDate("return_date") : "Pending",
                        rs.getInt("rental_days"),
                        rs.getDouble("late_fee"),
                        rs.getDouble("total_amount"),
                        rs.getString("status"));
            }
            System.out.println("  " + "─".repeat(115));
            System.out.println("  Total transactions: " + count);

        } catch (SQLException e) {
            System.out.println("  ✘ Error viewing rentals: " + e.getMessage());
        }
    }

    // ── 8. VIEW ACTIVE RENTALS ONLY ─────────────────────────────────
    public void viewActiveRentals() {
        String sql = "SELECT r.rental_id, c.name AS customer, i.name AS item, " +
                     "r.rent_date, r.due_date, r.total_amount " +
                     "FROM rentals r " +
                     "JOIN customers c ON r.customer_id = c.customer_id " +
                     "JOIN items i ON r.item_id = i.item_id " +
                     "WHERE r.status = 'ACTIVE' " +
                     "ORDER BY r.due_date ASC";
        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n  ── Active Rentals ──────────────────────────────────────────────────────────");
            System.out.printf("  %-4s %-18s %-20s %-12s %-12s %-10s%n",
                    "ID", "Customer", "Item", "RentDate", "DueDate", "Total");
            System.out.println("  " + "─".repeat(80));

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("  %-4d %-18s %-20s %-12s %-12s %-10.2f%n",
                        rs.getInt("rental_id"),
                        rs.getString("customer"),
                        rs.getString("item"),
                        rs.getDate("rent_date"),
                        rs.getDate("due_date"),
                        rs.getDouble("total_amount"));
            }
            System.out.println("  " + "─".repeat(80));
            System.out.println("  Active rentals: " + count);

        } catch (SQLException e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    // ── 9. SEARCH ITEM BY NAME OR CATEGORY ──────────────────────────
    public void searchItems() {
        System.out.print("\n  Enter item name or category to search : ");
        String keyword = sc.nextLine().trim();

        String sql = "SELECT * FROM items WHERE name LIKE ? OR category LIKE ? ORDER BY category";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n  ── Search Results ───────────────────────────────────────────────────────────");
            System.out.printf("  %-4s %-22s %-14s %-10s %-5s%n",
                    "ID", "Name", "Category", "Price/Day", "Qty");
            System.out.println("  " + "─".repeat(60));

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("  %-4d %-22s %-14s %-10.2f %-5d%n",
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price_per_day"),
                        rs.getInt("quantity"));
            }
            System.out.println("  " + "─".repeat(60));
            System.out.println("  Found: " + count + " result(s)");

        } catch (SQLException e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }
}