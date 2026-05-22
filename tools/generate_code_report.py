from pathlib import Path
from reportlab.lib import colors
from reportlab.lib.pagesizes import LETTER
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether
)

ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "reports"
OUT_DIR.mkdir(exist_ok=True)
PDF_PATH = OUT_DIR / "Stock_Portfolio_Tracker_Code_Report.pdf"


def file_stats(relative):
    path = ROOT / relative
    text = path.read_text(encoding="utf-8", errors="replace")
    lines = text.splitlines()
    return len(lines), len(text)


styles = getSampleStyleSheet()
styles.add(ParagraphStyle(
    name="TitlePage",
    parent=styles["Title"],
    fontName="Helvetica-Bold",
    fontSize=24,
    leading=30,
    textColor=colors.HexColor("#0B2545"),
    spaceAfter=18,
))
styles.add(ParagraphStyle(
    name="H1",
    parent=styles["Heading1"],
    fontName="Helvetica-Bold",
    fontSize=16,
    leading=20,
    textColor=colors.HexColor("#1F4D78"),
    spaceBefore=14,
    spaceAfter=8,
))
styles.add(ParagraphStyle(
    name="H2",
    parent=styles["Heading2"],
    fontName="Helvetica-Bold",
    fontSize=12,
    leading=15,
    textColor=colors.HexColor("#2E74B5"),
    spaceBefore=10,
    spaceAfter=5,
))
styles.add(ParagraphStyle(
    name="Body",
    parent=styles["BodyText"],
    fontName="Helvetica",
    fontSize=9.5,
    leading=13,
    spaceAfter=6,
))
styles.add(ParagraphStyle(
    name="CodeBlock",
    parent=styles["BodyText"],
    fontName="Courier",
    fontSize=8.5,
    leading=11,
    leftIndent=10,
    textColor=colors.HexColor("#263238"),
    backColor=colors.HexColor("#F4F6F8"),
    spaceBefore=3,
    spaceAfter=6,
))


def p(text, style="Body"):
    safe = (
        text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\n", "<br/>")
    )
    return Paragraph(safe, styles[style])


def bullet(text):
    return Paragraph("• " + text, styles["Body"])


def table(rows, widths=None):
    t = Table(rows, colWidths=widths, hAlign="LEFT")
    t.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#E8EEF5")),
        ("TEXTCOLOR", (0, 0), (-1, 0), colors.HexColor("#0B2545")),
        ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
        ("FONTNAME", (0, 1), (-1, -1), "Helvetica"),
        ("FONTSIZE", (0, 0), (-1, -1), 8.5),
        ("LEADING", (0, 0), (-1, -1), 11),
        ("GRID", (0, 0), (-1, -1), 0.25, colors.HexColor("#D0D7DE")),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 6),
        ("RIGHTPADDING", (0, 0), (-1, -1), 6),
        ("TOPPADDING", (0, 0), (-1, -1), 5),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 5),
    ]))
    return t


files = [
    "README.md", "schema.sql", "compile.ps1", "run.ps1",
    "src/com/tracker/app/Main.java",
    "src/com/tracker/config/DBConnection.java",
    "src/com/tracker/model/User.java",
    "src/com/tracker/model/Portfolio.java",
    "src/com/tracker/model/Stock.java",
    "src/com/tracker/model/Holding.java",
    "src/com/tracker/model/Transaction.java",
    "src/com/tracker/model/Anomaly.java",
    "src/com/tracker/model/Alert.java",
    "src/com/tracker/dao/UserDAO.java",
    "src/com/tracker/dao/PortfolioDAO.java",
    "src/com/tracker/dao/StockDAO.java",
    "src/com/tracker/dao/HoldingDAO.java",
    "src/com/tracker/dao/TransactionDAO.java",
    "src/com/tracker/dao/AlertDAO.java",
    "src/com/tracker/view/LoginFrame.java",
    "src/com/tracker/view/RegisterFrame.java",
    "src/com/tracker/view/DashboardFrame.java",
    "src/com/tracker/view/TransactionDialog.java",
]

