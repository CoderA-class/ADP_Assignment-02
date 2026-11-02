import { createContext, useCallback, useContext, useState } from 'react';
import Icon from '../components/Icons';

const ToastContext = createContext(null);
const ICONS = { success: 'checkCircle', error: 'alert', warning: 'alert', info: 'info' };
const COLORS = { success: 'var(--success)', error: 'var(--danger)', warning: 'var(--warning)', info: 'var(--primary)' };

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const dismiss = useCallback((id) => setToasts((t) => t.filter((x) => x.id !== id)), []);

  const show = useCallback((type, title, message) => {
    const id = Math.random().toString(36).slice(2);
    setToasts((t) => [...t.slice(-3), { id, type, title, message }]);
    setTimeout(() => dismiss(id), type === 'error' ? 6000 : 3500);
  }, [dismiss]);

  const toast = {
    success: (title, message) => show('success', title, message),
    error: (title, message) => show('error', title, message),
    warning: (title, message) => show('warning', title, message),
    info: (title, message) => show('info', title, message)
  };

  return (
    <ToastContext.Provider value={toast}>
      {children}
      <div className="toasts" role="status" aria-live="polite">
        {toasts.map((t) => (
          <div key={t.id} className={`toast ${t.type}`}>
            <span style={{ color: COLORS[t.type], marginTop: 1 }}><Icon name={ICONS[t.type]} /></span>
            <div style={{ flex: 1 }}>
              <div className="t-title">{t.title}</div>
              {t.message && <div className="t-msg">{t.message}</div>}
            </div>
            <button className="icon-btn" style={{ width: 24, height: 24 }} onClick={() => dismiss(t.id)} aria-label="Dismiss"><Icon name="x" size={14} /></button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export const useToast = () => useContext(ToastContext);
