# ==============================================================================
# SCRIPT DE DESPLIEGUE COMPLETO - Finanzas Proactivas
# ==============================================================================
# Este script ejecuta en orden:
#   1. Compila el APK de la app Android
#   2. Abre la carpeta donde está el APK generado
#   3. Hace push de todas las modificaciones a Git
#   4. Despliega el backend en Vercel
# ==============================================================================

param(
    [switch]$SkipApk,      # Saltar compilación APK
    [switch]$SkipGit,      # Saltar push a Git
    [switch]$SkipVercel,   # Saltar deploy Vercel
    [string]$CommitMsg = "Actualización: cambios automáticos - $(Get-Date -Format 'yyyy-MM-dd HH:mm')"
)

$ErrorActionPreference = "Stop"
$projectRoot = $PSScriptRoot

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  DESPLIEGUE COMPLETO - Finanzas Proactivas" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ------------------------------------------------------------------------------
# PASO 1: Compilar APK y abrir carpeta
# ------------------------------------------------------------------------------
if (-not $SkipApk) {
    Write-Host "[1/4] Compilando APK..." -ForegroundColor Yellow
    Push-Location "$projectRoot\android-app"
    try {
        if (Test-Path ".\gradlew.bat") {
            & .\gradlew.bat assembleDebug --no-daemon
        } elseif (Test-Path ".\gradlew") {
            & .\gradlew assembleDebug --no-daemon
        } else {
            throw "No se encontró gradlew o gradlew.bat"
        }
        
        if ($LASTEXITCODE -ne 0) {
            throw "Error en la compilación del APK"
        }
        
        $apkFolder = "$projectRoot\android-app\app\build\outputs\apk\debug"
        if (Test-Path $apkFolder) {
            Write-Host "      APK generado correctamente" -ForegroundColor Green
            Write-Host "[2/4] Abriendo carpeta del APK..." -ForegroundColor Yellow
            Start-Process explorer.exe -ArgumentList $apkFolder
            Write-Host "      Carpeta abierta en Explorador" -ForegroundColor Green
        } else {
            Write-Host "      Advertencia: No se encontró la carpeta del APK" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "      ERROR: $_" -ForegroundColor Red
        Write-Host "      Si no tienes Android SDK, usa -SkipApk para saltar este paso" -ForegroundColor Yellow
        Pop-Location
        exit 1
    }
    Pop-Location
} else {
    Write-Host "[1/4] Compilación APK omitida (-SkipApk)" -ForegroundColor Gray
    Write-Host "[2/4] Abrir carpeta APK omitido" -ForegroundColor Gray
}

# ------------------------------------------------------------------------------
# PASO 3: Push a Git
# ------------------------------------------------------------------------------
if (-not $SkipGit) {
    Write-Host ""
    Write-Host "[3/4] Subiendo cambios a Git..." -ForegroundColor Yellow
    Push-Location $projectRoot
    try {
        $gitStatus = git status --porcelain
        if ($gitStatus) {
            git add -A
            git commit -m "$CommitMsg"
            if ($LASTEXITCODE -eq 0) {
                git push
                if ($LASTEXITCODE -eq 0) {
                    Write-Host "      Cambios subidos correctamente a origin" -ForegroundColor Green
                } else {
                    Write-Host "      Advertencia: git push falló (¿conexión? ¿credenciales?)" -ForegroundColor Yellow
                }
            } else {
                Write-Host "      Advertencia: No hubo cambios para commitear" -ForegroundColor Yellow
            }
        } else {
            Write-Host "      No hay cambios pendientes para subir" -ForegroundColor Gray
        }
        
        # Push a todos los remotos si existen
        $remotes = git remote
        foreach ($remote in $remotes) {
            if ($remote -ne "origin") {
                Write-Host "      Subiendo a remoto: $remote" -ForegroundColor Cyan
                git push $remote 2>$null
            }
        }
    } catch {
        Write-Host "      ERROR Git: $_" -ForegroundColor Red
        Write-Host "      Usa -SkipGit para saltar este paso" -ForegroundColor Yellow
    }
    Pop-Location
} else {
    Write-Host "[3/4] Push a Git omitido (-SkipGit)" -ForegroundColor Gray
}

# ------------------------------------------------------------------------------
# PASO 4: Deploy en Vercel
# ------------------------------------------------------------------------------
if (-not $SkipVercel) {
    Write-Host ""
    Write-Host "[4/4] Desplegando backend en Vercel..." -ForegroundColor Yellow
    Push-Location "$projectRoot\backend"
    try {
        npm run deploy
        if ($LASTEXITCODE -eq 0) {
            Write-Host "      Backend desplegado correctamente en Vercel" -ForegroundColor Green
        } else {
            throw "Error en deploy de Vercel"
        }
    } catch {
        Write-Host "      ERROR Vercel: $_" -ForegroundColor Red
        Write-Host "      Asegúrate de tener Vercel CLI instalado (npm i -g vercel)" -ForegroundColor Yellow
        Write-Host "      Usa -SkipVercel para saltar este paso" -ForegroundColor Yellow
    }
    Pop-Location
} else {
    Write-Host "[4/4] Deploy Vercel omitido (-SkipVercel)" -ForegroundColor Gray
}

# ------------------------------------------------------------------------------
# Resumen final
# ------------------------------------------------------------------------------
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  DESPLIEGUE COMPLETADO" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "APK ubicación: android-app\app\build\outputs\apk\debug\" -ForegroundColor White
Write-Host ""
Write-Host "Opciones del script:" -ForegroundColor Yellow
Write-Host "  .\deploy-completo.ps1              # Ejecuta todo" -ForegroundColor White
Write-Host "  .\deploy-completo.ps1 -SkipApk     # Saltar compilación APK" -ForegroundColor White
Write-Host "  .\deploy-completo.ps1 -SkipGit     # Saltar push a Git" -ForegroundColor White
Write-Host "  .\deploy-completo.ps1 -SkipVercel  # Saltar deploy Vercel" -ForegroundColor White
Write-Host "  .\deploy-completo.ps1 -CommitMsg 'Mi mensaje'  # Mensaje personalizado" -ForegroundColor White
Write-Host ""
