import { useMemo, useState } from 'react';
import Icon from '../components/Icons';
import { LEAVE_TONE } from './Employees';
import { Avatar, Badge, EmptyState, ErrorState, Field, Loading, Modal, PageHeader, StatCard } from '../components/ui';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { useNotifications } from '../context/NotificationContext';
import { useApi } from '../hooks/useApi';
import { api } from '../services/api';
import { formatDate, formatDateTime, titleCase, todayISO } from '../utils/format';

const TYPES = ['ANNUAL', 'SICK', 'FAMILY', 'STUDY', 'UNPAID'];

export default function Leave() {
  const { user, isAdmin } = useAuth();
  const { refresh } = useNotifications();
  const toast = useToast();
  const [tab, setTab] = useState(isAdmin ? 'review' : 'mine');
  const [requesting, setRequesting] = useState(false);
  const [deciding, setDeciding] = useState(null);
  const mine = useApi(() => api.leave.forEmployee(user.empID), [user.empID]);
  const all = useApi(() => (isAdmin ? api.leave.all() : Promise.resolve([])), [isAdmin]);

  const pending = useMemo(() => (all.data || []).filter((l) => l.status === 'PENDING'), [all.data]);
  const reloadAll = () => { mine.reload({ silent: true }); all.reload({ silent: true }); refresh(); };

  const cancel = async (l) => {
    try {
      await api.leave.cancel(l.leaveId);
      toast.success('Request cancelled');
      reloadAll();
    } catch (err) { toast.error('Could not cancel', err.message); }
  };

  if (mine.loading || all.loading) return <Loading />;
  if (mine.error) return <ErrorState error={mine.error} onRetry={reloadAll} />;

  const myList = mine.data || [];
  const year = new Date().getFullYear();
  const usedThisYear = myList.filter((l) => l.status === 'APPROVED' && String(l.startDate).startsWith(String(year))).reduce((s, l) => s + l.days, 0);
  const list = tab === 'mine' ? myList : tab === 'review' ? pending : (all.data || []);

  return (
    <>
      <PageHeader eyebrow="Workplace" title={isAdmin ? 'Leave Requests' : 'My Leave'} subtitle={isAdmin ? 'Approve or decline your team\'s leave. Employees are notified straight away.' : 'Request time off. You\'ll get a notification when it\'s approved.'}>
        <button className="btn btn-primary" onClick={() => setRequesting(true)}><Icon name="calendar" size={17} /> Request leave</button>
      </PageHeader>

      <div className="grid grid-3 mb-4">
        {isAdmin
          ? <StatCard icon="alert" tone="amber" label="Waiting for approval" value={pending.length} />
          : <StatCard icon="alert" tone="amber" label="My pending requests" value={myList.filter((l) => l.status === 'PENDING').length} />}
        <StatCard icon="checkCircle" tone="green" label={`Days approved in ${year}`} value={usedThisYear} foot="Your own leave" />
        <StatCard icon="calendar" tone="blue" label={isAdmin ? 'All requests' : 'My requests'} value={isAdmin ? (all.data || []).length : myList.length} />
      </div>

      <div className="card">
        {isAdmin && (
          <div className="tabs">
            <button className={`tab${tab === 'review' ? ' active' : ''}`} onClick={() => setTab('review')}>To review {pending.length > 0 && <Badge tone="amber" plain>{pending.length}</Badge>}</button>
            <button className={`tab${tab === 'all' ? ' active' : ''}`} onClick={() => setTab('all')}>All requests</button>
            <button className={`tab${tab === 'mine' ? ' active' : ''}`} onClick={() => setTab('mine')}>My leave</button>
          </div>
        )}
        {list.length === 0 ? (
          <EmptyState icon="calendar" title={tab === 'review' ? 'Nothing to review' : 'No leave requests yet'} message={tab === 'review' ? 'New requests will show up here and in your notifications.' : 'Use “Request leave” to plan time off.'} />
        ) : (
          <div className="table-wrap">
            <table className="table">
              <thead><tr>{tab !== 'mine' && <th>Employee</th>}<th>Type</th><th>Dates</th><th className="num">Days</th><th>Reason</th><th>Status</th><th /></tr></thead>
              <tbody>
                {list.map((l) => (
                  <tr key={l.leaveId}>
                    {tab !== 'mine' && <td><div className="cell-entity"><Avatar name={l.employeeName} size={32} /><div><div className="t">{l.employeeName}</div><div className="s mono">{l.employeeId}</div></div></div></td>}
                    <td>{titleCase(l.leaveType)}</td>
                    <td>{formatDate(l.startDate)} – {formatDate(l.endDate)}<div className="small muted">Requested {formatDateTime(l.requestedAt)}</div></td>
                    <td className="num bold">{l.days}</td>
                    <td style={{ maxWidth: 260 }}>{l.reason || <span className="muted">—</span>}{l.reviewComment && <div className="small muted">Note: {l.reviewComment}</div>}</td>
                    <td><Badge tone={LEAVE_TONE[l.status]}>{titleCase(l.status)}</Badge>{l.reviewedBy && <div className="small muted">by {l.reviewedBy}</div>}</td>
                    <td className="actions">
                      {isAdmin && l.status === 'PENDING' && l.employeeId !== user.empID && <>
                        <button className="btn btn-success btn-sm" onClick={() => setDeciding({ leave: l, approve: true })}><Icon name="check" size={14} /> Approve</button>{' '}
                        <button className="btn btn-soft-danger btn-sm" onClick={() => setDeciding({ leave: l, approve: false })}>Decline</button>
                      </>}
                      {l.employeeId === user.empID && l.status === 'PENDING' && <button className="btn btn-ghost btn-sm" onClick={() => cancel(l)}>Cancel</button>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {requesting && <RequestLeaveModal onClose={() => setRequesting(false)} onSaved={() => { reloadAll(); setTab('mine'); }} />}
      {deciding && <DecisionModal {...deciding} onClose={() => setDeciding(null)} onSaved={reloadAll} />}
    </>
  );
}

function RequestLeaveModal({ onClose, onSaved }) {
  const { user } = useAuth();
  const toast = useToast();
  const [form, setForm] = useState({ leaveType: 'ANNUAL', startDate: todayISO(), endDate: todayISO(), reason: '' });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));
  const days = form.startDate && form.endDate ? Math.round((new Date(form.endDate) - new Date(form.startDate)) / 86400000) + 1 : 0;

  const save = async (e) => {
    e?.preventDefault();
    if (days <= 0) return setError('The end date must be on or after the start date.');
    setBusy(true);
    setError('');
    try {
      await api.leave.request({ ...form, employeeId: user.empID });
      toast.success('Leave requested', 'Your manager has been notified.');
      onSaved?.();
      onClose();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Modal title="Request leave" onClose={onClose}
      footer={<>
        <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
        <button className="btn btn-primary" onClick={save} disabled={busy}>{busy ? 'Sending…' : `Request ${days > 0 ? days : ''} day${days === 1 ? '' : 's'}`}</button>
      </>}>
      <form className="form-grid" onSubmit={save}>
        <Field full label="Type of leave">
          <div className="chips">{TYPES.map((t) => <button type="button" key={t} className={`chip${form.leaveType === t ? ' active' : ''}`} onClick={() => setForm((f) => ({ ...f, leaveType: t }))}>{titleCase(t)}</button>)}</div>
        </Field>
        <Field label="From" required><input className="input" type="date" value={form.startDate} onChange={set('startDate')} /></Field>
        <Field label="Until (inclusive)" required><input className="input" type="date" value={form.endDate} min={form.startDate} onChange={set('endDate')} /></Field>
        <Field full label="Reason" hint="Optional"><textarea className="textarea" value={form.reason} onChange={set('reason')} placeholder="e.g. Family holiday" /></Field>
        {error && <div className="full text-danger small">{error}</div>}
        <button type="submit" hidden />
      </form>
    </Modal>
  );
}

function DecisionModal({ leave, approve, onClose, onSaved }) {
  const { user } = useAuth();
  const toast = useToast();
  const [comment, setComment] = useState('');
  const [busy, setBusy] = useState(false);
  const save = async () => {
    setBusy(true);
    try {
      if (approve) await api.leave.approve(leave.leaveId, user.empID, comment);
      else await api.leave.decline(leave.leaveId, user.empID, comment);
      toast.success(approve ? 'Leave approved' : 'Leave declined', `${leave.employeeName} has been notified.`);
      onSaved?.();
      onClose();
    } catch (err) {
      toast.error('Could not update', err.message);
    } finally {
      setBusy(false);
    }
  };
  return (
    <Modal title={approve ? 'Approve leave' : 'Decline leave'} subtitle={`${leave.employeeName} · ${titleCase(leave.leaveType)} · ${formatDate(leave.startDate)} – ${formatDate(leave.endDate)} (${leave.days} day${leave.days === 1 ? '' : 's'})`} onClose={onClose} size="sm"
      footer={<>
        <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
        <button className={`btn ${approve ? 'btn-success' : 'btn-danger'}`} onClick={save} disabled={busy}>{busy ? 'Saving…' : approve ? 'Approve' : 'Decline'}</button>
      </>}>
      <Field label="Note to the employee" hint="Optional">
        <textarea className="textarea" value={comment} onChange={(e) => setComment(e.target.value)} placeholder={approve ? 'Enjoy your time off!' : 'e.g. We are short-staffed that week'} />
      </Field>
    </Modal>
  );
}
