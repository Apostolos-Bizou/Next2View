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

  return { myPerms, loadMyPermissions, can, isCEO }
})