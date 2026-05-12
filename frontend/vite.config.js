import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import { resolve } from "path";
const AZURE_BACKEND = "https://next2view-api.politefield-8c14abcc.northeurope.azurecontainerapps.io";
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      "@": resolve(__dirname, "src"),
    },
  },
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: AZURE_BACKEND,
        changeOrigin: true,
        secure: true,
        // rewrite removed: backend already serves under /api (per commit 8689c22)
      },
    },
  },
  preview: {
    port: 4173,
    proxy: {
      "/api": {
        target: AZURE_BACKEND,
        changeOrigin: true,
        secure: true,
      },
    },
  },
  build: {
    outDir: "dist",
    sourcemap: false,
  },
});
// emergency rollback - 2026-04-28 13:27:13
// rollback trigger 2
