import path from "node:path"
import { defineConfig } from "vite"
import react from "@vitejs/plugin-react"
import tailwindcss from "@tailwindcss/vite"

export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      "@": path.resolve(import.meta.dirname, "./src"),
    },
  },
  server: {
    port: 5173,
    // Em desenvolvimento o Vite encaminha /api para o Spring Boot,
    // evitando problemas de CORS e mantendo o cookie de refresh no mesmo host.
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
  build: {
    // O build do React e servido pelo proprio Spring Boot em producao.
    outDir: "../backend/src/main/resources/static",
    emptyOutDir: true,
  },
})
