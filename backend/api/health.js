export default async function handler(req, res) {
  // CORS simple
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
  
  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }
  
  res.status(200).json({
    status: 'ok',
    message: '✅ API funcionando correctamente',
    timestamp: new Date().toISOString(),
    version: '1.0.0',
    endpoints: {
      gemini: '/api/gemini/chat',
      movimientos: '/api/movimientos',
      categorias: '/api/categorias',
      stats: '/api/stats/resumen'
    }
  });
}
