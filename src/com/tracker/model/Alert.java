package com.tracker.model;

import java.sql.Date;

/**
 * POJO representing a user-facing security alert generated from an anomaly.
 * Maps to the Alert table (joined with Anomaly and Stock for display).
 */
public class Alert {

    private int alertId;
    private int anomalyId;
    private String message;

    // Display-only fields populated via JOINs
    private String ticker;
    private String anomalyType;
    private int severity;
    private Date dateId;

    public Alert() {}

    public Alert(int alertId, int anomalyId, String message) {
        this.alertId = alertId;
        this.anomalyId = anomalyId;
        this.message = message;
    }

    // Getters and Setters
    public int getAlertId() { return alertId; }
    public void setAlertId(int alertId) { this.alertId = alertId; }

    public int getAnomalyId() { return anomalyId; }
    public void setAnomalyId(int anomalyId) { this.anomalyId = anomalyId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public String getAnomalyType() { return anomalyType; }
    public void setAnomalyType(String anomalyType) { this.anomalyType = anomalyType; }

    public int getSeverity() { return severity; }
    public void setSeverity(int severity) { this.severity = severity; }

    public Date getDateId() { return dateId; }
    public void setDateId(Date dateId) { this.dateId = dateId; }

    @Override
    public String toString() {
        return "Alert{ticker='" + ticker + "', type='" + anomalyType + "', severity=" + severity + "}";
    }
}
