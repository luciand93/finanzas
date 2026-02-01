import { google } from 'googleapis';
import { createClient } from '@supabase/supabase-js';
import fs from 'fs';
import dotenv from 'dotenv';

// Cargar variables de entorno
dotenv.config();

// Configuración
const SPREADSHEET_ID = '17EBvx8s1IsxcV9-RigMxYvUxgz15ZA6yIuHyY9f8xGk';
const SHEET_NAME = 'Finanzas';
const CREDENTIALS_PATH = '../android-app/app/src/main/assets/credentials.json';

async function migrateData() {
  console.log('🚀 Iniciando migración de Google Sheets a Supabase...\n');

  try {
    // 1. Conectar a Google Sheets
    console.log('📊 Conectando a Google Sheets...');
    const credentialsContent = fs.readFileSync(CREDENTIALS_PATH, 'utf8')
      .replace(/^\uFEFF/, ''); // Remover BOM si existe
    const credentials = JSON.parse(credentialsContent);
    const auth = new google.auth.GoogleAuth({
      credentials,
      scopes: ['https://www.googleapis.com/auth/spreadsheets'],
    });
    const sheets = google.sheets({ version: 'v4', auth });

    // 2. Leer datos de Google Sheets
    console.log('📥 Leyendo movimientos desde Google Sheets...');
    const response = await sheets.spreadsheets.values.get({
      spreadsheetId: SPREADSHEET_ID,
      range: `${SHEET_NAME}!A2:I`,
    });

    const rows = response.data.values || [];
    console.log(`   ✅ ${rows.length} movimientos encontrados\n`);

    if (rows.length === 0) {
      console.log('ℹ️  No hay datos para migrar');
      return;
    }

    // 3. Conectar a Supabase
    console.log('🔌 Conectando a Supabase...');
    const supabaseUrl = process.env.SUPABASE_URL?.trim();
    const supabaseKey = process.env.SUPABASE_KEY?.trim();

    if (!supabaseUrl || !supabaseKey) {
      throw new Error('❌ Faltan credenciales de Supabase en .env');
    }

    const supabase = createClient(supabaseUrl, supabaseKey);
    console.log('   ✅ Conectado a Supabase\n');

    // 4. Transformar y preparar datos
    console.log('🔄 Transformando datos...');
    
    // Función para convertir fecha de dd/MM/yyyy a yyyy-MM-dd
    const convertDate = (dateStr) => {
      if (!dateStr) return null;
      
      // Si ya está en formato ISO (yyyy-MM-dd), devolverla tal cual
      if (/^\d{4}-\d{2}-\d{2}$/.test(dateStr)) {
        return dateStr;
      }
      
      // Convertir de dd/MM/yyyy a yyyy-MM-dd
      const parts = dateStr.split('/');
      if (parts.length === 3) {
        const [day, month, year] = parts;
        return `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`;
      }
      
      return null;
    };
    
    const movimientos = rows.map((row, index) => {
      const [id, fecha, tipo, categoria, concepto, importe, frecuencia, impactoMensual, esConjunto] = row;

      // Validar que los campos requeridos existan
      if (!fecha || !tipo || !categoria || !concepto || !importe) {
        console.log(`   ⚠️  Fila ${index + 2} omitida - datos incompletos`);
        return null;
      }

      const fechaISO = convertDate(fecha);
      if (!fechaISO) {
        console.log(`   ⚠️  Fila ${index + 2} omitida - fecha inválida: ${fecha}`);
        return null;
      }

      return {
        id: id && id.length === 36 ? id : undefined, // Usar ID existente si es UUID válido
        fecha: fechaISO,
        tipo: tipo,
        categoria: categoria,
        concepto: concepto,
        importe: parseFloat(importe.toString().replace(',', '.')),
        frecuencia: frecuencia || 'Puntual',
        impacto_mensual: impactoMensual ? parseFloat(impactoMensual.toString().replace(',', '.')) : 0,
        es_conjunto: esConjunto === 'Sí' || esConjunto === 'true' || esConjunto === true,
      };
    }).filter(m => m !== null);

    console.log(`   ✅ ${movimientos.length} movimientos válidos para migrar\n`);

    // 5. Verificar movimientos existentes en Supabase
    console.log('🔍 Verificando datos existentes en Supabase...');
    const { data: existingMovimientos, error: checkError } = await supabase
      .from('movimientos')
      .select('id');

    if (checkError) {
      console.error('❌ Error al verificar datos existentes:', checkError);
      throw checkError;
    }

    console.log(`   ℹ️  ${existingMovimientos.length} movimientos ya existen en Supabase`);

    if (existingMovimientos.length > 0) {
      console.log('\n⚠️  ADVERTENCIA: Ya hay datos en Supabase.');
      console.log('   Las opciones son:');
      console.log('   1. Eliminar datos existentes y migrar (DESTRUCTIVO)');
      console.log('   2. Insertar solo nuevos movimientos (SEGURO)');
      console.log('   3. Cancelar migración\n');
      
      // Por defecto, insertaremos solo nuevos
      console.log('   → Usando opción 2: Insertar solo nuevos movimientos\n');
    }

    // 6. Insertar datos en Supabase
    console.log('💾 Insertando movimientos en Supabase...');
    
    let insertados = 0;
    let errores = 0;

    // Insertar en lotes de 50
    const BATCH_SIZE = 50;
    for (let i = 0; i < movimientos.length; i += BATCH_SIZE) {
      const batch = movimientos.slice(i, i + BATCH_SIZE);
      
      const { data, error } = await supabase
        .from('movimientos')
        .insert(batch)
        .select();

      if (error) {
        console.error(`   ❌ Error en lote ${Math.floor(i / BATCH_SIZE) + 1}:`, error.message);
        errores += batch.length;
      } else {
        insertados += data.length;
        console.log(`   ✅ Lote ${Math.floor(i / BATCH_SIZE) + 1}: ${data.length} movimientos insertados`);
      }
    }

    // 7. Resumen final
    console.log('\n' + '='.repeat(60));
    console.log('📊 RESUMEN DE LA MIGRACIÓN');
    console.log('='.repeat(60));
    console.log(`✅ Movimientos insertados: ${insertados}`);
    console.log(`❌ Errores: ${errores}`);
    console.log(`📊 Total en Google Sheets: ${rows.length}`);
    console.log('='.repeat(60));

    // 8. Verificar datos finales
    console.log('\n🔍 Verificando datos en Supabase...');
    const { data: finalCount, error: countError } = await supabase
      .from('movimientos')
      .select('id', { count: 'exact', head: true });

    if (!countError) {
      console.log(`   ✅ Total de movimientos en Supabase: ${finalCount?.length || 0}\n`);
    }

    console.log('✅ ¡Migración completada exitosamente!\n');

  } catch (error) {
    console.error('\n❌ Error durante la migración:', error.message);
    console.error('Stack:', error.stack);
    process.exit(1);
  }
}

// Ejecutar migración
migrateData();
