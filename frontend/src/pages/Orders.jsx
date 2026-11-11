import { useMemo, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';
import Icon from '../components/Icons';
import SearchBar from '../components/SearchBar';
import { Avatar, Badge, ConfirmDialog, Drawer, EmptyState, ErrorState, Loading, PageHeader, StatCard } from '../components/ui';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { useApi } from '../hooks/useApi';
import { api } from '../services/api';
import { balanceDue, daysSince, formatDate, formatDateTime, fullName, matches, money, orderBreakdown, paymentLabel, paymentStatus } from '../utils/format';
import RecordPayment from '../components/RecordPayment';

const RANGES = [['all', 'All time'], ['today', 'Today'], ['7', 'Last 7 days'], ['30', 'Last 30 days']];

export default function Orders() {
  const { isAdmin } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const { data, loading, error, reload } = useApi(async () => {
    const [orders, customers] = await Promise.all([api.orders.all(), api.customers.all()]);
    return { orders, customers };
  });
  const [query, setQuery] = useState({ text: '', mode: 'name' });
  const location = useLocation();
  const [range, setRange] = useState(location.state?.range || 'all');
  const [viewing, setViewing] = useState(null);
  const [voiding, setVoiding] = useState(null);
  const [paying, setPaying] = useState(null);
  const [payFilter, setPayFilter] = useState(location.state?.pay || 'all');
  const [payments, setPayments] = useState([]);
  const openOrder = async (o) => {
    setViewing(o);
    setPayments([]);
    if (o.paymentMethod === 'ACCOUNT') { try { setPayments(await api.payments.forOrder(o.orderId)); } catch { /* ignore */ } }
  };

  const customerById = useMemo(() => Object.fromEntries((data?.customers || []).map((c) => [c.custID, c])), [data]);
  const nameOf = (o) => o.customerName || fullName(customerById[o.customerId]) || o.customerId;
  const orders = useMemo(() => (data?.orders || []).map((o) => ({ ...o, displayName: nameOf(o) })), [data]); // eslint-disable-line react-hooks/exhaustive-deps

  const rows = useMemo(() => orders
    .filter((o) => {
      if (range === 'all') return true;
      const d = daysSince(o.orderDate);
      return range === 'today' ? d === 0 : d !== null && d < Number(range);
    })
    .filter((o) => payFilter === 'all' || (payFilter === 'owing' ? balanceDue(o) > 0 : o.paymentMethod === payFilter))
    .filter((o) => (query.mode === 'id' ? matches(query.text, o.orderId) : matches(query.text, o.displayName, o.employeeName)))
    .sort((a, b) => String(b.orderTime || b.orderDate).localeCompare(String(a.orderTime || a.orderDate))), [orders, range, query, payFilter]);

  const lookUpId = async (id) => {
    try { openOrder(await api.orders.read(id)); } catch { toast.warning('Not found', `There is no order with ID ${id}.`); }
  };

  const voidOrder = async (o) => {
    await api.orders.remove(o.orderId);
    toast.success('Order voided', `${o.orderId} was removed and the stock was put back.`);
    setViewing(null);
    reload({ silent: true });
  };

  if (loading) return <Loading />;
  if (error) return <ErrorState error={error} onRetry={reload} />;

  const total = rows.reduce((s, o) => s + o.totalAmount, 0);
  const owed = orders.reduce((s, o) => s + balanceDue(o), 0);
  const owingCount = orders.filter((o) => balanceDue(o) > 0).length;

  return (
    <>
      <PageHeader eyebrow="Sales" title="Orders" subtitle="Every sale, how it was paid and what's still owed on account. Search by customer or shop name, or switch to order ID.">
        <button className="btn btn-primary" onClick={() => navigate('/pos')}><Icon name="cart" size={17} /> New sale</button>
      </PageHeader>

      <div className="grid grid-4 mb-4">
        <StatCard icon="receipt" tone="blue" label="Orders shown" value={rows.length} />
        <StatCard icon="money" tone="green" label="Value (incl. VAT)" value={money(total)} />
        <StatCard icon="trendUp" tone="violet" label="Average order" value={money(rows.length ? total / rows.length : 0)} />
        <StatCard icon="clipboard" tone={owed > 0 ? 'amber' : 'green'} label="Owed on account" value={money(owed)} foot={owingCount ? `${owingCount} unpaid order${owingCount === 1 ? '' : 's'}` : 'Nothing outstanding'} />
      </div>

      <div className="card">
        <div className="toolbar">
          <SearchBar items={orders} entityLabel="orders" getId={(o) => o.orderId} getName={(o) => o.displayName} getSub={(o) => formatDate(o.orderDate)}
            getRight={(o) => <span className="bold" style={{ color: 'var(--text)' }}>{money(o.totalAmount)}</span>}
            placeholder="Search orders by customer name…" idPlaceholder="Enter order ID, e.g. ORD-0001"
            onQueryChange={(text, mode) => setQuery({ text, mode })} onSelect={setViewing} onIdSearch={lookUpId} />
          <div className="chips">
            {RANGES.map(([k, l]) => <button key={k} className={`chip${range === k ? ' active' : ''}`} onClick={() => setRange(k)}>{l}</button>)}
          </div>
          <select className="select" style={{ width: 190 }} value={payFilter} onChange={(e) => setPayFilter(e.target.value)} aria-label="Payment">
            <option value="all">All payments</option>
            <option value="owing">Still owing{owingCount ? ` (${owingCount})` : ''}</option>
            <option value="ACCOUNT">On account</option>
            <option value="CASH">Cash</option>
            <option value="CARD">Card</option>
            <option value="EFT">EFT</option>
          </select>
        </div>
        {rows.length === 0 ? (
          <EmptyState icon="receipt" title={orders.length ? 'No matching orders' : 'No orders yet'} message={orders.length ? 'Try another name, date range or the order ID.' : 'Sales from the New Sale screen appear here.'}
            action={!orders.length && <button className="btn btn-primary" onClick={() => navigate('/pos')}>Make the first sale</button>} />
        ) : (
          <div className="table-wrap">
            <table className="table">
              <thead><tr><th>Order</th><th>Customer</th><th>Date</th><th>Items</th><th>Served by</th><th>Payment</th><th className="num">Total</th><th /></tr></thead>
              <tbody>
                {rows.map((o) => (
                  <tr key={o.orderId} className="clickable" onClick={() => openOrder(o)}>
                    <td><span className="mono bold">{o.orderId}</span></td>
                    <td><div className="cell-entity"><Avatar src={customerById[o.customerId]?.imageUrl} name={o.displayName} size={32} /><div><div className="t">{o.displayName}</div><div className="s mono">{o.customerId}</div></div></div></td>
                    <td>{formatDateTime(o.orderTime || o.orderDate)}</td>
                    <td><Badge tone="gray" plain>{(o.orderItems || []).reduce((s, i) => s + i.quantity, 0)} units</Badge></td>
                    <td>{o.employeeName || <span className="muted">—</span>}</td>
                    <td>
                      <div className="small">{o.paymentMethod ? paymentLabel(o.paymentMethod) : <span className="muted">Paid</span>}</div>
                      {o.paymentMethod === 'ACCOUNT' && <Badge tone={paymentStatus(o).tone}>{paymentStatus(o).label}</Badge>}
                    </td>
                    <td className="num bold">{money(o.totalAmount)}{balanceDue(o) > 0 && <div className="small text-danger">{money(balanceDue(o))} owed</div>}</td>
                    <td className="actions" onClick={(e) => e.stopPropagation()}>
                      {balanceDue(o) > 0
                        ? <button className="btn btn-secondary btn-sm" onClick={() => setPaying(o)}>Record payment</button>
                        : <Icon name="chevronRight" size={16} className="muted" />}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {viewing && (
        <Drawer title={`Order ${viewing.orderId}`} onClose={() => setViewing(null)}
          footer={<>
            {isAdmin && <button className="btn btn-soft-danger" onClick={() => setVoiding(viewing)}><Icon name="trash" size={16} /> Void order</button>}
            {balanceDue(viewing) > 0 && <button className="btn btn-primary" onClick={() => setPaying(viewing)}><Icon name="money" size={15} /> Record payment</button>}
            <button className="btn btn-secondary" onClick={() => window.print()}><Icon name="printer" size={16} /> Print</button>
          </>}>
          <div className="print-area">
            <div className="row-flex mb-4">
              <Avatar src={customerById[viewing.customerId]?.imageUrl} name={nameOf(viewing)} size={48} />
              <div><div className="bold" style={{ fontSize: 16 }}>{nameOf(viewing)}</div><div className="small muted mono">{viewing.customerId}</div></div>
            </div>
            <dl className="detail-list">
              <dt>Order ID</dt><dd className="mono">{viewing.orderId}</dd>
              <dt>Date</dt><dd>{formatDateTime(viewing.orderTime || viewing.orderDate)}</dd>
              <dt>Served by</dt><dd>{viewing.employeeName || '—'}</dd>
              <dt>Payment</dt><dd>{viewing.paymentMethod ? paymentLabel(viewing.paymentMethod) : 'Paid'}{viewing.paymentMethod === 'ACCOUNT' && <> · <Badge tone={paymentStatus(viewing).tone}>{paymentStatus(viewing).label}</Badge></>}</dd>
            </dl>
            <div className="card mt-4">
              <table className="table">
                <thead><tr><th>Item</th><th className="num">Qty</th><th className="num">Price</th><th className="num">Total</th></tr></thead>
                <tbody>
                  {(viewing.orderItems || []).map((it) => (
                    <tr key={it.orderItemId}>
                      <td><div className="bold">{it.productName || it.productId}</div><div className="small muted mono">{it.productId}</div></td>
                      <td className="num">{it.quantity}</td>
                      <td className="num">{it.unitPrice != null ? money(it.unitPrice) : '—'}</td>
                      <td className="num bold">{money(it.lineTotal)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              <div className="totals">
                {orderBreakdown(viewing).vat != null && <>
                  <div className="row"><span>Subtotal (excl. VAT)</span><span>{money(orderBreakdown(viewing).subtotal)}</span></div>
                  <div className="row"><span>VAT 15%</span><span>{money(orderBreakdown(viewing).vat)}</span></div>
                </>}
                <div className={`row grand${orderBreakdown(viewing).vat == null ? ' no-rule' : ''}`}><span>Total{orderBreakdown(viewing).vat != null ? ' (incl. VAT)' : ''}</span><span>{money(viewing.totalAmount)}</span></div>
                {orderBreakdown(viewing).vat == null && <div className="small muted mt-1">Sold before VAT was added to the system.</div>}
              </div>
              {viewing.paymentMethod === 'ACCOUNT' && (
                <div className="account-box">
                  <div className="row-flex between"><span>Paid so far</span><span className="bold">{money(viewing.amountPaid ?? 0)}</span></div>
                  <div className="row-flex between"><span>Still owed</span><span className={`bold${balanceDue(viewing) > 0 ? ' text-danger' : ' text-success'}`}>{money(balanceDue(viewing))}</span></div>
                  {payments.length > 0 && (
                    <div className="pay-history">
                      <div className="small muted bold mt-2">Payments</div>
                      {payments.map((p) => (
                        <div key={p.id} className="row-flex between small"><span>{formatDateTime(p.paidAt)} · {paymentLabel(p.method)}{p.note ? ` · ${p.note}` : ''}</span><span className="bold">{money(p.amount)}</span></div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        </Drawer>
      )}
      {paying && <RecordPayment order={paying} customerName={nameOf(paying)} onClose={() => setPaying(null)}
        onPaid={(saved) => { reload({ silent: true }); if (viewing?.orderId === saved.orderId) openOrder({ ...saved, displayName: nameOf(saved) }); }} />}
      {voiding && <ConfirmDialog title="Void this order?" message={`${voiding.orderId} will be deleted and the items will be added back into stock.`} confirmLabel="Void order" onConfirm={() => voidOrder(voiding)} onClose={() => setVoiding(null)} />}
    </>
  );
}
