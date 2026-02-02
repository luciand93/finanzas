# Arquitectura: Asistente IA (Gemini) vía Vercel

## Objetivo

**Desde Andorra** (y otras regiones) el servicio de Gemini no está disponible o tiene restricciones cuando se llama **directamente desde el dispositivo**. Por eso la app **nunca** llama a la API de Gemini desde el móvil.

En su lugar:

1. **La app** (Android, desde Andorra) solo llama a **Vercel**.
2. **El servidor de Vercel** (en una región donde Gemini sí está disponible) recibe la petición y **ejecuta la llamada a la API de Gemini**.
3. Vercel devuelve la respuesta de Gemini a la app.

Así, la restricción regional se evita: quien habla con Gemini es el servidor de Vercel, no el dispositivo del usuario.

---

## Flujo

```
[App Android - Andorra]
        │
        │  POST /api/gemini/chat
        │  (solo necesita llegar a Vercel)
        ▼
[Vercel - servidor en la nube]
        │
        │  Usa GEMINI_API_KEY (variable de entorno)
        │  Llama a Google Gemini API
        ▼
[API de Gemini - Google]
        │
        │  Respuesta
        ▼
[Vercel] → devuelve respuesta → [App Android]
```

---

## Implementación

| Componente | Rol |
|------------|-----|
| **App (GeminiRepository.kt)** | Envía `message` y `context` a `https://tu-proyecto.vercel.app/api/gemini/chat`. No contiene API key de Gemini. |
| **Vercel (api/gemini/chat.js)** | Recibe el POST, usa `process.env.GEMINI_API_KEY`, llama a `GoogleGenerativeAI` y devuelve la respuesta. |
| **Variable en Vercel** | `GEMINI_API_KEY` configurada en el dashboard de Vercel (Project → Settings → Environment Variables). |

La app solo necesita **conectividad a Vercel**. La clave de Gemini solo existe en el servidor de Vercel.

---

## Resumen

- **Idea:** usar Vercel para que **su servidor** ejecute las llamadas al asistente IA (Gemini) a través de su API.
- **Motivo:** desde Andorra (u otras regiones) el servicio no funciona si se llama desde el dispositivo; sí funciona cuando se llama desde el servidor de Vercel.
- **Estado:** esta arquitectura está implementada; la app ya usa solo Vercel para el chat con Gemini.