file_explanations = {
    "README.md": "Project overview and manual run instructions. It identifies the stack as Java Swing, JDBC, and MySQL, lists the main user features, and documents the original compile/run commands.",
    "schema.sql": "Creates the stock_tracker database, eight normalized tables, foreign keys, and seed data. It is the data foundation for login, portfolios, holdings, trades, market prices, anomalies, and alerts.",
    "compile.ps1": "Local helper added for setup. It recursively finds every Java source file under src, compiles them with javac, and writes class files into out.",
    "run.ps1": "Local helper added for setup. It compiles when out is missing, then launches com.tracker.app.Main with both out and the vendored MySQL Connector/J JAR on the runtime classpath.",
    "src/com/tracker/app/Main.java": "The application entry point. It sets the cross-platform Swing look and feel, then uses SwingUtilities.invokeLater so the LoginFrame is created on the Swing Event Dispatch Thread.",
    "src/com/tracker/config/DBConnection.java": "Singleton JDBC connection manager. It loads com.mysql.cj.jdbc.Driver, opens a MySQL connection, and reconnects if the connection is null or closed. It now reads DB_URL, DB_USER, and DB_PASSWORD environment variables, falling back to localhost stock_tracker, root, and blank password.",
    "src/com/tracker/model/User.java": "Plain Java object for the User table. It stores user_id, name, email, and password_hash, with constructors, getters/setters, and a toString for debugging.",
    "src/com/tracker/model/Portfolio.java": "Plain Java object for a user's portfolio. The toString returns the portfolio name, which lets Swing list and combo-box components display readable names automatically.",
    "src/com/tracker/model/Stock.java": "Plain Java object for listed stocks. It stores stock_id, ticker, and company_name. Its toString combines ticker and company name, which is used directly in the trade dialog stock selector.",
    "src/com/tracker/model/Holding.java": "Plain Java object for a portfolio holding. It stores database fields plus display-only joined fields such as ticker, companyName, and currentPrice. getProfitLoss computes (currentPrice - avgBuyPrice) * quantity.",
    "src/com/tracker/model/Transaction.java": "Plain Java object for BUY/SELL transaction history. It stores transaction id, portfolio id, stock id, type, quantity, price, timestamp, plus a display-only ticker field populated by joins.",
    "src/com/tracker/model/Anomaly.java": "Plain Java object for the Anomaly table. It represents unusual stock events with stock id, date, type, and numeric severity.",
    "src/com/tracker/model/Alert.java": "Plain Java object for alert messages linked to anomalies. It also carries joined display fields: ticker, anomaly type, severity, and date.",
    "src/com/tracker/dao/UserDAO.java": "Handles registration and authentication. registerUser hashes the raw password with SHA-256 and inserts the user. authenticate hashes the typed password and searches for a matching email/hash row. PreparedStatement protects these queries from SQL injection.",
    "src/com/tracker/dao/PortfolioDAO.java": "Handles portfolio persistence. getPortfoliosByUser loads the current user's portfolios for the sidebar. createPortfolio inserts a new row for the logged-in user.",
    "src/com/tracker/dao/StockDAO.java": "Handles stock lookup. getAllStocks returns stocks alphabetically for the trade selector. getLatestClosePrice queries Market_Data by date descending and returns the newest closing price.",
    "src/com/tracker/dao/HoldingDAO.java": "Handles holding reads and writes. getHoldingsByPortfolio joins Holding and Stock and uses a subquery for latest market price. findHolding finds a portfolio/stock pair. insertHolding, updateHolding, and deleteHolding support trade execution.",
    "src/com/tracker/dao/TransactionDAO.java": "Implements the trading rules. executeBuy logs a BUY and creates or updates holdings with weighted average price. executeSell validates enough shares, logs a SELL, and reduces or deletes the holding. getRecentTransactions loads the latest 50 trades.",
    "src/com/tracker/dao/AlertDAO.java": "Loads relevant anomaly alerts for the selected portfolio. It joins Alert, Anomaly, Stock, and Holding so users only see alerts for stocks they own, sorted by severity and date.",
    "src/com/tracker/view/LoginFrame.java": "Swing login screen. It builds a dark themed card with email/password fields, validates non-empty input, calls UserDAO.authenticate, opens DashboardFrame on success, and opens RegisterFrame from the signup link.",
    "src/com/tracker/view/RegisterFrame.java": "Swing registration screen. It validates name, email shape, password length, and password confirmation, calls UserDAO.registerUser, shows success/error status, and returns to LoginFrame.",
    "src/com/tracker/view/DashboardFrame.java": "Main logged-in UI. It owns the selected user and portfolio, builds the header/sidebar/holdings/transactions/alerts layout, loads portfolios, refreshes tables, computes total value and profit/loss, creates portfolios, opens TransactionDialog, and renders severity-colored alert cards.",
    "src/com/tracker/view/TransactionDialog.java": "Modal trade form. It loads stocks, auto-fills latest market price, lets the user choose BUY or SELL, validates quantity and price, calls TransactionDAO, and reports success or failure with JOptionPane.",
}

