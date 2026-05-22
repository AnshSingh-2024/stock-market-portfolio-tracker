from pathlib import Path
from reportlab.lib import colors
from reportlab.lib.pagesizes import LETTER
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether

ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "reports"
OUT_DIR.mkdir(exist_ok=True)
PDF_PATH = OUT_DIR / "Stock_Portfolio_Tracker_SQL_Query_Feature_Report.pdf"

styles = getSampleStyleSheet()
styles.add(ParagraphStyle(
    name="ReportTitle",
    parent=styles["Title"],
    fontName="Helvetica-Bold",
    fontSize=22,
    leading=28,
    textColor=colors.HexColor("#0B2545"),
    spaceAfter=14,
))
styles.add(ParagraphStyle(
    name="H1",
    parent=styles["Heading1"],
    fontName="Helvetica-Bold",
    fontSize=15,
    leading=19,
    textColor=colors.HexColor("#1F4D78"),
    spaceBefore=12,
    spaceAfter=7,
))
styles.add(ParagraphStyle(
    name="H2",
    parent=styles["Heading2"],
    fontName="Helvetica-Bold",
    fontSize=11.5,
    leading=15,
    textColor=colors.HexColor("#2E74B5"),
    spaceBefore=9,
    spaceAfter=4,
))
styles.add(ParagraphStyle(
    name="Body",
    parent=styles["BodyText"],
    fontName="Helvetica",
    fontSize=8.8,
    leading=11.5,
    spaceAfter=5,
))
styles.add(ParagraphStyle(
    name="CodeBlock",
    parent=styles["BodyText"],
    fontName="Courier",
    fontSize=7.4,
    leading=9.2,
    leftIndent=8,
    rightIndent=4,
    textColor=colors.HexColor("#263238"),
    backColor=colors.HexColor("#F4F6F8"),
    spaceBefore=3,
    spaceAfter=6,
))


def esc(text):
    return (
        str(text)
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\n", "<br/>")
    )


def p(text, style="Body"):
    return Paragraph(esc(text), styles[style])


def table(rows, widths):
    converted = [[cell if hasattr(cell, "wrap") else p(cell) for cell in row] for row in rows]
    t = Table(converted, colWidths=widths, hAlign="LEFT", repeatRows=1)
    t.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#E8EEF5")),
        ("TEXTCOLOR", (0, 0), (-1, 0), colors.HexColor("#0B2545")),
        ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
        ("FONTSIZE", (0, 0), (-1, -1), 8),
        ("LEADING", (0, 0), (-1, -1), 10),
        ("GRID", (0, 0), (-1, -1), 0.25, colors.HexColor("#D0D7DE")),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 5),
        ("RIGHTPADDING", (0, 0), (-1, -1), 5),
        ("TOPPADDING", (0, 0), (-1, -1), 4),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
    ]))
    return t


