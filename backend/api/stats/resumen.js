import { getSupabaseClient, handleCors, sendError, sendSuccess } from '../_lib/supabase.js';

export default async function handler(req, res) {
  // Handle CORS
  if (handleCors(req, res)) return;
  
  const supabase = getSupabaseClient();
  
  try {
    // Obtener resumen general
    const { data: movimientos, error } = await supabase
      .from('movimientos')
      .select('tipo, importe, fecha');
    
    if (error) throw error;
    
    // Calcular estadísticas
    const ahora = new Date();
    const mesActual = ahora.getMonth();
    const añoActual = ahora.getFullYear();
    
    const movimientosMesActual = movimientos.filter(m => {
      const fecha = new Date(m.fecha);
      return fecha.getMonth() === mesActual && fecha.getFullYear() === añoActual;
    });
    
    const ingresos = movimientos
      .filter(m => m.tipo === 'Ingreso')
      .reduce((sum, m) => sum + parseFloat(m.importe), 0);
    
    const gastos = movimientos
      .filter(m => m.tipo === 'Gasto')
      .reduce((sum, m) => sum + parseFloat(m.importe), 0);
    
    const ingresosMes = movimientosMesActual
      .filter(m => m.tipo === 'Ingreso')
      .reduce((sum, m) => sum + parseFloat(m.importe), 0);
    
    const gastosMes = movimientosMesActual
      .filter(m => m.tipo === 'Gasto')
      .reduce((sum, m) => sum + parseFloat(m.importe), 0);
    
    const resumen = {
      total: {
        movimientos: movimientos.length,
        ingresos: Math.round(ingresos * 100) / 100,
        gastos: Math.round(gastos * 100) / 100,
        balance: Math.round((ingresos - gastos) * 100) / 100
      },
      mesActual: {
        movimientos: movimientosMesActual.length,
        ingresos: Math.round(ingresosMes * 100) / 100,
        gastos: Math.round(gastosMes * 100) / 100,
        balance: Math.round((ingresosMes - gastosMes) * 100) / 100,
        mes: ahora.toLocaleDateString('es-ES', { month: 'long', year: 'numeric' })
      }
    };
    
    return sendSuccess(res, resumen);
    
  } catch (error) {
    console.error('❌ Error en resumen:', error);
    return sendError(res, 500, `Error: ${error.message}`);
  }
}
