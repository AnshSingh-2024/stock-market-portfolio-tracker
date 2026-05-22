# Stock Market Portfolio Tracker

A full-stack desktop application built with **Java Swing**, **JDBC**, and **MySQL** for tracking stock market portfolios, executing trades, and viewing simulated market-driven profit/loss changes.

> Developed as a 4th-semester Engineering College DBMS + Java project.

---

## Features

| Feature | Description |
|---------|-------------|
| **User Authentication** | Secure login and registration with SHA-256 password hashing |
| **Portfolio Dashboard** | Create and manage multiple portfolios with auto-refreshing P/L tracking |
| **Transaction Desk** | Execute BUY/SELL orders with weighted-average price recalculation |
| **Market Simulator** | Dashboard updates latest market prices every 10 seconds |

## Architecture

```text
src/com/tracker/
├── app/        -> Main.java (entry point)
├── config/     -> DBConnection.java (Singleton JDBC manager)
├── model/      -> POJOs: User, Portfolio, Stock, Holding, Transaction
├── dao/        -> Data Access Objects with PreparedStatements and try-with-resources
└── view/       -> Swing UI: LoginFrame, RegisterFrame, DashboardFrame, TransactionDialog
```

**Design Patterns Used:** Singleton (DB Connection), DAO Pattern, MVC Separation

## Tech Stack

- **Frontend:** Java Swing
- **Backend:** Core Java, JDBC
- **Database:** MySQL 8.0+
- **Driver:** MySQL Connector/J 8.3.0

## Database Schema

6 normalized tables with foreign key constraints and `ON DELETE CASCADE`:

```text
User -> Portfolio -> Holding <- Stock <- Market_Data
Portfolio + Stock -> Transaction
```

## How to Run

### Prerequisites

- Java 17+ (JDK)
- MySQL 8.0+

### 1. Start MySQL

```powershell
powershell -ExecutionPolicy Bypass -File .\start-mysql.ps1
```

### 2. Set up the database

```powershell
Get-Content .\schema.sql | & "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root
```

### 3. Configure database credentials

The app reads database settings from environment variables:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/stock_tracker"
$env:DB_USER="root"
$env:DB_PASSWORD=""
```

### 4. Compile

```powershell
powershell -ExecutionPolicy Bypass -File .\compile.ps1
```

### 5. Run

```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

The dashboard updates the latest `Market_Data` close prices every 10 seconds, then reloads holdings so current price and profit/loss change while the app is open.

## Demo Credentials

| Email | Password |
|-------|----------|
| `demo@tracker.com` | `password123` |

## Screens

1. **Login** - Dark themed sign-in with email/password
2. **Dashboard** - Portfolio sidebar, holdings table with live P/L, transaction history
3. **Trade Dialog** - Stock selector with auto-filled market prices
