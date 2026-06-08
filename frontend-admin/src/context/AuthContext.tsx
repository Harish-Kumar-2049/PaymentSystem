import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';

interface AuthUser {
  email: string;
  role: string;
  token: string;
}

interface AuthContextType {
  user: AuthUser | null;
  login: (token: string, email: string, role: string) => void;
  logout: () => void;
  isAuthenticated: boolean;
  isAdmin: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

function parseJwtPayload(token: string): Record<string, unknown> | null {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(atob(base64));
  } catch {
    return null;
  }
}

function isTokenExpired(token: string): boolean {
  const payload = parseJwtPayload(token);
  if (!payload || typeof payload.exp !== 'number') return true;
  return Date.now() >= payload.exp * 1000;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);

  useEffect(() => {
    const token = localStorage.getItem('jwt_token');
    const email = localStorage.getItem('jwt_email');
    const role = localStorage.getItem('jwt_role');

    if (token && email && role && !isTokenExpired(token)) {
      setUser({ token, email, role });
    } else {
      localStorage.removeItem('jwt_token');
      localStorage.removeItem('jwt_email');
      localStorage.removeItem('jwt_role');
    }
  }, []);

  const login = (token: string, email: string, role: string) => {
    localStorage.setItem('jwt_token', token);
    localStorage.setItem('jwt_email', email);
    localStorage.setItem('jwt_role', role);
    setUser({ token, email, role });
  };

  const logout = () => {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('jwt_email');
    localStorage.removeItem('jwt_role');
    setUser(null);
  };

  const isAuthenticated = user !== null;
  const isAdmin = user?.role === 'ADMIN';

  return (
    <AuthContext.Provider value={{ user, login, logout, isAuthenticated, isAdmin }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
