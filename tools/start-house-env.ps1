$mysqlConfig = "E:\project\study\all-project\tools\mysql-house.ini"
$mysqlExe = "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe"
$redisCli = "C:\Program Files\Redis\redis-cli.exe"
$startupTimeoutSeconds = 30

function Test-TcpListening {
    param(
        [Parameter(Mandatory = $true)]
        [int]$Port
    )

    try {
        $connection = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction Stop | Select-Object -First 1
        return $null -ne $connection
    } catch {
        $netstatMatch = netstat -ano | Select-String ":$Port" | Select-String "LISTENING"
        return $null -ne $netstatMatch
    }
}

function Wait-ForTcpListening {
    param(
        [Parameter(Mandatory = $true)]
        [int]$Port,
        [Parameter(Mandatory = $true)]
        [string]$ServiceName,
        [int]$TimeoutSeconds = 30
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-TcpListening -Port $Port) {
            return $true
        }
        Start-Sleep -Milliseconds 500
    }

    return $false
}

if (-not (Test-Path -LiteralPath $mysqlExe)) {
    throw "MySQL executable was not found: $mysqlExe"
}

$redisService = Get-Service -Name Redis -ErrorAction SilentlyContinue
if ($redisService -and $redisService.Status -ne "Running") {
    Start-Service Redis
}

$mysqlListening = Test-TcpListening -Port 3306
$mysqlProcess = Get-Process mysqld -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $mysqlListening -and -not $mysqlProcess) {
    Start-Process -FilePath $mysqlExe -ArgumentList "--defaults-file=$mysqlConfig" -WindowStyle Hidden
}

$mysqlReady = Wait-ForTcpListening -Port 3306 -ServiceName "MySQL" -TimeoutSeconds $startupTimeoutSeconds
$redisReady = if (Test-Path -LiteralPath $redisCli) { & $redisCli ping 2>$null } else { $null }

if (-not $mysqlReady) {
    throw "MySQL did not start correctly on port 3306 within $startupTimeoutSeconds seconds."
}

if ($redisReady -ne "PONG") {
    throw "Redis is not responding correctly on port 6379."
}

Write-Output "House backend environment is ready: MySQL 3306 and Redis 6379."
