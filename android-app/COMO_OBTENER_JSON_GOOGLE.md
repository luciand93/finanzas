# Cómo Obtener el JSON de Google (credentials.json) - Guía Paso a Paso

## 📋 Resumen Rápido

Necesitas crear un **Service Account** en Google Cloud Console y descargar su archivo JSON de credenciales.

---

## 🚀 Pasos Detallados

### Paso 1: Ir a Google Cloud Console

1. Abre tu navegador y ve a: **https://console.cloud.google.com/**
2. Inicia sesión con tu cuenta de Google (la misma que usas para Google Sheets)

### Paso 2: Crear un Proyecto (si no tienes uno)

1. En la parte superior izquierda, haz clic en el **selector de proyectos** (donde dice "Seleccionar un proyecto")
2. Haz clic en **"NUEVO PROYECTO"**
3. Ingresa un nombre para el proyecto (ej: "Finanzas Proactivas")
4. Haz clic en **"CREAR"**
5. Espera unos segundos y selecciona el proyecto recién creado

### Paso 3: Habilitar Google Sheets API

1. En el menú lateral izquierdo (☰), ve a **"APIs y servicios"** > **"Biblioteca"**
2. En el buscador de la parte superior, escribe: **"Google Sheets API"**
3. Haz clic en **"Google Sheets API"** (debe aparecer con el logo de Google)
4. Haz clic en el botón azul **"HABILITAR"**
5. Espera unos segundos hasta que aparezca "API habilitada"

### Paso 4: Crear Service Account

1. En el menú lateral, ve a **"APIs y servicios"** > **"Credenciales"**
2. En la parte superior, haz clic en **"+ CREAR CREDENCIALES"**
3. Selecciona **"Cuenta de servicio"** del menú desplegable

### Paso 5: Configurar el Service Account

1. En el formulario que aparece:
   - **Nombre de la cuenta de servicio**: Escribe `finanzas-proactivas` (o el nombre que prefieras)
   - **ID de la cuenta de servicio**: Se genera automáticamente (puedes dejarlo así)
   - **Descripción**: `Service Account para la app Finanzas Proactivas`
2. Haz clic en **"CREAR Y CONTINUAR"**

### Paso 6: Otorgar Permisos (Opcional)

1. En la sección **"Otorgar a esta cuenta de servicio acceso al proyecto"**:
   - Puedes dejar el rol en blanco o seleccionar **"Editor"**
   - Esto es opcional para nuestro caso
2. Haz clic en **"CONTINUAR"**
3. En la siguiente pantalla, puedes saltar el paso de usuarios (haz clic en **"LISTO"**)

### Paso 7: Descargar el Archivo JSON

1. Volverás a la lista de cuentas de servicio
2. Busca la cuenta que acabas de crear (debe aparecer en la lista)
3. Haz clic en el **email de la cuenta de servicio** (termina en `@...iam.gserviceaccount.com`)
4. Ve a la pestaña **"CLAVES"** (Keys) en la parte superior
5. Haz clic en **"AGREGAR CLAVE"** > **"Crear nueva clave"**
6. Selecciona el formato **"JSON"**
7. Haz clic en **"CREAR"**
8. **¡Se descargará automáticamente un archivo JSON!** 📥

### Paso 8: Renombrar y Colocar el Archivo

1. El archivo descargado tiene un nombre como: `tu-proyecto-xxxxx-xxxxx.json`
2. **Renómbralo** a: `credentials.json`
3. Copia el archivo `credentials.json` a la carpeta:
   ```
   android-app/app/src/main/assets/
   ```
4. Si la carpeta `assets` no existe, créala:
   - Crea la carpeta `assets` dentro de `android-app/app/src/main/`

### Paso 9: Compartir la Hoja de Google Sheets

**⚠️ MUY IMPORTANTE:** Sin este paso, la app no podrá acceder a tu hoja de Google Sheets.

1. Abre el archivo `credentials.json` que acabas de descargar
2. Busca el campo **"client_email"** (algo como `finanzas-proactivas@tu-proyecto-xxxxx.iam.gserviceaccount.com`)
3. **Copia ese email completo**
4. Abre tu hoja de Google Sheets:
   - https://docs.google.com/spreadsheets/d/17EBvx8s1IsxcV9-RigMxYvUxgz15ZA6yIuHyY9f8xGk/edit
