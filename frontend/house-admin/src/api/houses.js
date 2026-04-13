import http from './http'

export function getHouses(params) {
  return http.get('/houses', { params })
}

export function createHouse(data) {
  return http.post('/houses', data)
}

export function updateHouse(id, data) {
  return http.put(`/houses/${id}`, data)
}

export function updateHouseStatus(id, status) {
  return http.put(`/houses/${id}/status`, { status })
}

export function deleteHouse(id) {
  return http.delete(`/houses/${id}`)
}
