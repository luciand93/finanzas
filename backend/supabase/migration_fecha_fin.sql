-- Añadir columna fecha_fin a movimientos (opcional, para recurrentes mensuales/anuales)
-- Ejecutar en Supabase: Dashboard > SQL Editor > New query > pegar y Run

ALTER TABLE movimientos
ADD COLUMN IF NOT EXISTS fecha_fin DATE DEFAULT NULL;

COMMENT ON COLUMN movimientos.fecha_fin IS 'Fecha de finalización opcional para movimientos recurrentes (mensual/anual). Si es NULL, no tiene fin.';

-- Tras ejecutar esta migración, la app podrá guardar y leer fecha_fin en movimientos mensuales/anuales.
