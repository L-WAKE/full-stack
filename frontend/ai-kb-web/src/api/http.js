import axios from "axios";

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || "/api",
  timeout: 10000,
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem("ai-kb-token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default http;

