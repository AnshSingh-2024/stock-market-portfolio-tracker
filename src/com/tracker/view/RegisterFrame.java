package com.tracker.view;

import com.tracker.dao.UserDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Registration window for new user sign-up.
 * Collects name, email, and password, validates inputs, and creates the account.
 */
public class RegisterFrame extends JFrame {

    // ── Color palette (matches LoginFrame) ─────────────────────
    private static final Color BG_DARK       = new Color(30, 33, 40);
    private static final Color PANEL_BG      = new Color(40, 44, 55);
    private static final Color ACCENT        = new Color(80, 140, 255);
    private static final Color ACCENT_HOVER  = new Color(100, 160, 255);
    private static final Color TEXT_PRIMARY   = new Color(230, 233, 240);
    private static final Color TEXT_SECONDARY = new Color(150, 155, 170);
    private static final Color FIELD_BG      = new Color(50, 54, 68);
    private static final Color FIELD_BORDER  = new Color(70, 75, 90);
    private static final Color SUCCESS_GREEN = new Color(80, 200, 120);
    private static final Color ERROR_RED     = new Color(255, 90, 90);

    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JButton registerButton;
    private JLabel statusLabel;

    private final LoginFrame parentLogin;
    private final UserDAO userDAO = new UserDAO();

    /**
     * @param parentLogin Reference to the login frame so we can re-show it on cancel/success.
     */
    public RegisterFrame(LoginFrame parentLogin) {
        this.parentLogin = parentLogin;

        setTitle("Stock Tracker — Register");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(460, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG_DARK);

        // When the user closes this window, show the login frame again
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                parentLogin.setVisible(true);
            }
        });

        initComponents();
    }

    private void initComponents() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER, 1),
                new EmptyBorder(36, 40, 36, 40)
        ));
        card.setMaximumSize(new Dimension(380, 540));

        // Title
        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Join the portfolio tracker");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Fields
        nameField     = createTextField();
        emailField    = createTextField();
        passwordField = new JPasswordField(); styleField(passwordField);
        confirmField  = new JPasswordField(); styleField(confirmField);

        // Register button
        registerButton = new JButton("Create Account");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerButton.setForeground(Color.WHITE);
        registerButton.setBackground(ACCENT);
        registerButton.setFocusPainted(false);
        registerButton.setBorderPainted(false);
        registerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { registerButton.setBackground(ACCENT_HOVER); }
            public void mouseExited(MouseEvent e)  { registerButton.setBackground(ACCENT); }
        });
        registerButton.addActionListener(e -> handleRegister());

        // Back to login link
        JButton backButton = new JButton("Already have an account? Sign In");
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        backButton.setForeground(ACCENT);
        backButton.setBackground(PANEL_BG);
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> {
            parentLogin.setVisible(true);
            dispose();
        });

        // Status
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(ERROR_RED);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Assembly
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(24));
        card.add(createLabel("Full Name"));   card.add(Box.createVerticalStrut(6)); card.add(nameField);
        card.add(Box.createVerticalStrut(14));
        card.add(createLabel("Email Address")); card.add(Box.createVerticalStrut(6)); card.add(emailField);
        card.add(Box.createVerticalStrut(14));
        card.add(createLabel("Password"));     card.add(Box.createVerticalStrut(6)); card.add(passwordField);
        card.add(Box.createVerticalStrut(14));
        card.add(createLabel("Confirm Password")); card.add(Box.createVerticalStrut(6)); card.add(confirmField);
        card.add(Box.createVerticalStrut(24));
        card.add(registerButton);
        card.add(Box.createVerticalStrut(10));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(backButton);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BG_DARK);
        wrapper.add(card);
        setContentPane(wrapper);
    }

    /**
     * Validates all fields and registers the user via UserDAO.
     */
    private void handleRegister() {
        String name    = nameField.getText().trim();
        String email   = emailField.getText().trim();
        String pass    = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        // Validation
        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            showError("All fields are required.");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showError("Please enter a valid email address.");
            return;
        }
        if (pass.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }
        if (!pass.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }

        boolean success = userDAO.registerUser(name, email, pass);

        if (success) {
            statusLabel.setForeground(SUCCESS_GREEN);
            statusLabel.setText("Account created! Redirecting to login...");
            Timer timer = new Timer(1200, evt -> {
                parentLogin.setVisible(true);
                dispose();
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            showError("Registration failed. Email may already be in use.");
        }
    }

    private void showError(String msg) {
        statusLabel.setForeground(ERROR_RED);
        statusLabel.setText(msg);
    }

    // ── UI helpers ─────────────────────────────────────────────
    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
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
