$ErrorActionPreference = "Stop"

$javaFiles = Get-ChildItem -Recurse -Filter *.java -Path "src" | ForEach-Object { $_.FullName }
javac -d "out" $javaFiles

Write-Host "Compiled Java classes into .\out"
