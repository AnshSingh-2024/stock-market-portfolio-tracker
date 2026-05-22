$ErrorActionPreference = "Stop"

$mysqlExe = "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
$dbUser = if ($env:DB_USER) { $env:DB_USER } else { "root" }
$dbPassword = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { "" }

if (-not (Test-Path $mysqlExe)) {
    throw "mysql.exe was not found at $mysqlExe"
}

$authArgs = @("-u", $dbUser)
if ($dbPassword -ne "") {
    $authArgs += "-p$dbPassword"
}

Write-Host "Simulating market prices every 10 seconds. Press Ctrl+C to stop."

while ($true) {
    $sql = @"
USE stock_tracker;

UPDATE Market_Data md
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
    md.volume = GREATEST(1000, ROUND(md.volume * (1 + ((RAND() - 0.5) / 10))));

SELECT s.ticker, md.close_price
FROM Market_Data md
JOIN Stock s ON s.stock_id = md.stock_id
JOIN (
    SELECT stock_id, MAX(date_id) AS latest_date
    FROM Market_Data
    GROUP BY stock_id
) latest
    ON md.stock_id = latest.stock_id
   AND md.date_id = latest.latest_date
ORDER BY s.ticker;
"@

    $sql | & $mysqlExe @authArgs
    Start-Sleep -Seconds 10
}
