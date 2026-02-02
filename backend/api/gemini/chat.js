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
    // gemini-1.5-flash es más rápido y reduce timeouts en Vercel
    const model = genAI.getGenerativeModel({
      model: 'gemini-1.5-flash',
      generationConfig: {
        maxOutputTokens: 1024,
        temperature: 0.7,
      }
    });
    
    // Construir prompt con contexto (el mensaje ya puede incluir contexto desde la app)
    let fullPrompt = message;
    if (context && typeof context === 'object') {
      const contextStr = `
Contexto: Finanzas Proactivas. Fecha: ${new Date().toLocaleDateString('es-ES')}
${context.movimientos != null ? `Total movimientos: ${context.movimientos}` : ''}
${context.gastosMes != null ? `Gastos mes: ${context.gastosMes}€` : ''}
${context.ingresosMes != null ? `Ingresos mes: ${context.ingresosMes}€` : ''}

Pregunta: ${message}
`;
      fullPrompt = contextStr;
    }
    
    // Generar respuesta
    const result = await model.generateContent(fullPrompt);
    const response = await result.response;
    const text = response.text();
    
    return sendSuccess(res, {
      response: text,
      model: 'gemini-1.5-flash',
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
