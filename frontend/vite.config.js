import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

/*
 * Dev server: http://localhost:5173/adp-assignment/
 * API calls (/adp-assignment/customers, /adp-assignment/products, …) are proxied to Spring Boot
 * on port 8080, so the frontend and backend can be started in ANY order – if the backend
 * isn't up yet the app shows "waiting for server" and reconnects by itself.
 *
 * `npm run build` writes the finished app into the Spring Boot project
 * (src/main/resources/static) so running the backend from IntelliJ also serves the UI at
 * http://localhost:8080/adp-assignment/
 */
const BACKEND = process.env.BACKEND_URL || 'http://localhost:8080';
// Every backend controller must be listed here, otherwise `npm run dev` answers that path with
// index.html instead of the JSON from Spring Boot and the dashboard crashes after login.
const API_PATHS = ['health', 'auth', 'customers', 'employees', 'products', 'suppliers', 'orders', 'order-items',
  'leave', 'attendance', 'notifications', 'categories', 'stock-alerts', 'payments'];

const proxy = Object.fromEntries(API_PATHS.map((p) => [`/adp-assignment/${p}`, {
  target: BACKEND,
  changeOrigin: true,
  configure: (p) => {
    // Backend not running → answer with a clear JSON error instead of a long proxy stack trace
    p.on('error', (_err, _req, res) => {
      if (res && !res.headersSent && typeof res.writeHead === 'function') {
        res.writeHead(503, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ message: 'The backend is not running yet.', offline: true }));
      }
    });
  }
}]));

export default defineConfig({
  plugins: [react()],
  base: '/adp-assignment/',
  server: {
    host: '0.0.0.0',
    port: 5173,
    open: '/adp-assignment/',
    proxy
  },
  preview: {
    host: '0.0.0.0',
    port: 4173,
    proxy
  },
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true
  }
});
