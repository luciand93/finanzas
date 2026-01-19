# 📚 Configuración de Google Sheets para Finanzas

Esta guía te ayudará a configurar Google Sheets para que tus datos se guarden permanentemente en tu cuenta de Google Drive.

## 🎯 ¿Por qué configurar Google Sheets?

- ✅ **Persistencia permanente**: Los datos no se pierden al reiniciar Streamlit Cloud
- ✅ **Backup automático**: Tus datos están seguros en Google Drive
- ✅ **Acceso desde cualquier lugar**: Puedes ver tus datos directamente en Google Sheets
- ✅ **Sincronización**: Múltiples dispositivos acceden a los mismos datos

## 📋 Pasos para configurar

### Paso 1: Crear un proyecto en Google Cloud Console

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto o selecciona uno existente
3. Anota el nombre del proyecto

### Paso 2: Habilitar APIs necesarias

1. En el menú lateral, ve a **"APIs y servicios" > "Biblioteca"**
2. Busca y habilita:
   - **Google Sheets API**
   - **Google Drive API**

### Paso 3: Crear cuenta de servicio

1. Ve a **"APIs y servicios" > "Credenciales"**
2. Haz clic en **"Crear credenciales" > "Cuenta de servicio"**
3. Dale un nombre (ej: "streamlit-finanzas")
4. Haz clic en **"Crear y continuar"**
5. Selecciona el rol: **"Editor"** (o el que prefieras)
6. Haz clic en **"Listo"**

### Paso 4: Generar clave JSON

1. Haz clic en la cuenta de servicio que acabas de crear
2. Ve a la pestaña **"Claves"**
3. Haz clic en **"Agregar clave" > "Crear nueva clave"**
4. Selecciona **"JSON"**
5. Se descargará un archivo JSON (guárdalo de forma segura)

### Paso 5: Crear hoja de cálculo en Google Sheets

1. Ve a [Google Sheets](https://sheets.google.com)
2. Crea una nueva hoja de cálculo
3. Nómbrala como quieras (ej: "Mis Finanzas")
4. Copia el ID de la hoja desde la URL:
   ```
   https://docs.google.com/spreadsheets/d/[ESTE_ES_EL_ID]/edit
   ```
5. Comparte la hoja con el email de la cuenta de servicio:
   - Haz clic en "Compartir"
   - Pega el email de la cuenta de servicio (lo encontrarás en el archivo JSON como "client_email")
   - Dale permisos de "Editor"
   - Guarda

### Paso 6: Configurar variables de entorno en Streamlit Cloud

1. Ve a tu aplicación en [Streamlit Cloud](https://share.streamlit.io/)
2. Haz clic en **"Settings"** (⚙️) o **"Manage app"**
3. Ve a la sección **"Secrets"** o **"Environment variables"**
4. Agrega las siguientes variables:

   **Variable 1:**
   - Nombre: `GOOGLE_SHEETS_ENABLED`
   - Valor: `true`

   **Variable 2:**
   - Nombre: `GOOGLE_SHEET_ID`
   - Valor: El ID de tu hoja (el que copiaste del paso 5)

   **Variable 3:**
   - Nombre: `GOOGLE_CREDENTIALS_JSON`
   - Valor: Todo el contenido del archivo JSON descargado (ábrelo con un editor de texto y copia todo)

   ⚠️ **IMPORTANTE**: Para la variable `GOOGLE_CREDENTIALS_JSON`, copia TODO el contenido del JSON, incluyendo las llaves `{` y `}`. Debe verse así:
   ```json
   {
     "type": "service_account",
     "project_id": "...",
     "private_key_id": "...",
     ...
   }
   ```

### Paso 7: Reiniciar la aplicación

1. Guarda los cambios en Streamlit Cloud
2. La aplicación se reiniciará automáticamente
3. Ve a la pestaña **"⚙️ Configuración"** en tu app
4. Deberías ver: **"✅ Google Sheets conectado correctamente"**

## ✅ Verificación

Para verificar que todo funciona:

1. Agrega un movimiento desde el formulario
2. Guarda el movimiento
3. Ve a tu hoja de Google Sheets
4. Deberías ver los datos aparecer automáticamente en la hoja "Finanzas"

## 🔧 Estructura de las hojas

La aplicación creará automáticamente 3 hojas en tu libro de cálculo:

1. **Finanzas**: Todos tus movimientos financieros
2. **Categorias**: Lista de categorías personalizadas
3. **Recurrentes**: Plantillas de gastos recurrentes

## ❓ Solución de problemas

### "Error conectando a Google Sheets"
- Verifica que las APIs estén habilitadas
- Asegúrate de que el JSON de credenciales esté completo
- Verifica que la cuenta de servicio tenga acceso a la hoja

### "Hoja no encontrada"
- Verifica que el ID de la hoja sea correcto
- Asegúrate de haber compartido la hoja con la cuenta de servicio

### "Sin permisos"
- Verifica que la cuenta de servicio tenga permisos de "Editor" en la hoja
- Verifica que las APIs estén habilitadas en Google Cloud Console

## 🔒 Seguridad

- ⚠️ **NUNCA** compartas tu archivo JSON de credenciales públicamente
- ⚠️ **NUNCA** subas el archivo JSON a repositorios públicos
- ✅ Usa variables de entorno en Streamlit Cloud para las credenciales
- ✅ La cuenta de servicio solo tiene acceso a la hoja que compartiste

## 📞 Soporte

Si tienes problemas, verifica:
1. Que todas las APIs estén habilitadas
2. Que las variables de entorno estén correctamente configuradas
3. Que la hoja esté compartida con la cuenta de servicio
4. Los logs de Streamlit Cloud para ver errores detallados
