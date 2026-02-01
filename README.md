# 💶 Finanzas Proactivas - App Android

Aplicación nativa de Android para gestión financiera personal con sincronización en Google Sheets y asistente IA con Gemini.

## 📱 Características

- 📊 Dashboard interactivo con métricas en tiempo real
- 💰 Gestión completa de ingresos y gastos
- 📈 Gráficos y análisis de tendencias
- 🎯 Presupuestos inteligentes con alertas
- 🔄 Movimientos recurrentes automatizados
- ☁️ Sincronización automática con Google Sheets
- 🤖 Asistente IA con Gemini para consultas en lenguaje natural
- 🎨 Interfaz moderna con Material Design 3

---

## 🚀 Instalación

### APK Precompilado

El APK está en: `android-app/app/build/outputs/apk/debug/app-debug.apk`

**Instalar en dispositivo:**
```powershell
adb install -r android-app/app/build/outputs/apk/debug/app-debug.apk
```

O copia el APK a tu teléfono y ábrelo manualmente.

---

## 🔧 Compilar desde Código

### Requisitos

- Android Studio (recomendado)
- JDK 11 o superior
- Gradle (incluido en el proyecto)

### Compilación

**Opción 1: Android Studio**
1. Abre el proyecto `android-app` en Android Studio
2. `File > Sync Project with Gradle Files`
3. `Build > Build Bundle(s) / APK(s) > Build APK(s)`

**Opción 2: Línea de comandos**
```powershell
cd android-app
.\gradlew assembleDebug
```

**Opción 3: Script automatizado**
```powershell
cd android-app
.\compilar_apk.ps1
```

El APK se generará en: `app/build/outputs/apk/debug/app-debug.apk`

---

## ⚙️ Configuración

### Google Sheets (Obligatorio)

La app requiere Google Sheets para persistencia de datos.

**Configuración actual:**
- SPREADSHEET_ID: `17EBvx8s1IsxcV9-RigMxYvUxgz15ZA6yIuHyY9f8xGk`
- Service Account: `finanzas@vstudio-476115.iam.gserviceaccount.com`

**Pasos:**
1. Abre tu hoja: [Ver hoja](https://docs.google.com/spreadsheets/d/17EBvx8s1IsxcV9-RigMxYvUxgz15ZA6yIuHyY9f8xGk/edit)
2. Haz clic en "Compartir"
3. Agrega el email: `finanzas@vstudio-476115.iam.gserviceaccount.com`
4. Dale permisos de **Editor**

**Guía completa:** Ver `android-app/CONFIGURACION.md`

### Gemini AI (Opcional)

El asistente IA está preconfigurado con una API key.

**Si necesitas cambiar la API key:**
1. Obtén una nueva en: https://aistudio.google.com/app/apikey
2. Edita: `android-app/app/src/main/java/com/finanzasproactivas/data/repository/GeminiRepository.kt`
3. Línea 12: `private val defaultApiKey = "tu_nueva_api_key"`
4. Recompila el APK

---

## 📚 Documentación

### Guías de Configuración
- 📖 `android-app/CONFIGURACION.md` - Configuración completa
- 🔐 `android-app/OBTENER_CREDENCIALES_SERVICE_ACCOUNT.md` - Credenciales de Google
- 🤖 `android-app/CONFIGURACION_GEMINI_API_KEY.md` - API de Gemini

### Solución de Problemas
- 🔧 `android-app/SOLUCION_ERRORES_ANDROID.md` - Errores de Google Sheets y Gemini
- 📋 `android-app/RESUMEN_CAMBIOS_ANDROID.md` - Últimos cambios

### Compilación
- ⚙️ `android-app/COMPILAR_APK.md` - Guía de compilación
- 📍 `android-app/UBICACION_APK.md` - Dónde encontrar el APK

---

## 🛠️ Tecnologías

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose + Material Design 3
- **Gráficos:** Vico Charts
- **Backend:** Google Sheets API
- **IA:** Google Gemini API
- **Arquitectura:** MVVM + Repository Pattern

---

## 📊 Estructura del Proyecto

```
finanzas/
├── android-app/                    # Proyecto Android
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── assets/
│   │   │   │   └── credentials.json    # Credenciales de Google
│   │   │   ├── java/com/finanzasproactivas/
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/          # Modelos de datos
│   │   │   │   │   └── repository/     # Repositorios (Google Sheets, Gemini)
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/        # Pantallas de la app
│   │   │   │   │   ├── components/     # Componentes reutilizables
│   │   │   │   │   └── viewmodel/      # ViewModels
│   │   │   │   └── MainActivity.kt
│   │   │   └── res/                    # Recursos (layouts, strings, etc.)
│   │   └── build.gradle
│   ├── build.gradle
│   └── *.md                            # Documentación
├── logo_finanzas.png                   # Logo de la app
└── README.md                           # Este archivo
```

---

## 🐛 Solución de Problemas

### "Error 403: Sin permisos"

**Causa:** La hoja no está compartida con el service account.

**Solución:**
1. Abre la hoja de Google Sheets
2. Compartir con: `finanzas@vstudio-476115.iam.gserviceaccount.com`
3. Permisos de Editor

### "API Key Inválida" (Gemini)

**Causa:** La API key ha expirado o es inválida.

**Solución:**
1. Obtén nueva key: https://aistudio.google.com/app/apikey
2. Actualiza `GeminiRepository.kt` línea 12
3. Recompila

### Más problemas

Consulta: `android-app/SOLUCION_ERRORES_ANDROID.md`

---

## 📱 Capturas de Pantalla

La app incluye:
- Dashboard con métricas en tiempo real
- Gestión de movimientos con formularios intuitivos
- Gráficos interactivos con múltiples vistas
- Chat con IA para consultas financieras
- Gestión de presupuestos con alertas
- Análisis de tendencias y patrones

---

## 🔒 Seguridad

- 🔐 Credenciales almacenadas en archivo local (no en código)
- ☁️ Sincronización segura con Google Sheets API
- 🔑 API keys no expuestas en el repositorio
- ✅ Service account con permisos limitados

**Importante:** No compartas:
- `credentials.json`
- API key de Gemini
- SPREADSHEET_ID público

---

## 📞 Soporte

- 📚 **Documentación:** Carpeta `android-app/*.md`
- 🔧 **Problemas:** Ver `SOLUCION_ERRORES_ANDROID.md`
- 📝 **Logs:** `adb logcat | Select-String "finanzasproactivas"`

---

## 🆕 Últimas Actualizaciones

**27 de Enero, 2026:**
- ✅ Corregidos errores de Google Sheets con mensajes descriptivos
- ✅ Corregidos errores de Gemini AI con diagnóstico detallado
- ✅ Migración completa de Streamlit a Android nativo
- ✅ Documentación actualizada para Android

Ver detalles: `android-app/RESUMEN_CAMBIOS_ANDROID.md`

---

## 🚀 Inicio Rápido

```powershell
# 1. Compilar APK
cd android-app
.\gradlew assembleDebug

# 2. Instalar en dispositivo
adb install -r app\build\outputs\apk\debug\app-debug.apk

# 3. Verificar configuración
# - Compartir hoja de Google Sheets con service account
# - Abrir la app y verificar sincronización
```

---

## 📄 Licencia

Proyecto de uso personal.

---

**Hecho con ❤️ en Kotlin y Jetpack Compose**
