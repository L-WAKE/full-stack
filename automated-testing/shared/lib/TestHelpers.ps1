Set-StrictMode -Version Latest

Add-Type -AssemblyName System.Net.Http

function New-TestState {
    param(
        [string]$BaseUri
    )

    [pscustomobject]@{
        BaseUri      = $BaseUri.TrimEnd('/')
        StartedAt    = Get-Date
        Results      = New-Object System.Collections.Generic.List[object]
        CleanupTasks = New-Object System.Collections.Generic.List[object]
        Token        = $null
    }
}

function Add-TestResult {
    param(
        [Parameter(Mandatory = $true)]
        [object]$State,
        [Parameter(Mandatory = $true)]
        [string]$Name,
        [Parameter(Mandatory = $true)]
        [string]$Status,
        [string]$Details = "",
        [object]$Data = $null
    )

    $State.Results.Add([pscustomobject]@{
        name      = $Name
        status    = $Status
        details   = $Details
        data      = $Data
        timestamp = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
    })
}

function Register-CleanupTask {
    param(
        [Parameter(Mandatory = $true)]
        [object]$State,
        [Parameter(Mandatory = $true)]
        [string]$Name,
        [Parameter(Mandatory = $true)]
        [scriptblock]$Action
    )

    $State.CleanupTasks.Add([pscustomobject]@{
        Name   = $Name
        Action = $Action
    })
}

function Invoke-CleanupTasks {
    param(
        [Parameter(Mandatory = $true)]
        [object]$State
    )

    for ($index = $State.CleanupTasks.Count - 1; $index -ge 0; $index--) {
        $task = $State.CleanupTasks[$index]
        try {
            & $task.Action
            Add-TestResult -State $State -Name "清理::$($task.Name)" -Status "PASS" -Details "清理完成。"
        } catch {
            Add-TestResult -State $State -Name "清理::$($task.Name)" -Status "WARN" -Details $_.Exception.Message
        }
    }
}

function Invoke-ApiRequest {
    param(
        [Parameter(Mandatory = $true)]
        [ValidateSet("GET", "POST", "PUT", "DELETE")]
        [string]$Method,
        [Parameter(Mandatory = $true)]
        [string]$Uri,
        [string]$Token,
        [object]$Body
    )

    $client = [System.Net.Http.HttpClient]::new()
    try {
        $request = [System.Net.Http.HttpRequestMessage]::new([System.Net.Http.HttpMethod]::$Method, $Uri)
        $request.Headers.Accept.Add([System.Net.Http.Headers.MediaTypeWithQualityHeaderValue]::new("application/json"))

        if ($Token) {
            $request.Headers.Authorization = [System.Net.Http.Headers.AuthenticationHeaderValue]::new("Bearer", $Token)
        }

        if ($PSBoundParameters.ContainsKey("Body")) {
            $json = $Body | ConvertTo-Json -Depth 10 -Compress
            $request.Content = [System.Net.Http.StringContent]::new($json, [System.Text.Encoding]::UTF8, "application/json")
        }

        $response = $client.SendAsync($request).GetAwaiter().GetResult()
        $rawContent = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()

        $parsed = $null
        if (-not [string]::IsNullOrWhiteSpace($rawContent)) {
            try {
                $parsed = $rawContent | ConvertFrom-Json
            } catch {
                $parsed = $null
            }
        }

        [pscustomobject]@{
            StatusCode          = [int]$response.StatusCode
            IsSuccessStatusCode = [bool]$response.IsSuccessStatusCode
            Json                = $parsed
            RawContent          = $rawContent
        }
    } finally {
        $client.Dispose()
    }
}

function Assert-True {
    param(
        [Parameter(Mandatory = $true)]
        [bool]$Condition,
        [Parameter(Mandatory = $true)]
        [string]$Message
    )

    if (-not $Condition) {
        throw $Message
    }
}

