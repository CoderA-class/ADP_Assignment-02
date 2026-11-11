import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { api } from '../services/api';
import { storage } from '../utils/storage';

/*
 * Who is logged in, and are they an ADMIN or an EMPLOYEE?
 * Login is handled by Spring Security (session cookie). The employee is also cached in
 * localStorage so the screen doesn't flash on refresh – but the server session is the
 * real source of truth: /auth/me is checked on start-up and any 401 logs you out.
 */
const AuthContext = createContext(null);
const KEY = 'stockflow.user';

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => storage.get(KEY));
  const [expired, setExpired] = useState(false);
  const [signOutReason, setSignOutReason] = useState(null);   // e.g. 'SUSPENDED' when an admin bans the account

  const remember = useCallback((employee) => {
    if (employee) storage.set(KEY, employee); else storage.remove(KEY);
    setUser(employee);
  }, []);

  // Check the server session when the app opens
  useEffect(() => {
    let alive = true;
    api.auth.me()
      .then((me) => alive && remember(me))
      .catch((err) => {
        if (alive && err.status === 401) {   // offline? keep the cached user for now
          if (err.code === 'SUSPENDED') setSignOutReason('SUSPENDED');
          remember(null);
        }
      });
    return () => { alive = false; };
  }, [remember]);

  // Any 401 from the API = session ended
  useEffect(() => {
    const onExpired = (e) => {
      if (storage.get(KEY)) setExpired(true);
      // keep "suspended" even when the requests that follow come back as plain 401s
      setSignOutReason((prev) => (e?.detail?.code === 'SUSPENDED' || prev === 'SUSPENDED' ? 'SUSPENDED' : e?.detail?.code || null));
      remember(null);
    };
    window.addEventListener('auth-expired', onExpired);
    return () => window.removeEventListener('auth-expired', onExpired);
  }, [remember]);

  /* adminOnly = the "Admin" tab on the login screen: refuse employees there */
  const login = useCallback(async (email, password, rememberMe, adminOnly = false) => {
    const employee = await api.auth.login(email, password, rememberMe);
    if (adminOnly && employee.accessLevel !== 'ADMIN') {
      try { await api.auth.logout(); } catch { /* ignore */ }
      throw new Error('This account does not have admin access. Use the Employee login instead.');
    }
    setExpired(false);
    setSignOutReason(null);
    remember(employee);
    return employee;
  }, [remember]);

  const logout = useCallback(async () => {
    try { await api.auth.logout(); } catch { /* already logged out */ }
    remember(null);
  }, [remember]);

  const value = useMemo(() => ({
    user,
    isAdmin: user?.accessLevel === 'ADMIN',
    sessionExpired: expired,
    signOutReason,
    login,
    logout,
    updateUser: remember
  }), [user, expired, signOutReason, login, logout, remember]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
