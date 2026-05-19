package com.tracker.view;

import com.tracker.dao.StockDAO;
import com.tracker.dao.TransactionDAO;
import com.tracker.model.Portfolio;
import com.tracker.model.Stock;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Modal dialog for executing BUY/SELL transactions.
 * Allows the user to select a stock, enter quantity and price,
 * and choose the transaction type.
 *
 * BUY: Creates or updates a holding with weighted average price.
 * SELL: Validates sufficient shares before executing.
 */
public class TransactionDialog extends JDialog {

    // ── Color palette ──────────────────────────────────────────
    private static final Color BG           = new Color(36, 40, 50);
    private static final Color FIELD_BG     = new Color(50, 54, 68);
    private static final Color FIELD_BORDER = new Color(70, 75, 90);
    private static final Color TEXT_PRIMARY  = new Color(230, 233, 240);
    private static final Color TEXT_SECONDARY= new Color(150, 155, 170);
    private static final Color BUY_GREEN    = new Color(60, 180, 100);
    private static final Color BUY_HOVER    = new Color(80, 200, 120);
    private static final Color SELL_RED     = new Color(220, 70, 70);
    private static final Color SELL_HOVER   = new Color(240, 90, 90);

    private JComboBox<Stock> stockCombo;
    private JTextField quantityField;
    private JTextField priceField;
    private JRadioButton buyRadio;
    private JRadioButton sellRadio;

    private final Portfolio portfolio;
    private final StockDAO stockDAO = new StockDAO();
    private final TransactionDAO txnDAO = new TransactionDAO();

    /**
     * @param parent    The parent DashboardFrame
     * @param portfolio The portfolio to trade within
     */
    public TransactionDialog(JFrame parent, Portfolio portfolio) {
        super(parent, "Execute Trade — " + portfolio.getName(), true);
        this.portfolio = portfolio;

        setSize(440, 420);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(BG);

        initComponents();
    }

