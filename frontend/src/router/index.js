import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const routes = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/LoginView.vue"),
    meta: { public: true },
  },
  {
    path: "/",
    component: () => import("@/views/DashboardLayout.vue"),
    meta: { requiresAuth: true },
    children: [
      {
        path: "",
        name: "Dashboard",
        component: () => import("@/views/DashboardView.vue"),
      },
      {
        path: "projects",
        name: "Projects",
        component: () => import("@/views/ProjectsView.vue"),
      },
      {
        path: "projects/:id",
        name: "ProjectDetail",
        component: () => import("@/views/ProjectDetailView.vue"),
      },
      {
        path: "guide",
        name: "Guide",
        component: () => import("@/views/GuideView.vue"),
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to) => {
  const auth = useAuthStore();

  // Περιμένουμε το init να ολοκληρωθεί πρώτα
  if (!auth.initialized) {
    await auth.init();
  }

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: "Login" };
  }
  if (to.path === "/login" && auth.isAuthenticated) {
    return { path: "/" };
  }
});

export default router;