package com.tracker.dao;

import com.tracker.config.DBConnection;
import com.tracker.model.Holding;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Holding-related database operations.
 * Manages portfolio holdings — fetching, inserting, updating, and deleting.
 */
public class HoldingDAO {

    /**
     * Fetches all holdings for a given portfolio, enriched with stock ticker,
     * company name, and the latest close price from Market_Data.
     *
     * @param portfolioId The portfolio to query
     * @return List of enriched Holding objects
     */
    public List<Holding> getHoldingsByPortfolio(int portfolioId) {
        List<Holding> list = new ArrayList<>();
        String sql = "SELECT h.*, s.ticker, s.company_name, "
                   + "(SELECT md.close_price FROM Market_Data md "
                   + "  WHERE md.stock_id = h.stock_id ORDER BY md.date_id DESC LIMIT 1) AS current_price "
                   + "FROM Holding h "
                   + "JOIN Stock s ON h.stock_id = s.stock_id "
                   + "WHERE h.portfolio_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, portfolioId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Holding h = new Holding();
                    h.setHoldingId(rs.getInt("holding_id"));
                    h.setPortfolioId(rs.getInt("portfolio_id"));
                    h.setStockId(rs.getInt("stock_id"));
                    h.setQuantity(rs.getInt("quantity"));
                    h.setAvgBuyPrice(rs.getFloat("avg_buy_price"));
                    h.setTicker(rs.getString("ticker"));
                    h.setCompanyName(rs.getString("company_name"));
                    h.setCurrentPrice(rs.getFloat("current_price"));
                    list.add(h);
                }
            }

        } catch (SQLException e) {
            System.err.println("[HoldingDAO] Error fetching holdings: " + e.getMessage());
        }
        return list;
    }

    /**
     * Finds an existing holding for a given portfolio and stock combination.
     *
     * @return The Holding if found, null otherwise
     */
    public Holding findHolding(int portfolioId, int stockId) {
        String sql = "SELECT * FROM Holding WHERE portfolio_id = ? AND stock_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, portfolioId);
            ps.setInt(2, stockId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Holding h = new Holding();
                    h.setHoldingId(rs.getInt("holding_id"));
                    h.setPortfolioId(rs.getInt("portfolio_id"));
                    h.setStockId(rs.getInt("stock_id"));
                    h.setQuantity(rs.getInt("quantity"));
                    h.setAvgBuyPrice(rs.getFloat("avg_buy_price"));
                    return h;
                }
            }

        } catch (SQLException e) {
            System.err.println("[HoldingDAO] Error finding holding: " + e.getMessage());
        }
        return null;
    }

    /**
     * Inserts a new holding record into the database.
     */
    public boolean insertHolding(int portfolioId, int stockId, int quantity, float avgPrice) {
        String sql = "INSERT INTO Holding (portfolio_id, stock_id, quantity, avg_buy_price) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, portfolioId);
            ps.setInt(2, stockId);
            ps.setInt(3, quantity);
            ps.setFloat(4, avgPrice);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[HoldingDAO] Error inserting holding: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates an existing holding's quantity and average buy price.
     */
    public boolean updateHolding(int holdingId, int newQuantity, float newAvgPrice) {
        String sql = "UPDATE Holding SET quantity = ?, avg_buy_price = ? WHERE holding_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newQuantity);
            ps.setFloat(2, newAvgPrice);
            ps.setInt(3, holdingId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[HoldingDAO] Error updating holding: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a holding record (used when quantity reaches zero after selling).
     */
    public boolean deleteHolding(int holdingId) {
        String sql = "DELETE FROM Holding WHERE holding_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, holdingId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[HoldingDAO] Error deleting holding: " + e.getMessage());
            return false;
        }
    }
}
