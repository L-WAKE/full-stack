import http from "./http";

export function fetchModels() {
  return http.get("/settings/models");
}

export function updateChatModel(name) {
  return http.put("/settings/model", { name });
}

export function fetchRetrievalStrategy() {
  return http.get("/settings/retrieval-strategy");
}

export function updateRetrievalStrategy(payload) {
  return http.put("/settings/retrieval-strategy", payload);
}
