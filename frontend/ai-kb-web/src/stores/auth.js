import { defineStore } from "pinia";

import { fetchMySpaces, fetchProfile, login } from "../api/auth";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: localStorage.getItem("ai-kb-token") || "",
    profile: null,
    spaces: [],
  }),
  getters: {
    isAuthenticated(state) {
      return Boolean(state.token);
    },
  },
  actions: {
    async login(payload) {
      const { data } = await login(payload);
      this.token = data.access_token;
      this.profile = data.user;
      localStorage.setItem("ai-kb-token", data.access_token);
      return data;
    },
    async hydrate() {
      if (!this.token) {
        return;
      }
      const [{ data: profile }, { data: spaces }] = await Promise.all([fetchProfile(), fetchMySpaces()]);
      this.profile = profile;
      this.spaces = spaces;
    },
    async loadSpaces() {
      const { data } = await fetchMySpaces();
      this.spaces = data;
      return data;
    },
    logout() {
      this.token = "";
      this.profile = null;
      this.spaces = [];
      localStorage.removeItem("ai-kb-token");
    },
  },
});
