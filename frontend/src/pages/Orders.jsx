import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Icon from '../components/Icons';
import SearchBar from '../components/SearchBar';
import { Avatar, Badge, ConfirmDialog, Drawer, EmptyState, ErrorState, Loading, PageHeader, StatCard } from '../components/ui';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { useApi } from '../hooks/useApi';
import { api } from '../services/api';
import { daysSince, formatDate, formatDateTime, fullName, matches, money } from '../utils/format';

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
  const [range, setRange] = useState('all');
  const [viewing, setViewing] = useState(null);
  const [voiding, setVoiding] = useState(null);

  const customerById = useMemo(() => Object.fromEntries((data?.customers || []).map((c) => [c.custID, c])), [data]);
  const nameOf = (o) => o.customerName || fullName(customerById[o.customerId]) || o.customerId;
  const orders = useMemo(() => (data?.orders || []).map((o) => ({ ...o, displayName: nameOf(o) })), [data]); // eslint-disable-line react-hooks/exhaustive-deps

  const rows = useMemo(() => orders
    .filter((o) => {
      if (range === 'all') return true;
      const d = daysSince(o.orderDate);
      return range === 'today' ? d === 0 : d !== null && d < Number(range);
    })
    .filter((o) => (query.mode === 'id' ? matches(query.text, o.orderId) : matches(query.text, o.displayName, o.employeeName)))
    .sort((a, b) => String(b.orderTime || b.orderDate).localeCompare(String(a.orderTime || a.orderDate))), [orders, range, query]);

  const lookUpId = async (id) => {
    try { setViewing(await api.orders.read(id)); } catch { toast.warning('Not found', `There is no order with ID ${id}.`); }
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

  return (
    <>
      <PageHeader eyebrow="Sales" title="Orders" subtitle="Every completed sale. Search by customer name, or switch to order ID.">
        <button className="btn btn-primary" onClick={() => navigate('/pos')}><Icon name="cart" size={17} /> New sale</button>
      </PageHeader>

      <div className="grid grid-3 mb-4">
        <StatCard icon="receipt" tone="blue" label="Orders shown" value={rows.length} />
        <StatCard icon="money" tone="green" label="Value" value={money(total)} />
        <StatCard icon="trendUp" tone="violet" label="Average order" value={money(rows.length ? total / rows.length : 0)} />
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
        </div>
        {rows.length === 0 ? (
          <EmptyState icon="receipt" title={orders.length ? 'No matching orders' : 'No orders yet'} message={orders.length ? 'Try another name, date range or the order ID.' : 'Sales from the New Sale screen appear here.'}
            action={!orders.length && <button className="btn btn-primary" onClick={() => navigate('/pos')}>Make the first sale</button>} />
        ) : (
          <div className="table-wrap">
            <table className="table">
              <thead><tr><th>Order</th><th>Customer</th><th>Date</th><th>Items</th><th>Served by</th><th className="num">Total</th><th /></tr></thead>
              <tbody>
                {rows.map((o) => (
                  <tr key={o.orderId} className="clickable" onClick={() => setViewing(o)}>
                    <td><span className="mono bold">{o.orderId}</span></td>
                    <td><div className="cell-entity"><Avatar src={customerById[o.customerId]?.imageUrl} name={o.displayName} size={32} /><div><div className="t">{o.displayName}</div><div className="s mono">{o.customerId}</div></div></div></td>
                    <td>{formatDateTime(o.orderTime || o.orderDate)}</td>
                    <td><Badge tone="gray" plain>{(o.orderItems || []).reduce((s, i) => s + i.quantity, 0)} units</Badge></td>
                    <td>{o.employeeName || <span className="muted">—</span>}</td>
                    <td className="num bold">{money(o.totalAmount)}</td>
                    <td className="actions"><Icon name="chevronRight" size={16} className="muted" /></td>
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
              <div className="totals"><div className="row grand" style={{ marginTop: 0, borderTop: 0, paddingTop: 0 }}><span>Grand total</span><span>{money(viewing.totalAmount)}</span></div></div>
            </div>
          </div>
        </Drawer>
      )}
      {voiding && <ConfirmDialog title="Void this order?" message={`${voiding.orderId} will be deleted and the items will be added back into stock.`} confirmLabel="Void order" onConfirm={() => voidOrder(voiding)} onClose={() => setVoiding(null)} />}
    </>
  );
}
