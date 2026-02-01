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
    
    // GET - Listar todas las categorías
    if (req.method === 'GET') {
      const { data, error } = await supabase
        .from('categorias')
        .select('*')
        .order('nombre', { ascending: true });
      
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
    
    // POST - Crear nueva categoría
    if (req.method === 'POST') {
      const categoria = req.body;
      
      if (!categoria.nombre) {
        return res.status(400).json({
          error: true,
          message: 'El nombre es requerido'
        });
      }
      
      const { data, error } = await supabase
        .from('categorias')
        .insert([categoria])
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
        message: 'Categoría creada correctamente'
      });
    }
    
    return res.status(405).json({
      error: true,
      message: 'Método no permitido'
    });
    
  } catch (error) {
    console.error('❌ Error en categorías:', error);
    return res.status(500).json({
      error: true,
      message: error.message,
      stack: error.stack
    });
  }
}
