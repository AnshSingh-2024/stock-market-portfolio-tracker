package com.tracker.model;

/**
 * POJO representing a stock / listed security.
 * Maps to the Stock table in the database.
 */
public class Stock {

    private int stockId;
    private String ticker;
    private String companyName;

    public Stock() {}

    public Stock(int stockId, String ticker, String companyName) {
        this.stockId = stockId;
        this.ticker = ticker;
        this.companyName = companyName;
    }

    // Getters and Setters
    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    @Override
    public String toString() {
        return ticker + " — " + companyName;
    }
}
