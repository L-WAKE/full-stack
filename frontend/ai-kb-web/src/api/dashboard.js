import http from "./http";

export function fetchOverview() {
  return http.get("/statistics/overview");
}
