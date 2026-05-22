-- ============================================================
-- Stock Market Portfolio Tracker — Database Setup Script
-- Run this script in MySQL to create the database and seed data.
-- ============================================================

CREATE DATABASE IF NOT EXISTS stock_tracker;
USE stock_tracker;

-- Remove old anomaly/alert tables if this project was previously initialized
-- with the earlier anomaly-detection version.
DROP TABLE IF EXISTS Alert;
DROP TABLE IF EXISTS Anomaly;

-- -------------------------
-- 1. User table
-- -------------------------
CREATE TABLE IF NOT EXISTS User (
    user_id       INT PRIMARY KEY AUTO_INCREMENT,
    name          VARCHAR(100)  NOT NULL,
    email         VARCHAR(100)  NOT NULL UNIQUE,
    password_hash VARCHAR(255)  NOT NULL
) ENGINE=InnoDB;

-- -------------------------
-- 2. Portfolio table
-- -------------------------
CREATE TABLE IF NOT EXISTS Portfolio (
    portfolio_id  INT PRIMARY KEY AUTO_INCREMENT,
    user_id       INT           NOT NULL,
    name          VARCHAR(100)  NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -------------------------
-- 3. Stock table
-- -------------------------
CREATE TABLE IF NOT EXISTS Stock (
    stock_id      INT PRIMARY KEY AUTO_INCREMENT,
    ticker        VARCHAR(10)   NOT NULL UNIQUE,
    company_name  VARCHAR(100)  NOT NULL
) ENGINE=InnoDB;

-- -------------------------
-- 4. Holding table
-- -------------------------
CREATE TABLE IF NOT EXISTS Holding (
    holding_id    INT PRIMARY KEY AUTO_INCREMENT,
    portfolio_id  INT   NOT NULL,
    stock_id      INT   NOT NULL,
    quantity      INT   NOT NULL DEFAULT 0,
    avg_buy_price FLOAT NOT NULL DEFAULT 0,
    FOREIGN KEY (portfolio_id) REFERENCES Portfolio(portfolio_id) ON DELETE CASCADE,
    FOREIGN KEY (stock_id)     REFERENCES Stock(stock_id)         ON DELETE CASCADE
) ENGINE=InnoDB;

-- -------------------------
-- 5. Transaction table
-- -------------------------
CREATE TABLE IF NOT EXISTS Transaction (
    txn_id        INT PRIMARY KEY AUTO_INCREMENT,
    portfolio_id  INT          NOT NULL,
    stock_id      INT          NOT NULL,
    type          VARCHAR(10)  NOT NULL,   -- BUY or SELL
    quantity      INT          NOT NULL,
    price         FLOAT        NOT NULL,
    timestamp     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (portfolio_id) REFERENCES Portfolio(portfolio_id) ON DELETE CASCADE,
    FOREIGN KEY (stock_id)     REFERENCES Stock(stock_id)         ON DELETE CASCADE
) ENGINE=InnoDB;

-- -------------------------
-- 6. Market_Data table (composite PK)
-- -------------------------
CREATE TABLE IF NOT EXISTS Market_Data (
    stock_id    INT   NOT NULL,
    date_id     DATE  NOT NULL,
    open_price  FLOAT NOT NULL,
    close_price FLOAT NOT NULL,
    high        FLOAT NOT NULL,
    low         FLOAT NOT NULL,
    volume      BIGINT NOT NULL,
    PRIMARY KEY (stock_id, date_id),
    FOREIGN KEY (stock_id) REFERENCES Stock(stock_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- SEED DATA
-- ============================================================

-- Stocks
INSERT INTO Stock (ticker, company_name) VALUES
('AAPL', 'Apple Inc.'),
('TSLA', 'Tesla Inc.'),
('MSFT', 'Microsoft Corporation'),
('GOOGL', 'Alphabet Inc.'),
('AMZN', 'Amazon.com Inc.');

-- Market Data (last 5 trading days for each stock)
INSERT INTO Market_Data (stock_id, date_id, open_price, close_price, high, low, volume) VALUES
-- AAPL (stock_id = 1)
(1, '2026-05-12', 188.50, 190.20, 191.00, 187.80, 54000000),
(1, '2026-05-13', 190.20, 189.10, 191.50, 188.50, 48000000),
(1, '2026-05-14', 189.10, 192.40, 193.00, 188.90, 62000000),
(1, '2026-05-15', 192.40, 191.80, 193.50, 190.50, 51000000),
(1, '2026-05-16', 191.80, 193.50, 194.20, 191.00, 58000000),
-- TSLA (stock_id = 2)
(2, '2026-05-12', 172.30, 175.60, 176.90, 171.00, 98000000),
(2, '2026-05-13', 175.60, 173.20, 176.00, 172.50, 87000000),
(2, '2026-05-14', 173.20, 178.90, 179.50, 172.80, 110000000),
(2, '2026-05-15', 178.90, 176.50, 180.00, 175.00, 95000000),
(2, '2026-05-16', 176.50, 180.30, 181.50, 175.80, 102000000),
-- MSFT (stock_id = 3)
(3, '2026-05-12', 415.00, 418.30, 420.00, 414.00, 23000000),
(3, '2026-05-13', 418.30, 416.90, 419.50, 415.50, 20000000),
(3, '2026-05-14', 416.90, 422.10, 423.00, 416.00, 27000000),
(3, '2026-05-15', 422.10, 420.50, 424.00, 419.00, 22000000),
(3, '2026-05-16', 420.50, 425.80, 426.50, 420.00, 25000000),
-- GOOGL (stock_id = 4)
(4, '2026-05-12', 174.20, 176.50, 177.00, 173.50, 30000000),
(4, '2026-05-13', 176.50, 175.10, 177.20, 174.80, 27000000),
(4, '2026-05-14', 175.10, 179.30, 180.00, 174.50, 35000000),
(4, '2026-05-15', 179.30, 178.00, 180.50, 177.00, 29000000),
(4, '2026-05-16', 178.00, 181.20, 182.00, 177.50, 32000000),
-- AMZN (stock_id = 5)
(5, '2026-05-12', 185.40, 187.90, 188.50, 184.50, 45000000),
(5, '2026-05-13', 187.90, 186.30, 188.00, 185.50, 40000000),
(5, '2026-05-14', 186.30, 190.50, 191.20, 186.00, 52000000),
(5, '2026-05-15', 190.50, 189.10, 191.50, 188.00, 43000000),
(5, '2026-05-16', 189.10, 192.80, 193.50, 188.50, 49000000);

-- Sample user (password is SHA-256 hash of "password123")
INSERT INTO User (name, email, password_hash) VALUES
('Demo User', 'demo@tracker.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f');

-- Sample portfolio for demo user
INSERT INTO Portfolio (user_id, name) VALUES
(1, 'Tech Growth Portfolio');

-- Sample holdings
INSERT INTO Holding (portfolio_id, stock_id, quantity, avg_buy_price) VALUES
(1, 1, 50, 185.00),   -- 50 shares of AAPL at avg 185
(1, 2, 30, 170.00),   -- 30 shares of TSLA at avg 170
(1, 3, 20, 410.00);   -- 20 shares of MSFT at avg 410

-- Sample transactions
INSERT INTO Transaction (portfolio_id, stock_id, type, quantity, price, timestamp) VALUES
(1, 1, 'BUY', 50, 185.00, '2026-05-10 09:30:00'),
(1, 2, 'BUY', 30, 170.00, '2026-05-10 10:00:00'),
(1, 3, 'BUY', 20, 410.00, '2026-05-11 11:15:00');

