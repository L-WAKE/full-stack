import http from './http'

export function login(data) {
  return http.post('/auth/login', data)
}

export function logout() {
  return http.post('/auth/logout')
}

export function getProfile() {
  return http.get('/auth/profile')
}

export function getMenus() {
  return http.get('/auth/menus')
}