queries = [
    {
        "id": "Q1",
        "file": "src/com/tracker/dao/UserDAO.java:25",
        "method": "UserDAO.registerUser(name, email, password)",
        "feature": "Create Account / Registration",
        "caller": "RegisterFrame.handleRegister() at src/com/tracker/view/RegisterFrame.java:177",
        "type": "INSERT",
        "tables": "User",
        "sql": "INSERT INTO User (name, email, password_hash) VALUES (?, ?, ?)",
        "params": "1=name, 2=email, 3=SHA-256 password hash",
        "result": "executeUpdate() > 0 means registration succeeded.",
        "purpose": "Stores a new user after UI validation. The raw password is never inserted directly; UserDAO.hashPassword() creates the stored hash.",
    },
    {
        "id": "Q2",
        "file": "src/com/tracker/dao/UserDAO.java:53",
        "method": "UserDAO.authenticate(email, password)",
        "feature": "Login",
        "caller": "LoginFrame.handleLogin() at src/com/tracker/view/LoginFrame.java:162",
        "type": "SELECT",
        "tables": "User",
        "sql": "SELECT * FROM User WHERE email = ? AND password_hash = ?",
        "params": "1=email typed by user, 2=SHA-256 hash of typed password",
        "result": "If a row exists, UserDAO builds a User object and LoginFrame opens DashboardFrame.",
        "purpose": "Checks whether the submitted credentials match a stored account.",
    },
    {
        "id": "Q3",
        "file": "src/com/tracker/dao/PortfolioDAO.java:24",
        "method": "PortfolioDAO.getPortfoliosByUser(userId)",
        "feature": "Dashboard portfolio sidebar",
        "caller": "DashboardFrame.loadPortfolios() at src/com/tracker/view/DashboardFrame.java:312",
        "type": "SELECT",
        "tables": "Portfolio",
        "sql": "SELECT * FROM Portfolio WHERE user_id = ?",
        "params": "1=currentUser.getUserId()",
        "result": "Returns Portfolio objects shown in the left sidebar JList.",
        "purpose": "Loads only the logged-in user's portfolios.",
    },
    {
        "id": "Q4",
        "file": "src/com/tracker/dao/PortfolioDAO.java:55",
        "method": "PortfolioDAO.createPortfolio(userId, name)",
        "feature": "New Portfolio button",
        "caller": "DashboardFrame.createNewPortfolio() at src/com/tracker/view/DashboardFrame.java:480",
        "type": "INSERT",
        "tables": "Portfolio",
        "sql": "INSERT INTO Portfolio (user_id, name) VALUES (?, ?)",
        "params": "1=currentUser.getUserId(), 2=name from JOptionPane",
        "result": "executeUpdate() > 0 means the portfolio was created; DashboardFrame reloads the sidebar.",
        "purpose": "Creates a new portfolio owned by the current user.",
    },
    {
        "id": "Q5",
        "file": "src/com/tracker/dao/HoldingDAO.java:25",
        "method": "HoldingDAO.getHoldingsByPortfolio(portfolioId)",
        "feature": "Dashboard holdings table and total P/L",
        "caller": "DashboardFrame.loadHoldings() at src/com/tracker/view/DashboardFrame.java:332",
        "type": "SELECT with JOIN and subquery",
        "tables": "Holding, Stock, Market_Data",
        "sql": """SELECT h.*, s.ticker, s.company_name,
       (SELECT md.close_price FROM Market_Data md
          WHERE md.stock_id = h.stock_id ORDER BY md.date_id DESC LIMIT 1) AS current_price
FROM Holding h
JOIN Stock s ON h.stock_id = s.stock_id
WHERE h.portfolio_id = ?""",
        "params": "1=selectedPortfolio.getPortfolioId()",
        "result": "Returns Holding objects with ticker, company name, and latest close price. DashboardFrame computes total value and profit/loss from these objects.",
        "purpose": "Combines portfolio holdings with stock metadata and latest market price in one dashboard query.",
    },
    {
        "id": "Q6",
        "file": "src/com/tracker/dao/HoldingDAO.java:64",
        "method": "HoldingDAO.findHolding(portfolioId, stockId)",
        "feature": "BUY/SELL validation and holding update decision",
        "caller": "TransactionDAO.executeBuy() at line 39 and executeSell() at line 63",
        "type": "SELECT",
        "tables": "Holding",
        "sql": "SELECT * FROM Holding WHERE portfolio_id = ? AND stock_id = ?",
        "params": "1=portfolioId, 2=stockId",
        "result": "Returns an existing Holding or null.",
        "purpose": "Detects whether the selected portfolio already owns the stock. BUY uses it to decide insert vs update; SELL uses it to check available quantity.",
    },
    {
        "id": "Q7",
        "file": "src/com/tracker/dao/HoldingDAO.java:94",
        "method": "HoldingDAO.insertHolding(portfolioId, stockId, quantity, avgPrice)",
        "feature": "BUY new stock not already held",
        "caller": "TransactionDAO.executeBuy() at src/com/tracker/dao/TransactionDAO.java:49",
        "type": "INSERT",
        "tables": "Holding",
        "sql": "INSERT INTO Holding (portfolio_id, stock_id, quantity, avg_buy_price) VALUES (?, ?, ?, ?)",
        "params": "1=portfolioId, 2=stockId, 3=quantity, 4=buy price",
        "result": "Creates the initial holding row for that stock.",
        "purpose": "Adds a stock to the portfolio when the first BUY order for that stock is executed.",
    },
    {
        "id": "Q8",
        "file": "src/com/tracker/dao/HoldingDAO.java:116",
        "method": "HoldingDAO.updateHolding(holdingId, newQuantity, newAvgPrice)",
        "feature": "BUY existing stock / SELL partial stock",
        "caller": "TransactionDAO.executeBuy() at line 47 and executeSell() at line 81",
        "type": "UPDATE",
        "tables": "Holding",
        "sql": "UPDATE Holding SET quantity = ?, avg_buy_price = ? WHERE holding_id = ?",
        "params": "1=newQuantity, 2=newAvgPrice, 3=holdingId",
        "result": "Updates the existing holding row.",
        "purpose": "For BUY, stores the increased quantity and weighted average buy price. For partial SELL, stores the reduced quantity while keeping average price unchanged.",
    },
    {
        "id": "Q9",
        "file": "src/com/tracker/dao/HoldingDAO.java:137",
        "method": "HoldingDAO.deleteHolding(holdingId)",
        "feature": "SELL all shares of a stock",
        "caller": "TransactionDAO.executeSell() at src/com/tracker/dao/TransactionDAO.java:78",
        "type": "DELETE",
        "tables": "Holding",
        "sql": "DELETE FROM Holding WHERE holding_id = ?",
        "params": "1=holdingId",
        "result": "Removes the holding when remaining quantity becomes zero.",
        "purpose": "Keeps the holdings table clean by removing positions that no longer exist.",
    },
    {
        "id": "Q10",
        "file": "src/com/tracker/dao/StockDAO.java:23",
        "method": "StockDAO.getAllStocks()",
        "feature": "Trade dialog stock dropdown",
        "caller": "TransactionDialog.initComponents() at src/com/tracker/view/TransactionDialog.java:81",
        "type": "SELECT",
        "tables": "Stock",
        "sql": "SELECT * FROM Stock ORDER BY ticker",
        "params": "None",
        "result": "Returns all Stock objects sorted alphabetically by ticker.",
        "purpose": "Populates the JComboBox so the user can choose which stock to buy or sell.",
    },
    {
        "id": "Q11",
        "file": "src/com/tracker/dao/StockDAO.java:51",
        "method": "StockDAO.getLatestClosePrice(stockId)",
        "feature": "Trade dialog price autofill",
        "caller": "TransactionDialog stock combo listener at lines 88-91 and initial autofill at lines 122-125",
        "type": "SELECT",
        "tables": "Market_Data",
        "sql": "SELECT close_price FROM Market_Data WHERE stock_id = ? ORDER BY date_id DESC LIMIT 1",
        "params": "1=selectedStock.getStockId()",
        "result": "Returns a float latest close price, or 0.0 if not found.",
        "purpose": "Autofills the trade price field with the newest seeded market close price.",
    },
    {
        "id": "Q12",
        "file": "src/com/tracker/dao/TransactionDAO.java:94",
        "method": "TransactionDAO.getRecentTransactions(portfolioId)",
        "feature": "Dashboard recent transactions table",
        "caller": "DashboardFrame.loadTransactions() at src/com/tracker/view/DashboardFrame.java:366",
        "type": "SELECT with JOIN",
        "tables": "Transaction, Stock",
        "sql": """SELECT t.*, s.ticker FROM Transaction t
JOIN Stock s ON t.stock_id = s.stock_id
WHERE t.portfolio_id = ?
ORDER BY t.timestamp DESC LIMIT 50""",
        "params": "1=selectedPortfolio.getPortfolioId()",
        "result": "Returns recent Transaction objects with display ticker.",
        "purpose": "Shows the latest trade activity for the selected portfolio.",
    },
    {
        "id": "Q13",
        "file": "src/com/tracker/dao/TransactionDAO.java:130",
        "method": "TransactionDAO.logTransaction(portfolioId, stockId, type, quantity, price)",
        "feature": "BUY and SELL audit trail",
        "caller": "TransactionDAO.executeBuy() at line 34 and executeSell() at line 71",
        "type": "INSERT",
        "tables": "Transaction",
        "sql": """INSERT INTO Transaction (portfolio_id, stock_id, type, quantity, price, timestamp)
VALUES (?, ?, ?, ?, ?, NOW())""",
        "params": "1=portfolioId, 2=stockId, 3='BUY' or 'SELL', 4=quantity, 5=price",
        "result": "Creates one transaction history row.",
        "purpose": "Records every executed trade before holdings are updated.",
    },
    {
        "id": "Q14",
        "file": "src/com/tracker/dao/StockDAO.java:78",
        "method": "StockDAO.simulateLatestPriceMovement()",
        "feature": "Built-in dashboard market price simulator",
        "caller": "DashboardFrame.startAutoRefresh() calls it every 10 seconds before refreshDashboard()",
        "type": "UPDATE with JOIN",
        "tables": "Market_Data",
        "sql": """UPDATE Market_Data md
JOIN (
    SELECT stock_id, MAX(date_id) AS latest_date
    FROM Market_Data
    GROUP BY stock_id
) latest
    ON md.stock_id = latest.stock_id
   AND md.date_id = latest.latest_date
SET
    md.open_price = md.close_price,
    md.close_price = ROUND(GREATEST(1, md.close_price * (1 + ((RAND() - 0.5) / 50))), 2),
    md.high = GREATEST(md.high, md.close_price),
    md.low = LEAST(md.low, md.close_price),
    md.volume = GREATEST(1000, ROUND(md.volume * (1 + ((RAND() - 0.5) / 10))));""",
        "params": "No user parameters. Uses RAND() inside MySQL to generate small random price/volume movement.",
        "result": "Updates the latest Market_Data row for every stock; DashboardFrame then reloads holdings and recalculates current value and profit/loss.",
        "purpose": "Simulates market movement inside the Java app, so no second terminal or external market API is needed.",
    },
]

