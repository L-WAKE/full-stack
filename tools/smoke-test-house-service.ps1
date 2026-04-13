[CmdletBinding()]
param(
    [switch]$UseExistingServer
)

$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Net.Http

$root = Split-Path -Parent $PSScriptRoot
$jarPath = Join-Path $root "backend/house-service/target/house-service-0.0.1-SNAPSHOT.jar"
$javaPath = "F:\applcation\jdk\bin\java.exe"

if (-not (Test-Path $jarPath)) {
    throw "Backend jar not found: $jarPath"
}

if (-not (Test-Path $javaPath)) {
    throw "Java executable not found: $javaPath"
}

$logPath = Join-Path $root "backend/house-service/house-service.smoke.log"
$errPath = Join-Path $root "backend/house-service/house-service.smoke.err.log"

if (Test-Path $logPath) {
    Remove-Item -LiteralPath $logPath -Force
}

if (Test-Path $errPath) {
    Remove-Item -LiteralPath $errPath -Force
}

$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = $javaPath
$psi.Arguments = "-jar $jarPath"
$psi.UseShellExecute = $false
$psi.RedirectStandardOutput = $true
$psi.RedirectStandardError = $true

$process = $null
$stdoutTask = $null
$stderrTask = $null

if (-not $UseExistingServer) {
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $psi

    $null = $process.Start()
    $stdoutTask = $process.StandardOutput.ReadToEndAsync()
    $stderrTask = $process.StandardError.ReadToEndAsync()
}

$baseUri = "http://127.0.0.1:8080"
function Invoke-JsonRequest {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Method,
        [Parameter(Mandatory = $true)]
        [string]$Uri,
        [string]$Token,
        [object]$Body
    )

    $headers = @{}
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }

    if ($PSBoundParameters.ContainsKey("Body")) {
        $json = $Body | ConvertTo-Json -Depth 6 -Compress
        return Invoke-RestMethod -Method $Method -Uri $Uri -Headers $headers -ContentType "application/json" -Body $json -TimeoutSec 10
    }

    return Invoke-RestMethod -Method $Method -Uri $Uri -Headers $headers -TimeoutSec 10
}

try {
    $ready = $false
    for ($i = 0; $i -lt 30; $i++) {
        Start-Sleep -Seconds 2
        try {
            $login = Invoke-JsonRequest -Method Post -Uri "$baseUri/api/auth/login" -Body @{
                username = "admin"
                password = "Admin@123"
            }
            $ready = $true
            break
        } catch {
        }
    }

    if (-not $ready) {
        if ($UseExistingServer) {
            throw "Existing backend server did not respond within 60 seconds."
        }
        throw "Backend did not become ready within 60 seconds."
    }

    $token = $login.data.token
    $dashboard = Invoke-JsonRequest -Method Get -Uri "$baseUri/api/dashboard/overview" -Token $token

    $maintenanceCreated = Invoke-JsonRequest -Method Post -Uri "$baseUri/api/maintenance-orders" -Token $token -Body @{
        houseName = "SMOKE-HOUSE-A01"
        issueType = "AC_FAULT"
        reporterName = "smoke-test"
        assigneeName = "engineer-li"
        priority = "HIGH"
        status = "PENDING"
    }
    $maintenanceId = $maintenanceCreated.data.id

    $maintenanceUpdated = Invoke-JsonRequest -Method Put -Uri "$baseUri/api/maintenance-orders/$maintenanceId" -Token $token -Body @{
        houseName = "SMOKE-HOUSE-A01"
        issueType = "AC_LEAK"
        reporterName = "smoke-test"
        assigneeName = "engineer-zhang"
        priority = "URGENT"
        status = "PROCESSING"
    }

    $maintenanceDeleted = Invoke-JsonRequest -Method Delete -Uri "$baseUri/api/maintenance-orders/$maintenanceId" -Token $token

    $cleaningCreated = Invoke-JsonRequest -Method Post -Uri "$baseUri/api/cleaning-orders" -Token $token -Body @{
        houseName = "SMOKE-APT-B02"
        cleaningType = "DEEP"
        appointmentTime = "2026-04-14 10:00"
        assigneeName = "cleaner-wang"
        status = "SCHEDULED"
    }
    $cleaningId = $cleaningCreated.data.id

    $cleaningUpdated = Invoke-JsonRequest -Method Put -Uri "$baseUri/api/cleaning-orders/$cleaningId" -Token $token -Body @{
        houseName = "SMOKE-APT-B02"
        cleaningType = "MOVE_IN"
        appointmentTime = "2026-04-15 14:00"
        assigneeName = "cleaner-zhao"
        status = "IN_PROGRESS"
    }

    $cleaningDeleted = Invoke-JsonRequest -Method Delete -Uri "$baseUri/api/cleaning-orders/$cleaningId" -Token $token

    [pscustomobject]@{
        loginCode = $login.code
        dashboardCode = $dashboard.code
        maintenanceCreateId = $maintenanceId
        maintenanceUpdateStatus = $maintenanceUpdated.data.status
        maintenanceDeleteCode = $maintenanceDeleted.code
        cleaningCreateId = $cleaningId
        cleaningUpdateStatus = $cleaningUpdated.data.status
        cleaningDeleteCode = $cleaningDeleted.code
    } | ConvertTo-Json -Depth 6
}
finally {
    if ($process -ne $null) {
        if (-not $process.HasExited) {
            $process.Kill()
        }
        $process.WaitForExit()
        [System.IO.File]::WriteAllText($logPath, $stdoutTask.Result)
        [System.IO.File]::WriteAllText($errPath, $stderrTask.Result)
    }
}
