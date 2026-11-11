import { useEffect, useRef, useState } from 'react';
import Icon from './Icons';
import { Avatar, Badge, Field, Modal, ProductImage } from './ui';
import { useToast } from '../context/ToastContext';
import { api } from '../services/api';
import { categoryColor, categoryIcon, stockStatus, timeAgo } from '../utils/format';

/* Stock alerts: an employee tells the admins a product is running low.
   OPEN → ACKNOWLEDGED ("on it") → RESOLVED (restocked, or closed by an admin) */

export const isActiveAlert = (a) => a && a.status !== 'RESOLVED';
export const activeAlertFor = (alerts, productId) => (alerts || []).find((a) => a.productId === productId && isActiveAlert(a));

const STATUS = {
  OPEN: { label: 'Waiting for admin', tone: 'amber', icon: 'bullhorn' },
  ACKNOWLEDGED: { label: 'Admin is on it', tone: 'blue', icon: 'clock' },
  RESOLVED: { label: 'Sorted', tone: 'green', icon: 'checkCircle' }
};

export function AlertStatus({ alert }) {
  const s = STATUS[alert.status] || STATUS.OPEN;
  return <Badge tone={s.tone}><Icon name={s.icon} size={11} /> {s.label}</Badge>;
}

/* Small pill shown on a product card / row */
export function AlertPill({ alert, isAdmin }) {
  if (!alert) return null;
  const on = alert.status === 'ACKNOWLEDGED';
  return (
    <span className={`alert-pill${on ? ' on' : ''}`} title={`${alert.raisedByName} · ${timeAgo(alert.createdAt)}`}>
      <Icon name={on ? 'clock' : 'bullhorn'} size={11} /> {isAdmin ? (on ? 'Re-order in progress' : 'Staff alert') : (on ? 'Admin is on it' : 'Admin alerted')}
    </span>
  );
}

/* Button an employee clicks on a low-stock product */
export function AlertAdminButton({ product, alert, onClick, size = 'sm', block = false }) {
  if (stockStatus(product).key === 'ok') return null;
  if (alert) return <AlertPill alert={alert} />;
  return (
    <button type="button" className={`btn btn-alert btn-${size}${block ? ' btn-block' : ''}`} onClick={(e) => { e.stopPropagation(); onClick(product); }}>
      <Icon name="bullhorn" size={13} /> Alert admin
    </button>
  );
}

export function AlertAdminModal({ product, onClose, onSent }) {
  const toast = useToast();
  const [note, setNote] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const s = stockStatus(product);
  const quick = ['A customer is asking for it', 'Shelf is empty', 'Big order coming up', 'Last few left in the store room'];

  const send = async (e) => {
    e?.preventDefault();
    setBusy(true);
    setError('');
    try {
      const alert = await api.stockAlerts.raise(product.id, note);
      toast.success('Admin alerted', `The admins know ${product.name} is running low. You'll be notified when it's sorted.`);
      onSent?.(alert);
      onClose();
    } catch (err) {
      if (err.status === 409) {
        toast.info('Already reported', err.message);
        onSent?.(null);
        onClose();
      } else {
        setError(err.message);
      }
    } finally {
      setBusy(false);
    }
  };

  return (
    <Modal title="Alert the admin" subtitle="Let an admin know this product needs re-ordering." onClose={onClose} size="sm"
      footer={<>
        <button className="btn btn-secondary" onClick={onClose} disabled={busy}>Cancel</button>
        <button className="btn btn-alert" onClick={send} disabled={busy}><Icon name="bullhorn" size={14} /> {busy ? 'Sending…' : 'Send alert'}</button>
      </>}>
      <form onSubmit={send} className="stack" style={{ gap: 14 }}>
        <div className="alert-product">
          <div className="alert-product-img"><ProductImage src={product.imageUrl} name={product.name} category={product.category} iconSize={20} label={false} /></div>
          <div style={{ minWidth: 0 }}>
            <div className="bold">{product.name}</div>
            <div className="small muted"><span className="mono">{product.id}</span> · {product.quantity} left · re-order at {product.reorderLevel}</div>
          </div>
          <Badge tone={s.tone}>{s.label}</Badge>
        </div>
        <Field label="Add a note" hint="Optional – anything the admin should know">
          <textarea className="textarea" value={note} maxLength={500} onChange={(e) => setNote(e.target.value)} placeholder="e.g. Sipho's Spaza wants 20 on Friday" autoFocus />
        </Field>
        <div className="chips">{quick.map((q) => <button type="button" key={q} className="chip" onClick={() => setNote(q)}>{q}</button>)}</div>
        {error && <div className="text-danger small row-flex"><Icon name="alert" size={14} /> {error}</div>}
        <button type="submit" hidden />
      </form>
    </Modal>
  );
}

