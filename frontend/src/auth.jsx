import { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { api, getStoredUser, storeUser } from './api.js';

const AuthContext = createContext(null);
export const useAuth = () => useContext(AuthContext);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(getStoredUser());

  useEffect(() => {
    const onLogout = () => setUser(null);
    window.addEventListener('pt-logout', onLogout);
    return () => window.removeEventListener('pt-logout', onLogout);
  }, []);

  const login = useCallback(async (email) => {
    const u = await api('/auth/login', { method: 'POST', body: { email } });
    storeUser(u);
    setUser(u);
    return u;
  }, []);

  const logout = useCallback(() => { storeUser(null); setUser(null); }, []);

  return (
    <AuthContext.Provider value={{ user, login, logout, isAdmin: user?.role === 'ADMIN' }}>
      {children}
    </AuthContext.Provider>
  );
}
