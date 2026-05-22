$ErrorActionPreference = "Stop"

if (-not (Test-Path "out")) {
    & ".\compile.ps1"
}

java -cp "out;lib\mysql-connector-j-8.3.0.jar" com.tracker.app.Main
