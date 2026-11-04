# RentalHub – Rental Items Management System

## Project Overview

RentalHub is a menu-driven, console-based rental management application developed using Java, JDBC and MySQL.

The application allows administrators to manage rental items and customer records, while customers can browse available items and manage their rentals. It also tracks rental due dates and applies penalties for late returns.

## Technologies Used

- Java
- JDBC (Java Database Connectivity)
- MySQL
- Eclipse IDE

## Key Features

### 1. Admin Management
- Admin login and authentication.
- Manage rental items.
- Add new items.
- Update existing item details.
- Remove items.

### 2. Item Management
- View available rental items.
- Search for items.
- Maintain item details in the database.

### 3. Customer Management
- Manage customer information.
- Maintain customer records for rental transactions.

### 4. Rental Management
- Manage rental transactions.
- Track rented items.
- Maintain rental due dates.
- Handle item returns.
- Apply late-return penalties when items are returned after their due dates.

## Database Connectivity

The application uses JDBC to connect Java with a MySQL database.

- Database: `rentalhub`
- Host: `localhost`
- Port: `3306`

Database credentials are maintained in a local `db.properties` configuration file and are not included in the public repository.

## Project Structure

```text
RentalHub/
├── src/
│   ├── com/rentalhub/
│   │   ├── AdminService.java
│   │   ├── CustomerService.java
│   │   ├── DBconnection.java
│   │   ├── ItemService.java
│   │   ├── MainApp.java
│   │   └── RentalService.java
│   └── db.properties (local configuration)
├── .gitignore
├── .classpath
└── .project
```

## Setup and Execution

1. Clone this repository.
2. Import the project into Eclipse.
3. Install and configure MySQL.
4. Create the `rentalhub` database and its required tables.
5. Configure your local `db.properties` file with your MySQL connection details.
6. Add the MySQL Connector/J library to the project's build path.
7. Run `MainApp.java` to start the application.

## Learning Outcomes

This project demonstrates practical implementation of:

- Java programming and object-oriented concepts.
- JDBC database connectivity.
- MySQL database operations.
- CRUD operations (Create, Read, Update and Delete).
- Console-based application development.
- Rental transaction and due-date management.