story = []
story.append(p("Stock Market Portfolio Tracker", "TitlePage"))
story.append(p("Code Explanation and Setup Report", "H1"))
story.append(p("Generated for the Java Swing + JDBC + MySQL project in C:/Users/adris/Desktop/DB Clg proj/stock-market-portfolio-tracker."))
story.append(p("This report explains how to set up and run the project, how the architecture fits together, how data flows through the application, and what each source file does."))
story.append(Spacer(1, 0.15 * inch))

story.append(p("1. Setup Status", "H1"))
story.append(bullet("Java is installed: javac 25.0.3 was detected."))
story.append(bullet("The project compiles successfully into the out directory using compile.ps1."))
story.append(bullet("The MySQL command-line client was not found on PATH, so database import could not be completed from this environment."))
story.append(bullet("DBConnection.java was updated to read DB_URL, DB_USER, and DB_PASSWORD from environment variables so credentials do not need to be hardcoded."))
story.append(p("Compile command:", "H2"))
story.append(p("powershell -ExecutionPolicy Bypass -File .\\compile.ps1", "CodeBlock"))
story.append(p("Run command after MySQL is installed/configured:", "H2"))
story.append(p("$env:DB_USER='root'\n$env:DB_PASSWORD='your_mysql_password'\npowershell -ExecutionPolicy Bypass -File .\\run.ps1", "CodeBlock"))

story.append(p("2. Database Setup", "H1"))
story.append(p("Install MySQL 8.0 or newer, then run schema.sql in MySQL Workbench or CLI. The script creates the stock_tracker database and inserts demo data. Demo login: demo@tracker.com / password123."))
story.append(p("If mysql is available on PATH, the import command is:", "H2"))
story.append(p("mysql -u root -p < schema.sql", "CodeBlock"))
story.append(p("Tables", "H2"))
story.append(table([
    ["Table", "Purpose"],
    ["User", "Stores registered users and SHA-256 password hashes."],
    ["Portfolio", "Stores portfolios owned by users."],
    ["Stock", "Stores ticker symbols and company names."],
    ["Holding", "Stores current portfolio positions and average buy prices."],
    ["Transaction", "Stores BUY/SELL trade history."],
    ["Market_Data", "Stores dated OHLCV market data; latest close is used as current price."],
    ["Anomaly", "Stores detected unusual stock events and severity."],
    ["Alert", "Stores user-facing messages linked to anomalies."],
], [1.4 * inch, 5.0 * inch]))

story.append(p("3. Architecture", "H1"))
story.append(p("The project follows a simple MVC-style structure. Model classes represent database records. DAO classes contain SQL and business persistence operations. View classes build Swing screens and call DAOs when the user clicks buttons or changes selections. DBConnection centralizes the JDBC connection. Main launches the first screen."))
story.append(table([
    ["Layer", "Package", "Responsibility"],
    ["Entry", "com.tracker.app", "Starts the Swing app."],
    ["Config", "com.tracker.config", "Creates/reuses the database connection."],
    ["Model", "com.tracker.model", "Simple data containers used by DAO and UI layers."],
    ["DAO", "com.tracker.dao", "SQL queries, inserts, updates, authentication, and trade logic."],
    ["View", "com.tracker.view", "Swing windows, dialogs, tables, forms, buttons, validation, and navigation."],
], [0.8 * inch, 1.5 * inch, 4.1 * inch]))