5. Haz clic en el botón **"Compartir"** (arriba a la derecha, botón azul)
6. Pega el email del Service Account en el campo de texto
7. Asegúrate de que tenga permisos de **"Editor"** (no solo "Lector")
8. **Desmarca** la casilla "Notificar a las personas" (no es necesario)
9. Haz clic en **"Compartir"**

---

## ✅ Verificación Final

Una vez completados todos los pasos, verifica:

- ✅ El archivo `credentials.json` está en `android-app/app/src/main/assets/`
- ✅ La hoja de Google Sheets está compartida con el email del Service Account
- ✅ El Service Account tiene permisos de **Editor** en la hoja

---

## 🎯 Resumen Visual de la Ubicación del Archivo

```
finanzas/
└── android-app/
    └── app/
        └── src/
            └── main/
                └── assets/
                    └── credentials.json  ← AQUÍ debe estar el archivo
```

---

## 🔍 Cómo Verificar que el JSON es Correcto

### Opción 1: Usar el Script de Verificación

```powershell
cd android-app
.\verificar_credentials.ps1
```

### Opción 2: Verificación Manual

1. Abre el archivo `credentials.json` en un editor de texto
2. Verifica que contenga estos campos:
   - `"type": "service_account"`
   - `"project_id": "tu-proyecto-xxxxx"`
   - `"client_email": "tu-service-account@...iam.gserviceaccount.com"`
   - `"private_key": "-----BEGIN PRIVATE KEY-----..."`
   - Y otros campos más

---

## ❓ Preguntas Frecuentes

### ¿Necesito pagar por Google Cloud Console?

**No**, Google Cloud Console es gratuito. Solo pagas si usas servicios que consumen recursos (como servidores, almacenamiento, etc.). Para nuestro caso (solo usar Google Sheets API), es completamente gratuito.

### ¿Cuánto tiempo tarda en funcionar?

Una vez que:
1. Descargas el JSON
2. Lo colocas en la carpeta correcta
3. Compartes la hoja de Google Sheets con el Service Account

**Debería funcionar inmediatamente**. A veces puede tardar 1-2 minutos en propagarse los permisos.

### ¿Qué pasa si pierdo el archivo JSON?

No te preocupes, puedes crear una nueva clave:
1. Ve a Google Cloud Console > APIs y servicios > Credenciales
2. Busca tu Service Account
3. Ve a la pestaña "Claves"
4. Haz clic en "Agregar clave" > "Crear nueva clave" > "JSON"
5. Descarga el nuevo archivo

**Nota:** Si creas una nueva clave, la anterior seguirá funcionando hasta que la elimines manualmente.

### ¿Es seguro tener este archivo en mi proyecto?

El archivo `credentials.json` contiene credenciales sensibles. Por eso:
- ✅ Está en `.gitignore` para que NO se suba a GitHub
- ⚠️ **NO** lo compartas públicamente
- ⚠️ Si lo subiste a un repositorio público, elimina esa clave y crea una nueva

---

## 🆘 Solución de Problemas

### Error: "FileNotFoundException: credentials.json"

**Solución:**
- Verifica que el archivo esté exactamente en: `android-app/app/src/main/assets/credentials.json`
- El nombre debe ser exactamente `credentials.json` (todo en minúsculas, sin espacios)

### Error: "403 Forbidden" al acceder a Sheets

**Solución:**
- Verifica que hayas compartido la hoja de Google Sheets con el email del Service Account
- Asegúrate de darle permisos de **Editor** (no solo Lector)
- Espera 1-2 minutos después de compartir (puede tardar en propagarse)

### Error: "API not enabled"

**Solución:**
- Ve a Google Cloud Console > APIs y servicios > Biblioteca
- Busca "Google Sheets API"
- Verifica que esté **habilitada** (debe decir "API habilitada")

---

## 📝 Notas Importantes

1. **Un Service Account = Un archivo JSON**: Cada vez que creas una nueva clave, descargas un nuevo JSON. Puedes tener múltiples claves activas.

2. **El email del Service Account es único**: Cada Service Account tiene un email único que termina en `@...iam.gserviceaccount.com`. Este es el email que debes compartir con tu hoja de Google Sheets.

3. **Los permisos se dan a nivel de hoja**: Compartes la hoja de Google Sheets con el Service Account, no el proyecto completo.

---

## 🎉 ¡Listo!

Una vez que tengas el archivo `credentials.json` en la ubicación correcta y hayas compartido la hoja de Google Sheets, tu app debería poder leer y escribir datos en Google Sheets sin problemas.

Si tienes algún problema, revisa la sección "Solución de Problemas" o ejecuta el script de verificación.
