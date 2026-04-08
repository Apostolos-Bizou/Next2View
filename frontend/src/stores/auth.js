import { defineStore } from "pinia";
import { ref, computed } from "vue";
import api from "@/services/api";

export const useAuthStore = defineStore("auth", () => {
  const user = ref(null);
  const loading = ref(false);
  const error = ref(null);

  const isAuthenticated = computed(() => !!user.value);
  const isCEO = computed(() => user.value?.role === "CEO");

  async function init() {
    try {
      const res = await api.get("/auth/me");
      if (res.data?.id) {
        user.value = {
          id: res.data.id,
          fullName: res.data.fullName,
          email: res.data.email,
          role: res.data.role,
          department: res.data.department,
        };
      }
    } catch {
      user.value = null;
    }
  }

  async function login(email, password, mfaCode = null) {
    loading.value = true;
    error.value = null;
    try {
      const res = await api.post("/auth/login", { email, password, mfaCode });
      if (res.data.mfaRequired) return { mfaRequired: true };
      user.value = res.data.user;
      return { success: true };
    } catch (e) {
      error.value = e.response?.data?.message || "Login failed";
      return { success: false, error: error.value };
    } finally {
      loading.value = false;
    }
  }

  async function logout() {
    try {
      await api.post("/auth/logout");
    } finally {
      user.value = null;
    }
  }

  return { user, loading, error, isAuthenticated, isCEO, init, login, logout };
});
