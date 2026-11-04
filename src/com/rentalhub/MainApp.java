package com.rentalhub;

import java.util.Scanner;

public class MainApp {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        CustomerService customerService = new CustomerService();
        AdminService    adminService    = new AdminService();
        ItemService     itemService     = new ItemService();
        RentalService   rentalService   = new RentalService();

        printBanner();

        while (true) {
            printMainMenu();
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    customerService.registerCustomer();
                    break;

                case "2":
                    if (customerService.login()) {
                        int customerId = customerService.getLoggedInCustomerId();
                        String customerName = customerService.getLoggedInCustomerName();
                        customerMenu(customerId, customerName,
                                     itemService, rentalService, customerService);
                    }
                    break;

                case "3":
                    if (adminService.login()) {
                        adminMenu(adminService, itemService);
                    }
                    break;

                case "4":
                    System.out.println("\n  Thank you for using RentalHub. Goodbye!");
                    System.out.println("  " + "═".repeat(50));
                    sc.close();
                    System.exit(0);
                    break;

                default:
                    System.out.println("  ✘ Invalid choice. Please enter 1 to 4.");
            }
        }
    }

    // ── CUSTOMER MENU ────────────────────────────────────────────────
    static void customerMenu(int customerId, String customerName,
                             ItemService itemService,
                             RentalService rentalService,
                             CustomerService customerService) {
        while (true) {
            System.out.println("\n  ╔══════════════════════════════════════════╗");
            System.out.println("  ║         CUSTOMER MENU                    ║");
            System.out.println("  ║  Hello, " +
                    padRight(customerName, 33) + "║");
            System.out.println("  ╠══════════════════════════════════════════╣");
            System.out.println("  ║  [1]  View All Items                     ║");
            System.out.println("  ║  [2]  View Items by Category             ║");
            System.out.println("  ║  [3]  Search Items                       ║");
            System.out.println("  ║  [4]  View Item Details                  ║");
            System.out.println("  ║  [5]  Rent an Item                       ║");
            System.out.println("  ║  [6]  Return an Item                     ║");
            System.out.println("  ║  [7]  My Active Rentals                  ║");
            System.out.println("  ║  [8]  My Rental History                  ║");
            System.out.println("  ║  [9]  My Profile                         ║");
            System.out.println("  ║  [0]  Logout                             ║");
            System.out.println("  ╚══════════════════════════════════════════╝");
            System.out.print("  Enter choice : ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    itemService.viewItems();
                    break;
                case "2":
                    itemService.viewByCategory();
                    break;
                case "3":
                    itemService.searchItems();
                    break;
                case "4":
                    itemService.viewItemDetails();
                    break;
                case "5":
                    rentalService.rentItem(customerId);
                    break;
                case "6":
                    rentalService.returnItem(customerId);
                    break;
                case "7":
                    rentalService.viewActiveRentals(customerId);
                    break;
                case "8":
                    rentalService.viewMyRentals(customerId);
                    break;
                case "9":
                    customerService.viewProfile();
                    break;
                case "0":
                    System.out.println("\n  Logged out successfully. Goodbye, "
                                       + customerName + "!");
                    return;
                default:
                    System.out.println("  ✘ Invalid choice. Please enter 0 to 9.");
            }
        }
    }

    // ── ADMIN MENU ───────────────────────────────────────────────────
    static void adminMenu(AdminService adminService, ItemService itemService) {
        while (true) {
            System.out.println("\n  ╔══════════════════════════════════════════╗");
            System.out.println("  ║           ADMIN MENU                     ║");
            System.out.println("  ╠══════════════════════════════════════════╣");
            System.out.println("  ║  ITEMS                                   ║");
            System.out.println("  ║  [1]  Add New Item                       ║");
            System.out.println("  ║  [2]  View All Items                     ║");
            System.out.println("  ║  [3]  Search Items                       ║");
            System.out.println("  ║  [4]  Update Item Quantity               ║");
            System.out.println("  ║  [5]  Delete Item                        ║");
            System.out.println("  ╠══════════════════════════════════════════╣");
            System.out.println("  ║  CUSTOMERS                               ║");
            System.out.println("  ║  [6]  View All Customers                 ║");
            System.out.println("  ╠══════════════════════════════════════════╣");
            System.out.println("  ║  RENTALS                                 ║");
            System.out.println("  ║  [7]  View All Rentals                   ║");
            System.out.println("  ║  [8]  View Active Rentals                ║");
            System.out.println("  ╠══════════════════════════════════════════╣");
            System.out.println("  ║  [0]  Logout                             ║");
            System.out.println("  ╚══════════════════════════════════════════╝");
            System.out.print("  Enter choice : ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    adminService.addItem();
                    break;
                case "2":
                    adminService.viewAllItems();
                    break;
                case "3":
                    adminService.searchItems();
                    break;
                case "4":
                    adminService.updateItemQuantity();
                    break;
                case "5":
                    adminService.deleteItem();
                    break;
                case "6":
                    adminService.viewCustomers();
                    break;
                case "7":
                    adminService.viewRentals();
                    break;
                case "8":
                    adminService.viewActiveRentals();
                    break;
                case "0":
                    System.out.println("\n  Admin logged out successfully.");
                    return;
                default:
                    System.out.println("  ✘ Invalid choice. Please enter 0 to 8.");
            }
        }
    }

    // ── MAIN MENU ────────────────────────────────────────────────────
    static void printMainMenu() {
        System.out.println("\n  ╔══════════════════════════════════════════╗");
        System.out.println("  ║          RENTALHUB MAIN MENU             ║");
        System.out.println("  ╠══════════════════════════════════════════╣");
        System.out.println("  ║  [1]  Register as Customer               ║");
        System.out.println("  ║  [2]  Customer Login                     ║");
        System.out.println("  ║  [3]  Admin Login                        ║");
        System.out.println("  ║  [4]  Exit                               ║");
        System.out.println("  ╚══════════════════════════════════════════╝");
        System.out.print("  Enter choice : ");
    }

    // ── BANNER ───────────────────────────────────────────────────────
    static void printBanner() {
        System.out.println("\n  ═══════════════════════════════════════════════════");
        System.out.println("        ____  _____ _   _ _____  _    _    _   _ _   _ ____  ");
        System.out.println("       |  _ \\| ____| \\ | |_   _|/ \\  | |  | | | | | | | __ ) ");
        System.out.println("       | |_) |  _| |  \\| | | | / _ \\ | |  | |_| | | | |  _ \\ ");
        System.out.println("       |  _ <| |___| |\\  | | |/ ___ \\| |__|  _  | |_| | |_) |");
        System.out.println("       |_| \\_\\_____|_| \\_| |_/_/   \\_\\_____|_| |_|\\___/|____/ ");
        System.out.println("  ═══════════════════════════════════════════════════");
        System.out.println("         Equipment Rental & Return Management System");
        System.out.println("  ═══════════════════════════════════════════════════\n");
    }

    // ── HELPER ───────────────────────────────────────────────────────
    static String padRight(String text, int length) {
        if (text.length() >= length) return text.substring(0, length);
        return text + " ".repeat(length - text.length());
    }
}
