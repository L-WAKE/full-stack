import http from "./http";

export function fetchSpaces() {
  return http.get("/spaces");
}

export function fetchSpaceDetail(spaceId) {
  return http.get(`/spaces/${spaceId}`);
}

export function createSpace(payload) {
  return http.post("/spaces", payload);
}

export function assignSpaceMembers(spaceId, payload) {
  return http.post(`/spaces/${spaceId}/members`, payload);
}

export function updateSpaceMemberRole(spaceId, userId, role) {
  return http.put(`/spaces/${spaceId}/members/${userId}/role`, { role });
}

export function removeSpaceMember(spaceId, userId) {
  return http.delete(`/spaces/${spaceId}/members/${userId}`);
}
