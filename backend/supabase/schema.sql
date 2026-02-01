-- ========================================
-- SCHEMA FINANZAS PROACTIVAS - SUPABASE
-- ========================================

-- Extensiones necesarias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ========================================
-- TABLA: categorias
-- ========================================
CREATE TABLE IF NOT EXISTS categorias (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  nombre VARCHAR(100) NOT NULL UNIQUE,
  icono VARCHAR(50),
  color VARCHAR(7) DEFAULT '#6366f1',
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_categorias_nombre ON categorias(nombre);

-- ========================================
-- TABLA: movimientos
-- ========================================
CREATE TABLE IF NOT EXISTS movimientos (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  fecha DATE NOT NULL DEFAULT CURRENT_DATE,
  tipo VARCHAR(10) NOT NULL CHECK (tipo IN ('Ingreso', 'Gasto')),
  categoria VARCHAR(100) NOT NULL,
  concepto TEXT NOT NULL,
  importe DECIMAL(12,2) NOT NULL CHECK (importe >= 0),
  frecuencia VARCHAR(10) NOT NULL DEFAULT 'Puntual' CHECK (frecuencia IN ('Puntual', 'Mensual', 'Anual')),
  impacto_mensual DECIMAL(12,2) DEFAULT 0,
  es_conjunto BOOLEAN DEFAULT false,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Índices para búsquedas rápidas
CREATE INDEX IF NOT EXISTS idx_movimientos_fecha ON movimientos(fecha DESC);
CREATE INDEX IF NOT EXISTS idx_movimientos_tipo ON movimientos(tipo);
CREATE INDEX IF NOT EXISTS idx_movimientos_categoria ON movimientos(categoria);
CREATE INDEX IF NOT EXISTS idx_movimientos_fecha_tipo ON movimientos(fecha DESC, tipo);

-- ========================================
-- FUNCIÓN: Actualizar updated_at automáticamente
-- ========================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers para actualizar updated_at
CREATE TRIGGER update_categorias_updated_at BEFORE UPDATE ON categorias
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_movimientos_updated_at BEFORE UPDATE ON movimientos
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ========================================
-- DATOS INICIALES: Categorías por defecto
-- ========================================
INSERT INTO categorias (nombre, icono, color) VALUES
  ('Supermercado', '🛒', '#10b981'),
  ('Transporte', '🚗', '#3b82f6'),
  ('Restaurantes', '🍽️', '#f59e0b'),
  ('Salud', '⚕️', '#ef4444'),
  ('Ocio', '🎮', '#8b5cf6'),
  ('Vivienda', '🏠', '#06b6d4'),
  ('Educación', '📚', '#ec4899'),
  ('Ropa', '👕', '#14b8a6'),
  ('Tecnología', '💻', '#6366f1'),
  ('Servicios', '🔧', '#f97316'),
  ('Suscripciones', '📱', '#a855f7'),
  ('Regalos', '🎁', '#f43f5e'),
  ('Viajes', '✈️', '#0ea5e9'),
  ('Mascotas', '🐕', '#84cc16'),
  ('Gimnasio', '💪', '#eab308'),
  ('Seguros', '🛡️', '#64748b'),
  ('Impuestos', '💰', '#dc2626'),
  ('Inversiones', '📈', '#059669'),
  ('Ahorros', '🏦', '#0284c7'),
  ('Otros', '📦', '#94a3b8')
ON CONFLICT (nombre) DO NOTHING;

-- ========================================
-- VISTAS: Para consultas frecuentes
-- ========================================

-- Vista: Resumen mensual
CREATE OR REPLACE VIEW resumen_mensual AS
SELECT 
  DATE_TRUNC('month', fecha) as mes,
  tipo,
  COUNT(*) as total_movimientos,
  SUM(importe) as total_importe,
  AVG(importe) as promedio_importe
FROM movimientos
GROUP BY DATE_TRUNC('month', fecha), tipo
ORDER BY mes DESC, tipo;

-- Vista: Gastos por categoría (mes actual)
CREATE OR REPLACE VIEW gastos_por_categoria_mes_actual AS
SELECT 
  categoria,
  COUNT(*) as cantidad,
  SUM(importe) as total,
  AVG(importe) as promedio
FROM movimientos
WHERE tipo = 'Gasto'
  AND DATE_TRUNC('month', fecha) = DATE_TRUNC('month', CURRENT_DATE)
GROUP BY categoria
ORDER BY total DESC;

-- ========================================
-- POLÍTICAS DE SEGURIDAD (RLS)
-- ========================================
-- Por ahora deshabilitadas para simplicidad
-- En producción con multi-usuario, activar RLS

ALTER TABLE movimientos ENABLE ROW LEVEL SECURITY;
ALTER TABLE categorias ENABLE ROW LEVEL SECURITY;

-- Política: Permitir todo por ahora (single-user)
CREATE POLICY "Permitir todo en movimientos" ON movimientos
  FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Permitir todo en categorias" ON categorias
  FOR ALL USING (true) WITH CHECK (true);

-- ========================================
-- COMENTARIOS
-- ========================================
COMMENT ON TABLE movimientos IS 'Tabla principal de movimientos financieros';
COMMENT ON TABLE categorias IS 'Catálogo de categorías para clasificar movimientos';
COMMENT ON COLUMN movimientos.impacto_mensual IS 'Impacto mensual calculado según frecuencia';
COMMENT ON COLUMN movimientos.es_conjunto IS 'Indica si el gasto es compartido (se divide entre 2)';
