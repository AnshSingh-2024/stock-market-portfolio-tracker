package com.tracker.model;

import java.sql.Date;

/**
 * POJO representing a detected market anomaly on a given stock.
 * Maps to the Anomaly table in the database.
 */
public class Anomaly {

    private int anomalyId;
    private int stockId;
    private Date dateId;
    private String type;
    private int severity;

    public Anomaly() {}

    public Anomaly(int anomalyId, int stockId, Date dateId, String type, int severity) {
        this.anomalyId = anomalyId;
        this.stockId = stockId;
        this.dateId = dateId;
        this.type = type;
        this.severity = severity;
    }

    // Getters and Setters
    public int getAnomalyId() { return anomalyId; }
    public void setAnomalyId(int anomalyId) { this.anomalyId = anomalyId; }

    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }

    public Date getDateId() { return dateId; }
    public void setDateId(Date dateId) { this.dateId = dateId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getSeverity() { return severity; }
    public void setSeverity(int severity) { this.severity = severity; }

    @Override
    public String toString() {
        return "Anomaly{type='" + type + "', severity=" + severity + ", date=" + dateId + "}";
    }
}
