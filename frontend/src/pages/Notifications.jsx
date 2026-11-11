import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Icon from '../components/Icons';
import { EmptyState, PageHeader, StatCard } from '../components/ui';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { NOTIFICATION_META, useNotifications } from '../context/NotificationContext';
import { api } from '../services/api';
import { formatDate, timeAgo, toDate } from '../utils/format';

const GROUPS = [
  ['all', 'All', null],
  ['stock', 'Stock', ['LOW_STOCK', 'OUT_OF_STOCK', 'STOCK_ALERT', 'STOCK_ALERT_UPDATE']],
  ['customers', 'Customers', ['INACTIVE_CUSTOMER']],
  ['staff', 'Staff', ['LEAVE_REQUEST', 'LEAVE_DECISION', 'CLOCK_IN', 'CLOCK_OUT', 'PASSWORD_RESET']],
  ['sales', 'Sales', ['NEW_SALE']]
];

/* which day-heading a notification goes under */
function dayLabel(value) {
  const d = toDate(value);
  if (!d) return 'Earlier';
  const today = new Date(); today.setHours(0, 0, 0, 0);
  const that = new Date(d); that.setHours(0, 0, 0, 0);
  const diff = Math.round((today - that) / 86400000);
  if (diff <= 0) return 'Today';
  if (diff === 1) return 'Yesterday';
  if (diff < 7) return 'This week';
  return formatDate(d, { day: 'numeric', month: 'long', year: 'numeric' });
}

export default function Notifications() {
  const { isAdmin } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const { items, unread, markRead, markAllRead, refresh } = useNotifications();
  const [group, setGroup] = useState('all');
  const [unreadOnly, setUnreadOnly] = useState(false);
  const [checking, setChecking] = useState(false);

  const types = GROUPS.find((g) => g[0] === group)[2];
  const list = items.filter((n) => (!types || types.includes(n.type)) && (!unreadOnly || !n.seen));
  const sections = useMemo(() => {
    const out = [];
    list.forEach((n) => {
      const label = dayLabel(n.createdAt);
      const last = out[out.length - 1];
      if (last && last.label === label) last.items.push(n); else out.push({ label, items: [n] });
    });
    return out;
  }, [list]);
  const countOf = (t) => items.filter((n) => !n.seen && (!t || t.includes(n.type))).length;

  const runChecks = async () => {
    setChecking(true);
    try {
      await api.notifications.runChecks();
      await refresh();
      toast.success('Checks complete', 'Stock levels and customer activity were checked.');
    } catch (err) {
      toast.error('Could not run checks', err.message);
    } finally {
      setChecking(false);
    }
  };

  return (
    <>
      <PageHeader eyebrow="Inbox" title="Notifications" subtitle="Stock, customers, staff and sales in one place. Stock and customers are checked automatically every hour.">
        {isAdmin && <button className="btn btn-secondary" onClick={runChecks} disabled={checking}><Icon name="refresh" size={15} /> {checking ? 'Checking…' : 'Check now'}</button>}
        <button className="btn btn-primary" onClick={markAllRead} disabled={!unread}><Icon name="check" size={15} /> Mark all as read</button>
      </PageHeader>

      <div className="grid grid-4 mb-4">
        <StatCard icon="bell" tone="violet" label="Unread" value={unread} foot={unread ? 'Waiting for you' : 'All caught up'} />
        <StatCard icon="boxes" tone="amber" label="Stock" value={countOf(GROUPS[1][2])} foot="Unread stock updates" />
        <StatCard icon="users" tone="blue" label="Staff & customers" value={countOf([...GROUPS[2][2], ...GROUPS[3][2]])} foot="Unread people updates" />
        <StatCard icon="receipt" tone="green" label="Sales" value={countOf(GROUPS[4][2])} foot="Unread sales" />
      </div>

      <div className="notif-toolbar">
        <div className="chips">
          {GROUPS.map(([k, l, t]) => (
            <button key={k} className={`chip${group === k ? ' active' : ''}`} onClick={() => setGroup(k)}>{l}{countOf(t) > 0 && <span className="count">{countOf(t)}</span>}</button>
          ))}
        </div>
        <button className={`chip${unreadOnly ? ' active' : ''}`} style={{ marginLeft: 'auto' }} onClick={() => setUnreadOnly((u) => !u)} aria-pressed={unreadOnly}>
          <Icon name={unreadOnly ? 'check' : 'eye'} size={13} /> Unread only
        </button>
      </div>

      {list.length === 0 ? (
        <div className="card"><EmptyState icon="bell" title="Nothing here" message={unreadOnly ? 'No unread notifications in this group.' : "You're all caught up."} /></div>
      ) : sections.map((section) => (
        <section key={section.label} className="notif-section">
          <h3 className="notif-day">{section.label}</h3>
          <div className="notif-list">
            {section.items.map((n) => {
              const meta = NOTIFICATION_META[n.type] || NOTIFICATION_META.SYSTEM;
              return (
                <button key={n.id} type="button" className={`notif-card tone-card-${meta.tone}${n.seen ? '' : ' is-unread'}`}
                  onClick={() => { markRead(n.id); navigate(meta.link || '/'); }}>
                  <span className={`notif-card-ico tone-${meta.tone}`}><Icon name={meta.icon} size={16} /></span>
                  <span className="notif-card-text">
                    <span className="notif-card-title">{n.title}{!n.seen && <span className="new-pill">New</span>}</span>
                    <span className="notif-card-msg">{n.message}</span>
                  </span>
                  <span className="notif-card-side">
                    <span className="notif-card-time">{timeAgo(n.createdAt)}</span>
                    <span className="round-btn"><Icon name="arrowUpRight" size={12} /></span>
                  </span>
                </button>
              );
            })}
          </div>
        </section>
      ))}
    </>
  );
}
