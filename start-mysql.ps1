$ErrorActionPreference = "Stop"

$mysqlBase = "C:\Program Files\MySQL\MySQL Server 8.4"
$dataDir = Join-Path $PSScriptRoot "mysql-data"

if (-not (Test-Path $dataDir)) {
    New-Item -ItemType Directory -Force -Path $dataDir | Out-Null
    & "$mysqlBase\bin\mysqld.exe" --initialize-insecure --basedir="$mysqlBase" --datadir="$dataDir"
}

$running = Get-Process mysqld -ErrorAction SilentlyContinue
if (-not $running) {
    $psi = [System.Diagnostics.ProcessStartInfo]::new()
    $psi.FileName = "$mysqlBase\bin\mysqld.exe"
    $psi.Arguments = "--basedir=`"$mysqlBase`" --datadir=`"$dataDir`" --port=3306 --log-error=`"$dataDir\server-start.err`""
    $psi.WorkingDirectory = $PSScriptRoot
    $psi.UseShellExecute = $false
    $psi.CreateNoWindow = $true
    [System.Diagnostics.Process]::Start($psi) | Out-Null
    Start-Sleep -Seconds 5
}

& "$mysqlBase\bin\mysql.exe" -u root -e "SELECT VERSION();"
