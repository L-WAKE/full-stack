[CmdletBinding()]
param(
    [string]$BaseUri = "http://127.0.0.1:8080",
    [string]$Username = "admin",
    [string]$Password = "Admin@123",
    [int]$ReadyTimeoutSeconds = 60,
    [switch]$BootstrapEnvironment,
    [switch]$SkipCleanup
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
. (Join-Path (Split-Path -Parent (Split-Path -Parent $scriptRoot)) "shared/lib/TestHelpers.ps1")

$state = New-TestState -BaseUri $BaseUri
$runName = "house-api-test-" + (Get-Date -Format "yyyyMMdd-HHmmss")
$reportsDir = Join-Path (Split-Path -Parent $scriptRoot) "reports"
$uniqueSuffix = Get-Date -Format "yyyyMMddHHmmss"

if ($BootstrapEnvironment) {
    Invoke-TestStep -State $state -Name "准备依赖环境" -Action {
        & (Join-Path (Split-Path -Parent $scriptRoot) "tools/start-house-env.ps1") | Out-String
    } | Out-Null
}

Invoke-TestStep -State $state -Name "等待后端服务就绪" -Action {
    $ready = Wait-ForBackendReady -BaseUri $BaseUri -Username $Username -Password $Password -TimeoutSeconds $ReadyTimeoutSeconds
    Assert-True -Condition $ready -Message "后端服务在 $ReadyTimeoutSeconds 秒内未就绪，请先启动 backend/house-service。"
    "后端服务已就绪。"
} | Out-Null

try {
    Invoke-TestStep -State $state -Name "验证匿名访问被拦截" -Action {
        $response = Invoke-ApiRequest -Method GET -Uri "$BaseUri/api/auth/profile"
        Assert-True -Condition ($response.StatusCode -in @(401, 403)) -Message "未登录访问个人信息接口时应被拦截。"
        [pscustomobject]@{ statusCode = $response.StatusCode }
    } | Out-Null

    $loginResult = Invoke-TestStep -State $state -Name "管理员账号登录" -Action {
        $response = Invoke-ApiRequest -Method POST -Uri "$BaseUri/api/auth/login" -Body @{
            username = $Username
            password = $Password
        }
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "登录接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "登录接口业务码不符合预期。"
        Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($response.Json.data.token)) -Message "登录成功后返回的 token 不能为空。"
        $response.Json.data
    }
    $state.Token = $loginResult.token

    Invoke-TestStep -State $state -Name "读取当前用户信息" -Action {
        $response = Invoke-ApiRequest -Method GET -Uri "$BaseUri/api/auth/profile" -Token $state.Token
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "个人信息接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "个人信息接口业务码不符合预期。"
        Assert-Equal -Actual $response.Json.data.username -Expected $Username -Message "当前用户信息中的用户名不正确。"
        $response.Json.data
    } | Out-Null

    Invoke-TestStep -State $state -Name "加载菜单数据" -Action {
        $response = Invoke-ApiRequest -Method GET -Uri "$BaseUri/api/auth/menus" -Token $state.Token
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "菜单接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "菜单接口业务码不符合预期。"
        Assert-True -Condition (@($response.Json.data).Count -gt 0) -Message "菜单列表不应为空。"
        [pscustomobject]@{ menuCount = @($response.Json.data).Count }
    } | Out-Null

    Invoke-TestStep -State $state -Name "加载看板概览" -Action {
        $response = Invoke-ApiRequest -Method GET -Uri "$BaseUri/api/dashboard/overview" -Token $state.Token
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "看板概览接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "看板概览接口业务码不符合预期。"
        $response.Json.data
    } | Out-Null

    Invoke-TestStep -State $state -Name "加载看板趋势" -Action {
        $response = Invoke-ApiRequest -Method GET -Uri "$BaseUri/api/dashboard/trend" -Token $state.Token
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "看板趋势接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "看板趋势接口业务码不符合预期。"
        Assert-True -Condition (@($response.Json.data.labels).Count -gt 0) -Message "看板趋势标签不应为空。"
        $response.Json.data
    } | Out-Null

    $landlordName = "AUTO-LANDLORD-$uniqueSuffix"
    $tenantName = "AUTO-TENANT-$uniqueSuffix"
    $houseName = "AUTO-HOUSE-$uniqueSuffix"

    $landlord = Invoke-TestStep -State $state -Name "新增业主" -Action {
        $response = Invoke-ApiRequest -Method POST -Uri "$BaseUri/api/landlords" -Token $state.Token -Body @{
            name        = $landlordName
            mobile      = "1380000$($uniqueSuffix.Substring($uniqueSuffix.Length - 4))"
            idNo        = "50010119900101$($uniqueSuffix.Substring($uniqueSuffix.Length - 4))"
            address     = "Auto Test District"
            bankAccount = "62220000$($uniqueSuffix.Substring($uniqueSuffix.Length - 6))"
            remark      = "automation-created"
        }
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "新增业主接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "新增业主接口业务码不符合预期。"
        Assert-Equal -Actual $response.Json.data.name -Expected $landlordName -Message "新增后的业主姓名不正确。"
        $response.Json.data
    }
    Register-CleanupTask -State $state -Name "删除业主 $($landlord.id)" -Action {
        Invoke-ApiRequest -Method DELETE -Uri "$BaseUri/api/landlords/$($landlord.id)" -Token $state.Token | Out-Null
    }

    Invoke-TestStep -State $state -Name "按关键字查询业主" -Action {
        $response = Invoke-ApiRequest -Method GET -Uri "$BaseUri/api/landlords?pageNum=1&pageSize=10&keyword=$landlordName" -Token $state.Token
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "查询业主接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "查询业主接口业务码不符合预期。"
        Assert-True -Condition (@($response.Json.data.list | Where-Object { $_.id -eq $landlord.id }).Count -gt 0) -Message "业主列表中没有查到刚新增的数据。"
        [pscustomobject]@{ total = $response.Json.data.total }
    } | Out-Null

    Invoke-TestStep -State $state -Name "加载业主下拉选项" -Action {
        $response = Invoke-ApiRequest -Method GET -Uri "$BaseUri/api/landlords/options" -Token $state.Token
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "业主下拉选项接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "业主下拉选项接口业务码不符合预期。"
        Assert-True -Condition (@($response.Json.data | Where-Object { $_.id -eq $landlord.id }).Count -gt 0) -Message "业主下拉选项中缺少刚新增的业主。"
        [pscustomobject]@{ optionCount = @($response.Json.data).Count }
    } | Out-Null

    Invoke-TestStep -State $state -Name "更新业主" -Action {
        $response = Invoke-ApiRequest -Method PUT -Uri "$BaseUri/api/landlords/$($landlord.id)" -Token $state.Token -Body @{
            name        = "$landlordName-UPDATED"
            mobile      = "1390000$($uniqueSuffix.Substring($uniqueSuffix.Length - 4))"
            idNo        = "50010119910101$($uniqueSuffix.Substring($uniqueSuffix.Length - 4))"
            address     = "Auto Test District Updated"
            bankAccount = "62221111$($uniqueSuffix.Substring($uniqueSuffix.Length - 6))"
            remark      = "automation-updated"
        }
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "更新业主接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "更新业主接口业务码不符合预期。"
        Assert-Equal -Actual $response.Json.data.name -Expected "$landlordName-UPDATED" -Message "更新后的业主姓名不正确。"
        $response.Json.data
    } | Out-Null
    $landlordName = "$landlordName-UPDATED"

    $tenant = Invoke-TestStep -State $state -Name "新增租客" -Action {
        $response = Invoke-ApiRequest -Method POST -Uri "$BaseUri/api/tenants" -Token $state.Token -Body @{
            name             = $tenantName
            mobile           = "1370000$($uniqueSuffix.Substring($uniqueSuffix.Length - 4))"
            gender           = "MALE"
            houseName        = $houseName
            checkinDate      = "2026-04-14"
            checkoutDate     = "2026-10-14"
            emergencyContact = "AUTO-CONTACT"
            emergencyMobile  = "1360000$($uniqueSuffix.Substring($uniqueSuffix.Length - 4))"
        }
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "新增租客接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "新增租客接口业务码不符合预期。"
        Assert-Equal -Actual $response.Json.data.name -Expected $tenantName -Message "新增后的租客姓名不正确。"
        $response.Json.data
    }
    Register-CleanupTask -State $state -Name "删除租客 $($tenant.id)" -Action {
        Invoke-ApiRequest -Method DELETE -Uri "$BaseUri/api/tenants/$($tenant.id)" -Token $state.Token | Out-Null
    }

    Invoke-TestStep -State $state -Name "按关键字查询租客" -Action {
        $response = Invoke-ApiRequest -Method GET -Uri "$BaseUri/api/tenants?pageNum=1&pageSize=10&keyword=$tenantName" -Token $state.Token
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "查询租客接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "查询租客接口业务码不符合预期。"
        Assert-True -Condition (@($response.Json.data.list | Where-Object { $_.id -eq $tenant.id }).Count -gt 0) -Message "租客列表中没有查到刚新增的数据。"
        [pscustomobject]@{ total = $response.Json.data.total }
    } | Out-Null

    Invoke-TestStep -State $state -Name "更新租客" -Action {
        $response = Invoke-ApiRequest -Method PUT -Uri "$BaseUri/api/tenants/$($tenant.id)" -Token $state.Token -Body @{
            name             = "$tenantName-UPDATED"
            mobile           = "1350000$($uniqueSuffix.Substring($uniqueSuffix.Length - 4))"
            gender           = "FEMALE"
            houseName        = "$houseName-UPDATED"
            checkinDate      = "2026-04-15"
            checkoutDate     = "2026-11-15"
            emergencyContact = "AUTO-CONTACT-2"
            emergencyMobile  = "1340000$($uniqueSuffix.Substring($uniqueSuffix.Length - 4))"
        }
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "更新租客接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "更新租客接口业务码不符合预期。"
        Assert-Equal -Actual $response.Json.data.name -Expected "$tenantName-UPDATED" -Message "更新后的租客姓名不正确。"
        $response.Json.data
    } | Out-Null
    $tenantName = "$tenantName-UPDATED"

    $house = Invoke-TestStep -State $state -Name "新增房源" -Action {
        $response = Invoke-ApiRequest -Method POST -Uri "$BaseUri/api/houses" -Token $state.Token -Body @{
            houseName    = $houseName
            rentalMode   = "WHOLE"
            projectName  = "AUTO-PROJECT"
            address      = "Automation Street 18"
            area         = 88.5
            rentPrice    = 4200
            status       = "VACANT"
            landlordName = $landlordName
            tenantName   = $tenantName
        }
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "新增房源接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "新增房源接口业务码不符合预期。"
        Assert-Equal -Actual $response.Json.data.houseName -Expected $houseName -Message "新增后的房源名称不正确。"
        $response.Json.data
    }
    Register-CleanupTask -State $state -Name "删除房源 $($house.id)" -Action {
        Invoke-ApiRequest -Method DELETE -Uri "$BaseUri/api/houses/$($house.id)" -Token $state.Token | Out-Null
    }

    Invoke-TestStep -State $state -Name "按关键字查询房源" -Action {
        $response = Invoke-ApiRequest -Method GET -Uri "$BaseUri/api/houses?pageNum=1&pageSize=10&keyword=$houseName&rentalMode=WHOLE" -Token $state.Token
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "查询房源接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "查询房源接口业务码不符合预期。"
        Assert-True -Condition (@($response.Json.data.list | Where-Object { $_.id -eq $house.id }).Count -gt 0) -Message "房源列表中没有查到刚新增的数据。"
        [pscustomobject]@{ total = $response.Json.data.total }
    } | Out-Null

    Invoke-TestStep -State $state -Name "读取房源详情" -Action {
        $response = Invoke-ApiRequest -Method GET -Uri "$BaseUri/api/houses/$($house.id)" -Token $state.Token
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "房源详情接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "房源详情接口业务码不符合预期。"
        Assert-Equal -Actual $response.Json.data.houseName -Expected $houseName -Message "房源详情中的名称不正确。"
        $response.Json.data
    } | Out-Null

    Invoke-TestStep -State $state -Name "验证房源非法参数被拒绝" -Action {
        $response = Invoke-ApiRequest -Method POST -Uri "$BaseUri/api/houses" -Token $state.Token -Body @{
            houseName    = ""
            rentalMode   = "WHOLE"
            projectName  = "AUTO-PROJECT"
            address      = "Automation Street 18"
            area         = 88.5
            rentPrice    = 4200
            status       = "VACANT"
            landlordName = $landlordName
            tenantName   = $tenantName
        }
        Assert-Equal -Actual $response.StatusCode -Expected 400 -Message "非法房源参数应返回 HTTP 400。"
        Assert-Equal -Actual $response.Json.code -Expected 4001 -Message "非法房源参数返回的业务码不正确。"
        $response.Json
    } | Out-Null

    Invoke-TestStep -State $state -Name "更新房源" -Action {
        $response = Invoke-ApiRequest -Method PUT -Uri "$BaseUri/api/houses/$($house.id)" -Token $state.Token -Body @{
            houseName    = "$houseName-UPDATED"
            rentalMode   = "SHARED"
            projectName  = "AUTO-PROJECT-UPDATED"
            address      = "Automation Street 88"
            area         = 90.0
            rentPrice    = 4600
            status       = "OCCUPIED"
            landlordName = $landlordName
            tenantName   = $tenantName
        }
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "更新房源接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "更新房源接口业务码不符合预期。"
        Assert-Equal -Actual $response.Json.data.rentalMode -Expected "SHARED" -Message "更新后的房源出租模式不正确。"
        $response.Json.data
    } | Out-Null
    $houseName = "$houseName-UPDATED"

    Invoke-TestStep -State $state -Name "更新房源状态" -Action {
        $response = Invoke-ApiRequest -Method PUT -Uri "$BaseUri/api/houses/$($house.id)/status" -Token $state.Token -Body @{
            status = "VACANT"
        }
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "更新房源状态接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "更新房源状态接口业务码不符合预期。"
        Assert-Equal -Actual $response.Json.data.status -Expected "VACANT" -Message "更新后的房源状态不正确。"
        $response.Json.data
    } | Out-Null

    $maintenance = Invoke-TestStep -State $state -Name "新增维修工单" -Action {
        $response = Invoke-ApiRequest -Method POST -Uri "$BaseUri/api/maintenance-orders" -Token $state.Token -Body @{
            houseName    = $houseName
            issueType    = "WATER_LEAK"
            reporterName = "automation"
            assigneeName = "engineer-auto"
            priority     = "HIGH"
            status       = "PENDING"
        }
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "新增维修工单接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "新增维修工单接口业务码不符合预期。"
        Assert-Equal -Actual $response.Json.data.houseName -Expected $houseName -Message "新增后的维修工单房源名称不正确。"
        $response.Json.data
    }
    Register-CleanupTask -State $state -Name "删除维修工单 $($maintenance.id)" -Action {
        Invoke-ApiRequest -Method DELETE -Uri "$BaseUri/api/maintenance-orders/$($maintenance.id)" -Token $state.Token | Out-Null
    }

    Invoke-TestStep -State $state -Name "查询维修工单" -Action {
        $response = Invoke-ApiRequest -Method GET -Uri "$BaseUri/api/maintenance-orders?pageNum=1&pageSize=10&keyword=$houseName" -Token $state.Token
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "查询维修工单接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "查询维修工单接口业务码不符合预期。"
        Assert-True -Condition (@($response.Json.data.list | Where-Object { $_.id -eq $maintenance.id }).Count -gt 0) -Message "维修工单列表中没有查到刚新增的数据。"
        [pscustomobject]@{ total = $response.Json.data.total }
    } | Out-Null

    Invoke-TestStep -State $state -Name "更新维修工单" -Action {
        $response = Invoke-ApiRequest -Method PUT -Uri "$BaseUri/api/maintenance-orders/$($maintenance.id)" -Token $state.Token -Body @{
            houseName    = $houseName
            issueType    = "AC_FAULT"
            reporterName = "automation-updated"
            assigneeName = "engineer-auto-2"
            priority     = "URGENT"
            status       = "PROCESSING"
        }
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "更新维修工单接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "更新维修工单接口业务码不符合预期。"
        Assert-Equal -Actual $response.Json.data.status -Expected "PROCESSING" -Message "更新后的维修工单状态不正确。"
        $response.Json.data
    } | Out-Null

    $cleaning = Invoke-TestStep -State $state -Name "新增保洁工单" -Action {
        $response = Invoke-ApiRequest -Method POST -Uri "$BaseUri/api/cleaning-orders" -Token $state.Token -Body @{
            houseName       = $houseName
            cleaningType    = "DEEP"
            appointmentTime = "2026-04-15 10:00"
            assigneeName    = "cleaner-auto"
            status          = "ASSIGNED"
        }
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "新增保洁工单接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "新增保洁工单接口业务码不符合预期。"
        Assert-Equal -Actual $response.Json.data.houseName -Expected $houseName -Message "新增后的保洁工单房源名称不正确。"
        $response.Json.data
    }
    Register-CleanupTask -State $state -Name "删除保洁工单 $($cleaning.id)" -Action {
        Invoke-ApiRequest -Method DELETE -Uri "$BaseUri/api/cleaning-orders/$($cleaning.id)" -Token $state.Token | Out-Null
    }

    Invoke-TestStep -State $state -Name "查询保洁工单" -Action {
        $response = Invoke-ApiRequest -Method GET -Uri "$BaseUri/api/cleaning-orders?pageNum=1&pageSize=10&keyword=$houseName" -Token $state.Token
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "查询保洁工单接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "查询保洁工单接口业务码不符合预期。"
        Assert-True -Condition (@($response.Json.data.list | Where-Object { $_.id -eq $cleaning.id }).Count -gt 0) -Message "保洁工单列表中没有查到刚新增的数据。"
        [pscustomobject]@{ total = $response.Json.data.total }
    } | Out-Null

    Invoke-TestStep -State $state -Name "更新保洁工单" -Action {
        $response = Invoke-ApiRequest -Method PUT -Uri "$BaseUri/api/cleaning-orders/$($cleaning.id)" -Token $state.Token -Body @{
            houseName       = $houseName
            cleaningType    = "MOVE_IN"
            appointmentTime = "2026-04-16 15:00"
            assigneeName    = "cleaner-auto-2"
            status          = "IN_PROGRESS"
        }
        Assert-Equal -Actual $response.StatusCode -Expected 200 -Message "更新保洁工单接口 HTTP 状态码不符合预期。"
        Assert-Equal -Actual $response.Json.code -Expected 0 -Message "更新保洁工单接口业务码不符合预期。"
        Assert-Equal -Actual $response.Json.data.status -Expected "IN_PROGRESS" -Message "更新后的保洁工单状态不正确。"
        $response.Json.data
    } | Out-Null
} finally {
    if (-not $SkipCleanup) {
        Invoke-CleanupTasks -State $state
    } else {
        Add-TestResult -State $state -Name "跳过清理" -Status "WARN" -Details "本次运行使用了 SkipCleanup 参数，测试数据已保留。"
    }

    if ($state.Token) {
        try {
            Invoke-ApiRequest -Method POST -Uri "$BaseUri/api/auth/logout" -Token $state.Token | Out-Null
            Add-TestResult -State $state -Name "退出登录" -Status "PASS" -Details "退出登录完成。"
        } catch {
            Add-TestResult -State $state -Name "退出登录" -Status "WARN" -Details $_.Exception.Message
        }
    }

    $artifacts = Write-TestArtifacts -State $state -OutputDirectory $reportsDir -RunName $runName
    Write-Host ""
    Write-Host "自动化测试流程已完成。"
    Write-Host "JSON 报告：$($artifacts.JsonPath)"
    Write-Host "Markdown 报告：$($artifacts.MdPath)"
    Write-Host "通过：$($artifacts.Summary.passed)  失败：$($artifacts.Summary.failed)  告警：$($artifacts.Summary.warnings)"

    if ($artifacts.Summary.failed -gt 0) {
        exit 1
    }
}
