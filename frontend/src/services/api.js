import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api",
  withCredentials: true,
  headers: { "Content-Type": "application/json" },
});

// ── State για queue management κατά το refresh ──
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) prom.reject(error);
    else prom.resolve(token);
  });
  failedQueue = [];
};

// ── Request interceptor: προσθέτει Bearer token ──
api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem("access_token");
  if (token) {
    config.headers["Authorization"] = `Bearer ${token}`;
  }
  return config;
});

// ── Response interceptor: auto-refresh σε 401/403 ──
api.interceptors.response.use(
  (res) => res,
  async (err) => {
    const originalRequest = err.config;

    // Αν δεν είναι 401 ή αν έχουμε ήδη κάνει retry, βγες
    if ((err.response?.status !== 401) || originalRequest._retry) {
      return Promise.reject(err);
    }

    // Μην κάνεις refresh για το ίδιο το /auth/refresh endpoint
    if (originalRequest.url?.includes("/auth/refresh")) {
      sessionStorage.removeItem("access_token");
      sessionStorage.removeItem("refresh_token");
      window.location.href = "/login";
      return Promise.reject(err);
    }

    // Αν γίνεται ήδη refresh, βάλε το request σε queue
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      })
        .then((token) => {
          originalRequest.headers["Authorization"] = `Bearer ${token}`;
          return api(originalRequest);
        })
        .catch((e) => Promise.reject(e));
    }

    originalRequest._retry = true;
    isRefreshing = true;

    const refreshToken = sessionStorage.getItem("refresh_token");
    if (!refreshToken) {
      isRefreshing = false;
      sessionStorage.removeItem("access_token");
      window.location.href = "/login";
      return Promise.reject(err);
    }

    try {
      // Κάνε refresh με axios κατευθείαν για να αποφύγουμε recursive interception
      const baseURL = import.meta.env.VITE_API_BASE_URL || "/api";
      const { data } = await axios.post(
        `${baseURL}/auth/refresh`,
        { refreshToken },
        { headers: { "Content-Type": "application/json" } }
      );

      const newAccessToken = data.accessToken;
      const newRefreshToken = data.refreshToken;

      sessionStorage.setItem("access_token", newAccessToken);
      if (newRefreshToken) {
        sessionStorage.setItem("refresh_token", newRefreshToken);
      }

      // Unblock queued requests
      processQueue(null, newAccessToken);

      // Retry το αρχικό request με νέο token
      originalRequest.headers["Authorization"] = `Bearer ${newAccessToken}`;
      return api(originalRequest);
    } catch (refreshError) {
      processQueue(refreshError, null);
      sessionStorage.removeItem("access_token");
      sessionStorage.removeItem("refresh_token");
      window.location.href = "/login";
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  }
);

export default api;
