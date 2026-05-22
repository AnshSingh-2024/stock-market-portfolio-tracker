from pathlib import Path
import re
from reportlab.lib import colors
from reportlab.lib.pagesizes import LETTER
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, PageBreak

ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "reports"
OUT_DIR.mkdir(exist_ok=True)
PDF_PATH = OUT_DIR / "Stock_Portfolio_Tracker_Line_By_Line_Explanation.pdf"

FILES = [
    "README.md",
    "schema.sql",
    "compile.ps1",
    "run.ps1",
    "start-mysql.ps1",
    "src/com/tracker/app/Main.java",
    "src/com/tracker/config/DBConnection.java",
    "src/com/tracker/model/User.java",
    "src/com/tracker/model/Portfolio.java",
    "src/com/tracker/model/Stock.java",
    "src/com/tracker/model/Holding.java",
    "src/com/tracker/model/Transaction.java",
    "src/com/tracker/dao/UserDAO.java",
    "src/com/tracker/dao/PortfolioDAO.java",
    "src/com/tracker/dao/StockDAO.java",
    "src/com/tracker/dao/HoldingDAO.java",
    "src/com/tracker/dao/TransactionDAO.java",
    "src/com/tracker/view/LoginFrame.java",
    "src/com/tracker/view/RegisterFrame.java",
    "src/com/tracker/view/DashboardFrame.java",
    "src/com/tracker/view/TransactionDialog.java",
]

styles = getSampleStyleSheet()
styles.add(ParagraphStyle(
    name="ReportTitle",
    parent=styles["Title"],
    fontName="Helvetica-Bold",
    fontSize=22,
    leading=28,
    textColor=colors.HexColor("#0B2545"),
    spaceAfter=16,
))
styles.add(ParagraphStyle(
    name="Section",
    parent=styles["Heading1"],
    fontName="Helvetica-Bold",
    fontSize=15,
    leading=19,
    textColor=colors.HexColor("#1F4D78"),
    spaceBefore=12,
    spaceAfter=6,
))
styles.add(ParagraphStyle(
    name="Subsection",
    parent=styles["Heading2"],
    fontName="Helvetica-Bold",
    fontSize=11,
    leading=14,
    textColor=colors.HexColor("#2E74B5"),
    spaceBefore=9,
    spaceAfter=4,
))
styles.add(ParagraphStyle(
    name="NormalSmall",
    parent=styles["BodyText"],
    fontName="Helvetica",
    fontSize=8.5,
    leading=11,
    spaceAfter=4,
))
styles.add(ParagraphStyle(
    name="CodeLine",
    parent=styles["BodyText"],
    fontName="Courier",
    fontSize=7,
    leading=9,
    leftIndent=8,
    rightIndent=4,
    textColor=colors.HexColor("#263238"),
    backColor=colors.HexColor("#F4F6F8"),
    spaceBefore=1,
    spaceAfter=2,
))
styles.add(ParagraphStyle(
    name="Explain",
    parent=styles["BodyText"],
    fontName="Helvetica",
    fontSize=7.5,
    leading=9.5,
    leftIndent=18,
    textColor=colors.HexColor("#333333"),
    spaceAfter=4,
))


def esc(text):
    return (
        text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\t", "    ")
    )


def para(text, style="NormalSmall"):
    return Paragraph(esc(text), styles[style])


def explain_sql(line):
    s = line.strip()
    up = s.upper()
    if not s:
        return "Blank line used to visually separate SQL sections."
    if s.startswith("--"):
        return "SQL comment. It documents what the next block of schema or seed data is doing."
    if up.startswith("CREATE DATABASE"):
        return "Creates the project database if it does not already exist, so the script can be rerun safely."
    if up.startswith("USE "):
        return "Selects the stock_tracker database so every following table and insert statement targets it."
    if up.startswith("CREATE TABLE"):
        name = re.search(r"CREATE TABLE IF NOT EXISTS\s+([A-Za-z_]+)", s, re.I)
        return f"Starts a table definition for {name.group(1) if name else 'a database table'}."
    if "PRIMARY KEY" in up:
        return "Defines the table's primary key, which uniquely identifies each row."
    if "FOREIGN KEY" in up:
        return "Defines a relationship to another table; ON DELETE CASCADE removes dependent rows automatically."
    if "AUTO_INCREMENT" in up:
        return "Defines an integer id that MySQL automatically increments for new rows."
    if up.startswith("INSERT INTO"):
        return "Begins seed data insertion so the app has demo records to display immediately."
    if s.startswith("("):
        return "Seed row or column definition line. In an INSERT block, this is one record being added."
    if up.startswith("ENGINE="):
        return "Closes the table definition and chooses InnoDB so foreign keys are supported."
    return "Part of the database schema or seed data used by the Java application."


