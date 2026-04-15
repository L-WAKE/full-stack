import http from "./http";

export function fetchOverview() {
  return http.get("/statistics/overview");
}

export function fetchRecentActivity() {
  return http.get("/statistics/recent-activity");
}
