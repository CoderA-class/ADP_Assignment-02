import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import Icon from '../components/Icons';
import Logo from '../components/Logo';
import { APP_NAME } from '../components/Layout';
import { useAuth } from '../context/AuthContext';
import { useBackendStatus } from '../hooks/useBackendStatus';
import { Field, Modal, StickyNote, TipNote } from '../components/ui';
import { api } from '../services/api';


export default function Login() {
  const { user, login, sessionExpired, signOutReason } = useAuth();
  const [forgot, setForgot] = useState(false);
  const navigate = useNavigate();
  const online = useBackendStatus();
  const [portal, setPortal] = useState('EMPLOYEE');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(true);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  if (user) return <Navigate to="/" replace />;

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    if (!email.trim() || !password) {
      setError('Please enter your email and password.');
      return;
    }
    setBusy(true);
    try {
      await login(email.trim(), password, rememberMe, portal === 'ADMIN');
      navigate('/', { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="login-page">
      <aside className="login-aside">
        <div className="row-flex">
          <div className="brand-logo"><Logo size={22} /></div>
          <div>
            <div className="brand-name">{APP_NAME}</div>
            <div className="brand-sub">Wholesale &amp; spaza supply</div>
          </div>
        </div>
        <div>
          <h1>Stock, sales and trade accounts for your wholesale.</h1>
          <p className="lead">Built for wholesalers who supply spaza shops, tuck shops and corner stores, from the counter to the store room.</p>
          <div className="login-notes" aria-hidden="true">
            <StickyNote color="yellow" tilt={-3} pinned icon="cart" title="Counter sales" style={{ left: 0, top: 18 }}>
              <p>Ring up a shop's order with VAT worked out. Cash, card, EFT or on account.</p>
            </StickyNote>
            <StickyNote color="pink" tilt={2.5} icon="bell" title="Stock alerts" style={{ left: 170, top: 0 }}>
              <p>Low and out-of-stock lines are flagged before they run out.</p>
            </StickyNote>
            <StickyNote color="mint" tilt={-2} pinned icon="clipboard" title="Trade accounts" style={{ left: 120, top: 150 }}>
              <p>Let regular shops buy now, pay later, and record what they pay off.</p>
            </StickyNote>
          </div>
        </div>
        <div className="login-foot">© {new Date().getFullYear()} {APP_NAME} · Wholesale &amp; spaza supply</div>
      </aside>

      <main className="login-main">
        <form className="login-card" onSubmit={submit}>
          <h2>Sign in</h2>
          <p className="muted mt-1">Choose your account type and use your work email.</p>

          <div className="role-switch" role="tablist">
            <button type="button" className={portal === 'EMPLOYEE' ? 'active' : ''} onClick={() => setPortal('EMPLOYEE')}><Icon name="userTie" size={15} /> Employee</button>
            <button type="button" className={portal === 'ADMIN' ? 'active' : ''} onClick={() => setPortal('ADMIN')}><Icon name="shield" size={15} /> Admin</button>
          </div>

          {signOutReason === 'SUSPENDED' && (
            <div className="banner" style={{ borderRadius: 14, marginBottom: 14, background: 'var(--danger-50)', color: '#8a2a24' }}>
              <Icon name="lock" size={16} />
              <span>Your account has been suspended, so you were signed out. Please speak to an admin.</span>
            </div>
          )}
          {sessionExpired && signOutReason !== 'SUSPENDED' && online !== false && (
            <div className="banner" style={{ borderRadius: 14, marginBottom: 14, background: 'var(--accent-50)', color: '#8a3320' }}>
              <Icon name="lock" size={16} />
              <span>Your session ended. Please sign in again.</span>
            </div>
          )}
          {online === false && (
            <div className="banner offline" style={{ borderRadius: 14, marginBottom: 14, marginRight: 0 }}>
              <div className="spinner" style={{ width: 16, height: 16 }} />
              <span>Waiting for the server to start… you can log in as soon as it's up.</span>
            </div>
          )}

          <div className="stack" style={{ gap: 14 }}>
            <div className="field">
              <label htmlFor="email">Email address</label>
              <div className="input-group">
                <span className="lead"><Icon name="mail" size={17} /></span>
                <input id="email" className="input" type="email" autoComplete="username" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="you@company.co.za" autoFocus />
              </div>
            </div>
            <div className="field">
              <label htmlFor="password">Password</label>
              <div className="input-group">
                <span className="lead"><Icon name="lock" size={17} /></span>
                <input id="password" className="input" type={showPassword ? 'text' : 'password'} autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="••••••••" style={{ paddingRight: 40 }} />
                <button type="button" className="icon-btn" style={{ position: 'absolute', right: 2 }} onClick={() => setShowPassword((s) => !s)} aria-label="Show password"><Icon name="eye" size={17} /></button>
              </div>
            </div>
            <label className="row-flex small" style={{ cursor: 'pointer', color: 'var(--text-2)' }}>
              <input type="checkbox" checked={rememberMe} onChange={(e) => setRememberMe(e.target.checked)} /> Keep me signed in on this computer
              <button type="button" className="link-btn" style={{ marginLeft: 'auto' }} onClick={(e) => { e.preventDefault(); setForgot(true); }}>Forgot password?</button>
            </label>
            {error && <div className="text-danger small row-flex"><Icon name="alert" size={15} /> {error}</div>}
            <button className="btn btn-primary btn-lg btn-block" disabled={busy || online === false}>
              {busy ? 'Signing in…' : <>Sign in as {portal === 'ADMIN' ? 'Admin' : 'Employee'} <Icon name="login" size={17} /></>}
            </button>
          </div>

          <TipNote style={{ marginTop: 22 }}>
            <strong>First time?</strong> The backend creates an admin account on first start – the login details are printed in the IntelliJ console and in the README. Admins add employees (and their passwords) under <em>Employees</em>.
          </TipNote>
        </form>
      </main>
      {forgot && <ForgotPassword initialEmail={email} onClose={() => setForgot(false)} />}
    </div>
  );
}

/* "Forgot password?" – there is no email server, so the request goes to the admins,
   who reset it and give the employee a temporary password */
function ForgotPassword({ initialEmail, onClose }) {
  const [email, setEmail] = useState(initialEmail || '');
  const [sent, setSent] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const send = async (e) => {
    e?.preventDefault();
    if (!email.trim()) return setError('Enter the email you sign in with.');
    setBusy(true);
    setError('');
    try {
      const res = await api.auth.forgotPassword(email.trim());
      setSent(res.message);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Modal title="Forgot your password?" subtitle="We'll ask an admin to reset it for you." onClose={onClose} size="sm"
      footer={sent
        ? <button className="btn btn-primary" onClick={onClose}>Back to sign in</button>
        : <>
          <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={send} disabled={busy}>{busy ? 'Sending…' : 'Ask an admin'}</button>
        </>}>
      {sent ? (
        <div className="stack" style={{ gap: 12 }}>
          <div className="success-check"><Icon name="check" size={26} /></div>
          <p style={{ textAlign: 'center' }}>{sent}</p>
          <TipNote>When you sign in with the temporary password, you'll be asked to choose a new one straight away.</TipNote>
        </div>
      ) : (
        <form onSubmit={send} className="stack" style={{ gap: 12 }}>
          <Field label="Work email"><input className="input" type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="you@company.co.za" autoFocus /></Field>
          {error && <div className="text-danger small">{error}</div>}
          <button type="submit" hidden />
        </form>
      )}
    </Modal>
  );
}