function Assert-Equal {
    param(
        [Parameter(Mandatory = $true)]
        $Actual,
        [Parameter(Mandatory = $true)]
        $Expected,
        [Parameter(Mandatory = $true)]
        [string]$Message
    )

    if ($Actual -ne $Expected) {
        throw "$Message Expected: $Expected, Actual: $Actual"
    }
}

function Wait-ForBackendReady {
    param(
        [Parameter(Mandatory = $true)]
        [string]$BaseUri,
        [Parameter(Mandatory = $true)]
        [string]$Username,
        [Parameter(Mandatory = $true)]
        [string]$Password,
        [int]$TimeoutSeconds = 60
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        try {
            $response = Invoke-ApiRequest -Method POST -Uri "$($BaseUri.TrimEnd('/'))/api/auth/login" -Body @{
                username = $Username
                password = $Password
            }
            if ($response.StatusCode -eq 200 -and $response.Json.code -eq 0) {
                return $true
            }
        } catch {
        }
        Start-Sleep -Seconds 2
    } while ((Get-Date) -lt $deadline)

    return $false
}

function Invoke-TestStep {
    param(
        [Parameter(Mandatory = $true)]
        [object]$State,
        [Parameter(Mandatory = $true)]
        [string]$Name,
        [Parameter(Mandatory = $true)]
        [scriptblock]$Action
    )

    try {
        $data = & $Action
        Add-TestResult -State $State -Name $Name -Status "PASS" -Details "步骤执行完成。" -Data $data
        return $data
    } catch {
        Add-TestResult -State $State -Name $Name -Status "FAIL" -Details $_.Exception.Message
        throw
    }
}

function Write-TestArtifacts {
    param(
        [Parameter(Mandatory = $true)]
        [object]$State,
        [Parameter(Mandatory = $true)]
        [string]$OutputDirectory,
        [Parameter(Mandatory = $true)]
        [string]$RunName
    )

    if (-not (Test-Path $OutputDirectory)) {
        New-Item -ItemType Directory -Path $OutputDirectory | Out-Null
    }

    $endedAt = Get-Date
    $summary = [pscustomobject]@{
        runName   = $RunName
        baseUri   = $State.BaseUri
        startedAt = $State.StartedAt.ToString("yyyy-MM-dd HH:mm:ss")
        endedAt   = $endedAt.ToString("yyyy-MM-dd HH:mm:ss")
        duration  = [math]::Round(($endedAt - $State.StartedAt).TotalSeconds, 2)
        total     = $State.Results.Count
        passed    = @($State.Results | Where-Object { $_.status -eq "PASS" }).Count
        failed    = @($State.Results | Where-Object { $_.status -eq "FAIL" }).Count
        warnings  = @($State.Results | Where-Object { $_.status -eq "WARN" }).Count
        results   = $State.Results
    }

    $jsonPath = Join-Path $OutputDirectory "$RunName.json"
    $mdPath = Join-Path $OutputDirectory "$RunName.md"

    $summary | ConvertTo-Json -Depth 20 | Set-Content -Path $jsonPath -Encoding UTF8

    $lines = New-Object System.Collections.Generic.List[string]
    $lines.Add("# 自动化测试报告")
    $lines.Add("")
    $lines.Add("- 运行名称：$RunName")
    $lines.Add("- 服务地址：$($summary.baseUri)")
    $lines.Add("- 开始时间：$($summary.startedAt)")
    $lines.Add("- 结束时间：$($summary.endedAt)")
    $lines.Add("- 耗时（秒）：$($summary.duration)")
    $lines.Add("- 通过：$($summary.passed)")
    $lines.Add("- 失败：$($summary.failed)")
    $lines.Add("- 告警：$($summary.warnings)")
    $lines.Add("")
    $lines.Add("## 步骤结果")
    $lines.Add("")
    foreach ($item in $State.Results) {
        $lines.Add("- [$($item.status)] $($item.name): $($item.details)")
    }
    $lines | Set-Content -Path $mdPath -Encoding UTF8

    [pscustomobject]@{
        JsonPath = $jsonPath
        MdPath   = $mdPath
        Summary  = $summary
    }
}
