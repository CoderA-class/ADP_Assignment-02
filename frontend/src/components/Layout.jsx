import { useEffect, useRef, useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import Icon from './Icons';
import { Avatar } from './ui';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { NOTIFICATION_META, useNotifications } from '../context/NotificationContext';
import { useBackendStatus } from '../hooks/useBackendStatus';
import { api } from '../services/api';
import { fullName, minutesToHours, timeAgo } from '../utils/format';

export const APP_NAME = 'StockFlow';

const NAV = [
  { section: 'Overview' },
  { to: '/', label: 'Dashboard', icon: 'dashboard', end: true },
  { to: '/pos', label: 'New Sale', icon: 'cart' },
  { to: '/orders', label: 'Orders', icon: 'receipt' },
  { section: 'Inventory' },
  { to: '/products', label: 'Products & Stock', icon: 'box', badge: 'stock' },
  { to: '/suppliers', label: 'Suppliers', icon: 'truck' },
  { section: 'People' },
  { to: '/customers', label: 'Customers', icon: 'users' },
  { to: '/employees', label: 'Employees', icon: 'badge', admin: true },
  { section: 'Workplace' },
  { to: '/leave', label: 'Leave', adminLabel: 'Leave Requests', icon: 'calendar', badge: 'leave' },
  { to: '/attendance', label: 'My Attendance', adminLabel: 'Attendance', icon: 'clock' },
  { to: '/notifications', label: 'Notifications', icon: 'bell', badge: 'unread' }
];

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

  const lowStockAlerts = items.filter((n) => !n.seen && (n.type === 'LOW_STOCK' || n.type === 'OUT_OF_STOCK')).length;
  const leaveAlerts = items.filter((n) => !n.seen && (n.type === 'LEAVE_REQUEST' || n.type === 'LEAVE_DECISION')).length;
  const badges = { stock: lowStockAlerts, leave: leaveAlerts, unread };

  return (
    <div className="app-shell">
      <aside className={`sidebar${menuOpen ? ' open' : ''}`}>
        <div className="sidebar-brand">
          <div className="brand-logo"><Icon name="layers" size={19} /></div>
          <div>
            <div className="brand-name">{APP_NAME}</div>
            <div className="brand-sub">Wholesale Manager</div>
          </div>
        </div>
        <nav className="sidebar-nav">
          {NAV.filter((n) => !n.admin || isAdmin).map((item, i) => item.section ? (
            <div key={`s${i}`} className="nav-section">{item.section}</div>
          ) : (
            <NavLink key={item.to} to={item.to} end={item.end} className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>
              <Icon name={item.icon} size={18} />
              <span>{isAdmin && item.adminLabel ? item.adminLabel : item.label}</span>
              {item.badge && badges[item.badge] > 0 && <span className="nav-badge">{badges[item.badge]}</span>}
            </NavLink>
          ))}
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
          <div>
            <div className="topbar-title">{TITLES[location.pathname] || APP_NAME}</div>
            <div className="topbar-sub hide-sm">{new Date().toLocaleDateString('en-ZA', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}</div>
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
          <Outlet />
        </main>
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
      <button className={`btn btn-sm ${shift ? 'btn-secondary' : 'btn-primary'}`} style={{ height: 28, borderRadius: 7 }} onClick={toggle} disabled={busy}>
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
      <button className="row-flex" style={{ border: 0, background: 'none', cursor: 'pointer', padding: 0 }} onClick={() => setOpen((o) => !o)} aria-label="Account menu">
        <Avatar src={user?.imageUrl} name={fullName(user)} size={36} />
        <div className="hide-sm" style={{ textAlign: 'left', lineHeight: 1.2 }}>
          <div style={{ fontWeight: 600, fontSize: 13 }}>{fullName(user)}</div>
          <div className="muted" style={{ fontSize: 11 }}>{isAdmin ? 'Admin' : 'Employee'}</div>
        </div>
        <Icon name="chevronDown" size={16} className="hide-sm" />
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
