import { defineStore } from "pinia";
import { ref } from "vue";
import api from "@/services/api";
import { useAuthStore } from "@/stores/auth";

export const usePermissionStore = defineStore("permissions", () => {
  const myPerms = ref(null)

  async function loadMyPermissions() {
    try {
      const res = await api.get("/permissions/me")
      myPerms.value = res.data
    } catch {
      myPerms.value = null
    }
  }

  function can(permission) {
    if (!myPerms.value) return false
    return !!myPerms.value[permission]
  }

  function isCEO() {
    // Primary check: role from auth store
    const authStore = useAuthStore()
    if (authStore.user?.role === "CEO") return true
    // Legacy fallback: all perms = true
    return myPerms.value && Object.values(myPerms.value).every(v => v === true)
  }

  // Maps user.department -> project category (their base access)
  function departmentCategory() {
    const authStore = useAuthStore()
    const dept = authStore.user?.department
    if (!dept) return null
    const map = {
      finance:   "finance",
      legal:     "legal",
      dev:       "dev",
      marketing: "marketing",
      management: null, // management is not a project category
    }
    return map[dept] ?? null
  }

  // Maps project category -> permission flag (extension grants)
  function canViewCategory(category) {
    if (isCEO()) return true
    // Base access: department matches category
    if (departmentCategory() === category) return true
    // Extension access: explicit permission flag
    const map = {
      finance:   "viewFinance",
      legal:     "viewLegal",
      dev:       "viewDev",
      marketing: "viewMarketing",
    }
    const flag = map[category]
    if (!flag) return true // unknown category: show by default
    return can(flag)
  }

  // Can user view the Security Documentation tab in the Guide?
  // CEO always yes. Others need explicit viewSecurity grant via UserPermission.
  function canViewSecurity() {
    if (isCEO()) return true
    return can("viewSecurity")
  }

  return { myPerms, loadMyPermissions, can, isCEO, canViewCategory, departmentCategory, canViewSecurity }
})
