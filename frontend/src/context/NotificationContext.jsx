import { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react';
import { api } from '../services/api';
import { useAuth } from './AuthContext';
import { useToast } from './ToastContext';

/* Loads the logged-in user's notifications and refreshes them every 20 seconds */
const NotificationContext = createContext(null);

export function NotificationProvider({ children }) {
  const { user } = useAuth();
  const toast = useToast();
  const [items, setItems] = useState([]);
  const seenIds = useRef(null);

  const refresh = useCallback(async () => {
    if (!user) return;
    try {
      const list = await api.notifications.list();
      setItems(list || []);
      // Pop up a toast for brand-new unread notifications (not on first load)
      if (seenIds.current) {
        const fresh = (list || []).filter((n) => !n.seen && !seenIds.current.has(n.id));
        fresh.slice(0, 2).forEach((n) => toast.info(n.title, n.message));
      }
      seenIds.current = new Set((list || []).map((n) => n.id));
    } catch {
      /* backend offline – the banner already says so */
    }
  }, [user]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    seenIds.current = null;
    setItems([]);
    if (!user) return undefined;
    refresh();
    const t = setInterval(refresh, 20000);
    return () => clearInterval(t);
  }, [user, refresh]);

  const markRead = async (id) => {
    setItems((list) => list.map((n) => (n.id === id ? { ...n, seen: true } : n)));
    try { await api.notifications.markRead(id); } catch { /* ignore */ }
  };

  const markAllRead = async () => {
    setItems((list) => list.map((n) => ({ ...n, seen: true })));
    try { await api.notifications.markAllRead(); } catch { /* ignore */ }
  };

  const unread = items.filter((n) => !n.seen).length;

  return (
    <NotificationContext.Provider value={{ items, unread, refresh, markRead, markAllRead }}>
      {children}
    </NotificationContext.Provider>
  );
}

export const useNotifications = () => useContext(NotificationContext);

/* Icon + colour for each notification type, and where clicking it should go */
export const NOTIFICATION_META = {
  LOW_STOCK: { icon: 'alert', tone: 'amber', link: '/products' },
  OUT_OF_STOCK: { icon: 'box', tone: 'red', link: '/products' },
  INACTIVE_CUSTOMER: { icon: 'user', tone: 'violet', link: '/customers' },
  LEAVE_REQUEST: { icon: 'calendar', tone: 'blue', link: '/leave' },
  LEAVE_DECISION: { icon: 'calendar', tone: 'green', link: '/leave' },
  CLOCK_IN: { icon: 'clock', tone: 'green', link: '/attendance' },
  CLOCK_OUT: { icon: 'clock', tone: 'cyan', link: '/attendance' },
  NEW_SALE: { icon: 'receipt', tone: 'blue', link: '/orders' },
  STOCK_ALERT: { icon: 'bullhorn', tone: 'amber', link: '/products' },
  STOCK_ALERT_UPDATE: { icon: 'checkCircle', tone: 'green', link: '/products' },
  PASSWORD_RESET: { icon: 'lock', tone: 'violet', link: '/employees' },
  SYSTEM: { icon: 'info', tone: 'blue', link: '/notifications' }
};
