import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import Icon from '../components/Icons';
import { APP_NAME } from '../components/Layout';
import { useAuth } from '../context/AuthContext';
import { useBackendStatus } from '../hooks/useBackendStatus';
import { Sparkline } from '../components/Charts';

export default function Login() {
  const { user, login, sessionExpired } = useAuth();
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
          <div className="brand-logo"><Icon name="layers" size={19} /></div>
          <div>
            <div className="brand-name">{APP_NAME}</div>
            <div className="brand-sub">Wholesale Manager</div>
          </div>
        </div>
        <div>
          <h1>Stock, sales and staff — <em>one counter</em> for the whole warehouse.</h1>
          <p>Ring up orders, keep an eye on stock levels and look after your customers and team.</p>
          <div className="login-preview" aria-hidden="true">
            <div className="preview-card a">
              <div className="k">Sales today</div>
              <div className="v">R 12 480,00</div>
              <span className="trend up">▲ 18% vs yday</span>
              <div className="mt-2"><Sparkline data={[3, 5, 4, 7, 6, 9, 8, 11, 10, 14]} width={250} height={46} /></div>
            </div>
            <div className="preview-card b">
              <div className="row-flex" style={{ gap: 12 }}>
                <div className="stat-icon tone-amber" style={{ width: 38, height: 38 }}><Icon name="alert" size={18} /></div>
                <div>
                  <div className="k">Low stock</div>
                  <div style={{ fontWeight: 700 }}>Cake Flour 10kg</div>
                  <div className="small muted">9 left · re-order at 10</div>
                </div>
              </div>
            </div>
          </div>
          <div className="login-features" style={{ marginTop: 28 }}>
            <div className="login-feature"><span><Icon name="cart" /></span> Sales with automatic totals and receipts</div>
            <div className="login-feature"><span><Icon name="bell" /></span> Alerts for low stock, quiet customers and leave</div>
            <div className="login-feature"><span><Icon name="search" /></span> Find anything by name – IDs are made for you</div>
          </div>
        </div>
        <div className="small" style={{ color: '#7f8db0' }}>© {new Date().getFullYear()} {APP_NAME}</div>
      </aside>

      <main className="login-main">
        <form className="login-card" onSubmit={submit}>
          <div className="eyebrow">Sign in</div>
          <h2>Welcome back.</h2>
          <p className="muted mt-1">Pick your area, then sign in with your work email.</p>

          <div className="role-switch" role="tablist">
            <button type="button" className={portal === 'EMPLOYEE' ? 'active' : ''} onClick={() => setPortal('EMPLOYEE')}><Icon name="user" size={16} /> Employee</button>
            <button type="button" className={portal === 'ADMIN' ? 'active' : ''} onClick={() => setPortal('ADMIN')}><Icon name="shield" size={16} /> Admin</button>
          </div>

          {sessionExpired && online !== false && (
            <div className="banner" style={{ borderRadius: 10, marginBottom: 14, background: 'var(--accent-50)', border: '1px solid #f3dca8', color: '#7a4b00' }}>
              <Icon name="lock" size={16} />
              <span>Your session ended. Please sign in again.</span>
            </div>
          )}
          {online === false && (
            <div className="banner offline" style={{ borderRadius: 10, marginBottom: 14, border: '1px solid #fed7aa' }}>
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
            </label>
            {error && <div className="text-danger small row-flex"><Icon name="alert" size={15} /> {error}</div>}
            <button className="btn btn-primary btn-lg btn-block" disabled={busy || online === false}>
              {busy ? 'Signing in…' : <>Sign in as {portal === 'ADMIN' ? 'Admin' : 'Employee'} <Icon name="login" size={17} /></>}
            </button>
          </div>

          <div className="demo-box">
            <strong>First time?</strong> The backend creates an admin account on first start – the login details are printed in the IntelliJ console and in the README. Admins add employees (and their passwords) under <em>Employees</em>.
          </div>
        </form>
      </main>
    </div>
  );
}
