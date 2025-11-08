import axios from 'axios';

const api = axios.create({
  baseURL: '/api'
});

export const fetchLegacyClients = () => api.get('/legacy/clients');

export const fetchNewClients = () => api.get('/new/clients');

export const migrateClient = (id) => api.post(`/migrate/${id}`);

