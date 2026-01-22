# Finanzas Proactivas - Aplicación Android Nativa

Esta es una aplicación Android completamente nativa escrita en Kotlin usando Jetpack Compose. Ya NO depende de Streamlit.

## Características

✅ **Aplicación 100% Nativa**
- Escrita en Kotlin
- UI con Jetpack Compose
- Sin dependencia de Streamlit

✅ **Funcionalidades Online**
- Google Sheets API para almacenamiento de datos
- Gemini AI para chat inteligente
- Todo funciona online, sin necesidad de servidor propio

✅ **UI Moderna**
- Diseño dark mode
- Animaciones fluidas
- Interfaz intuitiva y responsive

## Configuración Requerida

### 1. Google Sheets API

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto o selecciona uno existente
3. Habilita la API de Google Sheets
4. Crea credenciales (Service Account)
5. Descarga el archivo JSON de credenciales
6. Colócalo en `app/src/main/assets/credentials.json`
7. Edita `GoogleSheetsRepository.kt` y configura `SPREADSHEET_ID`

### 2. Gemini API

1. Obtén tu API key de [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Configúrala en la pantalla de Configuración de la app
3. O edita `GeminiRepository.kt` para hardcodearla temporalmente

### 3. Compilar

```bash
# En Android Studio:
Build > Build Bundle(s) / APK(s) > Build APK(s)
```

## Estructura del Proyecto

```
app/src/main/java/com/finanzasproactivas/
├── data/
│   ├── model/          # Modelos de datos
│   └── repository/     # Repositorios (Google Sheets, Gemini)
├── ui/
│   ├── components/     # Componentes reutilizables
│   ├── screens/        # Pantallas principales
│   ├── navigation/     # Navegación
│   └── theme/          # Tema y colores
└── MainActivity.kt     # Actividad principal
```

## Funcionalidades Implementadas

- ✅ Dashboard principal con métricas
- ✅ Formulario de nuevo movimiento (bottom sheet)
- ✅ Menú lateral deslizable
- ✅ Chat con Gemini AI
- ✅ Recomendaciones inteligentes
- ✅ Análisis de patrones
- ✅ Navegación entre secciones

## Funcionalidades Pendientes

- ⏳ Gráficos interactivos
- ⏳ Tabla de movimientos editable
- ⏳ Exportar/Importar datos
- ⏳ Gestión de presupuestos
- ⏳ Edición de movimientos

## Notas

- La aplicación requiere conexión a internet
- Los datos se almacenan en Google Sheets
- La IA funciona completamente online con Gemini
- No hay servidor propio necesario
