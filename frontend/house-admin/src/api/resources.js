import http from './http'

export function getTenants(params) {
  return http.get('/tenants', { params })
}

export function createTenant(data) {
  return http.post('/tenants', data)
}

export function updateTenant(id, data) {
  return http.put(`/tenants/${id}`, data)
}

export function getLandlords(params) {
  return http.get('/landlords', { params })
}

export function getLandlordOptions() {
  return http.get('/landlords/options')
}

export function createLandlord(data) {
  return http.post('/landlords', data)
}

export function updateLandlord(id, data) {
  return http.put(`/landlords/${id}`, data)
}

export function getMaintenanceOrders(params) {
  return http.get('/maintenance-orders', { params })
}

export function createMaintenanceOrder(data) {
  return http.post('/maintenance-orders', data)
}

export function updateMaintenanceOrder(id, data) {
  return http.put(`/maintenance-orders/${id}`, data)
}

export function deleteMaintenanceOrder(id) {
  return http.delete(`/maintenance-orders/${id}`)
}

export function getCleaningOrders(params) {
  return http.get('/cleaning-orders', { params })
}

export function createCleaningOrder(data) {
  return http.post('/cleaning-orders', data)
}

export function updateCleaningOrder(id, data) {
  return http.put(`/cleaning-orders/${id}`, data)
}

export function deleteCleaningOrder(id) {
  return http.delete(`/cleaning-orders/${id}`)
}

export function getEmployees(params) {
  return http.get('/employees', { params })
}

export function createEmployee(data) {
  return http.post('/employees', data)
}

export function updateEmployee(id, data) {
  return http.put(`/employees/${id}`, data)
}

export function updateEmployeeStatus(id, status) {
  return http.put(`/employees/${id}/status`, { status })
}

export function getRoles() {
  return http.get('/roles')
}

export function createRole(data) {
  return http.post('/roles', data)
}

export function updateRole(id, data) {
  return http.put(`/roles/${id}`, data)
}

export function getMenuTree() {
  return http.get('/menus/tree')
}

export function createMenu(data) {
  return http.post('/menus', data)
}

export function updateMenu(id, data) {
  return http.put(`/menus/${id}`, data)
}
