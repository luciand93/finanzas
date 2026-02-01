import { GoogleGenerativeAI } from '@google/generative-ai';
import { handleCors, sendError, sendSuccess } from '../_lib/supabase.js';

export default async function handler(req, res) {
  // Handle CORS
  if (handleCors(req, res)) return;
  
  // Solo POST
  if (req.method !== 'POST') {
    return sendError(res, 405, 'Método no permitido. Usa POST');
  }
  
  try {
    const { message, context } = req.body;
    
    if (!message || typeof message !== 'string' || message.trim() === '') {
      return sendError(res, 400, 'El campo "message" es requerido');
    }
    
    // Inicializar Gemini
    const apiKey = process.env.GEMINI_API_KEY;
    if (!apiKey) {
      return sendError(res, 500, 'API Key de Gemini no configurada');
    }
    
    const genAI = new GoogleGenerativeAI(apiKey);
    const model = genAI.getGenerativeModel({ model: 'gemini-pro' });
    
    // Construir prompt con contexto
    let fullPrompt = message;
    if (context && typeof context === 'object') {
      const contextStr = `
Contexto de la conversación:
- Usuario: Finanzas Proactivas
- Fecha actual: ${new Date().toLocaleDateString('es-ES')}
${context.movimientos ? `- Total movimientos: ${context.movimientos}` : ''}
${context.gastosMes ? `- Gastos este mes: ${context.gastosMes}€` : ''}
${context.ingresosMes ? `- Ingresos este mes: ${context.ingresosMes}€` : ''}

Pregunta del usuario: ${message}
`;
      fullPrompt = contextStr;
    }
    
    // Generar respuesta
    const result = await model.generateContent(fullPrompt);
    const response = await result.response;
    const text = response.text();
    
    return sendSuccess(res, {
      response: text,
      model: 'gemini-pro',
      timestamp: new Date().toISOString()
    });
    
  } catch (error) {
    console.error('❌ Error en Gemini:', error);
    
    // Errores específicos de Gemini
    if (error.message?.includes('API_KEY_INVALID')) {
      return sendError(res, 500, '❌ API Key de Gemini inválida');
    }
    
    if (error.message?.includes('REGION_NOT_AVAILABLE')) {
      return sendError(res, 503, '❌ Gemini no disponible en esta región. El servidor debe estar en una región compatible.');
    }
    
    if (error.message?.includes('QUOTA_EXCEEDED')) {
      return sendError(res, 429, '❌ Cuota de Gemini excedida. Intenta más tarde.');
    }
    
    return sendError(res, 500, `❌ Error al generar respuesta: ${error.message}`);
  }
}
