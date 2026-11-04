package com.rentalhub;

import java.sql.*;
import java.util.Scanner;

public class ItemService {

    private Scanner sc = new Scanner(System.in);

    // ── 1. VIEW ALL ITEMS ────────────────────────────────────────────
    public void viewItems() {
        String sql = "SELECT * FROM items ORDER BY category, name";
        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n  ── All Items ────────────────────────────────────────────────────────────");
            System.out.printf("  %-4s %-22s %-14s %-10s %-5s %-10s %-8s%n",
                    "ID", "Name", "Category", "Price/Day", "Qty", "Condition", "Status");
            System.out.println("  " + "─".repeat(78));

            int total = 0;
            int available = 0;
            while (rs.next()) {
                total++;
                int qty = rs.getInt("quantity");
                if (qty > 0) available++;
                String status = qty > 0 ? "Available" : "Out of Stock";

                System.out.printf("  %-4d %-22s %-14s %-10.2f %-5d %-10s %-8s%n",
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price_per_day"),
                        qty,
                        rs.getString("condition_status"),
                        status);
            }
            System.out.println("  " + "─".repeat(78));
            System.out.println("  Total items: " + total +
                               "  |  Available: " + available +
                               "  |  Out of stock: " + (total - available));

        } catch (SQLException e) {
            System.out.println("  ✘ Error viewing items: " + e.getMessage());
        }
    }

    // ── 2. VIEW ITEMS BY CATEGORY ────────────────────────────────────
    public void viewByCategory() {
        System.out.println("\n  Categories: Electronics, Photography, Sports, Camping, Gaming, Music, Tools");
        System.out.print("  Enter category : ");
        String category = sc.nextLine().trim();

        if (category.isEmpty()) {
            System.out.println("  ✘ Category cannot be empty.");
            return;
        }

        String sql = "SELECT * FROM items WHERE category LIKE ? ORDER BY name";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + category + "%");
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n  ── Items in Category: " + category +
                               " ──────────────────────────────────────────");
            System.out.printf("  %-4s %-22s %-10s %-5s %-10s %-30s%n",
                    "ID", "Name", "Price/Day", "Qty", "Condition", "Description");
            System.out.println("  " + "─".repeat(85));

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("  %-4d %-22s %-10.2f %-5d %-10s %-30s%n",
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getDouble("price_per_day"),
                        rs.getInt("quantity"),
                        rs.getString("condition_status"),
                        rs.getString("description") != null
                                ? rs.getString("description") : "N/A");
            }
            System.out.println("  " + "─".repeat(85));
            if (count == 0)
                System.out.println("  No items found in category: " + category);
            else
                System.out.println("  Found: " + count + " item(s)");

        } catch (SQLException e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    // ── 3. SEARCH ITEMS BY KEYWORD ───────────────────────────────────
    public void searchItems() {
        System.out.print("\n  Enter keyword (name or category) : ");
        String keyword = sc.nextLine().trim();

        if (keyword.isEmpty()) {
            System.out.println("  ✘ Search keyword cannot be empty.");
            return;
        }

        String sql = "SELECT * FROM items WHERE name LIKE ? OR category LIKE ? ORDER BY category";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n  ── Search Results: \"" + keyword + "\" ─────────────────────────────────────");
            System.out.printf("  %-4s %-22s %-14s %-10s %-5s %-8s%n",
                    "ID", "Name", "Category", "Price/Day", "Qty", "Status");
            System.out.println("  " + "─".repeat(68));

            int count = 0;
            while (rs.next()) {
                count++;
                int qty = rs.getInt("quantity");
                String status = qty > 0 ? "Available" : "Out of Stock";
                System.out.printf("  %-4d %-22s %-14s %-10.2f %-5d %-8s%n",
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price_per_day"),
                        qty,
                        status);
            }
            System.out.println("  " + "─".repeat(68));
            if (count == 0)
                System.out.println("  No items found for: \"" + keyword + "\"");
            else
                System.out.println("  Found: " + count + " result(s)");

        } catch (SQLException e) {
            System.out.println("  ✘ Error searching items: " + e.getMessage());
        }
    }

    // ── 4. VIEW ITEM DETAILS ─────────────────────────────────────────
    public void viewItemDetails() {
        int itemId = 0;
        while (true) {
            try {
                System.out.print("\n  Enter Item ID for details : ");
                itemId = Integer.parseInt(sc.nextLine().trim());
                break;
            } catch (NumberFormatException e) {
                System.out.println("  ✘ Invalid input. Enter a valid number.");
            }
        }

        String sql = "SELECT * FROM items WHERE item_id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("\n  ── Item Details ─────────────────────────────────");
                System.out.println("  Item ID     : " + rs.getInt("item_id"));
                System.out.println("  Name        : " + rs.getString("name"));
                System.out.println("  Category    : " + rs.getString("category"));
                System.out.println("  Price/Day   : ₹" + rs.getDouble("price_per_day"));
                System.out.println("  Quantity    : " + rs.getInt("quantity"));
                System.out.println("  Condition   : " + rs.getString("condition_status"));
                System.out.println("  Description : " + (rs.getString("description") != null
                                   ? rs.getString("description") : "N/A"));
                System.out.println("  Added On    : " + rs.getDate("added_date"));
                System.out.println("  Status      : " +
                        (rs.getInt("quantity") > 0 ? "✔ Available" : "✘ Out of Stock"));
                System.out.println("  " + "─".repeat(48));
            } else {
                System.out.println("  ✘ Item not found.");
            }

        } catch (SQLException e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }
}