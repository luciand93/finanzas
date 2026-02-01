import { getSupabaseClient, handleCors, sendError, sendSuccess } from '../_lib/supabase.js';

export default async function handler(req, res) {
  // Handle CORS
  if (handleCors(req, res)) return;
  
  const supabase = getSupabaseClient();
  
  try {
    // Parámetro de período: 'mes' o 'general' (default: mes)
    const { periodo = 'mes' } = req.query;
    
    // Obtener todos los movimientos
    const { data: movimientos, error } = await supabase
      .from('movimientos')
      .select('tipo, importe, fecha, categoria, frecuencia, impacto_mensual');
    
    if (error) throw error;
    
    // Filtrar según período
    const ahora = new Date();
    const mesActual = ahora.getMonth();
    const añoActual = ahora.getFullYear();
    
    let movimientosFiltrados = movimientos;
    if (periodo === 'mes') {
      movimientosFiltrados = movimientos.filter(m => {
        const fecha = new Date(m.fecha);
        return fecha.getMonth() === mesActual && fecha.getFullYear() === añoActual;
      });
    }
    
    // Calcular estadísticas básicas
    const ingresos = movimientosFiltrados
      .filter(m => m.tipo === 'Ingreso')
      .reduce((sum, m) => sum + parseFloat(m.importe), 0);
    
    const gastos = movimientosFiltrados
      .filter(m => m.tipo === 'Gasto')
      .reduce((sum, m) => sum + parseFloat(m.importe), 0);
    
    const balance = ingresos - gastos;
    
    // Tasa de ahorro
    const tasaAhorro = ingresos > 0 ? (balance / ingresos * 100) : 0;
    
    // Gastos por categoría
    const gastosPorCategoria = {};
    movimientosFiltrados
      .filter(m => m.tipo === 'Gasto')
      .forEach(m => {
        if (!gastosPorCategoria[m.categoria]) {
          gastosPorCategoria[m.categoria] = 0;
        }
        gastosPorCategoria[m.categoria] += parseFloat(m.importe);
      });
    
    // Top 5 categorías
    const topCategorias = Object.entries(gastosPorCategoria)
      .map(([nombre, total]) => ({ nombre, total }))
      .sort((a, b) => b.total - a.total)
      .slice(0, 5);
    
    // Ingresos por categoría
    const ingresosPorCategoria = {};
    movimientosFiltrados
      .filter(m => m.tipo === 'Ingreso')
      .forEach(m => {
        if (!ingresosPorCategoria[m.categoria]) {
          ingresosPorCategoria[m.categoria] = 0;
        }
        ingresosPorCategoria[m.categoria] += parseFloat(m.importe);
      });
    
    // Gastos fijos vs variables (basado en frecuencia)
    const gastosFijos = movimientosFiltrados
      .filter(m => m.tipo === 'Gasto' && (m.frecuencia === 'Mensual' || m.frecuencia === 'Anual'))
      .reduce((sum, m) => {
        if (m.frecuencia === 'Anual') {
          return sum + (parseFloat(m.importe) / 12);
        }
        return sum + parseFloat(m.importe);
      }, 0);
    
    const gastosVariables = gastos - gastosFijos;
    
    // Promedio diario de gastos (solo para mes actual)
    const diasDelMes = periodo === 'mes' ? ahora.getDate() : 30;
    const promedioDiario = gastos / diasDelMes;
    
    // Proyección fin de mes (solo para mes actual)
    const diasTotalesMes = new Date(añoActual, mesActual + 1, 0).getDate();
    const proyeccionFinMes = periodo === 'mes' ? (promedioDiario * diasTotalesMes) : gastos;
    
    // Impacto mensual de gastos anuales
    const impactoGastosAnuales = movimientos
      .filter(m => m.tipo === 'Gasto' && m.frecuencia === 'Anual')
      .reduce((sum, m) => sum + (parseFloat(m.importe) / 12), 0);
    
    // Salud financiera (escala 0-100)
    let saludFinanciera = 50; // Base
    if (tasaAhorro > 20) saludFinanciera += 30;
    else if (tasaAhorro > 10) saludFinanciera += 20;
    else if (tasaAhorro > 0) saludFinanciera += 10;
    else saludFinanciera -= 20;
    
    // Bonus si gastos fijos < 50% de ingresos
    if (gastosFijos / ingresos < 0.5) saludFinanciera += 10;
    
    // Penalización si gastos > ingresos
    if (balance < 0) saludFinanciera -= 20;
    
    saludFinanciera = Math.max(0, Math.min(100, saludFinanciera));
    
    // Tendencia (comparar con mes anterior)
    const mesAnterior = new Date(añoActual, mesActual - 1);
    const movimientosMesAnterior = movimientos.filter(m => {
      const fecha = new Date(m.fecha);
      return fecha.getMonth() === mesAnterior.getMonth() && 
             fecha.getFullYear() === mesAnterior.getFullYear();
    });
    
    const gastosMesAnterior = movimientosMesAnterior
      .filter(m => m.tipo === 'Gasto')
      .reduce((sum, m) => sum + parseFloat(m.importe), 0);
    
    const tendenciaGastos = gastosMesAnterior > 0 
      ? ((gastos - gastosMesAnterior) / gastosMesAnterior * 100) 
      : 0;
    
    // Días hasta fin de mes
    const diasRestantes = periodo === 'mes' ? (diasTotalesMes - diasDelMes) : 0;
    
    const estadisticas = {
      periodo: periodo,
      fecha: ahora.toLocaleDateString('es-ES', { month: 'long', year: 'numeric' }),
      basico: {
        movimientos: movimientosFiltrados.length,
        ingresos: Math.round(ingresos * 100) / 100,
        gastos: Math.round(gastos * 100) / 100,
        balance: Math.round(balance * 100) / 100
      },
      indicadores: {
        tasaAhorro: Math.round(tasaAhorro * 100) / 100,
        saludFinanciera: Math.round(saludFinanciera),
        promedioDiario: Math.round(promedioDiario * 100) / 100,
        proyeccionFinMes: Math.round(proyeccionFinMes * 100) / 100,
        diasRestantes: diasRestantes,
        impactoGastosAnuales: Math.round(impactoGastosAnuales * 100) / 100
      },
      distribucion: {
        gastosFijos: Math.round(gastosFijos * 100) / 100,
        gastosVariables: Math.round(gastosVariables * 100) / 100,
        porcentajeGastosFijos: ingresos > 0 ? Math.round((gastosFijos / ingresos * 100) * 100) / 100 : 0,
        porcentajeGastosVariables: ingresos > 0 ? Math.round((gastosVariables / ingresos * 100) * 100) / 100 : 0
      },
      categorias: {
        topGastos: topCategorias.map(c => ({
          nombre: c.nombre,
          total: Math.round(c.total * 100) / 100,
          porcentaje: gastos > 0 ? Math.round((c.total / gastos * 100) * 100) / 100 : 0
        })),
        totalCategorias: Object.keys(gastosPorCategoria).length,
        ingresosPorCategoria: Object.entries(ingresosPorCategoria).map(([nombre, total]) => ({
          nombre,
          total: Math.round(total * 100) / 100
        }))
      },
      tendencias: {
        gastosVsMesAnterior: Math.round(tendenciaGastos * 100) / 100,
        direccion: tendenciaGastos > 0 ? 'aumento' : tendenciaGastos < 0 ? 'reduccion' : 'estable'
      }
    };
    
    return sendSuccess(res, estadisticas);
    
  } catch (error) {
    console.error('❌ Error en estadísticas detalladas:', error);
    return sendError(res, 500, `Error: ${error.message}`);
  }
}