    private void initComponents() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG);
        card.setBorder(new EmptyBorder(24, 32, 24, 32));

        // ── Title ──────────────────────────────────────────────
        JLabel title = new JLabel("New Transaction");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Stock selector ─────────────────────────────────────
        JLabel stockLabel = createLabel("Select Stock");
        stockCombo = new JComboBox<>();
        stockCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        stockCombo.setBackground(FIELD_BG);
        stockCombo.setForeground(TEXT_PRIMARY);
        stockCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        // Populate stocks from database
        List<Stock> stocks = stockDAO.getAllStocks();
        for (Stock s : stocks) {
            stockCombo.addItem(s);
        }

        // Auto-fill current price when stock changes
        stockCombo.addActionListener(e -> {
            Stock selected = (Stock) stockCombo.getSelectedItem();
            if (selected != null) {
                float latestPrice = stockDAO.getLatestClosePrice(selected.getStockId());
                priceField.setText(String.format("%.2f", latestPrice));
            }
        });

        // ── BUY / SELL radio ───────────────────────────────────
        JLabel typeLabel = createLabel("Transaction Type");
        buyRadio = new JRadioButton("BUY");
        sellRadio = new JRadioButton("SELL");
        ButtonGroup group = new ButtonGroup();
        group.add(buyRadio);
        group.add(sellRadio);
        buyRadio.setSelected(true);

        styleRadio(buyRadio);
        styleRadio(sellRadio);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        radioPanel.setOpaque(false);
        radioPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        radioPanel.add(buyRadio);
        radioPanel.add(sellRadio);

        // ── Quantity ───────────────────────────────────────────
        JLabel qtyLabel = createLabel("Quantity (Shares)");
        quantityField = createTextField();

        // ── Price ──────────────────────────────────────────────
        JLabel priceLabel = createLabel("Price per Share ($)");
        priceField = createTextField();

        // Pre-fill price for initially selected stock
        if (stockCombo.getItemCount() > 0) {
            Stock initial = stockCombo.getItemAt(0);
            float latestPrice = stockDAO.getLatestClosePrice(initial.getStockId());
            priceField.setText(String.format("%.2f", latestPrice));
        }

        // ── Execute button ─────────────────────────────────────
        JButton executeBtn = new JButton("Execute Trade");
        executeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        executeBtn.setForeground(Color.WHITE);
        executeBtn.setBackground(BUY_GREEN);
        executeBtn.setFocusPainted(false);
        executeBtn.setBorderPainted(false);
        executeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        executeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        executeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Change button color based on BUY/SELL selection
        buyRadio.addActionListener(e -> {
            executeBtn.setBackground(BUY_GREEN);
            executeBtn.setText("Execute BUY");
        });
        sellRadio.addActionListener(e -> {
            executeBtn.setBackground(SELL_RED);
            executeBtn.setText("Execute SELL");
        });

        executeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                executeBtn.setBackground(buyRadio.isSelected() ? BUY_HOVER : SELL_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                executeBtn.setBackground(buyRadio.isSelected() ? BUY_GREEN : SELL_RED);
            }
        });
        executeBtn.addActionListener(e -> executeTrade());

        // ── Assembly ───────────────────────────────────────────
        card.add(title);
        card.add(Box.createVerticalStrut(20));
        card.add(stockLabel);   card.add(Box.createVerticalStrut(6)); card.add(stockCombo);
        card.add(Box.createVerticalStrut(14));
        card.add(typeLabel);    card.add(Box.createVerticalStrut(6)); card.add(radioPanel);
        card.add(Box.createVerticalStrut(14));
        card.add(qtyLabel);     card.add(Box.createVerticalStrut(6)); card.add(quantityField);
        card.add(Box.createVerticalStrut(14));
        card.add(priceLabel);   card.add(Box.createVerticalStrut(6)); card.add(priceField);
        card.add(Box.createVerticalStrut(24));
        card.add(executeBtn);

        setContentPane(card);
    }

    /**
     * Validates inputs and executes the BUY or SELL transaction via TransactionDAO.
     * Displays success/error messages via JOptionPane.
     */
    private void executeTrade() {
        Stock selectedStock = (Stock) stockCombo.getSelectedItem();
        if (selectedStock == null) {
            showError("Please select a stock.");
            return;
        }

        // Parse & validate quantity
        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showError("Please enter a valid positive integer for quantity.");
            return;
        }

        // Parse & validate price
        float price;
        try {
            price = Float.parseFloat(priceField.getText().trim());
            if (price <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showError("Please enter a valid positive price.");
            return;
        }

        boolean success;
        String type;

        if (buyRadio.isSelected()) {
            type = "BUY";
            success = txnDAO.executeBuy(
                    portfolio.getPortfolioId(), selectedStock.getStockId(), quantity, price);
        } else {
            type = "SELL";
            success = txnDAO.executeSell(
                    portfolio.getPortfolioId(), selectedStock.getStockId(), quantity, price);
        }

        if (success) {
            JOptionPane.showMessageDialog(this,
                    type + " order executed successfully!\n"
                    + quantity + " shares of " + selectedStock.getTicker()
                    + " @ $" + String.format("%.2f", price),
                    "Trade Successful", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Close dialog — DashboardFrame will refresh
        } else {
            if ("SELL".equals(type)) {
                showError("SELL failed: You may not own enough shares of "
                        + selectedStock.getTicker() + " to sell " + quantity + ".");
            } else {
                showError("Transaction failed. Please check the logs for details.");
            }
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
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
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(FIELD_BG);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                new EmptyBorder(8, 12, 8, 12)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private void styleRadio(JRadioButton radio) {
        radio.setFont(new Font("Segoe UI", Font.BOLD, 13));
        radio.setForeground(TEXT_PRIMARY);
        radio.setBackground(BG);
        radio.setFocusPainted(false);
    }
}
