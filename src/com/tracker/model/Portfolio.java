package com.tracker.model;

/**
 * POJO representing a user's investment portfolio.
 * Maps to the Portfolio table in the database.
 */
public class Portfolio {

    private int portfolioId;
    private int userId;
    private String name;

    public Portfolio() {}

    public Portfolio(int portfolioId, int userId, String name) {
        this.portfolioId = portfolioId;
        this.userId = userId;
        this.name = name;
    }

    // Getters and Setters
    public int getPortfolioId() { return portfolioId; }
    public void setPortfolioId(int portfolioId) { this.portfolioId = portfolioId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return name;   // So JComboBox / JList displays the portfolio name directly
    }
}
