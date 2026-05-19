# 📈 Stock Market Portfolio Tracker with Anomaly Detection

A full-stack desktop application built with **Java Swing**, **JDBC**, and **MySQL** for tracking stock market portfolios, executing trades, and detecting market anomalies.

> Developed as a 4th-semester Engineering College DBMS + Java project.

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| **User Authentication** | Secure login & registration with SHA-256 password hashing |
| **Portfolio Dashboard** | Create and manage multiple portfolios with real-time P/L tracking |
| **Transaction Desk** | Execute BUY/SELL orders with weighted-average price recalculation |
| **Anomaly Alert Hub** | View severity-coded alerts for unusual market activity on your holdings |

## 🏗️ Architecture

```
src/com/tracker/
├── app/        → Main.java (entry point)
├── config/     → DBConnection.java (Singleton JDBC manager)
├── model/      → POJOs: User, Portfolio, Stock, Holding, Transaction, Anomaly, Alert
├── dao/        → Data Access Objects with PreparedStatements & try-with-resources
└── view/       → Swing UI: LoginFrame, RegisterFrame, DashboardFrame, TransactionDialog
```

**Design Patterns Used:** Singleton (DB Connection), DAO Pattern, MVC Separation

## 🛠️ Tech Stack

- **Frontend:** Java Swing (no external UI frameworks)
- **Backend:** Core Java, JDBC
- **Database:** MySQL 8.0
- **Driver:** MySQL Connector/J 8.3.0

## 📋 Database Schema

8 normalized tables with foreign key constraints and `ON DELETE CASCADE`:

`User` → `Portfolio` → `Holding` ← `Stock` ← `Market_Data`  
`Stock` → `Anomaly` → `Alert`  
`Portfolio` + `Stock` → `Transaction`

## 🚀 How to Run

### Prerequisites
- Java 17+ (JDK)
- MySQL 8.0+

### 1. Set up the Database

```sql
-- Open MySQL CLI or Workbench
source schema.sql
```

### 2. Configure Database Credentials

Edit `src/com/tracker/config/DBConnection.java`:

```java
private static final String URL      = "jdbc:mysql://localhost:3306/stock_tracker";
private static final String USER     = "root";
private static final String PASSWORD = "your_password_here";
```

### 3. Compile

```bash
javac -cp "lib/mysql-connector-j-8.3.0.jar" -d out src/com/tracker/**/*.java
```

### 4. Run

```bash
java -cp "out;lib/mysql-connector-j-8.3.0.jar" com.tracker.app.Main
```

### Demo Credentials

| Email | Password |
|-------|----------|
| `demo@tracker.com` | `password123` |

## 📸 Screens

1. **Login** — Dark themed sign-in with email/password
2. **Dashboard** — Portfolio sidebar, holdings table with live P/L, transaction history
3. **Trade Dialog** — Stock selector with auto-filled market prices
4. **Alert Hub** — Severity-coded anomaly cards for held stocks
