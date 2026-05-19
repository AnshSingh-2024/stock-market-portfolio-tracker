package com.tracker.model;

import java.sql.Timestamp;

/**
 * POJO representing a BUY/SELL transaction record.
 * Maps to the Transaction table in the database.
 */
public class Transaction {

    private int txnId;
    private int portfolioId;
    private int stockId;
    private String type;       // "BUY" or "SELL"
    private int quantity;
    private float price;
    private Timestamp timestamp;

    // Display-only helper field
    private String ticker;

    public Transaction() {}

    public Transaction(int txnId, int portfolioId, int stockId,
                       String type, int quantity, float price, Timestamp timestamp) {
        this.txnId = txnId;
        this.portfolioId = portfolioId;
        this.stockId = stockId;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getTxnId() { return txnId; }
    public void setTxnId(int txnId) { this.txnId = txnId; }

    public int getPortfolioId() { return portfolioId; }
    public void setPortfolioId(int portfolioId) { this.portfolioId = portfolioId; }

    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public float getPrice() { return price; }
    public void setPrice(float price) { this.price = price; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    @Override
    public String toString() {
        return "Txn{" + type + " " + quantity + " x " + ticker + " @ " + price + "}";
    }
}
