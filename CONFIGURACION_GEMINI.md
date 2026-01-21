# 🤖 Configuración de Gemini AI

## ¿Qué es Gemini?

Gemini es la inteligencia artificial de Google que te permite hacer preguntas sobre tus finanzas en lenguaje natural. El asistente puede:

- 📊 Analizar tus gastos e ingresos
- 💡 Responder preguntas específicas sobre tus datos
- 🔍 Comparar meses y categorías
- 💰 Ayudar con la gestión de presupuestos
- ✅ Ofrecer recomendaciones personalizadas

## 🚀 Configuración Paso a Paso

### 1. Obtener una API Key de Google AI

1. Visita [Google AI Studio](https://makersuite.google.com/app/apikey) o [aistudio.google.com](https://aistudio.google.com/app/apikey)
2. Inicia sesión con tu cuenta de Google
3. Haz clic en "Create API Key" o "Crear API Key"
4. Copia la API key generada (guárdala de forma segura)

**Nota:** La API key de Gemini es gratuita con límites generosos para uso personal.

### 2. Configurar la API Key en Streamlit Cloud

1. Ve a tu aplicación en [Streamlit Cloud](https://streamlit.io/cloud)
2. Haz clic en "Settings" (Configuración)
3. Ve a la pestaña "Secrets"
4. Agrega la siguiente línea:
   ```
   GEMINI_API_KEY = "tu_api_key_aqui"
   ```
5. Guarda los cambios
6. La aplicación se reiniciará automáticamente

### 3. Configurar la API Key Localmente

Si estás ejecutando la aplicación localmente:

1. Crea o edita el archivo `.streamlit/secrets.toml` en la raíz de tu proyecto
2. Agrega:
   ```toml
   GEMINI_API_KEY = "tu_api_key_aqui"
   ```
3. Reinicia la aplicación Streamlit

**Importante:** No compartas tu API key públicamente. El archivo `secrets.toml` está en `.gitignore` por defecto.

## 💬 Uso del Asistente

1. Ve a la pestaña **"🤖 Asesor"** en la aplicación
2. Encontrarás la sección **"🤖 Asistente IA con Gemini"**
3. Puedes:
   - Hacer preguntas en lenguaje natural
   - Usar las preguntas sugeridas
   - Consultar sobre gastos, ingresos, categorías, presupuestos, etc.

### Ejemplos de Preguntas

- "¿Cuánto he gastado este mes?"
- "¿Cuál es mi categoría con más gastos?"
- "¿Cómo van mis presupuestos?"
- "Compara mis gastos de este mes con el anterior"
- "¿En qué categoría debería ahorrar más?"
- "Dame recomendaciones para mejorar mis finanzas"

## 🔧 Solución de Problemas

### "Gemini no está configurado"

- Verifica que hayas agregado la API key en los secrets
- Asegúrate de que la variable se llame exactamente `GEMINI_API_KEY`
- Reinicia la aplicación después de agregar la API key

### "Error al comunicarse con Gemini"

- Verifica que tu API key sea válida
- Asegúrate de tener conexión a internet
- Revisa que la API key no haya expirado o sido revocada

### La librería no está instalada

Ejecuta:
```bash
pip install google-generativeai
```

O si usas `requirements.txt`:
```bash
pip install -r requirements.txt
```

## 📝 Notas Importantes

- La API key de Gemini es gratuita pero tiene límites de uso
- Los datos financieros se envían a la API de Gemini para generar respuestas
- La API key se almacena de forma segura en los secrets de Streamlit
- Gemini analiza tus datos en tiempo real según lo que esté guardado en tu aplicación

## 🔒 Privacidad

- Tus datos financieros se procesan a través de la API de Google Gemini
- Google no almacena tus conversaciones de forma permanente
- La API key es personal y no debe compartirse
- Para mayor privacidad, puedes revisar las políticas de Google AI