schema_sections = [
    ["schema.sql lines 6-12", "Database cleanup/setup", "CREATE DATABASE / USE / DROP TABLE IF EXISTS Alert, Anomaly", "Creates/selects the app database and removes old anomaly tables if an older version was initialized."],
    ["schema.sql User section", "User table", "CREATE TABLE IF NOT EXISTS User (...)", "Supports registration and login."],
    ["schema.sql Portfolio section", "Portfolio table", "CREATE TABLE IF NOT EXISTS Portfolio (...)", "Stores named portfolios belonging to users."],
    ["schema.sql Stock section", "Stock table", "CREATE TABLE IF NOT EXISTS Stock (...)", "Stores tradable stock master data."],
    ["schema.sql Holding section", "Holding table", "CREATE TABLE IF NOT EXISTS Holding (...)", "Stores current owned stock quantities and average buy prices."],
    ["schema.sql Transaction section", "Transaction table", "CREATE TABLE IF NOT EXISTS Transaction (...)", "Stores trade history."],
    ["schema.sql Market_Data section", "Market_Data table", "CREATE TABLE IF NOT EXISTS Market_Data (...)", "Stores OHLCV price history. Latest close is used as current price and is updated by the simulator."],
    ["schema.sql seed section", "Seed inserts", "INSERT INTO Stock / Market_Data / User / Portfolio / Holding / Transaction", "Adds demo data for immediate testing."],
]