/* Admin: the list of alerts from staff, with "On it", "Restock" and "Close" */
export function StaffAlertsPanel({ alerts, products, suppliers, onRestock, onChanged }) {
  const toast = useToast();
  const [busy, setBusy] = useState(null);
  const active = (alerts || []).filter(isActiveAlert);
  if (!active.length) return null;

  const act = async (alert, fn, done) => {
    setBusy(alert.id);
    try { await fn(); toast.success(done, alert.productName); onChanged?.(); }
    catch (err) { toast.error('Could not update the alert', err.message); }
    finally { setBusy(null); }
  };

  return (
    <div className="card alerts-card mb-4">
      <div className="card-header">
        <div><h3><Icon name="bullhorn" size={15} style={{ color: 'var(--warning)' }} /> Stock alerts from staff</h3>
          <div className="sub">{active.length} product{active.length === 1 ? '' : 's'} flagged by your team. Restocking closes an alert automatically.</div></div>
      </div>
      <div className="card-body flush">
        {active.map((a) => {
          const product = (products || []).find((p) => p.id === a.productId);
          return (
            <div className="list-row alert-row" key={a.id}>
              <Avatar src={product?.imageUrl} name={a.productName} tint={categoryColor(product?.category)} icon={categoryIcon(product?.category)} size={40} square />
              <div className="grow">
                <div className="t">{a.productName} <span className="muted small" style={{ fontWeight: 500 }}>· {product ? `${product.quantity} left now` : 'product removed'}</span></div>
                <div className="s">Raised by <strong>{a.raisedByName}</strong> {timeAgo(a.createdAt)}{a.handledByName && a.status === 'ACKNOWLEDGED' ? ` · ${a.handledByName} is on it` : ''}</div>
                {a.note && <div className="alert-note">“{a.note}”</div>}
              </div>
              <AlertStatus alert={a} />
              <div className="row-flex" style={{ gap: 6 }}>
                {a.status === 'OPEN' && <button className="btn btn-secondary btn-sm" disabled={busy === a.id} onClick={() => act(a, () => api.stockAlerts.acknowledge(a.id), 'Marked as in progress')}><Icon name="check" size={13} /> On it</button>}
                <ContactSupplier supplier={(suppliers || []).find((s) => s.supplierId === product?.supplierId)} product={product || { name: a.productName, id: a.productId }}
                  onContacted={() => { if (a.status === 'OPEN') act(a, () => api.stockAlerts.acknowledge(a.id), 'Marked as in progress'); }} />
                {product && <button className="btn btn-primary btn-sm" disabled={busy === a.id} onClick={() => onRestock(product)}><Icon name="plus" size={13} /> Restock</button>}
                <button className="icon-btn" title="Close alert" disabled={busy === a.id} onClick={() => act(a, () => api.stockAlerts.resolve(a.id), 'Alert closed')}><Icon name="x" size={15} /></button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

/* Employee: their own recent alerts and where each one is at */
export function MyAlertsPanel({ alerts, employeeId }) {
  const mine = (alerts || []).filter((a) => a.raisedById === employeeId)
    .filter((a) => isActiveAlert(a) || (a.handledAt && Date.now() - new Date(a.handledAt).getTime() < 3 * 86400000))
    .slice(0, 5);
  if (!mine.length) return null;
  return (
    <div className="card alerts-card mb-4">
      <div className="card-header">
        <div><h3><Icon name="bullhorn" size={15} style={{ color: 'var(--warning)' }} /> Your stock alerts</h3><div className="sub">You'll get a notification when an admin responds.</div></div>
      </div>
      <div className="card-body flush">
        {mine.map((a) => (
          <div className="list-row" key={a.id}>
            <div className="grow">
              <div className="t">{a.productName}</div>
              <div className="s">Sent {timeAgo(a.createdAt)}{a.resolution ? ` · ${a.resolution}` : a.handledByName ? ` · ${a.handledByName} is on it` : ''}</div>
            </div>
            <AlertStatus alert={a} />
          </div>
        ))}
      </div>
    </div>
  );
}

/* Admin: contact the product's supplier – opens the email app (with a ready-made re-order message) or the phone app */
export function ContactSupplier({ supplier, product, onContacted }) {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);
  useEffect(() => {
    const close = (e) => { if (ref.current && !ref.current.contains(e.target)) setOpen(false); };
    document.addEventListener('mousedown', close);
    return () => document.removeEventListener('mousedown', close);
  }, []);

  if (!supplier) {
    return <span className="contact-none" title="Link a supplier to this product (Edit product) to contact them from here."><Icon name="truck" size={12} /> No supplier linked</span>;
  }
  const subject = `Re-order request: ${product?.name || 'stock'}`;
  const body = `Hi ${supplier.contactPerson || supplier.name},\n\nWe would like to re-order ${product?.name || 'the following item'}${product?.id ? ` (our ref ${product.id})` : ''}.`
    + `${typeof product?.quantity === 'number' ? ` We currently have ${product.quantity} left.` : ''}\n\nPlease let us know the price and when you can deliver.\n\nThank you`;
  const mail = supplier.email ? `mailto:${supplier.email}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}` : null;
  const tel = supplier.phone ? `tel:${String(supplier.phone).replace(/[^\d+]/g, '')}` : null;

  return (
    <div className="contact-wrap" ref={ref}>
      <button type="button" className="btn btn-secondary btn-sm" onClick={(e) => { e.stopPropagation(); setOpen((o) => !o); }} aria-expanded={open}>
        <Icon name="phone" size={12} /> Contact
      </button>
      {open && (
        <div className="dropdown contact-menu" onClick={(e) => e.stopPropagation()}>
          <div className="contact-head"><div className="bold">{supplier.name}</div>{supplier.contactPerson && <div className="small muted">{supplier.contactPerson}</div>}</div>
          {mail
            ? <a className="menu-item" href={mail} onClick={() => { setOpen(false); onContacted?.(); }}><Icon name="mail" size={14} /><span><span className="bold">Email</span><span className="small muted"> · {supplier.email}</span></span></a>
            : <span className="menu-item disabled"><Icon name="mail" size={14} /> No email saved</span>}
          {tel
            ? <a className="menu-item" href={tel} onClick={() => { setOpen(false); onContacted?.(); }}><Icon name="phone" size={14} /><span><span className="bold">Call</span><span className="small muted"> · {supplier.phone}</span></span></a>
            : <span className="menu-item disabled"><Icon name="phone" size={14} /> No phone number saved</span>}
        </div>
      )}
    </div>
  );
}
