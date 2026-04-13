import { defineStore } from "pinia";

import { fetchConversations, fetchMessages, retrievalSearch } from "../api/chat";
import { fetchOverview } from "../api/dashboard";
import {
  deleteDocument,
  fetchDocumentChunks,
  fetchDocuments,
  triggerParse,
  updateDocumentVisibility,
  uploadDocument,
} from "../api/documents";
import {
  assignSpaceMembers,
  createSpace,
  fetchSpaceDetail,
  fetchSpaces,
  removeSpaceMember,
  updateSpaceMemberRole,
} from "../api/spaces";

export const useWorkspaceStore = defineStore("workspace", {
  state: () => ({
    overview: {
      space_count: 0,
      document_count: 0,
      conversation_count: 0,
    },
    spaces: [],
    activeSpaceId: null,
    activeSpaceDetail: null,
    documents: [],
    conversations: [],
    messages: [],
    chunks: [],
    retrievalResult: null,
  }),
  actions: {
    async loadOverview() {
      const { data } = await fetchOverview();
      this.overview = data;
    },
    async loadSpaces() {
      const { data } = await fetchSpaces();
      this.spaces = data;
      if (!this.activeSpaceId && data.length > 0) {
        this.activeSpaceId = data[0].id;
      }
      if (this.activeSpaceId) {
        await this.loadActiveSpaceDetail();
      }
      return data;
    },
    async loadActiveSpaceDetail() {
      if (!this.activeSpaceId) return null;
      const { data } = await fetchSpaceDetail(this.activeSpaceId);
      this.activeSpaceDetail = data;
      return data;
    },
    async addSpace(payload) {
      await createSpace(payload);
      return this.loadSpaces();
    },
    async addSpaceMembers(payload) {
      if (!this.activeSpaceId) return null;
      await assignSpaceMembers(this.activeSpaceId, payload);
      return this.loadActiveSpaceDetail();
    },
    async changeMemberRole(userId, role) {
      if (!this.activeSpaceId) return null;
      await updateSpaceMemberRole(this.activeSpaceId, userId, role);
      return this.loadActiveSpaceDetail();
    },
    async deleteMember(userId) {
      if (!this.activeSpaceId) return null;
      await removeSpaceMember(this.activeSpaceId, userId);
      return this.loadActiveSpaceDetail();
    },
    setActiveSpace(spaceId) {
      this.activeSpaceId = spaceId;
    },
    async loadDocuments() {
      if (!this.activeSpaceId) return [];
      const { data } = await fetchDocuments(this.activeSpaceId);
      this.documents = data;
      return data;
    },
    async addDocument(file, visibilityScope = "SPACE") {
      if (!this.activeSpaceId) return null;
      const formData = new FormData();
      formData.append("space_id", this.activeSpaceId);
      formData.append("visibility_scope", visibilityScope);
      formData.append("file", file);
      await uploadDocument(formData);
      return this.loadDocuments();
    },
    async parseDocument(documentId) {
      await triggerParse(documentId);
      return this.loadDocuments();
    },
    async removeDocument(documentId) {
      await deleteDocument(documentId);
      return this.loadDocuments();
    },
    async changeDocumentVisibility(documentId, visibilityScope) {
      await updateDocumentVisibility(documentId, visibilityScope);
      return this.loadDocuments();
    },
    async loadChunks(documentId) {
      const { data } = await fetchDocumentChunks(documentId);
      this.chunks = data;
      return data;
    },
    async loadConversations() {
      if (!this.activeSpaceId) return;
      const { data } = await fetchConversations(this.activeSpaceId);
      this.conversations = data;
    },
    async loadMessages(conversationId) {
      const { data } = await fetchMessages(conversationId);
      this.messages = data;
    },
    async runRetrieval(query) {
      if (!this.activeSpaceId) return null;
      const { data } = await retrievalSearch({
        space_id: this.activeSpaceId,
        query,
        top_k: 5,
      });
      this.retrievalResult = data;
      return data;
    },
  },
});
