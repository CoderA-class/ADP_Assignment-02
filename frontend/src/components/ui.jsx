import { useEffect, useRef, useState } from 'react';
import Icon from './Icons';
import { categoryColor, categoryIcon, colorFor, initials } from '../utils/format';
import { resizeImage } from '../utils/image';
import { Sparkline } from './Charts';

/* People get initials; square avatars (products, suppliers) get a tinted icon tile */
export function Avatar({ src, name, size = 36, square = false, icon = 'box', tint }) {
  const c = tint || colorFor(name);
  const style = { width: size, height: size, fontSize: Math.max(11, size * 0.38), background: src ? '#e2e8f0' : square ? `${c}1a` : c, color: square ? c : '#fff' };
  return (
    <span className={`avatar${square ? ' square' : ''}`} style={style} title={name}>
      {src ? <img src={src} alt={name} /> : square ? <Icon name={icon} size={Math.round(size * 0.5)} /> : initials(name)}
    </span>
  );
}

/* Product picture, or a tile tinted by category when no picture has been uploaded yet */
export function ProductImage({ src, name, category, iconSize = 28, label = iconSize >= 24 }) {
  if (src) return <img src={src} alt={name} />;
  const c = categoryColor(category);
  return (
    <div className="placeholder-img" style={{ background: `${c}17`, color: c }}>
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', width: '100%' }}>
        {iconSize >= 20
          ? <span className="ph-icon" style={label ? undefined : { width: 60 }}><Icon name={categoryIcon(category)} size={iconSize} /></span>
          : <Icon name={categoryIcon(category)} size={iconSize} />}
        {label && <span className="placeholder-label">{category || 'Product'}</span>}
      </div>
    </div>
  );
}

/* Thin bar showing how healthy a stock level is compared to its re-order level */
export function StockMeter({ product }) {
  const reorder = Math.max(product.reorderLevel || 10, 1);
  const pct = Math.min(100, Math.round((product.quantity / (reorder * 4)) * 100));
  const color = product.quantity <= 0 ? 'var(--danger)' : product.quantity <= reorder ? 'var(--warning)' : 'var(--success)';
  return <div className="progress" title={`${product.quantity} in stock`}><span style={{ width: `${Math.max(pct, product.quantity > 0 ? 4 : 0)}%`, background: color }} /></div>;
}

export function Badge({ tone = 'gray', children, plain = false }) {
  return <span className={`badge badge-${tone}${plain ? ' plain' : ''}`}>{children}</span>;
}

export function PageHeader({ title, subtitle, eyebrow, children }) {
  return (
    <div className="page-header">
      <div>
        {eyebrow && <div className="eyebrow">{eyebrow}</div>}
        <h1>{title}</h1>
        {subtitle && <p>{subtitle}</p>}
      </div>
      {children && <div className="page-actions">{children}</div>}
    </div>
  );
}

/* trend: number (% change) – shows a green/red arrow chip. spark: array of numbers for a mini chart */
export function StatCard({ icon, tone = 'blue', label, value, foot, trend, trendLabel, spark, sparkColor }) {
  return (
    <div className={`card stat-card${spark ? ' has-spark' : ''}`}>
      <div className={`stat-icon tone-${tone}`}><Icon name={icon} size={22} /></div>
      <div style={{ minWidth: 0 }}>
        <div className="stat-label">{label}</div>
        <div className="stat-value">{value}</div>
        {trend !== undefined && trend !== null && <TrendChip value={trend} label={trendLabel} />}
        {foot && <div className="stat-foot">{foot}</div>}
      </div>
      {spark && <div className="stat-spark"><Sparkline data={spark} width={120} height={40} color={sparkColor || 'var(--ink)'} /></div>}
    </div>
  );
}

export function TrendChip({ value, label }) {
  if (!Number.isFinite(value)) return <span className="trend flat">New {label ? `· ${label}` : ''}</span>;
  const dir = value > 0.5 ? 'up' : value < -0.5 ? 'down' : 'flat';
  return (
    <span className={`trend ${dir}`}>
      <Icon name={dir === 'up' ? 'trendUp' : dir === 'down' ? 'trendDown' : 'minus'} size={11} /> {Math.abs(value).toFixed(0)}%{label ? ` ${label}` : ''}
    </span>
  );
}

/* A sticky note. tilt in degrees; pinned shows a thumbtack instead of tape */
export function StickyNote({ color = 'yellow', tilt = -1.5, pinned = false, icon, title, children, foot, className = '', style }) {
  return (
    <div className={`sticky ${color}${pinned ? ' pinned' : ''} ${className}`} style={{ '--tilt': `${tilt}deg`, ...style }}>
      {pinned && <span className="pin"><Icon name="pinNote" size={20} /></span>}
      {title && <h4>{icon && <Icon name={icon} size={15} />}{title}</h4>}
      {children}
      {foot && <div className="sticky-foot">{foot}</div>}
    </div>
  );
}

/* Small yellow tip note used on forms and pages */
export function TipNote({ children, icon = 'lightbulb', style }) {
  return <div className="tip-note" style={style}><Icon name={icon} size={15} /><div>{children}</div></div>;
}

