package com.tracker.model;

/**
 * POJO representing a stock holding within a portfolio.
 * Maps to the Holding table in the database.
 */
public class Holding {

    private int holdingId;
    private int portfolioId;
    private int stockId;
    private int quantity;
    private float avgBuyPrice;

    // Additional display-only fields (not stored in the Holding table)
    private String ticker;
    private String companyName;
    private float currentPrice;   // Latest close_price from Market_Data

    public Holding() {}

    public Holding(int holdingId, int portfolioId, int stockId, int quantity, float avgBuyPrice) {
        this.holdingId = holdingId;
        this.portfolioId = portfolioId;
        this.stockId = stockId;
        this.quantity = quantity;
        this.avgBuyPrice = avgBuyPrice;
    }

    // Getters and Setters
    public int getHoldingId() { return holdingId; }
    public void setHoldingId(int holdingId) { this.holdingId = holdingId; }

    public int getPortfolioId() { return portfolioId; }
    public void setPortfolioId(int portfolioId) { this.portfolioId = portfolioId; }

    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public float getAvgBuyPrice() { return avgBuyPrice; }
    public void setAvgBuyPrice(float avgBuyPrice) { this.avgBuyPrice = avgBuyPrice; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public float getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(float currentPrice) { this.currentPrice = currentPrice; }

    /**
     * Calculates the total profit/loss for this holding.
     * P/L = (currentPrice - avgBuyPrice) * quantity
     */
    public float getProfitLoss() {
        return (currentPrice - avgBuyPrice) * quantity;
    }

    @Override
    public String toString() {
        return "Holding{ticker='" + ticker + "', qty=" + quantity + ", avgPrice=" + avgBuyPrice + "}";
    }
}
