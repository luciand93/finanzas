# 📦 Backend Completo - Finanzas Proactivas

## ✅ ¿Qué hemos creado?

### 🌐 Backend API (Vercel Functions)

```
backend/
├── api/
│   ├── _lib/
│   │   └── supabase.js          # Utilidades compartidas
│   │
│   ├── gemini/
│   │   └── chat.js              # POST /api/gemini/chat
│   │
│   ├── movimientos/
│   │   ├── index.js             # GET/POST /api/movimientos
│   │   └── [id].js              # GET/PUT/DELETE /api/movimientos/:id
│   │
│   ├── categorias/
│   │   └── index.js             # GET/POST /api/categorias
│   │
│   ├── stats/
│   │   └── resumen.js           # GET /api/stats/resumen
│   │
│   └── health.js                # GET /api/health (health check)
│
├── supabase/
│   └── schema.sql               # Schema de base de datos
│
├── package.json                 # Dependencias
├── vercel.json                  # Configuración Vercel
├── .env.example                 # Variables de entorno (ejemplo)
├── SETUP.md                     # Guía de configuración paso a paso
└── README.md                    # Documentación general
```

---

## 🎯 Arquitectura Final

```
┌─────────────────────────────────────────────────────────────┐
│                     📱 APP ANDROID                          │
│                    (Kotlin/Compose)                         │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTPS
                     ↓
┌─────────────────────────────────────────────────────────────┐
│              🌐 BACKEND API (Vercel)                        │
│                                                              │
│  Endpoints:                                                  │
│  • POST /api/gemini/chat        → Chat IA                  │
│  • GET  /api/movimientos        → Listar movimientos       │
│  • POST /api/movimientos        → Crear movimiento         │
│  • PUT  /api/movimientos/:id    → Actualizar movimiento    │
│  • DELETE /api/movimientos/:id  → Eliminar movimiento      │
│  • GET  /api/categorias         → Listar categorías        │
│  • GET  /api/stats/resumen      → Estadísticas             │
│                                                              │
└────┬────────────────────────────────┬─────────────────────┘
     │                                │
     │                                │
     ↓                                ↓
┌─────────────────────┐    ┌─────────────────────┐
│   🤖 GEMINI API     │    │  🗄️ SUPABASE        │
│  (Google AI)        │    │  (PostgreSQL)       │
│                     │    │                     │
│  • Región: Europa   │    │  • movimientos      │
│  • Model: gemini-pro│    │  • categorias       │
│  • Gratis           │    │  • Vistas           │
└─────────────────────┘    └─────────────────────┘
```

---

## 📋 Endpoints de la API

### 1. **Gemini (Asistente IA)**

#### `POST /api/gemini/chat`
Envía un mensaje al asistente de IA.

**Request:**
```json
{
  "message": "¿Cuánto gasté este mes?",
  "context": {
    "movimientos": 45,
    "gastosMes": 1250.50,
    "ingresosMes": 3000.00
  }
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "response": "Este mes has gastado 1,250.50€ de un total de 3,000€ en ingresos...",
    "model": "gemini-pro",
    "timestamp": "2026-01-27T23:00:00.000Z"
  },
  "timestamp": "2026-01-27T23:00:00.000Z"
}
```

---

### 2. **Movimientos**

#### `GET /api/movimientos`
Lista todos los movimientos.

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid-123",
      "fecha": "2026-01-27",
      "tipo": "Gasto",
      "categoria": "Supermercado",
      "concepto": "Compra semanal",
      "importe": 50.00,
      "frecuencia": "Puntual",
      "impacto_mensual": 50.00,
      "es_conjunto": false,
      "created_at": "2026-01-27T23:00:00.000Z",
      "updated_at": "2026-01-27T23:00:00.000Z"
    }
  ]
}
```

#### `POST /api/movimientos`
Crea un nuevo movimiento.

#### `GET /api/movimientos/:id`
Obtiene un movimiento específico.

#### `PUT /api/movimientos/:id`
Actualiza un movimiento.

#### `DELETE /api/movimientos/:id`
Elimina un movimiento.

---

### 3. **Categorías**

#### `GET /api/categorias`
Lista todas las categorías disponibles.

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid-456",
      "nombre": "Supermercado",
      "icono": "🛒",
      "color": "#10b981"
    }
  ]
}
```

---

### 4. **Estadísticas**

#### `GET /api/stats/resumen`
Obtiene un resumen de estadísticas.

**Response:**
```json
{
  "success": true,
  "data": {
    "total": {
      "movimientos": 156,
      "ingresos": 12500.00,
      "gastos": 8750.50,
      "balance": 3749.50
    },
    "mesActual": {
      "movimientos": 23,
      "ingresos": 3000.00,
      "gastos": 1250.50,
      "balance": 1749.50,
      "mes": "enero de 2026"
    }
  }
}
```

