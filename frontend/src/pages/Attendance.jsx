import { useEffect, useMemo, useState } from 'react';
import Icon from '../components/Icons';
import { Avatar, Badge, EmptyState, ErrorState, Loading, PageHeader, StatCard } from '../components/ui';
import { useAuth } from '../context/AuthContext';
import { useApi } from '../hooks/useApi';
import { api } from '../services/api';
import { daysSince, formatDate, formatTime, matches, minutesToHours } from '../utils/format';

export default function Attendance() {
  const { user, isAdmin } = useAuth();
  const { data, loading, error, reload } = useApi(() => (isAdmin ? api.attendance.all() : api.attendance.forEmployee(user.empID)), [isAdmin, user.empID]);
  const [range, setRange] = useState('7');
  const [name, setName] = useState('');

  // the clock in/out button in the top bar fires this event
  useEffect(() => {
    const handler = () => reload({ silent: true });
    window.addEventListener('attendance-changed', handler);
    return () => window.removeEventListener('attendance-changed', handler);
  }, [reload]);

  const shifts = data || [];
  const rows = useMemo(() => shifts
    .filter((s) => range === 'all' || (daysSince(s.clockIn) ?? 999) < Number(range))
    .filter((s) => matches(name, s.employeeName, s.employeeId)), [shifts, range, name]);

  if (loading) return <Loading />;
  if (error) return <ErrorState error={error} onRetry={reload} />;

  const onShift = shifts.filter((s) => !s.clockOut);
  const minutes = rows.reduce((s, r) => s + (r.minutesWorked || 0), 0);
  const todayCount = new Set(shifts.filter((s) => daysSince(s.clockIn) === 0).map((s) => s.employeeId)).size;

  return (
    <>
      <PageHeader eyebrow="Workplace" title={isAdmin ? 'Attendance' : 'My Attendance'} subtitle="Use the Clock in / Clock out button at the top of the screen to start and end a shift." />
      <div className="grid grid-3 mb-4">
        {isAdmin
          ? <StatCard icon="users" tone="green" label="On shift right now" value={onShift.length} foot={onShift.map((s) => s.employeeName).join(', ') || 'Nobody'} />
          : <StatCard icon="clock" tone="green" label="Status" value={onShift.length ? 'On shift' : 'Off shift'} foot={onShift.length ? `Since ${formatTime(onShift[0].clockIn)}` : 'Clock in at the top of the page'} />}
        <StatCard icon="clock" tone="blue" label="Hours in this period" value={minutesToHours(minutes)} />
        <StatCard icon="calendar" tone="violet" label={isAdmin ? 'People in today' : 'Shifts in this period'} value={isAdmin ? todayCount : rows.length} />
      </div>
      <div className="card">
        <div className="toolbar">
          {isAdmin && (
            <div className="input-group" style={{ maxWidth: 320, flex: 1 }}>
              <span className="lead"><Icon name="search" size={17} /></span>
              <input className="input" value={name} onChange={(e) => setName(e.target.value)} placeholder="Filter by employee name…" />
            </div>
          )}
          <div className="chips">
            {[['1', 'Today'], ['7', 'Last 7 days'], ['30', 'Last 30 days'], ['all', 'All']].map(([k, l]) => (
              <button key={k} className={`chip${range === k ? ' active' : ''}`} onClick={() => setRange(k)}>{l}</button>
            ))}
          </div>
        </div>
        {rows.length === 0 ? <EmptyState icon="clock" title="No shifts in this period" message="Clock-ins will appear here." /> : (
          <div className="table-wrap">
            <table className="table">
              <thead><tr>{isAdmin && <th>Employee</th>}<th>Date</th><th>Clock in</th><th>Clock out</th><th className="num">Hours</th><th>Status</th></tr></thead>
              <tbody>
                {rows.map((s) => (
                  <tr key={s.id}>
                    {isAdmin && <td><div className="cell-entity"><Avatar name={s.employeeName} size={32} /><div><div className="t">{s.employeeName}</div><div className="s mono">{s.employeeId}</div></div></div></td>}
                    <td>{formatDate(s.clockIn, { weekday: 'short', day: 'numeric', month: 'short' })}</td>
                    <td className="tabular">{formatTime(s.clockIn)}</td>
                    <td className="tabular">{s.clockOut ? formatTime(s.clockOut) : '—'}</td>
                    <td className="num bold">{minutesToHours(s.minutesWorked)}</td>
                    <td>{s.clockOut ? <Badge tone="gray">Completed</Badge> : <Badge tone="green">On shift</Badge>}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </>
  );
}
