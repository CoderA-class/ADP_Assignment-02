import { useEffect, useRef, useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import Icon from './Icons';
import Logo from './Logo';
import ErrorBoundary from './ErrorBoundary';
import { Avatar, Field } from './ui';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { NOTIFICATION_META, useNotifications } from '../context/NotificationContext';
import { useBackendStatus } from '../hooks/useBackendStatus';
import { api } from '../services/api';
import { fullName, minutesToHours, registerCategories, timeAgo } from '../utils/format';

export const APP_NAME = 'Nexus';

const NAV = [
  { section: 'Overview', icon: 'grid', color: 'teal' },
  { to: '/', label: 'Dashboard', icon: 'dashboard', end: true, color: 'teal' },
  { to: '/pos', label: 'New Sale', icon: 'pos', color: 'coral' },
  { to: '/orders', label: 'Orders', icon: 'receipt', color: 'sky' },
  { section: 'Inventory', icon: 'warehouse', color: 'amber' },
  { to: '/products', label: 'Products & Stock', icon: 'boxes', badge: 'stock', color: 'amber' },
  { to: '/suppliers', label: 'Suppliers', icon: 'truck', color: 'peach' },
  { section: 'People', icon: 'users', color: 'lilac' },
  { to: '/customers', label: 'Customers', icon: 'users', color: 'lilac' },
  { to: '/employees', label: 'Employees', icon: 'badge', admin: true, color: 'pink' },
  { section: 'Workplace', icon: 'clipboard', color: 'mint' },
  { to: '/leave', label: 'Leave', adminLabel: 'Leave Requests', icon: 'calendar', badge: 'leave', color: 'mint' },
  { to: '/attendance', label: 'My Attendance', adminLabel: 'Attendance', icon: 'userClock', color: 'yellow' },
  { to: '/notifications', label: 'Notifications', icon: 'bell', badge: 'unread', color: 'slate' }
];

/* the sidebar groups: each category with the pages that belong to it */
const GROUPS = NAV.reduce((groups, item) => {
  if (item.section) groups.push({ ...item, items: [] });
  else groups[groups.length - 1].items.push(item);
  return groups;
}, []);
const groupOf = (path) => GROUPS.find((g) => g.items.some((i) => i.to === path))?.section;

const NAV_BY_PATH = Object.fromEntries(NAV.filter((n) => n.to).map((n) => [n.to, n]));
NAV_BY_PATH['/profile'] = { icon: 'circleUser', color: 'teal' };

/* soft background icons that drift slowly behind the content */
const FLOATIES = [
  ['box', 18, 24, 54], ['truck', 28, 88, 46], ['tag', 52, 14, 38], ['clipboard', 74, 92, 50],
  ['cart', 88, 30, 44], ['warehouse', 40, 60, 58], ['receipt', 14, 70, 36], ['dolly', 64, 46, 42]
];

export function Floaties() {
  return (
    <div className="floaties" aria-hidden="true">
      {FLOATIES.map(([icon, top, left, size], i) => (
        <span key={i} className="floaty" style={{ top: `${top}%`, left: `${left}%` }}><Icon name={icon} size={size} /></span>
      ))}
    </div>
  );
}

const TITLES = {
  '/': 'Dashboard', '/pos': 'New Sale', '/orders': 'Orders', '/products': 'Products & Stock', '/suppliers': 'Suppliers',
  '/customers': 'Customers', '/employees': 'Employees', '/leave': 'Leave', '/attendance': 'Attendance',
  '/notifications': 'Notifications', '/profile': 'My Profile'
};

export default function Layout() {
  const { user, isAdmin } = useAuth();
  const { items, unread } = useNotifications();
  const online = useBackendStatus();
  const [menuOpen, setMenuOpen] = useState(false);
  const location = useLocation();

  useEffect(() => { setMenuOpen(false); }, [location.pathname]);

  // the admin's stock categories (icons + colours used for product pictures)
  useEffect(() => { api.categories.all().then((list) => registerCategories(Array.isArray(list) ? list : [])).catch(() => {}); }, []);

  /* Overview is open when the app starts; after that, moving to a page opens its category */
  const [openGroup, setOpenGroup] = useState('Overview');
  const lastPath = useRef(location.pathname);
  useEffect(() => {
    if (lastPath.current === location.pathname) return;
    lastPath.current = location.pathname;
    const g = groupOf(location.pathname);
    if (g) setOpenGroup(g);
  }, [location.pathname]);

  /* tint the page background to match the colour of the section you're in */
  useEffect(() => {
    document.body.dataset.tint = NAV_BY_PATH[location.pathname]?.color || 'teal';
    return () => { delete document.body.dataset.tint; };
  }, [location.pathname]);

  const lowStockAlerts = items.filter((n) => !n.seen && ['LOW_STOCK', 'OUT_OF_STOCK', 'STOCK_ALERT', 'STOCK_ALERT_UPDATE'].includes(n.type)).length;
  const leaveAlerts = items.filter((n) => !n.seen && (n.type === 'LEAVE_REQUEST' || n.type === 'LEAVE_DECISION')).length;
  const badges = { stock: lowStockAlerts, leave: leaveAlerts, unread };

  return (
    <div className="app-shell">
      <aside className={`sidebar${menuOpen ? ' open' : ''}`}>
        <div className="sidebar-brand">
          <div className="brand-logo"><Logo size={22} /></div>
          <div>
            <div className="brand-name">{APP_NAME}</div>
            <div className="brand-sub">Wholesale &amp; spaza supply</div>
          </div>
        </div>
        <nav className="sidebar-nav">
          {GROUPS.map((group) => {
            const items = group.items.filter((n) => !n.admin || isAdmin);
            const open = openGroup === group.section;
            const here = groupOf(location.pathname) === group.section;
            const count = items.reduce((sum, i) => sum + (i.badge ? badges[i.badge] || 0 : 0), 0);
            return (
              <div key={group.section} className={`nav-group g-${group.color}${open ? ' open' : ''}${here ? ' here' : ''}`}>
                <button type="button" className="nav-group-head" aria-expanded={open} onClick={() => setOpenGroup(open ? null : group.section)}>
                  <span className="nav-group-ico"><Icon name={group.icon} size={15} /></span>
                  <span className="nav-group-name">{group.section}</span>
                  {!open && count > 0 && <span className="nav-badge">{count}</span>}
                  <span className="nav-group-caret"><Icon name="chevronDown" size={12} /></span>
                </button>
                <div className="nav-group-body">
                  <div className="nav-group-inner">
                    {items.map((item) => (
                      <NavLink key={item.to} to={item.to} end={item.end} tabIndex={open ? 0 : -1} className={({ isActive }) => `nav-link n-${item.color}${isActive ? ' active' : ''}`}>
                        <span className={`nav-ico c-${item.color}`}><Icon name={item.icon} size={14} /></span>
                        <span>{isAdmin && item.adminLabel ? item.adminLabel : item.label}</span>
                        {item.badge && badges[item.badge] > 0 && <span className="nav-badge">{badges[item.badge]}</span>}
                      </NavLink>
                    ))}
                  </div>
                </div>
              </div>
            );
          })}
        </nav>
        <div className="sidebar-footer">
          <NavLink to="/profile" className="sidebar-user">
            <Avatar src={user?.imageUrl} name={fullName(user)} size={36} />
            <div style={{ minWidth: 0 }}>
              <div className="name">{fullName(user)}</div>
              <div className="role">{isAdmin ? 'Administrator' : user?.role || 'Employee'}</div>
            </div>
          </NavLink>
        </div>
      </aside>
      <div className={`sidebar-backdrop${menuOpen ? ' open' : ''}`} onClick={() => setMenuOpen(false)} />

      <div className="main">
        <header className="topbar">
          <button className="icon-btn menu-toggle" onClick={() => setMenuOpen(true)} aria-label="Open menu"><Icon name="menu" /></button>
          <div className="topbar-title">
            <div>
              <div className="topbar-sub hide-sm">{new Date().toLocaleDateString('en-ZA', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}</div>
              <div>{TITLES[location.pathname] || APP_NAME}</div>
            </div>
          </div>
          <div className="topbar-spacer" />
          <ClockWidget />
          <NotificationBell />
          <UserMenu />
        </header>
        {online === false && (
          <div className="banner offline">
            <Icon name="wifiOff" size={16} />
            <span><strong>Can't reach the server.</strong> Start the Spring Boot backend (and MySQL). This page reconnects automatically.</span>
          </div>
        )}
        <main className="page">
          <ErrorBoundary resetKey={location.pathname}><Outlet /></ErrorBoundary>
        </main>
        {user?.mustChangePassword && <ForcePasswordChange />}
      </div>
    </div>
  );
}

function useClickOutside(ref, onOutside) {
  useEffect(() => {
    const handler = (e) => { if (ref.current && !ref.current.contains(e.target)) onOutside(); };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, [ref, onOutside]);
}

function NotificationBell() {
  const { items, unread, markRead, markAllRead } = useNotifications();
  const [open, setOpen] = useState(false);
  const ref = useRef(null);
  const navigate = useNavigate();
  useClickOutside(ref, () => setOpen(false));

  const openItem = (n) => {
    markRead(n.id);
    setOpen(false);
    navigate(NOTIFICATION_META[n.type]?.link || '/notifications');
  };

  return (
    <div style={{ position: 'relative' }} ref={ref}>
      <button className="icon-btn bordered" onClick={() => setOpen((o) => !o)} aria-label={`Notifications (${unread} unread)`}>
        <Icon name="bell" />
        {unread > 0 && <span className="dot">{unread > 99 ? '99+' : unread}</span>}
      </button>
      {open && (
        <div className="dropdown">
          <div className="dropdown-header">
            <div><strong>Notifications</strong> <span className="muted small">{unread} unread</span></div>
            {unread > 0 && <button className="btn btn-ghost btn-sm" onClick={markAllRead}>Mark all read</button>}
          </div>
          <div className="dropdown-list">
            {items.length === 0 && <div className="empty" style={{ padding: 30 }}><p>You're all caught up.</p></div>}
            {items.slice(0, 8).map((n) => <NotificationRow key={n.id} n={n} onClick={() => openItem(n)} />)}
          </div>
          <div className="dropdown-footer">
            <button className="btn btn-ghost btn-sm" onClick={() => { setOpen(false); navigate('/notifications'); }}>View all notifications</button>
          </div>
        </div>
      )}
    </div>
  );
}

export function NotificationRow({ n, onClick }) {
  const meta = NOTIFICATION_META[n.type] || NOTIFICATION_META.SYSTEM;
  return (
    <div className={`notif${n.seen ? '' : ' unread'}`} onClick={onClick}>
      <div className={`n-icon tone-${meta.tone}`}><Icon name={meta.icon} size={17} /></div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div className="n-title">{n.title}</div>
        <div className="n-msg">{n.message}</div>
        <div className="n-time">{timeAgo(n.createdAt)}</div>
      </div>
    </div>
  );
}

/* Clock in / clock out button in the top bar */
function ClockWidget() {
  const { user } = useAuth();
  const toast = useToast();
  const [shift, setShift] = useState(undefined);
  const [busy, setBusy] = useState(false);
  const [, tick] = useState(0);

  useEffect(() => {
    let alive = true;
    api.attendance.current(user.empID).then((s) => alive && setShift(s || null)).catch(() => alive && setShift(undefined));
    const t = setInterval(() => tick((x) => x + 1), 30000);
    return () => { alive = false; clearInterval(t); };
  }, [user.empID]);

  const toggle = async () => {
    setBusy(true);
    try {
      if (shift) {
        const done = await api.attendance.clockOut(user.empID);
        setShift(null);
        toast.success('Clocked out', `You worked ${minutesToHours(done.minutesWorked)} today. See you next time!`);
      } else {
        const s = await api.attendance.clockIn(user.empID);
        setShift(s);
        toast.success('Clocked in', 'Have a great shift!');
      }
      window.dispatchEvent(new Event('attendance-changed'));
    } catch (err) {
      toast.error('Clock in/out failed', err.message);
    } finally {
      setBusy(false);
    }
  };

  if (shift === undefined) return null;
  const minutes = shift ? Math.max(0, Math.floor((Date.now() - new Date(shift.clockIn).getTime()) / 60000)) : 0;
  return (
    <div className={`clock-pill${shift ? ' on' : ''}`}>
      <Icon name="clock" size={15} />
      <span className="pill-text">{shift ? `On shift · ${minutesToHours(minutes)}` : 'Off shift'}</span>
      <button className={`btn btn-sm ${shift ? 'btn-secondary' : 'btn-primary'}`} style={{ height: 30, borderRadius: 10 }} onClick={toggle} disabled={busy}>
        <Icon name={shift ? 'stop' : 'play'} size={12} /> {shift ? 'Clock out' : 'Clock in'}
      </button>
    </div>
  );
}

function UserMenu() {
  const { user, isAdmin, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const ref = useRef(null);
  const navigate = useNavigate();
  useClickOutside(ref, () => setOpen(false));
  return (
    <div style={{ position: 'relative' }} ref={ref}>
      <button className="user-chip" onClick={() => setOpen((o) => !o)} aria-label="Account menu">
        <span className="user-chip-avatar"><Avatar src={user?.imageUrl} name={fullName(user)} size={40} /></span>
        <span className="user-chip-pill hide-sm">
          <span className="user-chip-text">
            <span className="user-chip-label">{isAdmin ? 'Administrator' : user?.role || 'Employee'}</span>
            <span className="user-chip-name">{fullName(user)}</span>
          </span>
          <span className="user-chip-caret"><Icon name="chevronDown" size={13} /></span>
        </span>
      </button>
      {open && (
        <div className="dropdown menu">
          <button className="menu-item" onClick={() => { setOpen(false); navigate('/profile'); }}><Icon name="user" size={16} /> My profile</button>
          <button className="menu-item" onClick={() => { setOpen(false); navigate('/leave'); }}><Icon name="calendar" size={16} /> {isAdmin ? 'Leave requests' : 'Request leave'}</button>
          <div className="divider" style={{ margin: '6px 0' }} />
          <button className="menu-item danger" onClick={async () => { await logout(); navigate('/login'); }}><Icon name="logout" size={16} /> Log out</button>
        </div>
      )}
    </div>
  );
}

/* After an admin resets someone's password they sign in with a temporary one – and must choose their own first */
function ForcePasswordChange() {
  const { user, updateUser, logout } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const [pw, setPw] = useState({ current: '', next: '', confirm: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => setPw((p) => ({ ...p, [k]: e.target.value }));

  const save = async (e) => {
    e.preventDefault();
    setError('');
    if (pw.next.length < 6) return setError('Use at least 6 characters for your new password.');
    if (pw.next !== pw.confirm) return setError("The new passwords don't match.");
    setBusy(true);
    try {
      await api.auth.changePassword(pw.current, pw.next);
      updateUser({ ...user, mustChangePassword: false });
      toast.success('Password updated', 'You are all set. Welcome back!');
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="overlay">
      <form className="modal sm" onSubmit={save} role="dialog" aria-modal="true" aria-label="Choose a new password">
        <div className="modal-header"><div>
          <h2>Choose a new password</h2>
          <p>An admin reset your password. Enter the temporary password they gave you, then pick your own.</p>
        </div></div>
        <div className="modal-body stack" style={{ gap: 14 }}>
          <Field label="Temporary password"><input className="input" type="password" autoComplete="current-password" value={pw.current} onChange={set('current')} autoFocus /></Field>
          <Field label="New password" hint="At least 6 characters"><input className="input" type="password" autoComplete="new-password" value={pw.next} onChange={set('next')} /></Field>
          <Field label="Confirm new password"><input className="input" type="password" autoComplete="new-password" value={pw.confirm} onChange={set('confirm')} /></Field>
          {error && <div className="text-danger small row-flex"><Icon name="alert" size={14} /> {error}</div>}
        </div>
        <div className="modal-footer">
          <button type="button" className="btn btn-secondary" onClick={async () => { await logout(); navigate('/login'); }}>Sign out</button>
          <button className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : 'Save new password'}</button>
        </div>
      </form>
    </div>
  );
}
