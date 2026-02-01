# Script de Verificación de credentials.json
$credentialsPath = "app\src\main\assets\credentials.json"

Write-Host "`n=== Verificación de credentials.json ===" -ForegroundColor Cyan

# 1. Verificar que el archivo existe
if (-not (Test-Path $credentialsPath)) {
    Write-Host "✗ ERROR: El archivo no existe" -ForegroundColor Red
    Write-Host "  Ubicación esperada: $credentialsPath" -ForegroundColor Yellow
    exit 1
}
Write-Host "✓ Archivo encontrado" -ForegroundColor Green

# 2. Verificar que sea JSON válido
try {
    $json = Get-Content $credentialsPath -Raw | ConvertFrom-Json
    Write-Host "✓ JSON válido" -ForegroundColor Green
} catch {
    Write-Host "✗ ERROR: JSON inválido: $_" -ForegroundColor Red
    exit 1
}

# 3. Verificar campos requeridos
$requiredFields = @("type", "project_id", "private_key_id", "private_key", "client_email", "client_id", "auth_uri", "token_uri", "auth_provider_x509_cert_url", "client_x509_cert_url")
$missing = @()
foreach ($field in $requiredFields) {
    if (-not $json.PSObject.Properties.Name.Contains($field)) {
        $missing += $field
    }
}

if ($missing.Count -eq 0) {
    Write-Host "✓ Todos los campos requeridos presentes" -ForegroundColor Green
} else {
    Write-Host "✗ ERROR: Faltan campos: $($missing -join ', ')" -ForegroundColor Red
    exit 1
}

# 4. Verificar tipo
if ($json.type -eq "service_account") {
    Write-Host "✓ Tipo correcto: service_account" -ForegroundColor Green
} else {
    Write-Host "✗ ERROR: Tipo incorrecto: $($json.type)" -ForegroundColor Red
    exit 1
}

# 5. Verificar email
if ($json.client_email -match "@.*\.iam\.gserviceaccount\.com$") {
    Write-Host "✓ Email válido: $($json.client_email)" -ForegroundColor Green
} else {
    Write-Host "✗ ERROR: Email inválido: $($json.client_email)" -ForegroundColor Red
    exit 1
}

# 6. Verificar clave privada
if ($json.private_key -match "-----BEGIN PRIVATE KEY-----" -and $json.private_key -match "-----END PRIVATE KEY-----") {
    Write-Host "✓ Clave privada con formato correcto" -ForegroundColor Green
} else {
    Write-Host "✗ ERROR: Clave privada con formato incorrecto" -ForegroundColor Red
    exit 1
}

# 7. Mostrar información
Write-Host "`n=== Información del Service Account ===" -ForegroundColor Cyan
Write-Host "Project ID: $($json.project_id)" -ForegroundColor White
Write-Host "Client Email: $($json.client_email)" -ForegroundColor White
Write-Host "Client ID: $($json.client_id)" -ForegroundColor White

Write-Host "`n✓ Verificación completada exitosamente" -ForegroundColor Green
Write-Host "`nIMPORTANTE: Asegúrate de que la hoja de Google Sheets esté compartida con:" -ForegroundColor Yellow
Write-Host "  $($json.client_email)" -ForegroundColor Yellow
Write-Host "  Con permisos de EDITOR" -ForegroundColor Yellow
