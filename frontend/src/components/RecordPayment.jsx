import { useState } from 'react';
import Icon from './Icons';
import { Field, Modal } from './ui';
import { useToast } from '../context/ToastContext';
import { api } from '../services/api';
import { money } from '../utils/format';

const METHODS = [['CASH', 'Cash'], ['CARD', 'Card'], ['EFT', 'EFT']];

/* Take money against an account order: pay it all off, or enter a part payment */
export default function RecordPayment({ order, customerName, onClose, onPaid }) {
  const toast = useToast();
  const due = order.balanceDue ?? 0;
  const [full, setFull] = useState(true);
  const [amount, setAmount] = useState('');
  const [method, setMethod] = useState('CASH');
  const [note, setNote] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const n = Number(amount);

  const save = async (e) => {
    e?.preventDefault();
    setError('');
    if (!full && (!n || n <= 0)) return setError('Enter how much the customer paid.');
    if (!full && n > due + 0.001) return setError(`That's more than the ${money(due)} still owed.`);
    setBusy(true);
    try {
      const saved = await api.orders.pay(order.orderId, full ? null : n, method, note);
      toast.success(saved.balanceDue > 0 ? 'Part payment recorded' : 'Account settled',
        saved.balanceDue > 0 ? `${money(full ? due : n)} received · ${money(saved.balanceDue)} still owed` : `${order.orderId} is now paid in full.`);
      onPaid?.(saved);
      onClose();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Modal title="Record a payment" subtitle={`${customerName || order.customerName} · ${order.orderId}`} onClose={onClose} size="sm"
      footer={<>
        <button className="btn btn-secondary" onClick={onClose} disabled={busy}>Cancel</button>
        <button className="btn btn-primary" onClick={save} disabled={busy}>{busy ? 'Saving…' : full ? `Pay off ${money(due)}` : `Record ${money(n || 0)}`}</button>
      </>}>
      <form onSubmit={save} className="stack" style={{ gap: 14 }}>
        <div className="owed-box"><span>Still owed on this order</span><strong>{money(due)}</strong></div>
        <div className="pay-methods two">
          <button type="button" className={`pay-method${full ? ' on' : ''}`} onClick={() => setFull(true)}><Icon name="checkCircle" size={14} /> Paid it all off</button>
          <button type="button" className={`pay-method${!full ? ' on' : ''}`} onClick={() => setFull(false)}><Icon name="money" size={14} /> Paid part of it</button>
        </div>
        {!full && (
          <Field label="Amount received">
            <div className="input-prefix"><span>R</span><input className="input" type="number" min="0.01" step="0.01" max={due} value={amount} onChange={(e) => setAmount(e.target.value)} placeholder="0.00" autoFocus /></div>
            {n > 0 && n <= due && <span className="hint">{money(due - n)} will still be owed afterwards.</span>}
          </Field>
        )}
        <Field label="Paid by">
          <div className="pay-methods three">
            {METHODS.map(([k, l]) => <button key={k} type="button" className={`pay-method${method === k ? ' on' : ''}`} onClick={() => setMethod(k)}>{l}</button>)}
          </div>
        </Field>
        <Field label="Note" hint="Optional, e.g. EFT reference"><input className="input" value={note} maxLength={200} onChange={(e) => setNote(e.target.value)} /></Field>
        {error && <div className="text-danger small row-flex"><Icon name="alert" size={14} /> {error}</div>}
        <button type="submit" hidden />
      </form>
    </Modal>
  );
}
