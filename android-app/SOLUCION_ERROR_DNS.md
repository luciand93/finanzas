# Solución: "unable to resolve host finanzas-api-three.vercel.app"

Este error significa que el **dispositivo o emulador no puede resolver el nombre del servidor** (DNS). La API funciona correctamente en internet; el problema está en la red que usa la app.

---

## Comprobar que la API responde

Desde un navegador en el **mismo dispositivo** donde falla la app, abre:

**https://finanzas-api-three.vercel.app/api/health**

- Si **no carga**: el problema es la red o el DNS de ese dispositivo.
- Si **sí carga**: sigue los pasos siguientes (emulador o permisos).

---

## Si usas **emulador Android**

1. **Reinicio en frío (Cold Boot)**  
   En Android Studio: AVD Manager → ⋮ del emulador → **Cold Boot Now**.

2. **Comprobar red del emulador**  
   Abre Chrome en el emulador y entra a https://finanzas-api-three.vercel.app/api/health.  
   Si tampoco carga ahí, el emulador no tiene DNS correcto.

3. **Usar DNS de Google en el emulador**  
   - En el emulador: **Settings → Network & internet → Internet** (o **Private DNS**).  
   - O crea un AVD con imagen **Google Play** (suelen tener mejor red).

4. **Probar en dispositivo físico**  
   Instala la APK en un móvil con **datos móviles** o otra WiFi. Si ahí funciona, el fallo es solo del emulador/red actual.

---

## Si usas **móvil o tablet (dispositivo real)**

1. **Probar otra red**  
   - Pasar de WiFi a **datos móviles** (o al revés).  
   - Probar en otra WiFi (casa, trabajo, otro sitio).

2. **Redes restrictivas**  
   En redes corporativas, universidad o con filtros, a veces bloquean o alteran DNS.  
   En ese caso: usar **datos móviles** o otra red donde no haya filtro.

3. **Comprobar en el navegador del propio dispositivo**  
   Abre **Chrome** (o Safari) en el móvil y entra a:  
   https://finanzas-api-three.vercel.app/api/health  
   Si no abre, el problema es de red/DNS en ese dispositivo.

---

## URL configurable (opcional)

Si en tu red no se puede usar `finanzas-api-three.vercel.app`, puedes **cambiar la URL** sin tocar Kotlin:

1. Abre **android-app/app/build.gradle** (módulo `app`).

2. Busca la línea:
   ```gradle
   buildConfigField "String", "API_BASE_URL", "\"https://finanzas-api-three.vercel.app/api/\""
   ```

3. Sustituye la URL por la que sí funcione en tu red (por ejemplo otro despliegue Vercel o un túnel como ngrok). Debe terminar en `/api/`.

4. **Build → Rebuild Project** y vuelve a ejecutar la app.

La app ya usa `BuildConfig.API_BASE_URL` en `RetrofitClient.kt`.

---

## Resumen

| Dónde falla | Qué hacer |
|-------------|-----------|
| Emulador   | Cold Boot, probar en dispositivo real o otra red. |
| Móvil WiFi | Probar datos móviles u otra WiFi. |
| Red trabajo/universidad | Probar en red doméstica o datos. |
| Navegador del dispositivo no abre la URL | Es problema de red/DNS del dispositivo; cambiar red o DNS. |

La API está operativa; el mensaje "no address associated with hostname" indica que **ese dispositivo, en esa red, no está pudiendo resolver el nombre** del servidor.
