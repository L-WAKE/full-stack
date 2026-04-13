export const houseStatusMap = {
  VACANT: '待出租',
  OCCUPIED: '已出租',
  RENOVATING: '装修中',
  OFFLINE: '已下架'
}

export const genderMap = {
  Male: '男',
  Female: '女'
}

export const employeeStatusMap = {
  ENABLED: '启用',
  DISABLED: '停用'
}

export const roleCodeMap = {
  ADMIN: '系统管理员',
  MANAGER: '门店经理',
  OPERATOR: '运营专员'
}

export const menuTypeMap = {
  MENU: '菜单',
  BUTTON: '按钮'
}

export const rentalModeMap = {
  WHOLE: '整租',
  SHARED: '合租',
  CENTRALIZED: '集中式'
}

export const maintenancePriorityMap = {
  LOW: '低',
  MEDIUM: '中',
  HIGH: '高',
  URGENT: '紧急'
}

export const maintenanceStatusMap = {
  PENDING: '待处理',
  PROCESSING: '处理中',
  COMPLETED: '已完成',
  CLOSED: '已关闭'
}

export const cleaningStatusMap = {
  PENDING_ASSIGN: '待派单',
  ASSIGNED: '已派单',
  SCHEDULED: '已预约',
  IN_PROGRESS: '服务中',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}

export const routeTitleMap = {
  '/dashboard': '首页看板',
  '/housing': '房源管理',
  '/housing/whole-rent': '整租房源',
  '/housing/shared-rent': '合租房源',
  '/housing/centralized': '集中式房源',
  '/customer': '客户管理',
  '/customer/tenant': '租客管理',
  '/customer/landlord': '房东管理',
  '/workorder': '工单管理',
  '/workorder/maintenance': '维修工单',
  '/workorder/cleaning': '保洁工单',
  '/system': '系统管理',
  '/system/employee': '员工管理',
  '/system/role': '角色管理',
  '/system/menu': '菜单管理'
}

export function localizeProfile(profile) {
  if (!profile) return profile
  return {
    ...profile,
    displayName: profile.displayName === 'System Admin' ? '系统管理员' : profile.displayName,
    roleCode: roleCodeMap[profile.roleCode] || profile.roleCode
  }
}

export function localizeMenus(menus = []) {
  return menus.map((menu) => ({
    ...menu,
    title: routeTitleMap[menu.path] || menu.title,
    children: localizeMenus(menu.children || [])
  }))
}

export function localizeRoleName(role) {
  if (!role) return role
  return roleCodeMap[role.code] || role.name || role.code
}
