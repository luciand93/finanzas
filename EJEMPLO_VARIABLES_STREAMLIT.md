# 📋 Ejemplo de Variables de Entorno para Streamlit Cloud

## 🎯 Variables a Configurar

En Streamlit Cloud, ve a **Settings** (⚙️) > **Secrets** y agrega estas 3 variables:

---

## 1️⃣ Variable: `GOOGLE_SHEETS_ENABLED`

**Nombre:**
```
GOOGLE_SHEETS_ENABLED
```

**Valor:**
```
true
```

**Nota:** Debe estar en minúsculas.

---

## 2️⃣ Variable: `GOOGLE_SHEET_ID`

**Nombre:**
```
GOOGLE_SHEET_ID
```

**Valor (ejemplo):**
```
1a2b3c4d5e6f7g8h9i0j1k2l3m4n5o6p7q8r9s0t
```

**Cómo obtener el ID:**
1. Abre tu hoja de Google Sheets
2. Mira la URL en el navegador:
   ```
   https://docs.google.com/spreadsheets/d/[ESTE_ES_EL_ID]/edit
   ```
3. Copia solo la parte del ID (sin las barras `/` ni nada más)

**Ejemplo de URL completa:**
```
https://docs.google.com/spreadsheets/d/1a2b3c4d5e6f7g8h9i0j1k2l3m4n5o6p7q8r9s0t/edit#gid=0
```

En este caso, el ID sería: `1a2b3c4d5e6f7g8h9i0j1k2l3m4n5o6p7q8r9s0t`

---

## 3️⃣ Variable: `GOOGLE_CREDENTIALS_JSON`

**Nombre:**
```
GOOGLE_CREDENTIALS_JSON
```

**Valor (ejemplo completo):**
```json
{
  "type": "service_account",
  "project_id": "mi-proyecto-finanzas",
  "private_key_id": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6",
  "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...\n(todo el contenido de la clave privada aquí...)\n-----END PRIVATE KEY-----\n",
  "client_email": "streamlit-finanzas@mi-proyecto-finanzas.iam.gserviceaccount.com",
  "client_id": "123456789012345678901",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/streamlit-finanzas%40mi-proyecto-finanzas.iam.gserviceaccount.com"
}
```

**⚠️ IMPORTANTE:**
- Copia **TODO** el contenido del archivo JSON que descargaste
- Incluye las llaves `{` y `}`
- Incluye todas las comillas `"` 
- Incluye TODA la clave privada (es muy larga, con saltos de línea `\n`)
- NO agregues espacios extra al inicio o final
- Debe ser un JSON válido completo

**Ejemplo de cómo se ve el archivo JSON descargado:**
```json
{
  "type": "service_account",
  "project_id": "tu-proyecto-123456",
  "private_key_id": "abc123def456...",
  "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQD...\n... (muchas líneas más) ...\n-----END PRIVATE KEY-----\n",
  "client_email": "nombre-cuenta-servicio@tu-proyecto-123456.iam.gserviceaccount.com",
  "client_id": "123456789012345678901",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/nombre-cuenta-servicio%40tu-proyecto-123456.iam.gserviceaccount.com"
}
```

---

## 📝 Instrucciones Paso a Paso en Streamlit Cloud

1. Ve a tu aplicación en [Streamlit Cloud](https://share.streamlit.io/)
2. Haz clic en los **3 puntos** (⋮) junto a tu app o en **"Manage app"**
3. Ve a la pestaña **"Secrets"** o **"Settings"**
4. Busca la sección **"Secrets"** o **"Environment variables"**
5. Haz clic en **"Edit"** o **"Add secret"**

### Para cada variable:

**Opción A: Si usas "Secrets" (archivo .toml):**
Agrega esto al editor de secrets:
```toml
GOOGLE_SHEETS_ENABLED = "true"
GOOGLE_SHEET_ID = "tu_id_aqui"
GOOGLE_CREDENTIALS_JSON = """
{
  "type": "service_account",
  "project_id": "...",
  ... (resto del JSON)
}
"""
```

**Opción B: Si usas "Environment variables" (variables individuales):**

Agrega 3 variables separadas:
- Variable 1:
  - Key: `GOOGLE_SHEETS_ENABLED`
  - Value: `true`

- Variable 2:
  - Key: `GOOGLE_SHEET_ID`
  - Value: `tu_id_de_la_hoja` (sin comillas)

- Variable 3:
  - Key: `GOOGLE_CREDENTIALS_JSON`
  - Value: (pega TODO el contenido del JSON, incluyendo `{` y `}`)

---

## ✅ Verificación

Después de configurar las variables:

1. **Reinicia tu aplicación** en Streamlit Cloud
2. Ve a la pestaña **"⚙️ Configuración"** en tu app
3. Deberías ver: **"✅ Google Sheets conectado correctamente"**
4. El nombre de tu libro de Google Sheets debería aparecer

---

## 🔍 Solución de Problemas

### Si ves "Google Sheets no está configurado":
- Verifica que las 3 variables estén configuradas
- Verifica que `GOOGLE_SHEETS_ENABLED` sea exactamente `true` (minúsculas)
- Verifica que el JSON esté completo y válido
- Reinicia la aplicación

### Si ves "Error conectando":
- Verifica que el JSON sea válido (puedes probarlo en [jsonlint.com](https://jsonlint.com))
- Verifica que la cuenta de servicio tenga acceso a la hoja
- Verifica que el ID de la hoja sea correcto

### Si el JSON no funciona:
Asegúrate de que:
- ✅ Todas las comillas `"` estén incluidas
- ✅ Las llaves `{` y `}` estén al inicio y final
- ✅ No haya espacios extra antes o después
- ✅ La clave privada completa esté incluida (es muy larga)

---

## 💡 Tip Pro

Si tienes problemas copiando el JSON completo:
1. Abre el archivo JSON descargado con un editor de texto (Notepad++, VS Code, etc.)
2. Selecciona TODO (Ctrl+A)
3. Copia TODO (Ctrl+C)
4. Pégalo directamente en Streamlit Cloud (Ctrl+V)
5. No edites nada, úsalo tal cual
