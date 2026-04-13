import { defineStore } from 'pinia'
import { getMenus, getProfile, login as loginApi, logout as logoutApi } from '../api/auth'
import { localizeMenus, localizeProfile } from '../utils/locale'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('house_admin_token') || '',
    profile: null,
    menus: [],
    permissions: []
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    hasPermission: (state) => (code) => state.permissions.includes(code)
  },
  actions: {
    async login(form) {
      const result = await loginApi(form)
      this.token = result.token
      this.profile = localizeProfile(result.user)
      this.permissions = result.user.permissions || []
      localStorage.setItem('house_admin_token', result.token)
    },
    async bootstrap() {
      if (!this.token) return
      const [profile, menus] = await Promise.all([getProfile(), getMenus()])
      this.profile = localizeProfile(profile)
      this.permissions = profile.permissions || []
      this.menus = localizeMenus(menus)
    },
    async logout() {
      if (this.token) {
        await logoutApi().catch(() => {})
      }
      this.token = ''
      this.profile = null
      this.menus = []
      this.permissions = []
      localStorage.removeItem('house_admin_token')
    }
  }
})
