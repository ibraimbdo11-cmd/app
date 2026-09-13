import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
    {
      name: 'api-solver-middleware',
      configureServer(server) {
        server.middlewares.use(async (req, res, next) => {
          if (req.url === '/api/health' && req.method === 'GET') {
            res.setHeader('Content-Type', 'application/json');
            res.end(JSON.stringify({ status: 'ok', service: 'js-agent-api' }));
            return;
          }

          if (req.url === '/api/solve' && req.method === 'POST') {
            let body = '';
            req.on('data', chunk => {
              body += chunk;
            });

            req.on('end', async () => {
              try {
                const data = JSON.parse(body || '{}');
                const { prompt, functionName, language = 'javascript' } = data;
                const apiKey = process.env.GEMINI_API_KEY;

                if (!apiKey) {
                  res.setHeader('Content-Type', 'application/json');
                  res.end(
                    JSON.stringify({
                      success: false,
                      error: 'GEMINI_API_KEY not configured',
                      fallbackToLocal: true,
                    })
                  );
                  return;
                }

                // Dynamic import to keep lazy
                const { GoogleGenAI } = await import('@google/genai');
                const ai = new GoogleGenAI({ apiKey });

                const systemInstruction = `You are an expert JavaScript coding agent.
Write only clean, pure, modern JavaScript code satisfying the user's question.
Return ONLY valid executable JavaScript code enclosed in \`\`\`javascript ... \`\`\` or raw code.
No explanations, no markdown intro, no tests, no other language syntax (strictly no Python, Java, etc.).`;

                const response = await ai.models.generateContent({
                  model: 'gemini-2.5-flash',
                  contents: `${systemInstruction}\n\nTask:\n${prompt}\nTarget function name: ${functionName || 'auto'}`,
                });

                let generatedText = response.text || '';
                // Clean markdown fences
                generatedText = generatedText.replace(/```javascript[\s\S]*?```/g, m => m.slice(13, -3).trim());
                generatedText = generatedText.replace(/```js[\s\S]*?```/g, m => m.slice(5, -3).trim());
                generatedText = generatedText.replace(/```[\s\S]*?```/g, m => m.slice(3, -3).trim()).trim();

                res.setHeader('Content-Type', 'application/json');
                res.end(
                  JSON.stringify({
                    success: true,
                    code: generatedText,
                    source: 'gemini-2.5-flash',
                  })
                );
              } catch (err: any) {
                res.setHeader('Content-Type', 'application/json');
                res.end(
                  JSON.stringify({
                    success: false,
                    error: err.message || 'Gemini solver error',
                    fallbackToLocal: true,
                  })
                );
              }
            });
            return;
          }

          next();
        });
      },
    },
  ],
  server: {
    host: '0.0.0.0',
    port: 3000,
  },
});
