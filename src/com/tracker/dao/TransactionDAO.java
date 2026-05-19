package com.tracker.dao;

import com.tracker.config.DBConnection;
import com.tracker.model.Holding;
import com.tracker.model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Transaction-related database operations.
 * Implements BUY and SELL transaction logic with holding management.
 */
public class TransactionDAO {

    private final HoldingDAO holdingDAO = new HoldingDAO();

    /**
     * Executes a BUY transaction:
     * 1. Logs the transaction in the Transaction table.
     * 2. If the user already holds this stock in the portfolio,
     *    updates the quantity and recalculates the weighted average buy price.
     * 3. If not, creates a new Holding entry.
     *
     * @param portfolioId Portfolio to buy into
     * @param stockId     Stock to purchase
     * @param quantity    Number of shares
     * @param price       Price per share
     * @return true if the buy succeeds
     */
    public boolean executeBuy(int portfolioId, int stockId, int quantity, float price) {
        // Step 1: Log the transaction
        if (!logTransaction(portfolioId, stockId, "BUY", quantity, price)) {
            return false;
        }

        // Step 2: Update or create the holding
        Holding existing = holdingDAO.findHolding(portfolioId, stockId);

        if (existing != null) {
            // Recalculate weighted average price:
            // newAvg = (oldQty * oldAvg + newQty * newPrice) / (oldQty + newQty)
            int totalQty = existing.getQuantity() + quantity;
            float newAvg = ((existing.getQuantity() * existing.getAvgBuyPrice())
                         + (quantity * price)) / totalQty;
            return holdingDAO.updateHolding(existing.getHoldingId(), totalQty, newAvg);
        } else {
            return holdingDAO.insertHolding(portfolioId, stockId, quantity, price);
        }
    }

    /**
     * Executes a SELL transaction:
     * 1. Validates that the user holds enough shares.
     * 2. Logs the transaction in the Transaction table.
     * 3. Decreases the holding quantity (or deletes the holding if qty reaches 0).
     *
     * @return true if the sell succeeds, false if insufficient shares or error
     */
    public boolean executeSell(int portfolioId, int stockId, int quantity, float price) {
        // Step 1: Validate sufficient holding
        Holding existing = holdingDAO.findHolding(portfolioId, stockId);

        if (existing == null || existing.getQuantity() < quantity) {
            System.err.println("[TransactionDAO] Insufficient shares to sell.");
            return false;
        }

        // Step 2: Log the transaction
        if (!logTransaction(portfolioId, stockId, "SELL", quantity, price)) {
            return false;
        }

        // Step 3: Update the holding
        int remainingQty = existing.getQuantity() - quantity;
        if (remainingQty == 0) {
            return holdingDAO.deleteHolding(existing.getHoldingId());
        } else {
            // Average buy price remains unchanged after selling
            return holdingDAO.updateHolding(existing.getHoldingId(), remainingQty, existing.getAvgBuyPrice());
        }
    }

    /**
     * Retrieves recent transactions for a given portfolio, ordered newest-first.
     * Joins with Stock table to include ticker symbols.
     *
     * @param portfolioId Portfolio to query
     * @return List of Transaction objects (most recent 50)
     */
    public List<Transaction> getRecentTransactions(int portfolioId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, s.ticker FROM Transaction t "
                   + "JOIN Stock s ON t.stock_id = s.stock_id "
                   + "WHERE t.portfolio_id = ? "
                   + "ORDER BY t.timestamp DESC LIMIT 50";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, portfolioId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction txn = new Transaction();
                    txn.setTxnId(rs.getInt("txn_id"));
                    txn.setPortfolioId(rs.getInt("portfolio_id"));
                    txn.setStockId(rs.getInt("stock_id"));
                    txn.setType(rs.getString("type"));
                    txn.setQuantity(rs.getInt("quantity"));
                    txn.setPrice(rs.getFloat("price"));
                    txn.setTimestamp(rs.getTimestamp("timestamp"));
                    txn.setTicker(rs.getString("ticker"));
                    list.add(txn);
                }
            }

        } catch (SQLException e) {
            System.err.println("[TransactionDAO] Error fetching transactions: " + e.getMessage());
        }
        return list;
    }

    /**
     * Internal helper to insert a record into the Transaction table.
     */
    private boolean logTransaction(int portfolioId, int stockId,
                                   String type, int quantity, float price) {
        String sql = "INSERT INTO Transaction (portfolio_id, stock_id, type, quantity, price, timestamp) "
                   + "VALUES (?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, portfolioId);
            ps.setInt(2, stockId);
            ps.setString(3, type);
            ps.setInt(4, quantity);
            ps.setFloat(5, price);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[TransactionDAO] Error logging transaction: " + e.getMessage());
            return false;
        }
    }
}
