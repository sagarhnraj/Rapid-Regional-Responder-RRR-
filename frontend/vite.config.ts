import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import fs from 'fs';

export default defineConfig({
  plugins: [react()],
  optimizeDeps: {
    exclude: ['lucide-react'],
  },
  server: {
    host: '172.20.10.2',
    https: {
      key: fs.readFileSync('./172.20.10.2-key.pem'),
      cert: fs.readFileSync('./172.20.10.2.pem')
    }
  }
});