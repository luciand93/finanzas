# 🚀 Guía de Configuración Completa

## Paso 1: Configurar Supabase

### 1.1 Crear cuenta y proyecto

1. Ve a [https://supabase.com](https://supabase.com)
2. Crea una cuenta gratis
3. Crea un nuevo proyecto:
   - **Nombre**: finanzas-proactivas
   - **Database Password**: (guárdala bien)
   - **Region**: Europe West (Ireland) - recomendado

### 1.2 Crear tablas

1. Ve a **SQL Editor** en el dashboard
2. Copia y pega el contenido de `supabase/schema.sql`
3. Click en **Run** (▶️)
4. Verifica que las tablas se crearon:
   - Ve a **Table Editor**
   - Deberías ver: `movimientos`, `categorias`

### 1.3 Obtener credenciales

1. Ve a **Settings** → **API**
2. Copia estos valores:
   - **Project URL**: `https://xxxxx.supabase.co`
   - **anon/public key**: `eyJhbGci...`

---

## Paso 2: Configurar Backend (Vercel)

### 2.1 Instalar dependencias

```bash
cd backend
npm install
```

### 2.2 Configurar variables de entorno locales

```bash
# Copiar archivo de ejemplo
cp .env.example .env

# Editar .env con tus valores
```

En `.env`:
```env
GEMINI_API_KEY=tu-api-key-de-gemini
SUPABASE_URL=https://xxxxx.supabase.co
SUPABASE_KEY=eyJhbGci...
```

### 2.3 Probar localmente

```bash
npm run dev
```

Abre: http://localhost:3000/api/health

Deberías ver:
```json
{
  "status": "ok",
  "message": "✅ API funcionando correctamente"
}
```

---

## Paso 3: Deployment en Vercel

### 3.1 Instalar Vercel CLI

```bash
npm install -g vercel
```

### 3.2 Login

```bash
vercel login
```

### 3.3 Deploy

```bash
cd backend
vercel
```

Sigue las instrucciones:
- **Set up and deploy?** → Yes
- **Which scope?** → Tu cuenta
- **Link to existing project?** → No
- **Project name?** → finanzas-api
- **Directory?** → ./ (current)

### 3.4 Configurar variables de entorno en Vercel

```bash
# Agregar cada variable
vercel env add GEMINI_API_KEY
vercel env add SUPABASE_URL
vercel env add SUPABASE_KEY
```

O desde el dashboard:
1. Ve a [https://vercel.com](https://vercel.com)
2. Selecciona tu proyecto
3. **Settings** → **Environment Variables**
4. Agrega las 3 variables

### 3.5 Deploy a producción

```bash
vercel --prod
```

Obtendrás una URL como: `https://finanzas-api.vercel.app`

---

## Paso 4: Migrar Datos de Google Sheets a Supabase

### 4.1 Exportar datos actuales

Crea un script temporal en el backend:

```bash
node scripts/migrate-from-sheets.js
```

(Te proporcionaré este script después)

---

## Paso 5: Actualizar App Android

### 5.1 Agregar URL del backend

En `app/build.gradle`:
```gradle
android {
    defaultConfig {
        buildConfigField "String", "API_BASE_URL", "\"https://finanzas-api.vercel.app\""
    }
}
```

### 5.2 Modificar repositorios

(Te proporcionaré el código Kotlin después)

---

## 📝 Verificación Final

### Prueba los endpoints:

```bash
# Health check
curl https://finanzas-api.vercel.app/api/health

# Listar movimientos (debería estar vacío al inicio)
curl https://finanzas-api.vercel.app/api/movimientos

# Crear movimiento de prueba
curl -X POST https://finanzas-api.vercel.app/api/movimientos \
  -H "Content-Type: application/json" \
  -d '{
    "fecha": "2026-01-27",
    "tipo": "Gasto",
    "categoria": "Supermercado",
    "concepto": "Compra semanal",
    "importe": 50.00,
    "frecuencia": "Puntual",
    "impacto_mensual": 50.00,
    "es_conjunto": false
  }'

# Probar Gemini
curl -X POST https://finanzas-api.vercel.app/api/gemini/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Hola, ¿cómo estás?"
  }'
```

---

## 🎯 URLs Importantes

- **API Base URL**: https://finanzas-api.vercel.app
- **Supabase Dashboard**: https://app.supabase.com/project/_
- **Vercel Dashboard**: https://vercel.com/dashboard

---

## 🔧 Comandos Útiles

```bash
# Ver logs en tiempo real
vercel logs --follow

# Redeployar
vercel --prod

# Ver información del proyecto
vercel inspect

# Eliminar deployment
vercel rm [deployment-url]
```

---

## ❓ Solución de Problemas

### Error: "GEMINI_API_KEY not configured"
- Verifica que agregaste la variable en Vercel
- Redeploy después de agregar variables

### Error: "Supabase credentials not configured"
- Verifica SUPABASE_URL y SUPABASE_KEY
- Asegúrate de copiar la **anon key**, no la **service_role key**

### Error 404 en endpoints
- Verifica que el archivo esté en `api/`
- Verifica que exporte una función `handler`

---

## 📞 Siguiente Paso

Una vez que tengas el backend funcionando, te proporcionaré:
1. Script de migración de datos
2. Código Kotlin para la app
3. Compilación del nuevo APK