def explain_powershell(line):
    s = line.strip()
    if not s:
        return "Blank line for readability."
    if s.startswith("$ErrorActionPreference"):
        return "Makes PowerShell stop immediately if a command fails, which prevents hidden setup errors."
    if "$javaFiles" in s:
        return "Finds all Java source files recursively under src and stores their full paths."
    if s.startswith("javac"):
        return "Compiles the Java source files and writes .class files into the out directory."
    if s.startswith("Write-Host"):
        return "Prints a success message after compilation."
    if s.startswith("if "):
        return "Checks whether compiled output already exists before launching the app."
    if s.startswith("while "):
        return "Starts a loop that keeps running until the user stops the script."
    if "Start-Sleep" in s:
        return "Pauses the script, used either to wait for MySQL startup or to repeat price simulation every 10 seconds."
    if "mysql.exe" in s:
        return "Stores or uses the path to the MySQL command-line client/server executable."
    if "UPDATE Market_Data" in s:
        return "Begins the SQL update that changes latest market prices for the simulator."
    if "RAND()" in s:
        return "Uses MySQL random numbers to move prices/volume slightly up or down."
    if "compile.ps1" in s:
        return "Runs the compile helper when class files are missing."
    if s.startswith("java "):
        return "Starts the Java application and includes both compiled classes and the MySQL JDBC driver on the classpath."
    return "PowerShell setup/run instruction."


def explain_markdown(line):
    s = line.strip()
    if not s:
        return "Blank line that separates README sections."
    if s.startswith("#"):
        return "Markdown heading used to organize the README."
    if s.startswith("|"):
        return "Markdown table row describing a feature, credential, or setup detail."
    if s.startswith("```"):
        return "Starts or ends a fenced code block."
    if s.startswith("-"):
        return "Bullet point in the documentation."
    if s.startswith(">"):
        return "Block quote used for a highlighted project note."
    return "Documentation text explaining project purpose, setup, architecture, or usage."


def explain_java(line, previous_nonempty=""):
    s = line.strip()
    if not s:
        return "Blank line used to separate logical parts of the Java file."
    if s.startswith("package "):
        return "Declares the Java package, which maps this file into the project's folder/module structure."
    if s.startswith("import "):
        return "Imports a Java or project class so it can be used by its short name in this file."
    if s.startswith("/**") or s.startswith("*") or s.startswith("*/"):
        return "Javadoc/comment text. It explains the purpose of the class, method, field, or block for readers."
    if s.startswith("//"):
        return "Inline comment that explains the intent of the nearby code."
    if " class " in f" {s} ":
        return "Declares a Java class. This is the main type defined by this file."
    if " interface " in f" {s} ":
        return "Declares a Java interface."
    if s.startswith("private static final Color"):
        return "Defines a reusable UI color constant for consistent styling across this Swing screen."
    if s.startswith("private static final String URL"):
        return "Defines the database URL, reading DB_URL from the environment or using the local MySQL default."
    if s.startswith("private static final String USER"):
        return "Defines the database username, reading DB_USER from the environment or defaulting to root."
    if s.startswith("private static final String PASSWORD"):
        return "Defines the database password, reading DB_PASSWORD from the environment or defaulting to blank."
    if s.startswith("private ") and ";" in s:
        return "Declares a private field that stores state, a DAO dependency, or a Swing component for later use."
    if s.startswith("public ") and "(" in s and ")" in s and "class" not in s:
        return "Declares a public constructor or method that other classes can call."
    if s.startswith("private ") and "(" in s and ")" in s:
        return "Declares a private helper method used internally by this class."
    if s.startswith("try "):
        return "Starts a try block for code that can throw an exception, often database or UI setup work."
    if s.startswith("catch "):
        return "Handles an exception so the program can show/log an error instead of crashing silently."
    if s.startswith("if "):
        return "Starts a conditional branch that only runs when the stated condition is true."
    if s.startswith("else"):
        return "Alternative branch that runs when the previous condition is false."
    if s.startswith("for "):
        return "Loops over a collection or array to process multiple items."
    if s.startswith("while "):
        return "Loops while a condition remains true, commonly while reading ResultSet rows."
    if "PreparedStatement" in s:
        return "Uses a PreparedStatement to run parameterized SQL safely."
    if "ResultSet" in s:
        return "Works with rows returned by a SQL SELECT query."
    if ".set" in s and "ps." in s:
        return "Binds a Java value into a PreparedStatement placeholder before executing SQL."
    if "executeQuery" in s:
        return "Runs a SELECT query and returns a ResultSet."
    if "executeUpdate" in s:
        return "Runs an INSERT, UPDATE, or DELETE and returns how many rows changed."
    if "DriverManager.getConnection" in s:
        return "Opens a JDBC connection to MySQL using the configured URL, username, and password."
    if "Class.forName" in s:
        return "Loads the MySQL JDBC driver class so DriverManager can create MySQL connections."
    if "SwingUtilities.invokeLater" in s:
        return "Schedules UI creation on Swing's Event Dispatch Thread, which is the correct thread for Swing components."
    if "new LoginFrame" in s:
        return "Creates or opens the login window."
    if "new DashboardFrame" in s:
        return "Creates or opens the dashboard window after successful login."
    if "new RegisterFrame" in s:
        return "Creates or opens the registration window."
    if "JOptionPane" in s:
        return "Shows a Swing dialog message to the user."
    if ".addActionListener" in s:
        return "Connects a button, combo box, or radio button action to code that should run when the user interacts with it."
    if ".add(" in s:
        return "Adds a Swing component to a container so it appears in the UI layout."
    if ".setText" in s:
        return "Updates visible text in a label, button, or field."
    if ".setFont" in s:
        return "Sets the font family, size, or weight for a Swing component."
    if ".setForeground" in s or ".setBackground" in s:
        return "Sets a Swing component's text color or background color."
    if ".setBorder" in s:
        return "Sets border/padding styling around a Swing component."
    if ".setSize" in s or ".setMinimumSize" in s or ".setPreferredSize" in s or ".setMaximumSize" in s:
        return "Controls the component/window dimensions used by the Swing layout."
    if "return " in s:
        return "Returns a value from the current method to its caller."
    if s in ("{", "}", "};", "});"):
        return "Closes or opens a Java block, method, class, lambda, or initializer."
    if "=" in s and s.endswith(";"):
        return "Assigns or initializes a value used by the surrounding method or class."
    return "Java statement that contributes to the surrounding class, method, UI layout, DAO query, or model behavior."


