$ErrorActionPreference = "Stop"

function Ensure-Dir([string]$path) {
    if (-not (Test-Path $path)) {
        New-Item -ItemType Directory -Path $path | Out-Null
    }
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$repoRoot = Split-Path -Parent $repoRoot

$flutterUiDir = Join-Path $repoRoot "flutter_ui"
$seedDir = Join-Path $repoRoot "flutter_ui_seed"

Write-Host "Repo root: $repoRoot"
Write-Host "Flutter UI dir: $flutterUiDir"

try {
    & flutter --version | Out-Null
} catch {
    throw "找不到 flutter 指令。請先安裝 Flutter SDK 並把 flutter/bin 加到 PATH。"
}

Ensure-Dir $flutterUiDir

# If the directory already looks like a flutter module, skip creation.
$metadataPath = Join-Path $flutterUiDir ".metadata"
$pubspecPath = Join-Path $flutterUiDir "pubspec.yaml"

if (-not (Test-Path $metadataPath)) {
    Write-Host "Creating Flutter module in flutter_ui/ ..."
    Push-Location $repoRoot
    try {
        & flutter create -t module flutter_ui
    } finally {
        Pop-Location
    }
} else {
    Write-Host "flutter_ui/.metadata exists, skipping flutter create."
}

if (-not (Test-Path $pubspecPath)) {
    throw "flutter_ui/pubspec.yaml 不存在，flutter module 可能建立失敗。"
}

if (Test-Path $seedDir) {
    $seedLibDir = Join-Path $seedDir "lib"
    $targetLibDir = Join-Path $flutterUiDir "lib"
    if (Test-Path $seedLibDir) {
        Ensure-Dir $targetLibDir

        $backupDir = Join-Path $flutterUiDir "lib_backup_before_seed"
        if (-not (Test-Path $backupDir) -and (Get-ChildItem -Path $targetLibDir -ErrorAction SilentlyContinue)) {
            Write-Host "Backing up existing flutter_ui/lib -> $backupDir"
            Copy-Item -Path $targetLibDir -Destination $backupDir -Recurse -Force
        }

        Write-Host "Copying seed UI into flutter_ui/lib ..."
        Copy-Item -Path (Join-Path $seedLibDir "*") -Destination $targetLibDir -Recurse -Force
    } else {
        Write-Host "Seed lib folder not found, skipping seed copy."
    }
} else {
    Write-Host "flutter_ui_seed/ not found, skipping seed copy."
}

Write-Host "Done. Next steps:"
Write-Host "  1) cd flutter_ui"
Write-Host "  2) flutter pub get"
Write-Host "  3) 回到 Android 專案做 Gradle include 與 Activity/Channel 串接"

