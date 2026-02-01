# 📦 Ubicación del APK para Instalación

## 📍 Ruta del APK

**Ubicación completa:**
```
C:\Users\Luci\Documents\finanzas\android-app\app\build\outputs\apk\debug\app-debug.apk
```

**Ruta relativa desde el proyecto:**
```
android-app\app\build\outputs\apk\debug\app-debug.apk
```

## 📱 Cómo Instalar en tu Teléfono Android

### Método 1: Instalación Manual (Más Fácil)

1. **Copiar el APK a tu teléfono:**
   - **Por USB**: Conecta tu teléfono, abre la carpeta del APK y cópialo a tu teléfono
   - **Por Email**: Envíatelo a ti mismo y ábrelo desde el teléfono
   - **Por Google Drive/Dropbox**: Sube el APK y descárgalo en tu teléfono

2. **En tu teléfono Android:**
   - Ve a **Configuración > Seguridad** (o **Configuración > Aplicaciones**)
   - Habilita **"Instalar desde fuentes desconocidas"** o **"Instalar aplicaciones desconocidas"**
   - Abre el archivo APK desde el administrador de archivos
   - Sigue las instrucciones de instalación

### Método 2: Instalación por USB (Más Rápido)

1. **Conecta tu teléfono por USB**
2. **Habilita "Depuración USB"** en tu teléfono:
   - Ve a **Configuración > Acerca del teléfono**
   - Toca **"Número de compilación"** 7 veces para activar opciones de desarrollador
   - Ve a **Configuración > Opciones de desarrollador**
   - Habilita **"Depuración USB"**

3. **Ejecuta este comando:**
   ```powershell
   cd android-app
   & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
   ```

### Método 3: Desde Android Studio

1. Conecta tu teléfono por USB
2. Habilita "Depuración USB"
3. Abre el proyecto en Android Studio
4. Haz clic en el botón **"Run"** (▶)
5. Selecciona tu dispositivo de la lista
6. La app se instalará y ejecutará automáticamente

## 🔍 Verificar que el APK Existe

Para verificar que el APK está en su lugar, ejecuta:

```powershell
Test-Path "android-app\app\build\outputs\apk\debug\app-debug.apk"
```

Si devuelve `True`, el APK está listo.

## 📝 Notas

- El APK se regenera cada vez que compilas la app
- Si cambias el código, necesitas recompilar para generar un nuevo APK
- El APK de debug es para pruebas (no está firmado para producción)
- Para distribución, necesitarías generar un APK de release firmado

## 🚀 Abrir Carpeta del APK

Para abrir la carpeta del APK en el explorador de Windows:

```powershell
explorer.exe "android-app\app\build\outputs\apk\debug"
```

O navega manualmente a:
```
C:\Users\Luci\Documents\finanzas\android-app\app\build\outputs\apk\debug
```
