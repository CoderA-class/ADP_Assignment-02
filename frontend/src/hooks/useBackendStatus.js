import { useEffect, useState } from 'react';
import { api } from '../services/api';

/*
 * Checks every few seconds whether the Spring Boot backend is reachable.
 * This is what lets you start the frontend before the backend: the app just
 * shows "waiting for server" and carries on as soon as it comes up.
 */
export function useBackendStatus() {
  const [online, setOnline] = useState(null); // null = still checking

  useEffect(() => {
    let cancelled = false;
    let timer;
    const check = async () => {
      let ok = false;
      try {
        await api.health();
        ok = true;
      } catch {
        ok = false;
      }
      if (!cancelled) {
        setOnline(ok);
        // check often while offline, rarely while everything is fine
        timer = setTimeout(check, ok ? 15000 : 3000);
      }
    };
    check();
    return () => { cancelled = true; clearTimeout(timer); };
  }, []);

  return online;
}
