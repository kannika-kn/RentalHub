package com.rentalhub;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class RentalService {

    private Scanner sc = new Scanner(System.in);

    // ── 1. VIEW AVAILABLE ITEMS ──────────────────────────────────────
    public void viewAvailableItems() {
        String sql = "SELECT * FROM items WHERE quantity > 0 ORDER BY category, name";
        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n  ── Available Items ──────────────────────────────────────────────────────");
            System.out.printf("  %-4s %-22s %-14s %-12s %-5s%n",
                    "ID", "Name", "Category", "Price/Day", "Qty");
            System.out.println("  " + "─".repeat(65));

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("  %-4d %-22s %-14s %-12.2f %-5d%n",
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price_per_day"),
                        rs.getInt("quantity"));
            }
            System.out.println("  " + "─".repeat(65));
            System.out.println("  Total available items: " + count);

        } catch (SQLException e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    // ── 2. RENT ITEM ─────────────────────────────────────────────────
    public void rentItem(int customerId) {
        viewAvailableItems();
        System.out.println();

        // Item ID validation
        int itemId = 0;
        while (true) {
            try {
                System.out.print("  Enter Item ID to rent : ");
                itemId = Integer.parseInt(sc.nextLine().trim());
                break;
            } catch (NumberFormatException e) {
                System.out.println("  ✘ Invalid input. Enter a valid number.");
            }
        }

        // Days validation
        int days = 0;
        while (true) {
            try {
                System.out.print("  How many days?        : ");
                days = Integer.parseInt(sc.nextLine().trim());
                if (days <= 0) {
                    System.out.println("  ✘ Days must be at least 1.");
                } else if (days > 30) {
                    System.out.println("  ✘ Maximum rental period is 30 days.");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("  ✘ Invalid input. Enter a valid number.");
            }
        }

        // Check item availability
        String checkSql = "SELECT name, quantity, price_per_day FROM items WHERE item_id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, itemId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("  ✘ Item not found!");
                return;
            }

            String itemName    = rs.getString("name");
            int quantity       = rs.getInt("quantity");
            double pricePerDay = rs.getDouble("price_per_day");

            if (quantity <= 0) {
                System.out.println("  ✘ Sorry! '" + itemName + "' is currently out of stock.");
                return;
            }

            // Check if customer already has active rental for same item
            String dupSql = "SELECT rental_id FROM rentals WHERE customer_id = ? " +
                            "AND item_id = ? AND status = 'ACTIVE'";
            try (PreparedStatement dupStmt = conn.prepareStatement(dupSql)) {
                dupStmt.setInt(1, customerId);
                dupStmt.setInt(2, itemId);
                ResultSet dupRs = dupStmt.executeQuery();
                if (dupRs.next()) {
                    System.out.println("  ✘ You already have an active rental for this item!");
                    return;
                }
            }

            // Calculate amounts
            LocalDate rentDate   = LocalDate.now();
            LocalDate dueDate    = rentDate.plusDays(days);
            double totalAmount   = days * pricePerDay;

            // Confirm before renting
            System.out.println("\n  ── Rental Summary ───────────────────────────────");
            System.out.println("  Item          : " + itemName);
            System.out.println("  Price/Day     : ₹" + pricePerDay);
            System.out.println("  Days          : " + days);
            System.out.println("  Rent Date     : " + rentDate);
            System.out.println("  Due Date      : " + dueDate);
            System.out.println("  Total Amount  : ₹" + totalAmount);
            System.out.println("  " + "─".repeat(48));
            System.out.print("  Confirm rental? (yes/no) : ");
            String confirm = sc.nextLine().trim();
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("  Rental cancelled.");
                return;
            }

            // Insert rental
            String insertSql = "INSERT INTO rentals(item_id, customer_id, rent_date, due_date, " +
                                "rental_days, late_fee, total_amount, status) " +
                                "VALUES(?, ?, ?, ?, ?, 0, ?, 'ACTIVE')";
            String updateQtySql = "UPDATE items SET quantity = quantity - 1 WHERE item_id = ?";

            try (PreparedStatement insertStmt = conn.prepareStatement(
                         insertSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement updateStmt = conn.prepareStatement(updateQtySql)) {

                insertStmt.setInt(1, itemId);
                insertStmt.setInt(2, customerId);
                insertStmt.setDate(3, Date.valueOf(rentDate));
                insertStmt.setDate(4, Date.valueOf(dueDate));
                insertStmt.setInt(5, days);
                insertStmt.setDouble(6, totalAmount);
                insertStmt.executeUpdate();

                ResultSet keys = insertStmt.getGeneratedKeys();
                int rentalId = 0;
                if (keys.next()) rentalId = keys.getInt(1);

                updateStmt.setInt(1, itemId);
                updateStmt.executeUpdate();

                System.out.println("\n  ✔ Item rented successfully!");
                System.out.println("  ── Rental Receipt ───────────────────────────────");
                System.out.println("  Rental ID     : #" + rentalId);
                System.out.println("  Item          : " + itemName);
                System.out.println("  Rent Date     : " + rentDate);
                System.out.println("  Due Date      : " + dueDate);
                System.out.println("  Total Amount  : ₹" + totalAmount);
                System.out.println("  Status        : ACTIVE");
                System.out.println("  " + "─".repeat(48));
                System.out.println("  Please return by " + dueDate + " to avoid late fees!");
            }

        } catch (SQLException e) {
            System.out.println("  ✘ Error renting item: " + e.getMessage());
        }
    }

    // ── 3. RETURN ITEM ───────────────────────────────────────────────
    public void returnItem(int customerId) {
        // Show customer's active rentals first
        viewActiveRentals(customerId);

        int rentalId = 0;
        while (true) {
            try {
                System.out.print("\n  Enter Rental ID to return : ");
                rentalId = Integer.parseInt(sc.nextLine().trim());
                break;
            } catch (NumberFormatException e) {
                System.out.println("  ✘ Invalid input. Enter a valid number.");
            }
        }

        // Get rental details — also verify it belongs to this customer
        String getRentalSql = "SELECT r.item_id, r.due_date, r.rental_days, r.status, " +
                              "i.name AS item_name, i.price_per_day " +
                              "FROM rentals r JOIN items i ON r.item_id = i.item_id " +
                              "WHERE r.rental_id = ? AND r.customer_id = ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement getStmt = conn.prepareStatement(getRentalSql)) {

            getStmt.setInt(1, rentalId);
            getStmt.setInt(2, customerId);
            ResultSet rs = getStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("  ✘ Rental not found or does not belong to you!");
                return;
            }

            String status    = rs.getString("status");
            if (status.equals("RETURNED")) {
                System.out.println("  ✘ This item has already been returned!");
                return;
            }

            int itemId         = rs.getInt("item_id");
            String itemName    = rs.getString("item_name");
            double pricePerDay = rs.getDouble("price_per_day");
            LocalDate dueDate  = rs.getDate("due_date").toLocalDate();
            LocalDate returnDate = LocalDate.now();

            // Calculate late fee
            double lateFee    = 0;
            long daysLate     = 0;
            if (returnDate.isAfter(dueDate)) {
                daysLate = ChronoUnit.DAYS.between(dueDate, returnDate);
                lateFee  = daysLate * pricePerDay * 1.5; // 1.5x per day late
            }

            // Recalculate total
            int rentalDays      = rs.getInt("rental_days");
            double baseAmount   = rentalDays * pricePerDay;
            double totalAmount  = baseAmount + lateFee;

            // Show return summary
            System.out.println("\n  ── Return Summary ───────────────────────────────");
            System.out.println("  Item          : " + itemName);
            System.out.println("  Due Date      : " + dueDate);
            System.out.println("  Return Date   : " + returnDate);
            if (daysLate > 0) {
                System.out.println("  Days Late     : " + daysLate);
                System.out.println("  Late Fee      : ₹" + String.format("%.2f", lateFee));
            } else {
                System.out.println("  Returned      : On time ✔");
            }
            System.out.println("  Base Amount   : ₹" + String.format("%.2f", baseAmount));
            System.out.println("  Total Paid    : ₹" + String.format("%.2f", totalAmount));
            System.out.println("  " + "─".repeat(48));

            // Update rental record
            String updateRentalSql = "UPDATE rentals SET return_date = ?, late_fee = ?, " +
                                     "total_amount = ?, status = 'RETURNED' WHERE rental_id = ?";
            String updateQtySql    = "UPDATE items SET quantity = quantity + 1 WHERE item_id = ?";

            try (PreparedStatement updateStmt = conn.prepareStatement(updateRentalSql);
                 PreparedStatement updateItemStmt = conn.prepareStatement(updateQtySql)) {

                updateStmt.setDate(1, Date.valueOf(returnDate));
                updateStmt.setDouble(2, lateFee);
                updateStmt.setDouble(3, totalAmount);
                updateStmt.setInt(4, rentalId);
                updateStmt.executeUpdate();

                updateItemStmt.setInt(1, itemId);
                updateItemStmt.executeUpdate();

                System.out.println("  ✔ Item returned successfully! Thank you.");
            }

        } catch (SQLException e) {
            System.out.println("  ✘ Error returning item: " + e.getMessage());
        }
    }

    // ── 4. VIEW MY ACTIVE RENTALS ────────────────────────────────────
    public void viewActiveRentals(int customerId) {
        String sql = "SELECT r.rental_id, i.name, r.rent_date, r.due_date, r.total_amount " +
                     "FROM rentals r JOIN items i ON r.item_id = i.item_id " +
                     "WHERE r.customer_id = ? AND r.status = 'ACTIVE' " +
                     "ORDER BY r.due_date ASC";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n  ── My Active Rentals ────────────────────────────────────────────────────");
            System.out.printf("  %-4s %-22s %-12s %-12s %-10s%n",
                    "ID", "Item", "RentDate", "DueDate", "Total");
            System.out.println("  " + "─".repeat(65));

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("  %-4d %-22s %-12s %-12s %-10.2f%n",
                        rs.getInt("rental_id"),
                        rs.getString("name"),
                        rs.getDate("rent_date"),
                        rs.getDate("due_date"),
                        rs.getDouble("total_amount"));
            }
            System.out.println("  " + "─".repeat(65));
            if (count == 0)
                System.out.println("  No active rentals.");

        } catch (SQLException e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    // ── 5. VIEW ALL MY RENTALS ───────────────────────────────────────
    public void viewMyRentals(int customerId) {
        String sql = "SELECT r.rental_id, i.name, r.rent_date, r.due_date, " +
                     "r.return_date, r.late_fee, r.total_amount, r.status " +
                     "FROM rentals r JOIN items i ON r.item_id = i.item_id " +
                     "WHERE r.customer_id = ? ORDER BY r.rent_date DESC";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n  ── My Rental History ────────────────────────────────────────────────────");
            System.out.printf("  %-4s %-22s %-12s %-12s %-12s %-8s %-10s %-8s%n",
                    "ID", "Item", "RentDate", "DueDate", "ReturnDate",
                    "LateFee", "Total", "Status");
            System.out.println("  " + "─".repeat(95));

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("  %-4d %-22s %-12s %-12s %-12s %-8.2f %-10.2f %-8s%n",
                        rs.getInt("rental_id"),
                        rs.getString("name"),
                        rs.getDate("rent_date"),
                        rs.getDate("due_date"),
                        rs.getDate("return_date") != null
                                ? rs.getDate("return_date").toString() : "Pending",
                        rs.getDouble("late_fee"),
                        rs.getDouble("total_amount"),
                        rs.getString("status"));
            }
            System.out.println("  " + "─".repeat(95));
            if (count == 0)
                System.out.println("  No rental history found.");
            else
                System.out.println("  Total rentals: " + count);

        } catch (SQLException e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }
}