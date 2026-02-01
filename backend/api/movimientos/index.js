import { createClient } from '@supabase/supabase-js';

export default async function handler(req, res) {
  // CORS simple
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
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
    
    // GET - Listar todos los movimientos
    if (req.method === 'GET') {
      const { data, error } = await supabase
        .from('movimientos')
        .select('*')
        .order('fecha', { ascending: false });
      
      if (error) {
        console.error('Error Supabase:', error);
        return res.status(500).json({
          error: true,
          message: error.message,
          details: error
        });
      }
      
      return res.status(200).json({
        success: true,
        data: data,
        count: data.length
      });
    }
    
    // POST - Crear nuevo movimiento
    if (req.method === 'POST') {
      const movimiento = req.body;
      
      // Validaciones
      if (!movimiento.fecha || !movimiento.tipo || !movimiento.categoria || 
          !movimiento.concepto || movimiento.importe === undefined) {
        return res.status(400).json({
          error: true,
          message: 'Faltan campos requeridos'
        });
      }
      
      const { data, error } = await supabase
        .from('movimientos')
        .insert([movimiento])
        .select()
        .single();
      
      if (error) {
        console.error('Error Supabase:', error);
        return res.status(500).json({
          error: true,
          message: error.message
        });
      }
      
      return res.status(200).json({
        success: true,
        data: data,
        message: 'Movimiento creado correctamente'
      });
    }
    
    return res.status(405).json({
      error: true,
      message: 'Método no permitido'
    });
    
  } catch (error) {
    console.error('❌ Error en movimientos:', error);
    return res.status(500).json({
      error: true,
      message: error.message,
      stack: error.stack
    });
  }
}
