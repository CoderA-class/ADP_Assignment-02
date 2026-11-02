import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Icon from '../components/Icons';
import { NotificationRow } from '../components/Layout';
import { EmptyState, PageHeader } from '../components/ui';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { NOTIFICATION_META, useNotifications } from '../context/NotificationContext';
import { api } from '../services/api';

const GROUPS = [
  ['all', 'All', null],
  ['stock', 'Stock', ['LOW_STOCK', 'OUT_OF_STOCK']],
  ['customers', 'Customers', ['INACTIVE_CUSTOMER']],
  ['staff', 'Staff', ['LEAVE_REQUEST', 'LEAVE_DECISION', 'CLOCK_IN', 'CLOCK_OUT']],
  ['sales', 'Sales', ['NEW_SALE']]
];

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
      <PageHeader eyebrow="Inbox" title="Notifications" subtitle="Low stock, inactive customers, leave requests and clock-ins. Stock and customers are checked automatically every hour.">
        {isAdmin && <button className="btn btn-secondary" onClick={runChecks} disabled={checking}><Icon name="refresh" size={16} /> {checking ? 'Checking…' : 'Check now'}</button>}
        <button className="btn btn-primary" onClick={markAllRead} disabled={!unread}><Icon name="check" size={16} /> Mark all as read</button>
      </PageHeader>
      <div className="card">
        <div className="toolbar">
          <div className="chips">
            {GROUPS.map(([k, l, t]) => {
              const count = items.filter((n) => !n.seen && (!t || t.includes(n.type))).length;
              return <button key={k} className={`chip${group === k ? ' active' : ''}`} onClick={() => setGroup(k)}>{l}{count > 0 && <span className="count">{count}</span>}</button>;
            })}
          </div>
          <label className="row-flex small" style={{ marginLeft: 'auto', cursor: 'pointer' }}>
            <input type="checkbox" checked={unreadOnly} onChange={(e) => setUnreadOnly(e.target.checked)} /> Unread only
          </label>
        </div>
        {list.length === 0 ? <EmptyState icon="bell" title="Nothing here" message="You're all caught up." /> : list.map((n) => (
          <NotificationRow key={n.id} n={n} onClick={() => { markRead(n.id); navigate(NOTIFICATION_META[n.type]?.link || '/'); }} />
        ))}
      </div>
    </>
  );
}
