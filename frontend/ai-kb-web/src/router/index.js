import { createRouter, createWebHashHistory } from "vue-router";

import AppLayout from "../views/dashboard/AppLayout.vue";
import ChatView from "../views/chat/ChatView.vue";
import DashboardView from "../views/dashboard/DashboardView.vue";
import DocumentView from "../views/document/DocumentView.vue";
import SpaceView from "../views/kb-space/SpaceView.vue";
import LoginView from "../views/login/LoginView.vue";
import SettingView from "../views/setting/SettingView.vue";

const routes = [
  { path: "/login", name: "login", component: LoginView },
  {
    path: "/",
    component: AppLayout,
    children: [
      { path: "", redirect: "/dashboard" },
      { path: "/dashboard", name: "dashboard", component: DashboardView },
      { path: "/spaces", name: "spaces", component: SpaceView },
      { path: "/documents", name: "documents", component: DocumentView },
      { path: "/chat", name: "chat", component: ChatView },
      { path: "/settings", name: "settings", component: SettingView },
    ],
  },
  { path: "/:pathMatch(.*)*", redirect: "/login" },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
});

router.beforeEach((to) => {
  const token = localStorage.getItem("ai-kb-token");
  if (to.path !== "/login" && !token) {
    return "/login";
  }
  if (to.path === "/login" && token) {
    return "/dashboard";
  }
  return true;
});

export default router;
