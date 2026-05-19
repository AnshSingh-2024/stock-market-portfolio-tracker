package com.tracker.view;

import com.tracker.dao.*;
import com.tracker.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * Main dashboard displayed after successful login.
 * Contains four major sections:
 *   1. Portfolio selector (left sidebar)
 *   2. Holdings table (center)
 *   3. Recent transactions table (center-bottom)
 *   4. Anomaly alert hub (right panel)
 *
 * Uses BorderLayout and nested panels with proper layout managers — no null layouts.
 */
public class DashboardFrame extends JFrame {

    // ── Color palette ──────────────────────────────────────────
    private static final Color BG_DARK       = new Color(24, 27, 33);
    private static final Color SIDEBAR_BG    = new Color(30, 34, 42);
    private static final Color PANEL_BG      = new Color(36, 40, 50);
    private static final Color HEADER_BG     = new Color(20, 22, 28);
    private static final Color ACCENT        = new Color(80, 140, 255);
    private static final Color ACCENT_HOVER  = new Color(100, 160, 255);
    private static final Color TEXT_PRIMARY   = new Color(230, 233, 240);
    private static final Color TEXT_SECONDARY = new Color(150, 155, 170);
    private static final Color PROFIT_GREEN  = new Color(80, 200, 120);
    private static final Color LOSS_RED      = new Color(255, 90, 90);
    private static final Color TABLE_ROW_ALT = new Color(40, 44, 56);
    private static final Color TABLE_ROW     = new Color(34, 38, 48);
    private static final Color BORDER_COLOR  = new Color(50, 55, 68);
    private static final Color ALERT_HIGH    = new Color(255, 80, 80);
    private static final Color ALERT_MED     = new Color(255, 180, 60);
    private static final Color ALERT_LOW     = new Color(80, 200, 120);

    // ── Session ────────────────────────────────────────────────
    private final User currentUser;
    private Portfolio selectedPortfolio;

    // ── DAOs ───────────────────────────────────────────────────
    private final PortfolioDAO portfolioDAO   = new PortfolioDAO();
    private final HoldingDAO holdingDAO       = new HoldingDAO();
    private final TransactionDAO txnDAO       = new TransactionDAO();
    private final AlertDAO alertDAO           = new AlertDAO();

    // ── UI Components ──────────────────────────────────────────
    private DefaultListModel<Portfolio> portfolioListModel;
    private JList<Portfolio> portfolioList;
    private DefaultTableModel holdingsTableModel;
    private JTable holdingsTable;
    private DefaultTableModel txnTableModel;
    private JTable txnTable;
    private JPanel alertsPanel;
    private JLabel totalValueLabel;
    private JLabel totalPLLabel;

    public DashboardFrame(User user) {
        this.currentUser = user;

        setTitle("Stock Tracker — Dashboard (" + user.getName() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);

        initComponents();
        loadPortfolios();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // ─── TOP HEADER BAR ────────────────────────────────────
        add(createHeaderBar(), BorderLayout.NORTH);

        // ─── LEFT SIDEBAR — Portfolio list ─────────────────────
        add(createSidebar(), BorderLayout.WEST);

        // ─── CENTER — Holdings + Transactions ──────────────────
        add(createCenterPanel(), BorderLayout.CENTER);

        // ─── RIGHT — Alert Hub ─────────────────────────────────
        add(createAlertPanel(), BorderLayout.EAST);
    }

    // ================================================================
    //  HEADER BAR
    // ================================================================
    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Left: App title
        JLabel appTitle = new JLabel("\u25C8  Stock Portfolio Tracker");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        appTitle.setForeground(TEXT_PRIMARY);
        header.add(appTitle, BorderLayout.WEST);

        // Right: User info + Logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);

        JLabel userLabel = new JLabel("Signed in as " + currentUser.getName());
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userLabel.setForeground(TEXT_SECONDARY);
        rightPanel.add(userLabel);

        JButton logoutBtn = createStyledButton("Logout", new Color(200, 60, 60), new Color(230, 80, 80));
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        rightPanel.add(logoutBtn);

        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    // ================================================================
    //  SIDEBAR — Portfolio list
    // ================================================================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        // Title
        JLabel sideTitle = new JLabel("  My Portfolios");
        sideTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sideTitle.setForeground(TEXT_PRIMARY);
        sideTitle.setBorder(new EmptyBorder(14, 10, 10, 10));
        sidebar.add(sideTitle, BorderLayout.NORTH);

