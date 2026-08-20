import React, { createContext, useContext, useState, useEffect } from 'react';
import { User } from '../types';
import { api } from '../services/api';

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, name: string, phone: string, role: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const savedUser = localStorage.getItem('rrr_user');
    const token = localStorage.getItem('rrr_token');

    if (savedUser && token) {
      try {
        setUser(JSON.parse(savedUser));
      } catch {
        localStorage.removeItem('rrr_user');
        localStorage.removeItem('rrr_token');
      }
    }
    setLoading(false);

    const handleUnauthorized = () => {
      setUser(null);
    };

    window.addEventListener('auth_unauthorized', handleUnauthorized);
    return () => window.removeEventListener('auth_unauthorized', handleUnauthorized);
  }, []);

  const login = async (email: string, password: string) => {
    const response = await api.post('/auth/login', { email, password });
    const { token, userId, email: userEmail, name, role } = response.data.data;

    const userData: User = { id: userId, email: userEmail, name, role };
    localStorage.setItem('rrr_token', token);
    localStorage.setItem('rrr_user', JSON.stringify(userData));
    setUser(userData);
  };

  const register = async (email: string, password: string, name: string, phone: string, role: string) => {
    const response = await api.post('/auth/register', { email, password, name, phone, role });
    const { token, userId, email: userEmail, name: userName, role: userRole } = response.data.data;

    const userData: User = { id: userId, email: userEmail, name: userName, role: userRole };
    localStorage.setItem('rrr_token', token);
    localStorage.setItem('rrr_user', JSON.stringify(userData));
    setUser(userData);
  };

  const logout = () => {
    localStorage.removeItem('rrr_token');
    localStorage.removeItem('rrr_user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