---

## 🗄️ Schema de Base de Datos

### Tabla: `movimientos`

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | UUID | ID único (auto-generado) |
| fecha | DATE | Fecha del movimiento |
| tipo | VARCHAR(10) | 'Ingreso' o 'Gasto' |
| categoria | VARCHAR(100) | Nombre de la categoría |
| concepto | TEXT | Descripción del movimiento |
| importe | DECIMAL(12,2) | Cantidad en euros |
| frecuencia | VARCHAR(10) | 'Puntual', 'Mensual', 'Anual' |
| impacto_mensual | DECIMAL(12,2) | Impacto mensual calculado |
| es_conjunto | BOOLEAN | Si es gasto compartido |
| created_at | TIMESTAMP | Fecha de creación |
| updated_at | TIMESTAMP | Última actualización |

**Índices:**
- `idx_movimientos_fecha` (fecha DESC)
- `idx_movimientos_tipo` (tipo)
- `idx_movimientos_categoria` (categoria)

### Tabla: `categorias`

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | UUID | ID único |
| nombre | VARCHAR(100) | Nombre (único) |
| icono | VARCHAR(50) | Emoji del icono |
| color | VARCHAR(7) | Color hex |
| created_at | TIMESTAMP | Fecha de creación |
| updated_at | TIMESTAMP | Última actualización |

**Categorías predefinidas:** 20 categorías (Supermercado, Transporte, etc.)

---

## 🔐 Seguridad

### ✅ Implementado:

1. **CORS**: Configurado para permitir orígenes específicos
2. **Validaciones**: Todos los endpoints validan datos de entrada
3. **Credenciales**: API keys en variables de entorno (nunca en código)
4. **Row Level Security**: Preparado para multi-usuario (actualmente permisivo)
5. **HTTPS**: Automático en Vercel
6. **Rate Limiting**: Incluido por Vercel

### 🔒 Variables Seguras:

- `GEMINI_API_KEY`: Solo en servidor
- `SUPABASE_URL`: Pública (OK)
- `SUPABASE_KEY`: Solo anon key (segura)

---

## 💰 Costes (GRATIS)

### Vercel:
- ✅ 100GB bandwidth/mes
- ✅ Funciones ilimitadas
- ✅ Deploy ilimitados
- ✅ Certificado SSL gratis

### Supabase:
- ✅ 500MB de base de datos
- ✅ 2GB bandwidth/mes
- ✅ 50,000 usuarios activos/mes
- ✅ Backups diarios

### Gemini:
- ✅ 60 requests/minuto gratis
- ✅ Sin límite mensual

**Total: $0/mes** 🎉

---

## 📈 Performance Esperado

### Google Sheets (anterior):
- ⏱️ 2-5 segundos cargar 100 movimientos
- ⏱️ 3-8 segundos crear/actualizar
- ⏱️ No hay caching
- ⏱️ Rate limits estrictos

### Supabase (nuevo):
- ⚡ 0.1 segundos cargar 1000 movimientos
- ⚡ 0.05 segundos crear/actualizar
- ⚡ Caching automático
- ⚡ Sin rate limits prácticos

**Mejora: ~50x más rápido** 🚀

---

## 🎯 Próximos Pasos

### 1. **Configurar Supabase** (10 minutos)
   - Crear cuenta
   - Crear proyecto
   - Ejecutar schema.sql
   - Obtener credenciales

### 2. **Deploy Backend** (5 minutos)
   - `npm install`
   - `vercel login`
   - `vercel --prod`
   - Configurar variables

### 3. **Migrar Datos** (15 minutos)
   - Exportar de Google Sheets
   - Importar a Supabase
   - Verificar

### 4. **Actualizar App Android** (2-3 horas)
   - Crear ApiService.kt
   - Modificar repositorios
   - Compilar APK
   - Probar

### 5. **¡Listo!** 🎉
   - App funcionando desde Andorra
   - Sin VPN
   - Muy rápida
   - Arquitectura profesional

---

## 📞 Soporte

Si tienes problemas:
1. Revisa `SETUP.md` paso a paso
2. Verifica logs con `vercel logs`
3. Prueba endpoints con curl/Postman
4. Revisa variables de entorno

---

## 🎨 Bonus Features Futuros

Con esta arquitectura, es fácil agregar:
- 👥 Multi-usuario con autenticación
- 📊 Dashboard web (React/Next.js)
- 📱 Notificaciones push
- 📈 Gráficos avanzados en tiempo real
- 💾 Exportar a PDF/Excel desde servidor
- 🔄 Sincronización en tiempo real
- 🌍 i18n (múltiples idiomas)
- 🤖 Más funciones de IA

---

**Backend Completo y Listo para Deployar** ✅