        // Portfolio list
        portfolioListModel = new DefaultListModel<>();
        portfolioList = new JList<>(portfolioListModel);
        portfolioList.setBackground(SIDEBAR_BG);
        portfolioList.setForeground(TEXT_PRIMARY);
        portfolioList.setSelectionBackground(ACCENT);
        portfolioList.setSelectionForeground(Color.WHITE);
        portfolioList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        portfolioList.setFixedCellHeight(38);
        portfolioList.setBorder(new EmptyBorder(4, 10, 4, 10));
        portfolioList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedPortfolio = portfolioList.getSelectedValue();
                if (selectedPortfolio != null) {
                    refreshDashboard();
                }
            }
        });

        JScrollPane listScroll = new JScrollPane(portfolioList);
        listScroll.setBorder(BorderFactory.createEmptyBorder());
        listScroll.getViewport().setBackground(SIDEBAR_BG);
        sidebar.add(listScroll, BorderLayout.CENTER);

        // "New Portfolio" button at bottom
        JButton newPortfolioBtn = createStyledButton("+ New Portfolio", ACCENT, ACCENT_HOVER);
        newPortfolioBtn.addActionListener(e -> createNewPortfolio());
        JPanel btnWrap = new JPanel(new BorderLayout());
        btnWrap.setBackground(SIDEBAR_BG);
        btnWrap.setBorder(new EmptyBorder(8, 12, 12, 12));
        btnWrap.add(newPortfolioBtn, BorderLayout.CENTER);
        sidebar.add(btnWrap, BorderLayout.SOUTH);

        return sidebar;
    }

    // ================================================================
    //  CENTER — Holdings table + Transaction table
    // ================================================================
    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(0, 2));
        center.setBackground(BG_DARK);

        // ─── Summary strip ─────────────────────────────────────
        JPanel summaryStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 8));
        summaryStrip.setBackground(PANEL_BG);
        summaryStrip.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        totalValueLabel = new JLabel("Total Value: —");
        totalValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalValueLabel.setForeground(TEXT_PRIMARY);

        totalPLLabel = new JLabel("Total P/L: —");
        totalPLLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalPLLabel.setForeground(TEXT_SECONDARY);

        JButton tradeBtn = createStyledButton("New Trade", ACCENT, ACCENT_HOVER);
        tradeBtn.addActionListener(e -> openTradeDialog());

        JButton refreshBtn = createStyledButton("Refresh", new Color(60, 65, 80), new Color(75, 80, 95));
        refreshBtn.addActionListener(e -> refreshDashboard());

        summaryStrip.add(totalValueLabel);
        summaryStrip.add(totalPLLabel);
        summaryStrip.add(tradeBtn);
        summaryStrip.add(refreshBtn);
        center.add(summaryStrip, BorderLayout.NORTH);

        // ─── Split: Holdings (top) / Transactions (bottom) ────
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerSize(4);
        splitPane.setBackground(BG_DARK);
        splitPane.setBorder(null);

        splitPane.setTopComponent(createHoldingsPanel());
        splitPane.setBottomComponent(createTransactionsPanel());

        center.add(splitPane, BorderLayout.CENTER);
        return center;
    }

    /** Holdings table panel */
    private JPanel createHoldingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG);

        JLabel label = new JLabel("  Holdings");
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        label.setBorder(new EmptyBorder(10, 8, 6, 0));
        panel.add(label, BorderLayout.NORTH);

        String[] cols = {"Ticker", "Company", "Qty", "Avg Buy Price", "Current Price", "P/L", "P/L %"};
        holdingsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        holdingsTable = createStyledTable(holdingsTableModel);
        panel.add(new JScrollPane(holdingsTable) {{
            setBorder(null);
            getViewport().setBackground(TABLE_ROW);
        }}, BorderLayout.CENTER);

        return panel;
    }

    /** Transactions table panel */
    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG);

        JLabel label = new JLabel("  Recent Transactions");
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        label.setBorder(new EmptyBorder(10, 8, 6, 0));
        panel.add(label, BorderLayout.NORTH);

        String[] cols = {"ID", "Ticker", "Type", "Qty", "Price", "Timestamp"};
        txnTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        txnTable = createStyledTable(txnTableModel);
        panel.add(new JScrollPane(txnTable) {{
            setBorder(null);
            getViewport().setBackground(TABLE_ROW);
        }}, BorderLayout.CENTER);

        return panel;
    }

    // ================================================================
    //  RIGHT — Alert Hub
    // ================================================================
    private JPanel createAlertPanel() {
        JPanel alertOuter = new JPanel(new BorderLayout());
        alertOuter.setPreferredSize(new Dimension(310, 0));
        alertOuter.setBackground(PANEL_BG);
        alertOuter.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER_COLOR));

        JLabel title = new JLabel("  \u26A0  Anomaly Alert Hub");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(14, 8, 10, 8));
        alertOuter.add(title, BorderLayout.NORTH);

        alertsPanel = new JPanel();
        alertsPanel.setLayout(new BoxLayout(alertsPanel, BoxLayout.Y_AXIS));
        alertsPanel.setBackground(PANEL_BG);

        JScrollPane scroll = new JScrollPane(alertsPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(PANEL_BG);
        alertOuter.add(scroll, BorderLayout.CENTER);

        return alertOuter;
    }

    // ================================================================
    //  DATA LOADING
    // ================================================================

    /** Load portfolios into the sidebar list. */
    private void loadPortfolios() {
        portfolioListModel.clear();
        List<Portfolio> portfolios = portfolioDAO.getPortfoliosByUser(currentUser.getUserId());
        for (Portfolio p : portfolios) {
            portfolioListModel.addElement(p);
        }
        if (!portfolios.isEmpty()) {
            portfolioList.setSelectedIndex(0);
        }
    }

    /** Refresh holdings, transactions, and alerts for the selected portfolio. */
    private void refreshDashboard() {
        if (selectedPortfolio == null) return;
        loadHoldings();
        loadTransactions();
        loadAlerts();
    }

    /** Populate the holdings table and compute totals. */
    private void loadHoldings() {
        holdingsTableModel.setRowCount(0);
        List<Holding> holdings = holdingDAO.getHoldingsByPortfolio(selectedPortfolio.getPortfolioId());

        float totalValue = 0;
        float totalPL = 0;

        for (Holding h : holdings) {
            float pl = h.getProfitLoss();
            float plPercent = h.getAvgBuyPrice() != 0
                    ? ((h.getCurrentPrice() - h.getAvgBuyPrice()) / h.getAvgBuyPrice()) * 100
                    : 0;

            holdingsTableModel.addRow(new Object[]{
                    h.getTicker(),
                    h.getCompanyName(),
                    h.getQuantity(),
                    String.format("$%.2f", h.getAvgBuyPrice()),
                    String.format("$%.2f", h.getCurrentPrice()),
                    String.format("%s$%.2f", pl >= 0 ? "+" : "", pl),
                    String.format("%s%.1f%%", plPercent >= 0 ? "+" : "", plPercent)
            });

            totalValue += h.getCurrentPrice() * h.getQuantity();
            totalPL += pl;
        }

        totalValueLabel.setText(String.format("Total Value: $%,.2f", totalValue));
        totalPLLabel.setText(String.format("Total P/L: %s$%,.2f",
                totalPL >= 0 ? "+" : "", totalPL));
        totalPLLabel.setForeground(totalPL >= 0 ? PROFIT_GREEN : LOSS_RED);
    }

    /** Populate the transactions table. */
    private void loadTransactions() {
        txnTableModel.setRowCount(0);
        List<Transaction> txns = txnDAO.getRecentTransactions(selectedPortfolio.getPortfolioId());

        for (Transaction t : txns) {
            txnTableModel.addRow(new Object[]{
                    t.getTxnId(),
                    t.getTicker(),
                    t.getType(),
                    t.getQuantity(),
                    String.format("$%.2f", t.getPrice()),
                    t.getTimestamp().toString()
            });
        }
    }

    /** Populate the alerts panel with styled alert cards. */
    private void loadAlerts() {
        alertsPanel.removeAll();
        List<Alert> alerts = alertDAO.getAlertsForPortfolio(selectedPortfolio.getPortfolioId());

        if (alerts.isEmpty()) {
            JLabel empty = new JLabel("No active alerts for your holdings.");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            empty.setForeground(TEXT_SECONDARY);
            empty.setBorder(new EmptyBorder(20, 16, 20, 16));
            alertsPanel.add(empty);
        } else {
            for (Alert a : alerts) {
                alertsPanel.add(createAlertCard(a));
                alertsPanel.add(Box.createVerticalStrut(6));
            }
        }

        alertsPanel.revalidate();
        alertsPanel.repaint();
    }

    /** Creates a single alert card widget. */
    private JPanel createAlertCard(Alert alert) {
        JPanel card = new JPanel(new BorderLayout(8, 4));
        card.setBackground(new Color(44, 48, 60));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, getSeverityColor(alert.getSeverity())),
                new EmptyBorder(10, 12, 10, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Top: Ticker + type badge
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topRow.setOpaque(false);

        JLabel tickerLbl = new JLabel(alert.getTicker());
        tickerLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tickerLbl.setForeground(TEXT_PRIMARY);

        JLabel typeBadge = new JLabel(" " + alert.getAnomalyType() + " ");
        typeBadge.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        typeBadge.setForeground(Color.WHITE);
        typeBadge.setOpaque(true);
        typeBadge.setBackground(getSeverityColor(alert.getSeverity()));

        JLabel dateLbl = new JLabel(alert.getDateId() != null ? alert.getDateId().toString() : "");
        dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dateLbl.setForeground(TEXT_SECONDARY);

        topRow.add(tickerLbl);
        topRow.add(typeBadge);
        topRow.add(dateLbl);
        card.add(topRow, BorderLayout.NORTH);

        // Message body
        JTextArea msgArea = new JTextArea(alert.getMessage());
        msgArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        msgArea.setForeground(TEXT_SECONDARY);
        msgArea.setBackground(new Color(44, 48, 60));
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        msgArea.setEditable(false);
        msgArea.setBorder(new EmptyBorder(4, 0, 0, 0));
        card.add(msgArea, BorderLayout.CENTER);

        return card;
    }

    private Color getSeverityColor(int severity) {
        if (severity >= 4) return ALERT_HIGH;
        if (severity >= 3) return ALERT_MED;
        return ALERT_LOW;
    }

    // ================================================================
    //  ACTIONS
    // ================================================================

    /** Opens the New Trade dialog. */
    private void openTradeDialog() {
        if (selectedPortfolio == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a portfolio first.",
                    "No Portfolio Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        TransactionDialog dialog = new TransactionDialog(this, selectedPortfolio);
        dialog.setVisible(true);
        // Refresh data after the dialog closes (trade may have been executed)
        refreshDashboard();
    }

    /** Prompts for a name and creates a new portfolio. */
    private void createNewPortfolio() {
        String name = JOptionPane.showInputDialog(this,
                "Enter a name for your new portfolio:",
                "New Portfolio", JOptionPane.PLAIN_MESSAGE);

        if (name != null && !name.trim().isEmpty()) {
            boolean ok = portfolioDAO.createPortfolio(currentUser.getUserId(), name.trim());
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Portfolio '" + name.trim() + "' created successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPortfolios();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to create portfolio.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ================================================================
    //  UI HELPERS
    // ================================================================

    /** Creates a styled flat button. */
    private JButton createStyledButton(String text, Color bg, Color hoverBg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hoverBg); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    /** Creates a JTable with dark theme styling and alternating row colors. */
    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(TABLE_ROW);
        table.setGridColor(BORDER_COLOR);
        table.setRowHeight(30);
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setForeground(TEXT_SECONDARY);
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Alternating row renderer with P/L coloring
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? TABLE_ROW : TABLE_ROW_ALT);

                    // Color P/L columns green/red
                    String text = value != null ? value.toString() : "";
                    if (text.startsWith("+")) {
                        c.setForeground(PROFIT_GREEN);
                    } else if (text.startsWith("-")) {
                        c.setForeground(LOSS_RED);
                    } else if ("BUY".equals(text)) {
                        c.setForeground(PROFIT_GREEN);
                    } else if ("SELL".equals(text)) {
                        c.setForeground(LOSS_RED);
                    } else {
                        c.setForeground(TEXT_PRIMARY);
                    }
                }
                return c;
            }
        });

        return table;
    }
}
