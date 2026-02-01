import { createClient } from '@supabase/supabase-js';
import dotenv from 'dotenv';

dotenv.config();

async function verifyData() {
  console.log('🔍 Verificando datos en Supabase...\n');

  const supabaseUrl = process.env.SUPABASE_URL?.trim();
  const supabaseKey = process.env.SUPABASE_KEY?.trim();

  if (!supabaseUrl || !supabaseKey) {
    console.error('❌ Faltan credenciales de Supabase');
    process.exit(1);
  }

  const supabase = createClient(supabaseUrl, supabaseKey);

  // Verificar movimientos
  const { data: movimientos, error } = await supabase
    .from('movimientos')
    .select('*')
    .order('fecha', { ascending: false });

  if (error) {
    console.error('❌ Error al consultar movimientos:', error);
    process.exit(1);
  }

  console.log(`✅ Total de movimientos en Supabase: ${movimientos.length}\n`);

  if (movimientos.length > 0) {
    console.log('📋 Primeros 5 movimientos:\n');
    movimientos.slice(0, 5).forEach((m, i) => {
      console.log(`${i + 1}. ${m.fecha} | ${m.tipo} | ${m.categoria} | ${m.concepto} | $${m.importe}`);
    });
  }

  console.log('\n✅ Verificación completada');
}

verifyData();
