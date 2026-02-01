# 🌐 Backend API - Finanzas Proactivas

Backend serverless para la app de Finanzas Proactivas.

## 🚀 Tecnologías

- **Vercel Functions**: Hosting serverless
- **Supabase**: Base de datos PostgreSQL
- **Gemini API**: Asistente de IA

## 📋 Endpoints

### Gemini (Asistente IA)
- `POST /api/gemini/chat` - Chat con el asistente

### Movimientos
- `GET /api/movimientos` - Listar todos los movimientos
- `GET /api/movimientos/:id` - Obtener un movimiento
- `POST /api/movimientos` - Crear nuevo movimiento
- `PUT /api/movimientos/:id` - Actualizar movimiento
- `DELETE /api/movimientos/:id` - Eliminar movimiento

### Categorías
- `GET /api/categorias` - Listar categorías
- `POST /api/categorias` - Crear categoría

### Estadísticas
- `GET /api/stats/resumen` - Resumen general
- `GET /api/stats/mensual?mes=2024-01` - Estadísticas mensuales

## 🛠️ Instalación Local

```bash
# Instalar dependencias
npm install

# Copiar variables de entorno
cp .env.example .env

# Editar .env con tus keys

# Ejecutar en local
npm run dev
```

## 🚀 Deployment

```bash
# Instalar Vercel CLI
npm i -g vercel

# Login
vercel login

# Deploy
npm run deploy
```

## 🔑 Variables de Entorno

Configura en Vercel Dashboard o localmente en `.env`:

- `GEMINI_API_KEY`: Tu API key de Google AI
- `SUPABASE_URL`: URL de tu proyecto Supabase
- `SUPABASE_KEY`: Anon key de Supabase

## 📖 Documentación

Ver carpeta `/docs` para más detalles.
