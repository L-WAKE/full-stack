import http from "./http";

export function fetchConversations(spaceId) {
  return http.get("/chat/conversations", { params: { space_id: spaceId } });
}

export function createConversation(payload) {
  return http.post("/chat/conversations", payload);
}

export function fetchMessages(conversationId) {
  return http.get(`/chat/conversations/${conversationId}/messages`);
}

export function sendMessage(conversationId, payload) {
  return http.post(`/chat/conversations/${conversationId}/messages`, payload);
}

export function retrievalSearch(payload) {
  return http.post("/retrieval/search", payload);
}
