# Cómo Verificar que credentials.json sea Correcto

## Estructura del Archivo credentials.json

El archivo `credentials.json` debe tener la siguiente estructura (es un archivo JSON válido):

```json
{
  "type": "service_account",
  "project_id": "tu-proyecto-xxxxx",
  "private_key_id": "xxxxx",
  "private_key": "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n",
  "client_email": "tu-service-account@tu-proyecto-xxxxx.iam.gserviceaccount.com",
  "client_id": "xxxxx",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/..."
}
```

## Campos Requeridos

El archivo DEBE contener estos campos (todos son obligatorios):

1. **`type`**: Debe ser exactamente `"service_account"`
2. **`project_id`**: ID de tu proyecto de Google Cloud
3. **`private_key_id`**: ID de la clave privada
4. **`private_key`**: La clave privada completa (incluyendo `-----BEGIN PRIVATE KEY-----` y `-----END PRIVATE KEY-----`)
5. **`client_email`**: El email del Service Account (termina en `@...iam.gserviceaccount.com`)
6. **`client_id`**: ID del cliente
7. **`auth_uri`**: URL de autenticación (generalmente `"https://accounts.google.com/o/oauth2/auth"`)
8. **`token_uri`**: URL del token (generalmente `"https://oauth2.googleapis.com/token"`)
9. **`auth_provider_x509_cert_url`**: URL del certificado
10. **`client_x509_cert_url`**: URL del certificado del cliente

## Cómo Verificar que el Archivo sea Correcto

### 1. Verificar la Ubicación del Archivo

El archivo debe estar en:
```
android-app/app/src/main/assets/credentials.json
```

**Verificación:**
```bash
# En Windows PowerShell
Test-Path "android-app\app\src\main\assets\credentials.json"
# Debe devolver: True
```

### 2. Verificar que sea un JSON Válido

**Opción A: Usando PowerShell**
```powershell
# Lee el archivo y verifica que sea JSON válido
$json = Get-Content "android-app\app\src\main\assets\credentials.json" -Raw
try {
    $parsed = $json | ConvertFrom-Json
    Write-Host "✓ JSON válido" -ForegroundColor Green
    Write-Host "Tipo: $($parsed.type)" -ForegroundColor Cyan
    Write-Host "Email: $($parsed.client_email)" -ForegroundColor Cyan
    Write-Host "Project ID: $($parsed.project_id)" -ForegroundColor Cyan
} catch {
    Write-Host "✗ JSON inválido: $_" -ForegroundColor Red
}
```

**Opción B: Usando un editor de texto**
- Abre el archivo en un editor de texto (VS Code, Notepad++, etc.)
- Verifica que:
  - Empiece con `{`
  - Termine con `}`
  - Todas las comillas estén cerradas
  - No haya comas finales antes de `}` o `]`
  - Los valores de texto estén entre comillas dobles

### 3. Verificar Campos Específicos

**Verificar que contenga los campos requeridos:**
```powershell
$json = Get-Content "android-app\app\src\main\assets\credentials.json" -Raw | ConvertFrom-Json

$requiredFields = @("type", "project_id", "private_key_id", "private_key", "client_email", "client_id", "auth_uri", "token_uri", "auth_provider_x509_cert_url", "client_x509_cert_url")

$missing = @()
foreach ($field in $requiredFields) {
    if (-not $json.PSObject.Properties.Name.Contains($field)) {
        $missing += $field
    }
}

if ($missing.Count -eq 0) {
    Write-Host "✓ Todos los campos requeridos están presentes" -ForegroundColor Green
} else {
    Write-Host "✗ Faltan campos: $($missing -join ', ')" -ForegroundColor Red
}
```

### 4. Verificar el Tipo de Cuenta

```powershell
$json = Get-Content "android-app\app\src\main\assets\credentials.json" -Raw | ConvertFrom-Json

if ($json.type -eq "service_account") {
    Write-Host "✓ Tipo correcto: service_account" -ForegroundColor Green
} else {
    Write-Host "✗ Tipo incorrecto: $($json.type) (debe ser 'service_account')" -ForegroundColor Red
}
```

### 5. Verificar el Email del Service Account

```powershell
$json = Get-Content "android-app\app\src\main\assets\credentials.json" -Raw | ConvertFrom-Json

if ($json.client_email -match "@.*\.iam\.gserviceaccount\.com$") {
    Write-Host "✓ Email del Service Account válido: $($json.client_email)" -ForegroundColor Green
} else {
    Write-Host "✗ Email inválido: $($json.client_email)" -ForegroundColor Red
}
```

### 6. Verificar la Clave Privada

```powershell
$json = Get-Content "android-app\app\src\main\assets\credentials.json" -Raw | ConvertFrom-Json

if ($json.private_key -match "-----BEGIN PRIVATE KEY-----" -and $json.private_key -match "-----END PRIVATE KEY-----") {
    Write-Host "✓ Clave privada tiene formato correcto" -ForegroundColor Green
} else {
    Write-Host "✗ Clave privada con formato incorrecto" -ForegroundColor Red
}
```

## Verificación Completa (Script PowerShell)

Copia y pega este script completo en PowerShell para verificar todo:

```powershell
$credentialsPath = "android-app\app\src\main\assets\credentials.json"

Write-Host "`n=== Verificación de credentials.json ===" -ForegroundColor Cyan

# 1. Verificar que el archivo existe
if (-not (Test-Path $credentialsPath)) {
    Write-Host "✗ ERROR: El archivo no existe en: $credentialsPath" -ForegroundColor Red
    Write-Host "  Debe estar en: android-app\app\src\main\assets\credentials.json" -ForegroundColor Yellow
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
```

## Verificar que la Hoja de Google Sheets esté Compartida

1. Abre tu hoja de Google Sheets: https://docs.google.com/spreadsheets/d/17EBvx8s1IsxcV9-RigMxYvUxgz15ZA6yIuHyY9f8xGk/edit
2. Haz clic en el botón **"Compartir"** (arriba a la derecha)
3. Verifica que el email del Service Account (el `client_email` del JSON) esté en la lista
4. Asegúrate de que tenga permisos de **"Editor"** (no solo "Lector")

## Problemas Comunes

### Error: "FileNotFoundException"
- **Solución**: Verifica que el archivo esté exactamente en `android-app/app/src/main/assets/credentials.json`
- El nombre debe ser exactamente `credentials.json` (todo en minúsculas, sin espacios)

### Error: "403 Forbidden"
- **Solución**: Comparte la hoja de Google Sheets con el email del Service Account
- Asegúrate de darle permisos de **Editor** (no solo Lector)

### Error: "JSON inválido"
- **Solución**: Verifica que el archivo sea un JSON válido
- Asegúrate de que todas las comillas estén cerradas
- No debe haber comas finales antes de `}` o `]`

### Error: "API not enabled"
- **Solución**: Ve a Google Cloud Console y habilita la **Google Sheets API**
- Ruta: APIs y servicios > Biblioteca > Buscar "Google Sheets API" > Habilitar

## Nota de Seguridad

⚠️ **IMPORTANTE**: 
- El archivo `credentials.json` contiene información sensible
- **NO** lo subas a repositorios públicos de GitHub
- Si ya lo subiste, elimina esa clave en Google Cloud Console y crea una nueva
- El archivo está en `.gitignore` para evitar que se suba accidentalmente
