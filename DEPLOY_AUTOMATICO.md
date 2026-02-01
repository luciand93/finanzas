# 🚀 Despliegue Automático - Finanzas Proactivas

## Descripción

Cada vez que hagas cambios en el proyecto, ejecuta el script de despliegue completo. Este script:

1. **Compila el APK** de la app Android
2. **Abre la carpeta** donde está generado el APK
3. **Sube todas las modificaciones** a Git (origin y otros remotos)
4. **Despliega automáticamente** el backend en Vercel

---

## Uso Rápido

### Opción 1: PowerShell (recomendado)

Desde la carpeta raíz del proyecto:

```powershell
.\deploy-completo.ps1
```

### Opción 2: npm

```bash
npm run deploy
```

---

## Ubicación del APK

Tras la compilación, el APK se genera en:

```
android-app\app\build\outputs\apk\debug\app-debug.apk
```

La carpeta se abre automáticamente en el Explorador de Windows.

---

## Opciones del Script

| Parámetro | Descripción | Ejemplo |
|-----------|-------------|---------|
| (ninguno) | Ejecuta todo | `.\deploy-completo.ps1` |
| `-SkipApk` | Salta compilación APK | `.\deploy-completo.ps1 -SkipApk` |
| `-SkipGit` | Salta push a Git | `.\deploy-completo.ps1 -SkipGit` |
| `-SkipVercel` | Salta deploy Vercel | `.\deploy-completo.ps1 -SkipVercel` |
| `-CommitMsg "texto"` | Mensaje de commit personalizado | `.\deploy-completo.ps1 -CommitMsg "Fix bug login"` |

### Ejemplos

```powershell
# Solo compilar APK y abrir carpeta (sin Git ni Vercel)
.\deploy-completo.ps1 -SkipGit -SkipVercel

# Solo subir a Git y desplegar Vercel (sin recompilar APK)
.\deploy-completo.ps1 -SkipApk

# Todo con mensaje personalizado
.\deploy-completo.ps1 -CommitMsg "Nuevos indicadores financieros"
```

### Scripts npm alternativos

```bash
npm run deploy              # Todo completo
npm run deploy:apk-only     # Solo APK
npm run deploy:vercel-only  # Solo Vercel (backend)
npm run deploy:skip-apk     # Todo excepto APK
npm run deploy:skip-git     # Todo excepto Git
npm run deploy:skip-vercel  # Todo excepto Vercel
```

---

## Requisitos Previos

### Para compilar APK:
- **Android SDK** instalado (viene con Android Studio)
- **Java 17** o superior
- Ejecutar desde la carpeta del proyecto

### Para Git push:
- Repositorio Git inicializado
- Remoto configurado (origin)
- Credenciales configuradas (GitHub, GitLab, etc.)

### Para Vercel:
- **Vercel CLI** instalado: `npm install -g vercel`
- Proyecto vinculado a Vercel: `cd backend && vercel link`
- Variables de entorno configuradas en Vercel

---

## Orden de Ejecución

1. **APK**: `gradlew assembleDebug` → genera `app-debug.apk`
2. **Abrir carpeta**: `explorer.exe` sobre la carpeta del APK
3. **Git**: `git add -A` → `git commit` → `git push origin` + otros remotos
4. **Vercel**: `cd backend && npm run deploy` → `vercel --prod`

---

## Solución de Problemas

### "No se puede ejecutar scripts en este sistema"
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Error al compilar APK
- Verifica que Android SDK esté instalado
- Usa `-SkipApk` si no necesitas el APK en este momento
- Compila manualmente desde Android Studio

### Error en git push
- Verifica tu conexión a internet
- Comprueba credenciales: `git config --list`
- Usa `-SkipGit` temporalmente

### Error en Vercel
- Instala Vercel CLI: `npm i -g vercel`
- Vincula el proyecto: `cd backend && vercel link`
- Configura variables en el dashboard de Vercel

---

## Flujo de Trabajo Recomendado

1. Haz tus cambios en el código
2. Ejecuta: `.\deploy-completo.ps1`
3. Espera a que termine (APK + Git + Vercel)
4. La carpeta del APK se abrirá automáticamente
5. Copia el APK a tu teléfono o compártelo
6. El backend ya estará actualizado en producción

---

## Notas

- El APK **no se sube a Git** (está en .gitignore) - solo el código fuente
- Si no hay cambios en Git, el script continuará sin error
- El deploy de Vercel actualiza la API en producción
- Puedes ejecutar el script desde cualquier lugar si usas la ruta completa al script
