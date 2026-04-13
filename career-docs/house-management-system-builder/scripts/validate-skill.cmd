@echo off
set "SCRIPT_DIR=%~dp0"
set "CODEX_SKILL_SCRIPT_DIR=%SCRIPT_DIR%"
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "& ([scriptblock]::Create((Get-Content -Raw -Encoding UTF8 '%SCRIPT_DIR%validate-skill.ps1'))) %*"
