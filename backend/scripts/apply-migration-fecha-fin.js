import 'dotenv/config';
import pg from 'pg';

const { Client } = pg;

const dbUrl = process.env.SUPABASE_DB_URL || process.env.DATABASE_URL;

if (!dbUrl) {
  console.error('❌ Falta SUPABASE_DB_URL (o DATABASE_URL) en el entorno.');
  console.error('Añádelo en tu .env y vuelve a ejecutar el script.');
  process.exit(1);
}

const sql = `
ALTER TABLE movimientos
ADD COLUMN IF NOT EXISTS fecha_fin DATE DEFAULT NULL;

COMMENT ON COLUMN movimientos.fecha_fin IS 'Fecha de finalización opcional para movimientos recurrentes (mensual/anual). Si es NULL, no tiene fin.';
`;

async function run() {
  const client = new Client({ connectionString: dbUrl });
  try {
    await client.connect();
    await client.query(sql);
    console.log('✅ Migración aplicada: fecha_fin añadida a movimientos.');
  } catch (err) {
    console.error('❌ Error aplicando migración:', err.message || err);
    process.exitCode = 1;
  } finally {
    await client.end();
  }
}

run();
