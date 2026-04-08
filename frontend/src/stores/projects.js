import { defineStore } from "pinia";
import { ref, computed } from "vue";
import api from "@/services/api";

export const useProjectStore = defineStore("projects", () => {
  const projects = ref([]);
  const companies = ref([]);
  const loading = ref(false);
  const selectedProject = ref(null);

  const byCategory = computed(() => (cat) =>
    projects.value.filter((p) => p.category === cat)
  );

  const overallCompletion = computed(() => {
    if (!projects.value.length) return 0;
    return Math.round(
      projects.value.reduce((s, p) => s + p.completion, 0) / projects.value.length
    );
  });

  const atRisk = computed(() =>
    projects.value.filter((p) => p.status === "at_risk" || p.status === "stale")
  );

  async function fetchProjects(companyId = null, category = null) {
    loading.value = true;
    try {
      const params = {};
      if (companyId) params.companyId = companyId;
      if (category) params.category = category;
      const res = await api.get("/projects", { params });
      projects.value = res.data;
    } finally {
      loading.value = false;
    }
  }

  async function fetchCompanies() {
    const res = await api.get("/companies");
    companies.value = res.data;
  }

  async function fetchProject(id) {
    const res = await api.get(`/projects/${id}`);
    selectedProject.value = res.data;
    return res.data;
  }

  async function createProject(data) {
    const res = await api.post("/projects", data);
    projects.value.unshift(res.data);
    return res.data;
  }

  async function updateProject(id, data) {
    const res = await api.put(`/projects/${id}`, data);
    const idx = projects.value.findIndex((p) => p.id === id);
    if (idx >= 0) projects.value[idx] = res.data;
    selectedProject.value = res.data;
    return res.data;
  }

  async function deleteProject(id) {
    await api.delete(`/projects/${id}`);
    projects.value = projects.value.filter((p) => p.id !== id);
  }

  return {
    projects, companies, loading, selectedProject,
    byCategory, overallCompletion, atRisk,
    fetchProjects, fetchCompanies, fetchProject,
    createProject, updateProject, deleteProject,
  };
});