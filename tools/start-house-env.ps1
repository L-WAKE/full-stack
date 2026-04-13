$mysqlConfig = "E:\project\study\all-project\tools\mysql-house.ini"
$mysqlExe = "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe"
$redisService = Get-Service -Name Redis -ErrorAction SilentlyContinue

if ($redisService -and $redisService.Status -ne "Running") {
    Start-Service Redis
}

$mysqlListening = netstat -ano | Select-String ":3306" | Select-String "LISTENING"
if (-not $mysqlListening) {
    Start-Process -FilePath $mysqlExe -ArgumentList "--defaults-file=$mysqlConfig"
    Start-Sleep -Seconds 5
}

$mysqlReady = netstat -ano | Select-String ":3306" | Select-String "LISTENING"
$redisReady = & "C:\Program Files\Redis\redis-cli.exe" ping 2>$null

if (-not $mysqlReady) {
    throw "MySQL did not start correctly on port 3306."
}

if ($redisReady -ne "PONG") {
    throw "Redis is not responding correctly on port 6379."
}

Write-Output "House backend environment is ready: MySQL 3306 and Redis 6379."