export function EmptyState({ icon = 'box', title, message, action }) {
  return (
    <div className="empty">
      <div className="e-icon"><Icon name={icon} size={26} /></div>
      <h4>{title}</h4>
      {message && <p>{message}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
}

export function Loading({ label = 'Loading…' }) {
  return (
    <div className="loading-block">
      <div className="row-flex muted"><div className="spinner" /> {label}</div>
    </div>
  );
}

export function ErrorState({ error, onRetry }) {
  return (
    <EmptyState
      icon={error?.offline ? 'wifiOff' : 'alert'}
      title={error?.offline ? 'Server not reachable' : 'Something went wrong'}
      message={error?.message}
      action={onRetry && <button className="btn btn-secondary" onClick={() => onRetry()}><Icon name="refresh" size={16} /> Try again</button>}
    />
  );
}

function useEscape(onClose) {
  useEffect(() => {
    const handler = (e) => { if (e.key === 'Escape') onClose?.(); };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [onClose]);
}

export function Modal({ title, subtitle, onClose, children, footer, size = '' }) {
  useEscape(onClose);
  return (
    <div className="overlay" onMouseDown={(e) => { if (e.target === e.currentTarget) onClose?.(); }}>
      <div className={`modal ${size}`} role="dialog" aria-modal="true" aria-label={title}>
        <div className="modal-header">
          <div>
            <h2>{title}</h2>
            {subtitle && <p>{subtitle}</p>}
          </div>
          <button className="icon-btn" onClick={onClose} aria-label="Close"><Icon name="x" /></button>
        </div>
        <div className="modal-body">{children}</div>
        {footer && <div className="modal-footer">{footer}</div>}
      </div>
    </div>
  );
}

export function Drawer({ title, onClose, children, footer }) {
  useEscape(onClose);
  return (
    <>
      <div className="drawer-overlay" onClick={onClose} />
      <aside className="drawer" role="dialog" aria-label={title}>
        <div className="drawer-header">
          <h3 style={{ fontSize: 16 }}>{title}</h3>
          <button className="icon-btn" onClick={onClose} aria-label="Close"><Icon name="x" /></button>
        </div>
        <div className="drawer-body">{children}</div>
        {footer && <div className="drawer-footer">{footer}</div>}
      </aside>
    </>
  );
}

export function ConfirmDialog({ title, message, confirmLabel = 'Delete', tone = 'danger', onConfirm, onClose }) {
  const [busy, setBusy] = useState(false);
  const run = async () => {
    setBusy(true);
    try { await onConfirm(); onClose(); } finally { setBusy(false); }
  };
  return (
    <Modal title={title} onClose={onClose} size="sm"
      footer={<>
        <button className="btn btn-secondary" onClick={onClose} disabled={busy}>Cancel</button>
        <button className={`btn btn-${tone}`} onClick={run} disabled={busy}>{busy ? 'Working…' : confirmLabel}</button>
      </>}>
      <p className="muted">{message}</p>
    </Modal>
  );
}

export function Field({ label, required, hint, full, children }) {
  return (
    <div className={`field${full ? ' full' : ''}`}>
      {label && <label>{label}{required && <span className="req">*</span>}</label>}
      {children}
      {hint && <span className="hint">{hint}</span>}
    </div>
  );
}

/* Shows that the ID will be created automatically (or the existing ID when editing) */
export function IdNotice({ id, prefix }) {
  return (
    <span className="id-chip">
      <Icon name="hash" size={13} />
      {id ? <>ID <strong className="mono">{id}</strong></> : <>ID will be generated automatically ({prefix}-0001, {prefix}-0002 …)</>}
    </span>
  );
}

export function ImageUpload({ value, onChange, name, round = false, onError }) {
  const inputRef = useRef(null);
  const pick = async (file) => {
    if (!file) return;
    try {
      onChange(await resizeImage(file));
    } catch (err) {
      onError?.(err.message);
    }
  };
  return (
    <div className="image-upload">
      <div className={`preview${round ? ' round' : ''}`} onClick={() => inputRef.current?.click()} title="Upload a picture"
        onDragOver={(e) => e.preventDefault()} onDrop={(e) => { e.preventDefault(); pick(e.dataTransfer.files?.[0]); }}>
        {value ? <img src={value} alt={name || 'Preview'} /> : <Icon name="image" size={28} />}
      </div>
      <div>
        <div className="row-flex wrap">
          <button type="button" className="btn btn-secondary btn-sm" onClick={() => inputRef.current?.click()}>
            <Icon name="upload" size={15} /> {value ? 'Change picture' : 'Upload picture'}
          </button>
          {value && <button type="button" className="btn btn-ghost btn-sm" onClick={() => onChange('')}>Remove</button>}
        </div>
        <div className="hint small muted mt-1">JPG or PNG. Click or drag an image here. It's resized automatically.</div>
      </div>
      <input ref={inputRef} type="file" accept="image/*" hidden onChange={(e) => { pick(e.target.files?.[0]); e.target.value = ''; }} />
    </div>
  );
}

export function Highlight({ text = '', query = '' }) {
  const q = query.trim();
  if (!q) return text;
  const idx = String(text).toLowerCase().indexOf(q.toLowerCase());
  if (idx < 0) return text;
  return <>{text.slice(0, idx)}<mark>{text.slice(idx, idx + q.length)}</mark>{text.slice(idx + q.length)}</>;
}
