import { useState } from 'react';
import Icon from '../components/Icons';
import { Avatar, Badge, Field, ImageUpload, PageHeader } from '../components/ui';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { api } from '../services/api';
import { formatDate, fullName, titleCase } from '../utils/format';

export default function Profile() {
  const { user, isAdmin, updateUser } = useAuth();
  const toast = useToast();
  const [form, setForm] = useState({ phone: user.phone || '', imageUrl: user.imageUrl || '' });
  const [pw, setPw] = useState({ current: '', next: '', confirm: '' });
  const [busy, setBusy] = useState(false);

  const saveProfile = async (e) => {
    e.preventDefault();
    setBusy(true);
    try {
      const saved = await api.auth.updateMe({ phone: form.phone, imageUrl: form.imageUrl });
      updateUser(saved);
      toast.success('Profile updated');
    } catch (err) {
      toast.error('Could not save', err.message);
    } finally {
      setBusy(false);
    }
  };

  const changePassword = async (e) => {
    e.preventDefault();
    if (pw.next.length < 6) return toast.error('Password too short', 'Use at least 6 characters.');
    if (pw.next !== pw.confirm) return toast.error('Passwords don\'t match', 'Type the new password the same way twice.');
    try {
      await api.auth.changePassword(pw.current, pw.next);
      setPw({ current: '', next: '', confirm: '' });
      toast.success('Password changed');
    } catch (err) {
      toast.error('Could not change password', err.message);
    }
  };

  return (
    <>
      <PageHeader eyebrow="Account" title="My Profile" subtitle="Your details and login." />
      <div className="grid grid-2" style={{ alignItems: 'start' }}>
        <div className="card">
          <div className="card-body">
            <div className="row-flex mb-4">
              <Avatar src={form.imageUrl} name={fullName(user)} size={64} />
              <div>
                <h3 style={{ fontSize: 18 }}>{fullName(user)}</h3>
                <div className="row-flex small mt-1"><span className="mono muted">{user.empID}</span><Badge tone={isAdmin ? 'violet' : 'blue'} plain>{isAdmin ? 'Admin' : 'Employee'}</Badge></div>
              </div>
            </div>
            <dl className="detail-list mb-4">
              <dt>Job title</dt><dd>{user.role}</dd>
              <dt>Email (login)</dt><dd>{user.email}</dd>
              <dt>Status</dt><dd>{titleCase(user.empStatus)}</dd>
              <dt>Started</dt><dd>{formatDate(user.hireDate)}</dd>
            </dl>
            <form className="stack" onSubmit={saveProfile}>
              <Field label="Photo"><ImageUpload value={form.imageUrl} onChange={(v) => setForm((f) => ({ ...f, imageUrl: v }))} name={fullName(user)} round onError={(m) => toast.error('Image', m)} /></Field>
              <Field label="Phone"><input className="input" value={form.phone} onChange={(e) => setForm((f) => ({ ...f, phone: e.target.value }))} /></Field>
              <div><button className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : 'Save profile'}</button></div>
            </form>
          </div>
        </div>
        <div className="card">
          <div className="card-header"><h3><Icon name="lock" size={16} /> Change password</h3></div>
          <form className="card-body stack" onSubmit={changePassword}>
            <Field label="Current password"><input className="input" type="password" autoComplete="current-password" value={pw.current} onChange={(e) => setPw((p) => ({ ...p, current: e.target.value }))} /></Field>
            <Field label="New password" hint="At least 6 characters"><input className="input" type="password" autoComplete="new-password" value={pw.next} onChange={(e) => setPw((p) => ({ ...p, next: e.target.value }))} /></Field>
            <Field label="Confirm new password"><input className="input" type="password" autoComplete="new-password" value={pw.confirm} onChange={(e) => setPw((p) => ({ ...p, confirm: e.target.value }))} /></Field>
            <div><button className="btn btn-primary">Update password</button></div>
          </form>
        </div>
      </div>
    </>
  );
}
