package com.tracker.view;

import com.tracker.dao.UserDAO;
import com.tracker.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Login window — the first screen the user sees.
 * Allows authentication via email/password and navigation to registration.
 * Uses a modern flat color palette with structured layout managers.
 */
public class LoginFrame extends JFrame {

    // ── Color palette ──────────────────────────────────────────
    private static final Color BG_DARK      = new Color(30, 33, 40);
    private static final Color PANEL_BG     = new Color(40, 44, 55);
    private static final Color ACCENT       = new Color(80, 140, 255);
    private static final Color ACCENT_HOVER = new Color(100, 160, 255);
    private static final Color TEXT_PRIMARY  = new Color(230, 233, 240);
    private static final Color TEXT_SECONDARY= new Color(150, 155, 170);
    private static final Color FIELD_BG     = new Color(50, 54, 68);
    private static final Color FIELD_BORDER = new Color(70, 75, 90);
    private static final Color ERROR_RED    = new Color(255, 90, 90);

    // ── Components ─────────────────────────────────────────────
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;

    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("Stock Tracker — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 540);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG_DARK);

        initComponents();
    }

    private void initComponents() {
        // ── Main card panel ────────────────────────────────────
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER, 1),
                new EmptyBorder(40, 40, 40, 40)
        ));
        card.setMaximumSize(new Dimension(380, 440));

        // ── Title ──────────────────────────────────────────────
        JLabel icon = new JLabel("\u2603");  // decorative
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        icon.setForeground(ACCENT);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Portfolio Tracker");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to manage your portfolios");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Email field ────────────────────────────────────────
        JLabel emailLabel = createLabel("Email Address");
        emailField = createTextField();

        // ── Password field ─────────────────────────────────────
        JLabel passLabel = createLabel("Password");
        passwordField = new JPasswordField();
        styleField(passwordField);

        // ── Login button ───────────────────────────────────────
        loginButton = new JButton("Sign In");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(ACCENT);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { loginButton.setBackground(ACCENT_HOVER); }
            public void mouseExited(MouseEvent e)  { loginButton.setBackground(ACCENT); }
        });
        loginButton.addActionListener(e -> handleLogin());

        // ── Register link ──────────────────────────────────────
        registerButton = new JButton("Don't have an account? Register");
        registerButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        registerButton.setForeground(ACCENT);
        registerButton.setBackground(PANEL_BG);
        registerButton.setBorderPainted(false);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.addActionListener(e -> openRegister());

        // ── Status label ───────────────────────────────────────
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(ERROR_RED);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Assembly ───────────────────────────────────────────
        card.add(icon);
        card.add(Box.createVerticalStrut(8));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(28));
        card.add(emailLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(emailField);
        card.add(Box.createVerticalStrut(16));
        card.add(passLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(24));
        card.add(loginButton);
        card.add(Box.createVerticalStrut(12));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(registerButton);

        // ── Center the card on screen ──────────────────────────
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BG_DARK);
        wrapper.add(card);

        setContentPane(wrapper);
    }

    // ── Event handlers ─────────────────────────────────────────

    /**
     * Validates input and authenticates the user via UserDAO.
     * On success, opens the DashboardFrame and disposes of the login window.
     */
    private void handleLogin() {
        String email = emailField.getText().trim();
        String pass  = new String(passwordField.getPassword());

        if (email.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            return;
        }

        User user = userDAO.authenticate(email, pass);

        if (user != null) {
            statusLabel.setForeground(new Color(80, 200, 120));
            statusLabel.setText("Welcome, " + user.getName() + "!");
            // Open dashboard after a brief delay for visual feedback
            Timer timer = new Timer(500, evt -> {
                new DashboardFrame(user).setVisible(true);
                dispose();
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            statusLabel.setForeground(ERROR_RED);
            statusLabel.setText("Invalid email or password.");
        }
    }

    /**
     * Opens the registration window and hides the login frame.
     */
    private void openRegister() {
        new RegisterFrame(this).setVisible(true);
        setVisible(false);
    }

    // ── UI helpers ─────────────────────────────────────────────

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        styleField(field);
        return field;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(FIELD_BG);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                new EmptyBorder(8, 12, 8, 12)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    }
}
