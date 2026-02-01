# 📱 Crear Emulador Google Pixel 10 Pro XL

## Nota Importante

**Google Pixel 10 Pro XL aún no existe** (el último modelo es Pixel 8 Pro). Sin embargo, puedes crear un emulador con especificaciones similares a un Pixel grande.

## Opción 1: Desde Android Studio (Recomendado)

### Paso 1: Abrir AVD Manager
1. Abre Android Studio
2. Ve a **Tools > Device Manager** (o **Tools > AVD Manager** en versiones anteriores)
3. Haz clic en **"Create Device"**

### Paso 2: Seleccionar Hardware
1. En la lista de dispositivos, busca **"Pixel 7 Pro"** o **"Pixel 8 Pro"** (los más cercanos)
2. O selecciona **"Phone"** y luego **"Pixel 6 Pro"**
3. Haz clic en **"Next"**

### Paso 3: Seleccionar Imagen del Sistema
1. Selecciona una imagen del sistema (recomendado: **API 34** o superior)
2. Si no está descargada, haz clic en **"Download"** junto a la imagen
3. Espera a que se descargue
4. Haz clic en **"Next"**

### Paso 4: Configurar AVD
1. **AVD Name**: `Pixel_10_Pro_XL` (o el nombre que prefieras)
2. **Startup orientation**: Portrait (vertical)
3. Haz clic en **"Show Advanced Settings"** para personalizar:
   - **RAM**: 4096 MB (4 GB) o más
   - **VM heap**: 512 MB
   - **Internal Storage**: 4096 MB
   - **SD Card**: Opcional
4. Haz clic en **"Finish"**

### Paso 5: Iniciar Emulador
1. En Device Manager, encuentra tu nuevo emulador
2. Haz clic en el botón **▶ (Play)** para iniciarlo
3. Espera a que el emulador se inicie (puede tardar 1-2 minutos)

## Opción 2: Desde Línea de Comandos

### Crear AVD con especificaciones personalizadas

```powershell
# 1. Listar imágenes del sistema disponibles
$sdkPath = "$env:LOCALAPPDATA\Android\Sdk"
& "$sdkPath\cmdline-tools\latest\bin\sdkmanager.bat" --list | Select-String "system-images"

# 2. Instalar imagen del sistema (si no está instalada)
& "$sdkPath\cmdline-tools\latest\bin\sdkmanager.bat" "system-images;android-34;google_apis;x86_64"

# 3. Crear AVD (requiere avdmanager)
& "$sdkPath\cmdline-tools\latest\bin\avdmanager.bat" create avd `
    -n Pixel_10_Pro_XL `
    -k "system-images;android-34;google_apis;x86_64" `
    -d "pixel_7_pro"
```

## Especificaciones Recomendadas para Pixel 10 Pro XL

- **Pantalla**: 6.7" o superior
- **Resolución**: 1440 x 3200 (QHD+)
- **RAM**: 4-8 GB
- **Almacenamiento interno**: 4 GB mínimo
- **API Level**: 34 (Android 14) o superior

## Iniciar Emulador desde Línea de Comandos

Una vez creado el emulador:

```powershell
$sdkPath = "$env:LOCALAPPDATA\Android\Sdk"
& "$sdkPath\emulator\emulator.exe" -avd Pixel_10_Pro_XL
```

## Instalar la App en el Emulador

Una vez que el emulador esté corriendo:

```powershell
cd android-app
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install app\build\outputs\apk\debug\app-debug.apk
```

O desde Android Studio:
1. Con el emulador corriendo
2. Haz clic en **Run** (▶)
3. Selecciona el emulador de la lista
4. La app se instalará y ejecutará automáticamente

## Solución de Problemas

### Error: "No system images installed"
- Abre Android Studio
- Ve a **Tools > SDK Manager**
- Pestaña **"SDK Platforms"**
- Marca **"Show Package Details"**
- Expande **"Android 14.0 (API 34)"**
- Marca **"Google APIs x86_64 System Image"**
- Haz clic en **"Apply"** y espera la descarga

### El emulador es muy lento
- Habilita **Hardware Acceleration** (HAXM o Hyper-V)
- Reduce la RAM asignada
- Cierra otras aplicaciones pesadas

### No puedo crear AVD desde línea de comandos
- Usa Android Studio (Opción 1) - es más fácil y visual
