import http from "./http";

export function login(payload) {
  return http.post("/auth/login", payload);
}

export function fetchProfile() {
  return http.get("/auth/profile");
}

export function fetchMySpaces() {
  return http.get("/auth/spaces");
}

