import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const routes = [
  { path: "/login", name: "Login", component: () => import("@/views/LoginView.vue"), meta: { public: true } },
  { path: "/forgot-password", name: "ForgotPassword", component: () => import("@/views/ForgotPasswordView.vue"), meta: { public: true } },
  { path: "/reset-password", name: "ResetPassword", component: () => import("@/views/ResetPasswordView.vue"), meta: { public: true } },
  {
    path: "/",
    component: () => import("@/views/DashboardLayout.vue"),
    meta: { requiresAuth: true },
    children: [
      { path: "", name: "Dashboard", component: () => import("@/views/DashboardView.vue") },
      { path: "projects", name: "Projects", component: () => import("@/views/ProjectsView.vue") },
      { path: "projects/:id", name: "ProjectDetail", component: () => import("@/views/ProjectDetailView.vue") },
      { path: "guide", name: "Guide", component: () => import("@/views/GuideView.vue") },
      { path: "admin", name: "Admin", component: () => import("@/views/AdminView.vue"), meta: { requiresCEO: true } },
      { path: "notifications", name: "Notifications", component: () => import("@/views/NotificationsView.vue") },
    ],
  },
];

const router = createRouter({ history: createWebHistory(), routes });

router.beforeEach(async (to) => {
  const auth = useAuthStore();
  if (!auth.initialized) await auth.init();
  if (to.meta.requiresAuth && !auth.isAuthenticated) return { name: "Login" };
  if (to.path === "/login" && auth.isAuthenticated) return { path: "/" };
  if (to.meta.requiresCEO && auth.user?.role !== "CEO") return { path: "/" };
});

export default router;