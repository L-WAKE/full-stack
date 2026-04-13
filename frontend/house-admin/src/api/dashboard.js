import http from './http'

export function getOverview() {
  return http.get('/dashboard/overview')
}

export function getTrend() {
  return http.get('/dashboard/trend')
}