feature_map = [
    ["Feature", "View method that triggers it", "DAO method(s)", "Query IDs"],
    ["Register new account", "RegisterFrame.handleRegister()", "UserDAO.registerUser()", "Q1"],
    ["Login", "LoginFrame.handleLogin()", "UserDAO.authenticate()", "Q2"],
    ["Load portfolio sidebar", "DashboardFrame.loadPortfolios()", "PortfolioDAO.getPortfoliosByUser()", "Q3"],
    ["Create new portfolio", "DashboardFrame.createNewPortfolio()", "PortfolioDAO.createPortfolio()", "Q4"],
    ["Load holdings and compute dashboard totals", "DashboardFrame.loadHoldings()", "HoldingDAO.getHoldingsByPortfolio()", "Q5"],
    ["Open trade dialog and populate stocks", "TransactionDialog.initComponents()", "StockDAO.getAllStocks()", "Q10"],
    ["Autofill latest trade price", "TransactionDialog stock selection listener", "StockDAO.getLatestClosePrice()", "Q11"],
    ["Execute BUY order", "TransactionDialog.executeTrade()", "TransactionDAO.executeBuy(), logTransaction(), HoldingDAO.findHolding(), insertHolding()/updateHolding()", "Q13, Q6, Q7/Q8"],
    ["Execute SELL order", "TransactionDialog.executeTrade()", "TransactionDAO.executeSell(), logTransaction(), HoldingDAO.findHolding(), updateHolding()/deleteHolding()", "Q6, Q13, Q8/Q9"],
    ["Load recent transactions", "DashboardFrame.loadTransactions()", "TransactionDAO.getRecentTransactions()", "Q12"],
    ["Simulate moving market prices", "DashboardFrame.startAutoRefresh()", "StockDAO.simulateLatestPriceMovement()", "Q14"],
]

