import { createClient } from '@supabase/supabase-js';

export default async function handler(req, res) {
  // CORS simple
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
  
  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }
  
  try {
    const supabaseUrl = process.env.SUPABASE_URL?.trim();
    const supabaseKey = process.env.SUPABASE_KEY?.trim();
    
    if (!supabaseUrl || !supabaseKey) {
      return res.status(500).json({
        error: true,
        message: 'Faltan credenciales de Supabase'
      });
    }
    
    const supabase = createClient(supabaseUrl, supabaseKey);
    
    // Obtener ID del movimiento desde la URL
    const { id } = req.query;
    
    if (!id) {
      return res.status(400).json({
        error: true,
        message: 'ID de movimiento requerido'
      });
    }
    
    // GET - Obtener un movimiento específico
    if (req.method === 'GET') {
      const { data, error } = await supabase
        .from('movimientos')
        .select('*')
        .eq('id', id)
        .single();
      
      if (error) {
        if (error.code === 'PGRST116') {
          return res.status(404).json({
            error: true,
            message: 'Movimiento no encontrado'
          });
        }
        throw error;
      }
      
      return res.status(200).json({
        success: true,
        data: data
      });
    }
    
    // PUT - Actualizar un movimiento
    if (req.method === 'PUT') {
      const movimiento = req.body;
      
      // Validaciones
      if (!movimiento.fecha || !movimiento.tipo || !movimiento.categoria || 
          !movimiento.concepto || movimiento.importe === undefined) {
        return res.status(400).json({
          error: true,
          message: 'Faltan campos requeridos'
        });
      }
      
      // Preparar datos (sin incluir id en el update)
      const { id: _, ...datosActualizar } = movimiento;
      
      const { data, error } = await supabase
        .from('movimientos')
        .update(datosActualizar)
        .eq('id', id)
        .select()
        .single();
      
      if (error) {
        if (error.message?.includes('fecha_fin') || error.message?.includes('column "fecha_fin"')) {
          return res.status(400).json({
            error: true,
            message: 'La base de datos no tiene la columna fecha_fin. Ejecuta la migración migration_fecha_fin.sql en Supabase.',
            details: error
          });
        }
        console.error('Error Supabase:', error);
        return res.status(500).json({
          error: true,
          message: error.message
        });
      }
      
      return res.status(200).json({
        success: true,
        data: data,
        message: 'Movimiento actualizado correctamente'
      });
    }
    
    // DELETE - Eliminar un movimiento
    if (req.method === 'DELETE') {
      const { error } = await supabase
        .from('movimientos')
        .delete()
        .eq('id', id);
      
      if (error) {
        console.error('Error Supabase:', error);
        return res.status(500).json({
          error: true,
          message: error.message
        });
      }
      
      return res.status(200).json({
        success: true,
        message: 'Movimiento eliminado correctamente'
      });
    }
    
    return res.status(405).json({
      error: true,
      message: 'Método no permitido'
    });
    
  } catch (error) {
    console.error('❌ Error en movimientos/[id]:', error);
    return res.status(500).json({
      error: true,
      message: error.message,
      stack: error.stack
    });
  }
}
