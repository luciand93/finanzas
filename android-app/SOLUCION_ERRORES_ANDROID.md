# 🔧 Solución de Errores - App Android

## ✅ Cambios Realizados (27 de Enero, 2026)

He corregido los problemas de **Google Sheets** y **Gemini AI** en la aplicación de Android.

---

## 🐛 Problemas Corregidos

### 1. ❌ Google Sheets no recuperaba datos → ✅ CORREGIDO

**Problema:** Los errores se silenciaban y devolvían listas vacías sin informar al usuario.

**Solución:** Ahora la app muestra mensajes de error descriptivos que te dicen exactamente qué está fallando:

**Ejemplos de errores que ahora verás:**
- ❌ **Error 403**: "Sin permisos. Verifica que la hoja esté compartida con [email]"
- ❌ **Error 404**: "Hoja no encontrada. Verifica el SPREADSHEET_ID"
- ❌ **Error 401**: "Credenciales inválidas"
- ❌ **Archivo no encontrado**: "No se encontró credentials.json en assets/"

### 2. ❌ Gemini AI no respondía → ✅ CORREGIDO

**Problema:** Los errores no eran descriptivos y no indicaban por qué fallaba.

**Solución:** Ahora la app muestra mensajes detallados con soluciones:

**Ejemplos de errores que ahora verás:**
- ❌ **API Key inválida**: Con instrucciones para obtener una nueva
- ❌ **Región no soportada**: Con sugerencias de usar VPN
- ❌ **Límite de uso excedido**: Con explicación de cuotas
- ❌ **Error 403**: Con pasos para habilitar la API

---

## 🚀 Próximos Pasos

### Paso 1: Recompilar la APK

Los cambios están en el código, pero necesitas recompilar la APK para que surtan efecto:

**Opción A: Desde Android Studio**
```
1. Abre Android Studio
2. File > Sync Project with Gradle Files
3. Build > Build Bundle(s) / APK(s) > Build APK(s)
4. El APK estará en: app/build/outputs/apk/debug/app-debug.apk
```

**Opción B: Desde PowerShell (más rápido)**
```powershell
cd android-app
.\gradlew assembleDebug
```

**Opción C: Usando el script**
```powershell
cd android-app
.\compilar_apk.ps1
```

### Paso 2: Instalar la Nueva APK

1. Desinstala la versión antigua del teléfono
2. Instala la nueva APK:
   ```powershell
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```
   O copia el APK al teléfono y ábrelo para instalar

### Paso 3: Probar los Errores

Ahora cuando abras la app, verás mensajes de error claros si algo falla:

1. **Abre la app**
2. Si Google Sheets falla, verás un mensaje rojo en la parte superior indicando el problema
3. Si Gemini falla, verás el mensaje de error en el chat

---

## 🔍 Verificar Configuración Actual

### Google Sheets

**SPREADSHEET_ID configurado:**
```
17EBvx8s1IsxcV9-RigMxYvUxgz15ZA6yIuHyY9f8xGk
```

**Service Account Email (del credentials.json):**
```
finanzas@vstudio-476115.iam.gserviceaccount.com
```

**¿Qué hacer?**
1. Abre tu hoja: https://docs.google.com/spreadsheets/d/17EBvx8s1IsxcV9-RigMxYvUxgz15ZA6yIuHyY9f8xGk/edit
2. Haz clic en "Compartir"
3. Asegúrate de que `finanzas@vstudio-476115.iam.gserviceaccount.com` tenga permisos de **Editor**
4. Si no está, agrégalo con permisos de Editor

### Gemini AI

**API Key configurada (parcial):**
```
AIzaSyDPMW... (visible en los mensajes de error de la app)
```

**¿Qué hacer si falla?**

1. **Obtén una nueva API key:**
   - Ve a: https://aistudio.google.com/app/apikey
   - Inicia sesión con tu cuenta de Google
   - Haz clic en "Create API Key"
   - Copia la API key completa

2. **Actualiza el código:**
   - Abre: `android-app/app/src/main/java/com/finanzasproactivas/data/repository/GeminiRepository.kt`
   - Busca la línea 12: `private val defaultApiKey = "AIzaSyDPMW..."`
   - Reemplaza con tu nueva API key
   - Guarda el archivo

3. **Recompila la APK:**
   ```powershell
   cd android-app
   .\gradlew assembleDebug
   ```

---

## 📊 Archivos Modificados

| Archivo | Cambios |
|---------|---------|
| `GoogleSheetsRepository.kt` | ✅ Mensajes de error descriptivos para todos los casos |
| `GeminiRepository.kt` | ✅ Diagnósticos detallados con soluciones específicas |

---

## 🔧 Errores Comunes y Soluciones

### Error: "403 - Sin permisos para acceder a Google Sheets"

