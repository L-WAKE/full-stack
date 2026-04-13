$mysqlProcesses = Get-Process mysqld -ErrorAction SilentlyContinue
if ($mysqlProcesses) {
    $mysqlProcesses | ForEach-Object {
        Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
    }
}

Write-Output "House MySQL process stopped. Redis service is left running."
