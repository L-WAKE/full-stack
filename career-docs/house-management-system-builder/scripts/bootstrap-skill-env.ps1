param(
    [string]$WorkspaceRoot = "E:\project\study\all-project",
    [string]$VenvPath = ""
)

$scriptRoot = if ($PSScriptRoot) { $PSScriptRoot } elseif ($env:CODEX_SKILL_SCRIPT_DIR) { $env:CODEX_SKILL_SCRIPT_DIR } else { (Get-Location).Path }

if (-not $VenvPath) {
    $VenvPath = Join-Path $WorkspaceRoot "career-docs\.venv-skill-tools"
}

$requirementsPath = Join-Path $scriptRoot "requirements.txt"
$pythonExe = Join-Path $VenvPath "Scripts\python.exe"

if (-not (Test-Path $pythonExe)) {
    Write-Host "正在创建虚拟环境：$VenvPath"
    python -m venv $VenvPath
    if ($LASTEXITCODE -ne 0) {
        throw "创建虚拟环境失败。"
    }
}

Write-Host "正在安装 Python 依赖：$requirementsPath"
& $pythonExe -m pip install --disable-pip-version-check -r $requirementsPath
if ($LASTEXITCODE -ne 0) {
    throw "安装 Python 依赖失败。"
}

Write-Host "Skill 本地环境已准备完成。"
