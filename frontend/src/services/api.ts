import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach JWT token to every request if present
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ── Auth ──
export const authAPI = {
  register: (data: { email: string; password: string; fullName: string }) =>
    api.post('/auth/register', data),

  login: (data: { email: string; password: string }) =>
    api.post('/auth/login', data),
};

// ── Wallets ──
export const walletAPI = {
  create: (currency: string = 'INR') =>
    api.post(`/wallets?currency=${currency}`),

  getById: (walletId: string) =>
    api.get(`/wallets/${walletId}`),

  getMyWallets: () =>
    api.get('/wallets/my-wallets'),
};

// ── Transactions ──
export const transactionAPI = {
  transfer: (data: {
    sourceWalletId: string;
    targetWalletId: string;
    amount: number;
    idempotencyKey: string;
  }) => api.post('/transactions/transfer', data),

  refund: (data: { transactionId: string; reason: string }) =>
    api.post('/transactions/refund', data),

  getById: (transactionId: string) =>
    api.get(`/transactions/${transactionId}`),
};

// ── Audit ──
export const auditAPI = {
  getByUser: (userId: string) =>
    api.get(`/audit/user/${userId}`),

  getByTransaction: (transactionId: string) =>
    api.get(`/audit/transaction/${transactionId}`),
};

// ── Admin ──
export const adminAPI = {
  deposit: (walletId: string, amount: number) =>
    api.post(`/admin/wallets/${walletId}/deposit?amount=${amount}`),
};

export default api;
