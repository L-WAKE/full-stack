import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const AppLayout = () => import('../layout/AppLayout.vue')
const LoginView = () => import('../views/login/LoginView.vue')
const DashboardView = () => import('../views/dashboard/DashboardView.vue')
const HouseListView = () => import('../views/housing/HouseListView.vue')
const TenantView = () => import('../views/customer/TenantView.vue')
const LandlordView = () => import('../views/customer/LandlordView.vue')
const MaintenanceView = () => import('../views/workorder/MaintenanceView.vue')
const CleaningView = () => import('../views/workorder/CleaningView.vue')
const EmployeeView = () => import('../views/system/EmployeeView.vue')
const RoleView = () => import('../views/system/RoleView.vue')
const MenuView = () => import('../views/system/MenuView.vue')
const NoticeView = () => import('../views/system/NoticeView.vue')

const routes = [
  { path: '/login', component: LoginView },
  {
    path: '/',
    component: AppLayout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: DashboardView, meta: { title: '首页看板' } },
      { path: 'housing/whole-rent', component: HouseListView, props: { rentalMode: 'WHOLE', title: '整租房源' } },
      { path: 'housing/shared-rent', component: HouseListView, props: { rentalMode: 'SHARED', title: '合租房源' } },
      { path: 'housing/centralized', component: HouseListView, props: { rentalMode: 'CENTRALIZED', title: '集中式房源' } },
      { path: 'customer/tenant', component: TenantView },
      { path: 'customer/landlord', component: LandlordView },
      { path: 'workorder/maintenance', component: MaintenanceView },
      { path: 'workorder/cleaning', component: CleaningView },
      { path: 'system/employee', component: EmployeeView },
      { path: 'system/role', component: RoleView },
      { path: 'system/menu', component: MenuView },
      { path: 'system/notice', component: NoticeView }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  if (to.path === '/login') {
    return userStore.isLoggedIn ? '/dashboard' : true
  }
  if (!userStore.isLoggedIn) {
    return '/login'
  }
  if (!userStore.profile) {
    await userStore.bootstrap()
  }
  return true
})

export default router
