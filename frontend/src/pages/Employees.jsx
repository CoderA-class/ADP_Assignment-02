import { useMemo, useState } from 'react';
import Icon from '../components/Icons';
import SearchBar from '../components/SearchBar';
import { Avatar, Badge, ConfirmDialog, Drawer, EmptyState, ErrorState, Field, IdNotice, ImageUpload, Loading, Modal, PageHeader, StatCard } from '../components/ui';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { useApi } from '../hooks/useApi';
import { api } from '../services/api';
import { formatDate, formatDateTime, fullName, matches, money, titleCase } from '../utils/format';

const STATUS_TONE = { ACTIVE: 'green', SUSPENDED: 'amber', TERMINATED: 'red' };

/* Admin only – add, edit and remove staff and choose who has admin access */
export default function Employees() {
  const { user, updateUser } = useAuth();
  const toast = useToast();
  const { data, loading, error, reload } = useApi(async () => {
    const [employees, attendance] = await Promise.all([api.employees.all(), api.attendance.all()]);
    return { employees, attendance };
  });
  const [query, setQuery] = useState({ text: '', mode: 'name' });
  const [filter, setFilter] = useState('all');
  const [editing, setEditing] = useState(null);
  const [viewing, setViewing] = useState(null);
  const [deleting, setDeleting] = useState(null);

  const employees = data?.employees || [];
  const onShift = useMemo(() => new Set((data?.attendance || []).filter((a) => !a.clockOut).map((a) => a.employeeId)), [data]);

  const rows = useMemo(() => employees
    .filter((e) => filter === 'all' || (filter === 'admin' ? e.accessLevel === 'ADMIN' : filter === 'shift' ? onShift.has(e.empID) : e.empStatus !== 'ACTIVE'))
    .filter((e) => (query.mode === 'id' ? matches(query.text, e.empID) : matches(query.text, fullName(e), e.role, e.email)))
    .sort((a, b) => fullName(a).localeCompare(fullName(b))), [employees, filter, query, onShift]);

  const lookUpId = async (id) => {
    try { setViewing(await api.employees.read(id)); } catch { toast.warning('Not found', `There is no employee with ID ${id}.`); }
  };

  const remove = async (e) => {
    try {
      await api.employees.remove(e.empID);
      toast.success('Employee removed', fullName(e));
      setViewing(null);
      reload({ silent: true });
    } catch (err) {
      toast.error('Could not delete', err.message);
    }
  };

  if (loading) return <Loading />;
  if (error) return <ErrorState error={error} onRetry={reload} />;

  const payroll = employees.filter((e) => e.empStatus === 'ACTIVE').reduce((s, e) => s + (e.salary || 0), 0);

  return (
    <>
      <PageHeader eyebrow="People" title="Employees" subtitle="Add your team, set their login and choose who is an admin.">
        <button className="btn btn-primary" onClick={() => setEditing({})}><Icon name="userPlus" size={17} /> Add employee</button>
      </PageHeader>

      <div className="grid grid-4 mb-4">
        <StatCard icon="badge" tone="blue" label="Employees" value={employees.length} foot={`${employees.filter((e) => e.empStatus === 'ACTIVE').length} active`} />
        <StatCard icon="clock" tone="green" label="On shift now" value={onShift.size} />
        <StatCard icon="shield" tone="violet" label="Admins" value={employees.filter((e) => e.accessLevel === 'ADMIN').length} />
        <StatCard icon="money" tone="cyan" label="Monthly payroll" value={money(payroll)} foot="Active employees" />
      </div>

      <div className="card">
        <div className="toolbar">
          <SearchBar items={employees} entityLabel="employees" getId={(e) => e.empID} getName={fullName} getSub={(e) => e.role} getImage={(e) => e.imageUrl}
            placeholder="Search employees by name or job title…" idPlaceholder="Enter employee ID, e.g. EMP-0001"
            onQueryChange={(text, mode) => setQuery({ text, mode })} onSelect={setViewing} onIdSearch={lookUpId} />
          <div className="chips">
            {[['all', 'All'], ['shift', 'On shift'], ['admin', 'Admins'], ['inactive', 'Suspended / ended']].map(([k, l]) => (
              <button key={k} className={`chip${filter === k ? ' active' : ''}`} onClick={() => setFilter(k)}>{l}</button>
            ))}
          </div>
        </div>
        {rows.length === 0 ? <EmptyState icon="badge" title="No matching employees" message="Try another name or switch the search to ID." /> : (
          <div className="table-wrap">
            <table className="table">
              <thead><tr><th>Employee</th><th>Job title</th><th>Access</th><th>Status</th><th>Contact</th><th className="num">Salary</th><th /></tr></thead>
              <tbody>
                {rows.map((e) => (
                  <tr key={e.empID} className="clickable" onClick={() => setViewing(e)}>
                    <td><div className="cell-entity"><Avatar src={e.imageUrl} name={fullName(e)} /><div><div className="t">{fullName(e)} {onShift.has(e.empID) && <Badge tone="green">On shift</Badge>}</div><div className="s mono">{e.empID}</div></div></div></td>
                    <td>{e.role}</td>
                    <td><Badge tone={e.accessLevel === 'ADMIN' ? 'violet' : 'blue'} plain>{e.accessLevel === 'ADMIN' ? 'Admin' : 'Employee'}</Badge></td>
                    <td><Badge tone={STATUS_TONE[e.empStatus] || 'gray'}>{titleCase(e.empStatus)}</Badge></td>
                    <td><div>{e.email}</div><div className="small muted">{e.phone}</div></td>
                    <td className="num">{money(e.salary)}</td>
                    <td className="actions" onClick={(ev) => ev.stopPropagation()}>
                      <button className="icon-btn" title="Edit" onClick={() => setEditing(e)}><Icon name="edit" size={16} /></button>
                      {e.empID !== user.empID && <button className="icon-btn danger" title="Delete" onClick={() => setDeleting(e)}><Icon name="trash" size={16} /></button>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {editing && <EmployeeForm employee={editing} onClose={() => setEditing(null)} onSaved={(saved) => { reload({ silent: true }); if (viewing) setViewing(saved); if (saved.empID === user.empID) updateUser(saved); }} />}
      {deleting && <ConfirmDialog title="Remove employee?" message={`${fullName(deleting)} (${deleting.empID}) will be deleted and can no longer log in. Tip: set their status to “Terminated” instead to keep their history.`} onConfirm={() => remove(deleting)} onClose={() => setDeleting(null)} />}
      {viewing && <EmployeeDrawer employee={viewing} onShift={onShift.has(viewing.empID)} onClose={() => setViewing(null)} onEdit={() => setEditing(viewing)} />}
    </>
  );
}

function EmployeeDrawer({ employee: e, onShift, onClose, onEdit }) {
  const { data } = useApi(async () => {
    const [shifts, leave] = await Promise.all([api.attendance.forEmployee(e.empID), api.leave.forEmployee(e.empID)]);
    return { shifts, leave };
  }, [e.empID]);
  return (
    <Drawer title="Employee details" onClose={onClose} footer={<button className="btn btn-secondary" onClick={onEdit}><Icon name="edit" size={16} /> Edit</button>}>
      <div className="profile-hero">
        <Avatar src={e.imageUrl} name={fullName(e)} size={84} />
        <h3>{fullName(e)}</h3>
        <div className="row-flex wrap" style={{ justifyContent: 'center' }}>
          <span className="mono muted">{e.empID}</span>
          <Badge tone={e.accessLevel === 'ADMIN' ? 'violet' : 'blue'} plain>{e.accessLevel === 'ADMIN' ? 'Admin' : 'Employee'}</Badge>
          <Badge tone={STATUS_TONE[e.empStatus] || 'gray'}>{titleCase(e.empStatus)}</Badge>
          {onShift && <Badge tone="green">On shift</Badge>}
        </div>
      </div>
      <dl className="detail-list">
        <dt>Job title</dt><dd>{e.role}</dd>
        <dt>Email (login)</dt><dd>{e.email}</dd>
        <dt>Phone</dt><dd>{e.phone || '—'}</dd>
        <dt>Salary</dt><dd>{money(e.salary)} / month</dd>
        <dt>Started</dt><dd>{formatDate(e.hireDate)}</dd>
      </dl>
      <div className="divider" />
      <h4 style={{ fontSize: 14, marginBottom: 8 }}>Recent shifts</h4>
      {(data?.shifts || []).length === 0 ? <p className="small muted">No clock-ins yet.</p> : (data.shifts.slice(0, 5).map((s) => (
        <div key={s.id} className="row-flex between small" style={{ padding: '6px 0', borderBottom: '1px solid var(--border)' }}>
          <span>{formatDateTime(s.clockIn)}</span><span className="muted">{s.clockOut ? `${Math.floor(s.minutesWorked / 60)}h ${s.minutesWorked % 60}m` : 'On shift'}</span>
        </div>
      )))}
      <h4 style={{ fontSize: 14, margin: '16px 0 8px' }}>Leave</h4>
      {(data?.leave || []).length === 0 ? <p className="small muted">No leave requests.</p> : (data.leave.slice(0, 5).map((l) => (
        <div key={l.leaveId} className="row-flex between small" style={{ padding: '6px 0', borderBottom: '1px solid var(--border)' }}>
          <span>{titleCase(l.leaveType)} · {formatDate(l.startDate)} – {formatDate(l.endDate)}</span><Badge tone={LEAVE_TONE[l.status]}>{titleCase(l.status)}</Badge>
        </div>
      )))}
    </Drawer>
  );
}

export const LEAVE_TONE = { PENDING: 'amber', APPROVED: 'green', DECLINED: 'red', CANCELLED: 'gray' };

function EmployeeForm({ employee, onClose, onSaved }) {
  const toast = useToast();
  const editing = Boolean(employee?.empID);
  const [form, setForm] = useState({
    firstName: '', lastName: '', email: '', phone: '', role: '', salary: '', accessLevel: 'EMPLOYEE', empStatus: 'ACTIVE', imageUrl: '', password: '', ...employee
  });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e?.target ? e.target.value : e }));

  const save = async (e) => {
    e?.preventDefault();
    if (!form.firstName.trim()) return setError('Please enter the employee\'s name.');
    if (!form.email.trim()) return setError('An email address is needed – it\'s their login.');
    if (form.password && form.password.length < 6) return setError('The password must be at least 6 characters.');
    setBusy(true);
    setError('');
    try {
      const payload = { ...form, salary: Number(form.salary) || 0, password: form.password || '' };
      const saved = editing ? await api.employees.update(payload) : await api.employees.create({ ...payload, empID: '' });
      toast.success(editing ? 'Employee updated' : 'Employee added', editing ? fullName(saved) : `${fullName(saved)} · ${saved.empID}. They can log in with ${saved.email}.`);
      onSaved?.(saved);
      onClose();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Modal title={editing ? 'Edit employee' : 'Add employee'} subtitle="Name and email are required. The ID is created automatically." onClose={onClose} size="lg"
      footer={<>
        <button className="btn btn-secondary" onClick={onClose} disabled={busy}>Cancel</button>
        <button className="btn btn-primary" onClick={save} disabled={busy}>{busy ? 'Saving…' : editing ? 'Save changes' : 'Add employee'}</button>
      </>}>
      <form className="form-grid" onSubmit={save}>
        <div className="full"><IdNotice id={form.empID} prefix="EMP" /></div>
        <Field full label="Photo"><ImageUpload value={form.imageUrl} onChange={set('imageUrl')} name={form.firstName} round onError={(m) => toast.error('Image', m)} /></Field>
        <Field label="First name" required><input className="input" value={form.firstName} onChange={set('firstName')} autoFocus /></Field>
        <Field label="Last name"><input className="input" value={form.lastName} onChange={set('lastName')} /></Field>
        <Field label="Email (used to log in)" required><input className="input" type="email" value={form.email} onChange={set('email')} placeholder="name@company.co.za" /></Field>
        <Field label="Phone"><input className="input" value={form.phone} onChange={set('phone')} /></Field>
        <Field label="Job title"><input className="input" list="job-titles" value={form.role} onChange={set('role')} placeholder="e.g. Sales Assistant" />
          <datalist id="job-titles">{['Store Manager', 'Sales Assistant', 'Cashier', 'Stock Controller', 'Driver', 'Warehouse Assistant', 'Administrator'].map((r) => <option key={r} value={r} />)}</datalist>
        </Field>
        <Field label="Monthly salary"><div className="input-prefix"><span>R</span><input className="input" type="number" min="0" value={form.salary} onChange={set('salary')} /></div></Field>
        <Field label="App access" hint="Admins can add people, approve leave and manage stock">
          <select className="select" value={form.accessLevel} onChange={set('accessLevel')}>
            <option value="EMPLOYEE">Employee</option>
            <option value="ADMIN">Admin</option>
          </select>
        </Field>
        <Field label="Status">
          <select className="select" value={form.empStatus} onChange={set('empStatus')}>
            <option value="ACTIVE">Active</option>
            <option value="SUSPENDED">Suspended</option>
            <option value="TERMINATED">Terminated</option>
          </select>
        </Field>
        <Field full label={editing ? 'New password' : 'Password'} hint={editing ? 'Leave blank to keep their current password.' : 'Leave blank to use the default password Welcome@123 – they can change it under My Profile.'}>
          <input className="input" type="password" autoComplete="new-password" value={form.password} onChange={set('password')} placeholder={editing ? '••••••••' : 'At least 6 characters'} />
        </Field>
        {error && <div className="full text-danger small">{error}</div>}
        <button type="submit" hidden />
      </form>
    </Modal>
  );
}
