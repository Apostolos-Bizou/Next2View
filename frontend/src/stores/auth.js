import { defineStore } from "pinia";
import { ref, computed } from "vue";
import api from "@/services/api";

export const useAuthStore = defineStore("auth", () => {
  const user = ref(null);
  const loading = ref(false);
  const error = ref(null);
  const initialized = ref(false);

  const isAuthenticated = computed(() => !!user.value);
  const isCEO = computed(() => user.value?.role === "CEO");

  async function init() {
    // Αν έχει ήδη γίνει init, μην το ξανακάνεις
    if (initialized.value) return;
    initialized.value = true;
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
      // 401 = not logged in, αυτό είναι φυσιολογικό
      user.value = null;
    }
  }

  async function login(email, password, mfaCode = null) {
    loading.value = true;
    error.value = null;
    try {
      const res = await api.post("/auth/login", { email, password, mfaCode });
      if (res.data.mfaRequired) return { mfaRequired: true };
      // Αποθηκεύουμε τον user από το login response
      user.value = res.data.user;
      initialized.value = true;
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
      initialized.value = false;
    }
  }

  return { user, loading, error, initialized, isAuthenticated, isCEO, init, login, logout };
});