story.append(p("4. Runtime Flow", "H1"))
for item in [
    "Main starts the program and shows LoginFrame.",
    "LoginFrame validates email/password and calls UserDAO.authenticate.",
    "On success, DashboardFrame opens with the authenticated User object.",
    "DashboardFrame loads portfolios from PortfolioDAO. Selecting a portfolio triggers refreshDashboard.",
    "refreshDashboard loads holdings, recent transactions, and alerts through HoldingDAO, TransactionDAO, and AlertDAO.",
    "New Trade opens TransactionDialog. A BUY recalculates weighted average price. A SELL validates share quantity before updating holdings.",
    "Market_Data is not fetched live from the internet. It is seeded in schema.sql and read as historical/local sample data.",
]:
    story.append(bullet(item))

story.append(p("5. File Inventory", "H1"))
rows = [["File", "Lines", "Role"]]
for f in files:
    lines, _ = file_stats(f)
    rows.append([f, str(lines), file_explanations[f]])
story.append(table([[p(str(c)) for c in row] for row in rows], [2.35 * inch, 0.45 * inch, 3.7 * inch]))

story.append(PageBreak())
story.append(p("6. File-by-File Explanation", "H1"))
for f in files:
    lines, chars = file_stats(f)
    story.append(KeepTogether([
        p(f, "H2"),
        p(f"Size: {lines} lines, {chars} characters."),
        p(file_explanations[f]),
    ]))

story.append(p("7. Important Implementation Details", "H1"))
story.append(p("Authentication", "H2"))
story.append(p("Passwords are hashed with SHA-256 in UserDAO before storage or comparison. This is better than storing plaintext, but a production-grade system should use salted adaptive hashing such as bcrypt, scrypt, or Argon2."))
story.append(p("Trade Logic", "H2"))
story.append(p("BUY records are inserted into Transaction first. If the holding already exists, the average buy price is recalculated by weighted average: ((oldQty * oldAvg) + (newQty * newPrice)) / (oldQty + newQty). SELL records require an existing holding with enough quantity, then reduce or delete the holding."))
story.append(p("Portfolio Valuation", "H2"))
story.append(p("DashboardFrame computes total value as currentPrice * quantity for every holding. Profit/loss is computed per holding by Holding.getProfitLoss and summed for the selected portfolio."))
story.append(p("Alert Filtering", "H2"))
story.append(p("AlertDAO only shows alerts for stocks present in the selected portfolio by joining Holding with Stock, Anomaly, and Alert. This makes alerts contextual instead of global."))
story.append(p("UI Threading", "H2"))
story.append(p("Main correctly creates the first Swing frame on the Event Dispatch Thread. DAO calls are currently made directly from Swing event handlers, which is acceptable for a college-scale demo but can freeze the UI if the database is slow. SwingWorker would be a good future improvement."))

story.append(p("8. Suggested Improvements", "H1"))
for item in [
    "Add a connection test screen or startup error dialog when MySQL is unavailable.",
    "Use DECIMAL instead of FLOAT for money values in schema.sql.",
    "Wrap BUY/SELL transaction logging and holding updates in a single SQL transaction for atomicity.",
    "Add UNIQUE(portfolio_id, stock_id) to Holding to prevent duplicate holdings for the same stock.",
    "Add salt and adaptive password hashing for stronger security.",
    "Add automated tests for DAO trade logic using a test database.",
]:
    story.append(bullet(item))


def add_footer(canvas, doc):
    canvas.saveState()
    canvas.setFont("Helvetica", 8)
    canvas.setFillColor(colors.HexColor("#555555"))
    canvas.drawString(inch, 0.5 * inch, "Stock Portfolio Tracker Code Report")
    canvas.drawRightString(7.5 * inch, 0.5 * inch, f"Page {doc.page}")
    canvas.restoreState()


doc = SimpleDocTemplate(
    str(PDF_PATH),
    pagesize=LETTER,
    rightMargin=0.75 * inch,
    leftMargin=0.75 * inch,
    topMargin=0.75 * inch,
    bottomMargin=0.75 * inch,
)
doc.build(story, onFirstPage=add_footer, onLaterPages=add_footer)
print(PDF_PATH)
