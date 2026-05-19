package com.tracker.dao;

import com.tracker.config.DBConnection;
import com.tracker.model.Portfolio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Portfolio-related database operations.
 * Supports listing, creation, and lookup of portfolios.
 */
public class PortfolioDAO {

    /**
     * Retrieves all portfolios belonging to a specific user.
     *
     * @param userId The ID of the logged-in user
     * @return List of Portfolio objects
     */
    public List<Portfolio> getPortfoliosByUser(int userId) {
        List<Portfolio> list = new ArrayList<>();
        String sql = "SELECT * FROM Portfolio WHERE user_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Portfolio p = new Portfolio();
                    p.setPortfolioId(rs.getInt("portfolio_id"));
                    p.setUserId(rs.getInt("user_id"));
                    p.setName(rs.getString("name"));
                    list.add(p);
                }
            }

        } catch (SQLException e) {
            System.err.println("[PortfolioDAO] Error fetching portfolios: " + e.getMessage());
        }
        return list;
    }

    /**
     * Creates a new portfolio for the given user.
     *
     * @param userId The owner's user ID
     * @param name   Portfolio name
     * @return true if creation succeeds
     */
    public boolean createPortfolio(int userId, String name) {
        String sql = "INSERT INTO Portfolio (user_id, name) VALUES (?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, name);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[PortfolioDAO] Error creating portfolio: " + e.getMessage());
            return false;
        }
    }
}
