# Solución: Movimientos Perdidos y Problemas con Gemini

## 🔍 Problema 1: Movimientos Perdidos

### Posibles Causas

1. **Rango incorrecto al leer**: El código estaba leyendo solo hasta la columna H, pero ahora guardamos hasta la columna I (incluye `esConjunto`)
2. **IDs no persistentes**: Si los movimientos antiguos no tenían ID, pueden no aparecer correctamente
3. **Problema de sincronización**: Los datos pueden estar en Google Sheets pero no se están cargando correctamente

### Solución Implementada

✅ **Corregido el rango de lectura**: Cambiado de `Finanzas!A2:H` a `Finanzas!A2:I` para incluir todas las columnas

### Verificación

1. **Abre tu hoja de Google Sheets directamente**:
   - https://docs.google.com/spreadsheets/d/17EBvx8s1IsxcV9-RigMxYvUxgz15ZA6yIuHyY9f8xGk/edit
   - Verifica que los movimientos estén ahí

2. **Si los movimientos están en la hoja pero no aparecen en la app**:
   - Puede ser que no tengan ID en la columna A
   - La app necesita un ID para identificar cada movimiento

3. **Si los movimientos NO están en la hoja**:
   - Puede ser que no se guardaron correctamente
   - Verifica los permisos del Service Account
   - Verifica que la hoja esté compartida con el email del Service Account

### Recuperar Movimientos Perdidos

Si los movimientos están en la hoja pero no aparecen:

1. Abre la hoja de Google Sheets
2. Verifica que la columna A (ID) tenga valores
3. Si la columna A está vacía para algunos movimientos:
   - Agrega un ID único (puede ser un UUID o un número secuencial)
   - Guarda los cambios
   - Recarga la app

---

## 🔍 Problema 2: Asistente de Gemini No Funciona (Andorra)

### Posible Causa: Restricción Regional

Sí, **puede ser un problema de localización**. Google Gemini puede tener restricciones regionales y Andorra puede no estar en la lista de países soportados inicialmente.

### Soluciones Implementadas

✅ **Múltiples modelos de fallback**: La app ahora intenta con:
1. `gemini-1.5-flash` (más rápido, más disponible)
2. `gemini-1.5-pro` (más potente)
3. `gemini-pro` (modelo estándar)

✅ **Mensajes de error mejorados**: Ahora indica si es un problema de región

### Soluciones Alternativas

#### Opción 1: Usar VPN
1. Conecta a una VPN con servidor en España, Francia o otro país donde Gemini esté disponible
2. Prueba el asistente nuevamente

#### Opción 2: Verificar API Key
1. Ve a Google AI Studio: https://makersuite.google.com/app/apikey
2. Verifica que tu API key esté activa
3. Verifica que tengas acceso a la API de Gemini

#### Opción 3: Usar API Key con Acceso Global
1. Algunas API keys pueden tener restricciones regionales
2. Verifica en Google Cloud Console si hay restricciones en tu API key
3. Si hay restricciones, créala sin restricciones o con acceso global

#### Opción 4: Probar desde otro dispositivo/red
1. Prueba desde una red diferente (móvil vs WiFi)
2. Prueba desde otro dispositivo si es posible

### Verificación del Problema

Para saber si es un problema de región, revisa el mensaje de error:

- Si dice "not available" o "region" → Es un problema de localización
- Si dice "API key invalid" → Es un problema de credenciales
- Si dice "network error" → Es un problema de conexión

---

## 🛠️ Pasos de Diagnóstico

### Para Movimientos Perdidos

1. **Verificar Google Sheets directamente**:
   ```
   https://docs.google.com/spreadsheets/d/17EBvx8s1IsxcV9-RigMxYvUxgz15ZA6yIuHyY9f8xGk/edit
   ```

2. **Verificar permisos del Service Account**:
   - Abre la hoja
   - Haz clic en "Compartir"
   - Verifica que el email del Service Account esté en la lista con permisos de "Editor"

3. **Verificar el rango en el código**:
   - El código ahora lee `Finanzas!A2:I` (incluye todas las columnas)

4. **Verificar IDs**:
   - Los movimientos necesitan un ID único en la columna A
   - Si falta, la app puede no cargarlos correctamente

### Para Gemini

1. **Probar con VPN**:
   - Conecta a VPN (España, Francia, etc.)
   - Prueba el asistente

2. **Verificar API Key**:
   - Ve a: https://makersuite.google.com/app/apikey
   - Verifica que la key esté activa

3. **Verificar en Google Cloud Console**:
   - Ve a: https://console.cloud.google.com/
   - APIs y servicios > Credenciales
   - Verifica que no haya restricciones regionales en tu API key

4. **Probar desde otro dispositivo/red**:
   - Prueba desde móvil con datos
   - O desde otro dispositivo

---

## 📝 Notas Importantes

1. **Los movimientos NO se pierden si están en Google Sheets**: Los datos están en la nube, no se pierden fácilmente
2. **Gemini puede tener restricciones regionales**: Esto es común con servicios de IA
3. **La app ahora es más robusta**: Con los cambios implementados, debería funcionar mejor

---

## 🔄 Próximos Pasos

1. **Recompila la app** con los cambios
2. **Verifica Google Sheets** directamente para ver si los movimientos están ahí
3. **Prueba el asistente** con VPN si es necesario
4. **Reporta los resultados** para poder ayudar más
