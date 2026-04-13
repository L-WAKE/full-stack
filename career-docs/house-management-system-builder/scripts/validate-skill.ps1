param(
    [string]$SkillPath = "",
    [string]$WorkspaceRoot = "E:\project\study\all-project",
    [string]$VenvPath = ""
)

$scriptRoot = if ($PSScriptRoot) { $PSScriptRoot } elseif ($env:CODEX_SKILL_SCRIPT_DIR) { $env:CODEX_SKILL_SCRIPT_DIR } else { (Get-Location).Path }

if (-not $SkillPath) {
    $SkillPath = (Resolve-Path (Join-Path $scriptRoot "..")).Path
}

if (-not $VenvPath) {
    $VenvPath = Join-Path $WorkspaceRoot "career-docs\.venv-skill-tools"
}

$pythonExe = Join-Path $VenvPath "Scripts\python.exe"
$validator = "C:\Users\admin\.codex\skills\.system\skill-creator\scripts\quick_validate.py"

if (-not (Test-Path $pythonExe)) {
    throw "未找到虚拟环境：$VenvPath。请先运行 bootstrap-skill-env.ps1。"
}

if (-not (Test-Path $validator)) {
    throw "未找到官方 skill 校验脚本：$validator。"
}

$env:PYTHONUTF8 = "1"
$env:PYTHONIOENCODING = "utf-8"

Write-Host "正在运行官方校验..."
& $pythonExe $validator $SkillPath
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "正在运行元数据检查..."
@'
from pathlib import Path
import re
import sys
import yaml

skill_path = Path(sys.argv[1])
skill_md = skill_path / "SKILL.md"
openai_yaml = skill_path / "agents" / "openai.yaml"

text = skill_md.read_text(encoding="utf-8")
frontmatter_match = re.match(r"^---\n(.*?)\n---", text, re.S)
if not frontmatter_match:
    raise SystemExit("SKILL.md 缺少 frontmatter。")

frontmatter = yaml.safe_load(frontmatter_match.group(1))
if frontmatter["name"] != skill_path.name:
    raise SystemExit(f"Skill 目录名 '{skill_path.name}' 与 frontmatter 中的 name '{frontmatter['name']}' 不一致。")

openai = yaml.safe_load(openai_yaml.read_text(encoding="utf-8"))
interface = openai.get("interface", {})
short_description = interface.get("short_description", "")
default_prompt = interface.get("default_prompt", "")

if not (25 <= len(short_description) <= 64):
    raise SystemExit(f"short_description 长度必须在 25 到 64 个字符之间，当前为 {len(short_description)}。")

expected_skill_ref = f"${frontmatter['name']}"
if expected_skill_ref not in default_prompt:
    raise SystemExit(f"default_prompt 必须包含 {expected_skill_ref}。")

print("元数据检查通过。")
'@ | & $pythonExe - $SkillPath
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "所有 skill 检查均已通过。"