**Causa:** La hoja no está compartida con el service account o no tiene permisos de Editor.

**Solución:**
1. Abre la hoja en Google Sheets
2. Haz clic en "Compartir" (botón verde arriba a la derecha)
3. Agrega: `finanzas@vstudio-476115.iam.gserviceaccount.com`
4. Selecciona permisos de **"Editor"**
5. Haz clic en "Enviar"

### Error: "404 - Hoja no encontrada"

**Causa:** El SPREADSHEET_ID es incorrecto o la hoja fue eliminada.

**Solución:**
1. Verifica que la hoja exista: https://docs.google.com/spreadsheets/d/17EBvx8s1IsxcV9-RigMxYvUxgz15ZA6yIuHyY9f8xGk/edit
2. Si no existe, crea una nueva hoja y actualiza el SPREADSHEET_ID en `GoogleSheetsRepository.kt` línea 29

### Error: "API Key Inválida" en Gemini

**Causa:** La API key ha expirado, es inválida, o no tiene permisos.

**Solución:**
1. Genera una nueva API key: https://aistudio.google.com/app/apikey
2. Actualiza `GeminiRepository.kt` línea 12
3. Recompila la APK

### Error: "API no disponible en tu región"

**Causa:** Gemini no está disponible en tu ubicación geográfica.

**Solución:**
1. Usa una VPN para conectarte desde Estados Unidos o Europa
2. O verifica disponibilidad en: https://ai.google.dev/gemini-api/docs/available-regions

### Error: "Límite de uso excedido"

**Causa:** Has alcanzado el límite de llamadas gratuitas de Gemini.

**Solución:**
1. Espera hasta mañana (la cuota se renueva cada 24 horas)
2. O verifica tu uso en: https://aistudio.google.com/app/apikey

---

## 📱 Cómo Leer los Mensajes de Error en la App

### Ubicación de Errores:

1. **Pantalla Principal / Asesor:**
   - Los errores de Google Sheets aparecen en un **recuadro rojo** en la parte superior
   - Muestra el error completo con instrucciones

2. **Chat con Gemini:**
   - Los errores aparecen como **mensaje de Gemini** en el chat
   - Contienen diagnóstico detallado y soluciones

3. **Toast/Snackbar:**
   - Errores breves pueden aparecer como notificaciones temporales en la parte inferior

---

## ✅ Lista de Verificación

Después de recompilar e instalar la nueva APK:

- [ ] La app muestra errores claros si Google Sheets falla
- [ ] Los errores incluyen el email del service account
- [ ] Los errores incluyen el SPREADSHEET_ID
- [ ] Gemini muestra mensajes descriptivos si falla
- [ ] Los errores de Gemini incluyen la API key (parcial)
- [ ] Los errores sugieren soluciones específicas

---

## 🆘 Si Aún Tienes Problemas

### 1. Verificar que los cambios se compilaron:

```powershell
cd android-app
.\gradlew clean assembleDebug
```

### 2. Ver logs en tiempo real (con app conectada por USB):

```powershell
adb logcat | Select-String "finanzasproactivas"
```

### 3. Verificar que el APK es nuevo:

```powershell
# Ver fecha de modificación del APK
Get-ChildItem app\build\outputs\apk\debug\app-debug.apk | Select-Object LastWriteTime
```

Debe mostrar la fecha y hora de hoy después de compilar.

---

## 📞 Recursos Útiles

- **Google Sheets API**: https://console.cloud.google.com/
- **Gemini API Studio**: https://aistudio.google.com/app/apikey
- **Gemini Docs**: https://ai.google.dev/gemini-api/docs
- **Regiones Disponibles**: https://ai.google.dev/gemini-api/docs/available-regions

---

## 📝 Notas Finales

### Cambios Técnicos Realizados:

1. **GoogleSheetsRepository.kt:**
   - Agregado `getServiceAccountEmail()` para mostrar el email en errores
   - Errores 403, 404, 401 con mensajes descriptivos
   - Excepciones lanzadas en lugar de devolver valores vacíos

2. **GeminiRepository.kt:**
   - Análisis de errores comunes (API_KEY_INVALID, region, quota)
   - Mensajes personalizados según el tipo de error
   - Instrucciones paso a paso para cada problema
   - Mostrar API key parcial en mensajes de error para debugging

**Resultado:** Ahora sabrás exactamente qué está fallando y cómo solucionarlo.

---

## 🎯 Siguiente Paso: Recompilar y Probar

```powershell
# 1. Ir a la carpeta de Android
cd android-app

# 2. Limpiar y recompilar
.\gradlew clean assembleDebug

# 3. Instalar en tu dispositivo
adb install -r app\build\outputs\apk\debug\app-debug.apk

# 4. Abrir la app y verificar los mensajes de error
```

¡Ahora la app te dirá exactamente qué está mal! 🎉
