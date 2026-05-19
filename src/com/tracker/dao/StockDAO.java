package com.tracker.dao;

import com.tracker.config.DBConnection;
import com.tracker.model.Stock;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Stock-related database operations.
 * Provides methods to list all stocks and retrieve by ID or ticker.
 */
public class StockDAO {

    /**
     * Fetches all available stocks from the Stock table.
     *
     * @return List of all Stock objects
     */
    public List<Stock> getAllStocks() {
        List<Stock> list = new ArrayList<>();
        String sql = "SELECT * FROM Stock ORDER BY ticker";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Stock s = new Stock();
                s.setStockId(rs.getInt("stock_id"));
                s.setTicker(rs.getString("ticker"));
                s.setCompanyName(rs.getString("company_name"));
                list.add(s);
            }

        } catch (SQLException e) {
            System.err.println("[StockDAO] Error fetching stocks: " + e.getMessage());
        }
        return list;
    }

    /**
     * Fetches the latest close price for a given stock from Market_Data.
     * Uses ORDER BY date_id DESC LIMIT 1 to get the most recent record.
     *
     * @param stockId The stock's primary key
     * @return The latest close price, or 0.0f if not found
     */
    public float getLatestClosePrice(int stockId) {
        String sql = "SELECT close_price FROM Market_Data WHERE stock_id = ? ORDER BY date_id DESC LIMIT 1";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, stockId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getFloat("close_price");
                }
            }

        } catch (SQLException e) {
            System.err.println("[StockDAO] Error fetching price: " + e.getMessage());
        }
        return 0.0f;
    }
}
