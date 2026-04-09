import { defineStore } from "pinia";
import { ref } from "vue";
import api from "@/services/api";

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
    return myPerms.value && Object.values(myPerms.value).every(v => v === true)
  }

  // Maps project category -> permission flag
  function canViewCategory(category) {
    if (isCEO()) return true
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

  return { myPerms, loadMyPermissions, can, isCEO, canViewCategory }
})