def explain_line(path, line, prev):
    if path.endswith(".sql"):
        return explain_sql(line)
    if path.endswith(".ps1"):
        return explain_powershell(line)
    if path.endswith(".md"):
        return explain_markdown(line)
    if path.endswith(".java"):
        return explain_java(line, prev)
    return "Project file line."


def file_intro(path):
    if path.endswith(".java"):
        return "Java source file. The following entries explain each line in order."
    if path.endswith(".sql"):
        return "SQL database setup file. The following entries explain each schema and seed-data line."
    if path.endswith(".ps1"):
        return "PowerShell helper script. The following entries explain each command line."
    if path.endswith(".md"):
        return "Markdown documentation file. The following entries explain each documentation line."
    return "Project file."


story = []
story.append(para("Stock Portfolio Tracker", "ReportTitle"))
story.append(para("Line-by-Line File-Wise Code Explanation", "Section"))
story.append(para("This report covers every human-authored project file: README, SQL schema, PowerShell helper scripts, and all Java source files. Generated .class files and the MySQL connector JAR are intentionally excluded because they are binary/build artifacts, not readable source code."))
story.append(para("How to start the project", "Section"))
for step in [
    "Open PowerShell and go to: C:\\Users\\adris\\Desktop\\DB Clg proj\\stock-market-portfolio-tracker",
    "Start MySQL with: powershell -ExecutionPolicy Bypass -File .\\start-mysql.ps1",
    "Import/update the database with: Get-Content .\\schema.sql | & \"C:\\Program Files\\MySQL\\MySQL Server 8.4\\bin\\mysql.exe\" -u root",
    "Set credentials with: $env:DB_USER='root' and $env:DB_PASSWORD=''",
    "Compile with: powershell -ExecutionPolicy Bypass -File .\\compile.ps1",
    "Run with: powershell -ExecutionPolicy Bypass -File .\\run.ps1",
    "Login using demo@tracker.com / password123 after the schema has been imported.",
]:
    story.append(para("• " + step))

for path in FILES:
    full = ROOT / path
    lines = full.read_text(encoding="utf-8", errors="replace").splitlines()
    story.append(PageBreak())
    story.append(para(path, "Section"))
    story.append(para(f"{file_intro(path)} Total lines: {len(lines)}."))
    prev = ""
    for i, line in enumerate(lines, start=1):
        explanation = explain_line(path, line, prev)
        code = line if line else "[blank line]"
        story.append(para(f"Line {i}: {code}", "CodeLine"))
        story.append(para(f"Explanation: {explanation}", "Explain"))
        if line.strip():
            prev = line.strip()


def footer(canvas, doc):
    canvas.saveState()
    canvas.setFont("Helvetica", 7)
    canvas.setFillColor(colors.HexColor("#555555"))
    canvas.drawString(0.7 * inch, 0.45 * inch, "Stock Portfolio Tracker - Line-by-Line Explanation")
    canvas.drawRightString(7.8 * inch, 0.45 * inch, f"Page {doc.page}")
    canvas.restoreState()


doc = SimpleDocTemplate(
    str(PDF_PATH),
    pagesize=LETTER,
    rightMargin=0.55 * inch,
    leftMargin=0.55 * inch,
    topMargin=0.55 * inch,
    bottomMargin=0.65 * inch,
)
doc.build(story, onFirstPage=footer, onLaterPages=footer)
print(PDF_PATH)