story = []
story.append(p("Stock Portfolio Tracker", "ReportTitle"))
story.append(p("SQL Query and Feature Usage Report", "H1"))
story.append(p("This report explains where every SQL query is located, which feature uses it, which Java method runs it, what parameters are passed into it, and how its result affects the application."))

story.append(p("Quick Summary", "H1"))
story.append(p("All runtime SQL queries are inside DAO classes under src/com/tracker/dao. The Swing view classes do not write SQL directly; they call DAO methods. schema.sql contains setup SQL: database/table creation and demo seed data."))

story.append(p("Feature to Query Map", "H1"))
story.append(table(feature_map, [1.45 * inch, 1.75 * inch, 2.4 * inch, 0.85 * inch]))

story.append(PageBreak())
story.append(p("Database Setup SQL", "H1"))
story.append(p("schema.sql runs before the Java app. It creates the database structure and inserts demo data used by login, dashboard, trades, and market-price simulation."))
story.append(table([["Location", "Section", "SQL", "Used for"]] + schema_sections, [1.35 * inch, 1.2 * inch, 2.25 * inch, 1.65 * inch]))

story.append(PageBreak())
story.append(p("Runtime Query Details", "H1"))
for q in queries:
    story.append(KeepTogether([
        p(f"{q['id']} - {q['feature']}", "H2"),
        p(f"Location: {q['file']}"),
        p(f"Java method: {q['method']}"),
        p(f"Triggered by: {q['caller']}"),
        p(f"Query type: {q['type']}"),
        p(f"Tables used: {q['tables']}"),
        p("SQL:", "Body"),
        p(q["sql"], "CodeBlock"),
        p(f"Parameters: {q['params']}"),
        p(f"Purpose: {q['purpose']}"),
        p(f"Result in app: {q['result']}"),
        Spacer(1, 0.08 * inch),
    ]))

story.append(PageBreak())
story.append(p("Important Observations", "H1"))
observations = [
    "The project correctly uses PreparedStatement for all runtime SQL with user input. This is safer than string concatenation and helps prevent SQL injection.",
    "The DAO pattern keeps SQL in one layer. UI classes call methods like authenticate(), getHoldingsByPortfolio(), and executeBuy() instead of embedding SQL inside button handlers.",
    "The market simulator now runs inside the Java app through StockDAO.simulateLatestPriceMovement(), called by DashboardFrame every 10 seconds.",
    "BUY and SELL are multi-step operations. A BUY can run Q13 + Q6 + Q7 or Q13 + Q6 + Q8. A SELL can run Q6 + Q13 + Q8 or Q6 + Q13 + Q9.",
    "Trade execution is not wrapped in a database transaction. If logTransaction succeeds but holding update fails, the data can become inconsistent. For a stronger implementation, executeBuy and executeSell should use conn.setAutoCommit(false), commit, and rollback.",
    "Money values are stored as FLOAT in schema.sql. For real financial systems, DECIMAL is safer because it avoids floating-point rounding issues.",
    "Holding should ideally have UNIQUE(portfolio_id, stock_id). The current Java logic tries to prevent duplicates through findHolding(), but a database constraint would be stronger.",
]
for item in observations:
    story.append(p("• " + item))


def footer(canvas, doc):
    canvas.saveState()
    canvas.setFont("Helvetica", 7.5)
    canvas.setFillColor(colors.HexColor("#555555"))
    canvas.drawString(0.7 * inch, 0.45 * inch, "Stock Portfolio Tracker - SQL Query Feature Report")
    canvas.drawRightString(7.8 * inch, 0.45 * inch, f"Page {doc.page}")
    canvas.restoreState()


doc = SimpleDocTemplate(
    str(PDF_PATH),
    pagesize=LETTER,
    rightMargin=0.55 * inch,
    leftMargin=0.55 * inch,
    topMargin=0.6 * inch,
    bottomMargin=0.65 * inch,
)
doc.build(story, onFirstPage=footer, onLaterPages=footer)
print(PDF_PATH)
