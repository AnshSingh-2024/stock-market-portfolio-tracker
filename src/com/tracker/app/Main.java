package com.tracker.app;

import com.tracker.view.LoginFrame;

import javax.swing.*;

/**
 * Main entry point for the Stock Market Portfolio Tracker application.
 * Sets the system look-and-feel and launches the Login frame on the EDT.
 */
public class Main {
    public static void main(String[] args) {
        // Set cross-platform look and feel for consistency
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set Look and Feel: " + e.getMessage());
        }

        // Launch the UI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
