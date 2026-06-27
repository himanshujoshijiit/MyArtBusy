'use client';

import { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { AuthUser, getUser, clearAuth, setAuth } from '@/lib/api';

interface AuthContextType {
  user: AuthUser | null;
  login: (token: string, user: AuthUser) => void;
  logout: () => void;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType>({
  user: null,
  login: () => {},
  logout: () => {},
  loading: true,
});

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setUser(getUser());
    setLoading(false);
  }, []);

  const login = (token: string, u: AuthUser) => {
    setAuth(token, u);
    setUser(u);
  };

  const logout = () => {
    clearAuth();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
