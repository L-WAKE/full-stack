import http from "./http";

export function fetchDocuments(spaceId) {
  return http.get("/documents", { params: { space_id: spaceId } });
}

export function uploadDocument(formData) {
  return http.post("/documents/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
}

export function triggerParse(documentId) {
  return http.post(`/documents/${documentId}/parse`);
}

export function fetchDocumentChunks(documentId) {
  return http.get(`/documents/${documentId}/chunks`);
}

export function updateDocumentVisibility(documentId, visibilityScope) {
  return http.put(`/documents/${documentId}/visibility`, {
    visibility_scope: visibilityScope,
  });
}

export function deleteDocument(documentId) {
  return http.delete(`/documents/${documentId}`);
}
