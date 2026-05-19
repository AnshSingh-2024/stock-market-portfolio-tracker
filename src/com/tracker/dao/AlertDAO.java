package com.tracker.dao;

import com.tracker.config.DBConnection;
import com.tracker.model.Alert;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Alert-related database operations.
 * Fetches anomaly-driven alerts for stocks present in a user's holdings.
 */
public class AlertDAO {

    /**
     * Retrieves all alerts for stocks that are currently held in a specific portfolio.
     * Performs a multi-table JOIN: Alert → Anomaly → Stock → Holding
     * This ensures only relevant alerts (for owned stocks) are shown.
     *
     * @param portfolioId The portfolio whose stock alerts to fetch
     * @return List of Alert objects enriched with anomaly details
     */
    public List<Alert> getAlertsForPortfolio(int portfolioId) {
        List<Alert> list = new ArrayList<>();
        String sql = "SELECT al.alert_id, al.anomaly_id, al.message, "
                   + "       s.ticker, an.type AS anomaly_type, an.severity, an.date_id "
                   + "FROM Alert al "
                   + "JOIN Anomaly an ON al.anomaly_id = an.anomaly_id "
                   + "JOIN Stock s    ON an.stock_id   = s.stock_id "
                   + "JOIN Holding h  ON h.stock_id    = s.stock_id "
                   + "WHERE h.portfolio_id = ? "
                   + "ORDER BY an.severity DESC, an.date_id DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, portfolioId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Alert a = new Alert();
                    a.setAlertId(rs.getInt("alert_id"));
                    a.setAnomalyId(rs.getInt("anomaly_id"));
                    a.setMessage(rs.getString("message"));
                    a.setTicker(rs.getString("ticker"));
                    a.setAnomalyType(rs.getString("anomaly_type"));
                    a.setSeverity(rs.getInt("severity"));
                    a.setDateId(rs.getDate("date_id"));
                    list.add(a);
                }
            }

        } catch (SQLException e) {
            System.err.println("[AlertDAO] Error fetching alerts: " + e.getMessage());
        }
        return list;
    }
}
