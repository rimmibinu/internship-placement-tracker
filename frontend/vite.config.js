import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// In dev, /api calls are proxied to the Spring Boot backend (no CORS issues).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: { '/api': { target: process.env.VITE_BACKEND || 'http://localhost:8080', changeOrigin: true } },
  },
});
