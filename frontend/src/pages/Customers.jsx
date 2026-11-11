import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Icon from '../components/Icons';
import SearchBar from '../components/SearchBar';
import CustomerForm from '../components/CustomerForm';
import { Avatar, Badge, ConfirmDialog, Drawer, EmptyState, ErrorState, Loading, PageHeader, StatCard } from '../components/ui';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { useApi } from '../hooks/useApi';
import { api } from '../services/api';
import { balanceDue, daysSince, formatDate, formatDateTime, fullName, matches, money, paymentLabel, paymentStatus } from '../utils/format';
import RecordPayment from '../components/RecordPayment';

const activity = (c) => {
  const d = daysSince(c.lastPurchaseDate);
  if (d === null) return { label: 'No purchases yet', tone: 'gray', key: 'new' };
  if (d > 30) return { label: `Inactive · ${d} days`, tone: 'violet', key: 'inactive' };
  return { label: d === 0 ? 'Bought today' : `Active · ${d}d ago`, tone: 'green', key: 'active' };
};

export default function Customers() {
  const { isAdmin } = useAuth();
  const toast = useToast();
  const { data, loading, error, reload } = useApi(async () => {
    const [customers, orders] = await Promise.all([api.customers.all(), api.orders.all()]);
    return { customers, orders };
  });
  const [query, setQuery] = useState({ text: '', mode: 'name' });
  const [filter, setFilter] = useState('all');
  const [editing, setEditing] = useState(null);
  const [viewing, setViewing] = useState(null);
  const [deleting, setDeleting] = useState(null);

  const spend = useMemo(() => {
    const map = {};
    (data?.orders || []).forEach((o) => {
      map[o.customerId] = map[o.customerId] || { total: 0, count: 0, owed: 0 };
      map[o.customerId].total += o.totalAmount;
      map[o.customerId].count += 1;
      map[o.customerId].owed += balanceDue(o);
    });
    return map;
  }, [data]);

  const customers = data?.customers || [];
  const counts = useMemo(() => ({
    all: customers.length,
    active: customers.filter((c) => activity(c).key === 'active').length,
    inactive: customers.filter((c) => activity(c).key === 'inactive').length,
    new: customers.filter((c) => activity(c).key === 'new').length,
    owing: customers.filter((c) => (spend[c.custID]?.owed || 0) > 0.001).length
  }), [customers, spend]);

  const rows = useMemo(() => customers
    .filter((c) => filter === 'all' || (filter === 'owing' ? (spend[c.custID]?.owed || 0) > 0.001 : activity(c).key === filter))
    .filter((c) => (query.mode === 'id' ? matches(query.text, c.custID) : matches(query.text, fullName(c), c.company, c.phoneNumber, c.email)))
    .sort((a, b) => fullName(a).localeCompare(fullName(b))), [customers, filter, query, spend]);

  const lookUpId = async (id) => {
    try { setViewing(await api.customers.read(id)); } catch { toast.warning('Not found', `There is no customer with ID ${id}.`); }
  };

  const remove = async (c) => {
    await api.customers.remove(c.custID);
    toast.success('Customer deleted', fullName(c));
    reload({ silent: true });
  };

  if (loading) return <Loading />;
  if (error) return <ErrorState error={error} onRetry={reload} />;

  return (
    <>
      <PageHeader eyebrow="People" title="Customers" subtitle="Your trade customers and spaza shops, what they buy and what they owe on account.">
        {isAdmin
          ? <button className="btn btn-primary" onClick={() => setEditing({})}><Icon name="userPlus" size={17} /> Add customer</button>
          : <span className="small muted">New customers can be added from the New Sale screen.</span>}
      </PageHeader>

      <div className="grid grid-4 mb-4">
        <StatCard icon="users" tone="blue" label="Total customers" value={counts.all} />
        <StatCard icon="checkCircle" tone="green" label="Active (30 days)" value={counts.active} />
        <StatCard icon="alert" tone="violet" label="Inactive" value={counts.inactive} foot="No purchase in 30+ days" />
        <StatCard icon="clipboard" tone={counts.owing ? 'amber' : 'cyan'} label="Owed on account" value={money(Object.values(spend).reduce((s, x) => s + x.owed, 0))}
          foot={counts.owing ? `${counts.owing} customer${counts.owing === 1 ? '' : 's'} owe you` : 'All accounts settled'} />
      </div>

      <div className="card">
        <div className="toolbar">
          <SearchBar items={customers} entityLabel="customers" getId={(c) => c.custID} getName={fullName} getSub={(c) => c.company || c.phoneNumber}
            getImage={(c) => c.imageUrl} placeholder="Search customers by name or shop…" idPlaceholder="Enter customer ID, e.g. CUS-0001"
            onQueryChange={(text, mode) => setQuery({ text, mode })} onSelect={setViewing} onIdSearch={lookUpId} />
          <div className="chips">
            {[['all', 'All'], ['owing', 'Owes money'], ['active', 'Active'], ['inactive', 'Inactive'], ['new', 'No purchases']].map(([k, l]) => (
              <button key={k} className={`chip${filter === k ? ' active' : ''}`} onClick={() => setFilter(k)}>{l} <span className="count">{counts[k]}</span></button>
            ))}
          </div>
        </div>
        {rows.length === 0 ? (
          <EmptyState icon="users" title={customers.length ? 'No matching customers' : 'No customers yet'}
            message={customers.length ? 'Try a different name, or switch the search to ID.' : 'Add your first customer – you only need their name.'}
            action={!customers.length && isAdmin && <button className="btn btn-primary" onClick={() => setEditing({})}><Icon name="plus" size={16} /> Add customer</button>} />
        ) : (
          <div className="table-wrap">
            <table className="table">
              <thead><tr><th>Customer</th><th>Contact</th><th>Status</th><th className="num">Orders</th><th className="num">Total spent</th><th className="num">Owes</th><th>Customer since</th><th /></tr></thead>
              <tbody>
                {rows.map((c) => {
                  const a = activity(c);
                  return (
                    <tr key={c.custID} className="clickable" onClick={() => setViewing(c)}>
                      <td><div className="cell-entity"><Avatar src={c.imageUrl} name={fullName(c)} /><div><div className="t">{fullName(c)}</div><div className="s"><span className="mono">{c.custID}</span>{c.company ? ` · ${c.company}` : ''}</div></div></div></td>
                      <td><div>{c.phoneNumber || '—'}</div><div className="small muted">{c.email}</div></td>
                      <td><Badge tone={a.tone}>{a.label}</Badge></td>
                      <td className="num">{spend[c.custID]?.count || 0}</td>
                      <td className="num bold">{money(spend[c.custID]?.total || 0)}</td>
                      <td className="num">{(spend[c.custID]?.owed || 0) > 0.001 ? <span className="owes">{money(spend[c.custID].owed)}</span> : <span className="muted">—</span>}</td>
                      <td>{formatDate(c.createdDate)}</td>
                      <td className="actions" onClick={(e) => e.stopPropagation()}>
                        {isAdmin && <button className="icon-btn" title="Edit" onClick={() => setEditing(c)}><Icon name="edit" size={16} /></button>}
                        {isAdmin && <button className="icon-btn danger" title="Delete" onClick={() => setDeleting(c)}><Icon name="trash" size={16} /></button>}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {editing && <CustomerForm customer={editing} onClose={() => setEditing(null)} onSaved={(saved) => { reload({ silent: true }); if (viewing) setViewing(saved); }} />}
      {viewing && <CustomerDrawer customer={viewing} onClose={() => setViewing(null)} onEdit={isAdmin ? () => setEditing(viewing) : null} />}
      {deleting && <ConfirmDialog title="Delete customer?" message={`${fullName(deleting)} (${deleting.custID}) will be removed. Their past orders stay in the system.`} onConfirm={() => remove(deleting)} onClose={() => setDeleting(null)} />}
    </>
  );
}

function CustomerDrawer({ customer: c, onClose, onEdit }) {
  const navigate = useNavigate();
  const { data, loading, reload } = useApi(async () => {
    const [orders, payments] = await Promise.all([api.customers.orders(c.custID), api.payments.forCustomer(c.custID).catch(() => [])]);
    return { orders, payments };
  }, [c.custID]);
  const [paying, setPaying] = useState(null);
  const orders = data?.orders;
  const payments = data?.payments || [];
  const a = activity(c);
  const total = (orders || []).reduce((s, o) => s + o.totalAmount, 0);
  const owing = (orders || []).filter((o) => balanceDue(o) > 0);
  const owed = owing.reduce((s, o) => s + balanceDue(o), 0);
  return (
    <Drawer title="Customer details" onClose={onClose}
      footer={<>
        {onEdit && <button className="btn btn-secondary" onClick={onEdit}><Icon name="edit" size={16} /> Edit</button>}
        <button className="btn btn-primary" onClick={() => navigate('/pos', { state: { customerId: c.custID } })}><Icon name="cart" size={16} /> New sale</button>
      </>}>
      <div className="profile-hero">
        <Avatar src={c.imageUrl} name={fullName(c)} size={84} />
        <h3>{fullName(c)}</h3>
        <div className="row-flex"><span className="mono muted">{c.custID}</span><Badge tone={a.tone}>{a.label}</Badge></div>
      </div>
      <dl className="detail-list">
        <dt>Business</dt><dd>{c.company || '—'}</dd>
        <dt>Phone</dt><dd>{c.phoneNumber || '—'}</dd>
        <dt>Email</dt><dd>{c.email || '—'}</dd>
        <dt>Address</dt><dd>{c.address || '—'}</dd>
        <dt>Customer since</dt><dd>{formatDate(c.createdDate)}</dd>
        <dt>Last purchase</dt><dd>{formatDate(c.lastPurchaseDate)}</dd>
        <dt>Total spent</dt><dd className="bold">{money(total)}</dd>
      </dl>
      <div className={`account-box mt-4${owed > 0 ? ' owing' : ''}`}>
        <div className="row-flex between"><span className="bold">Account balance</span><span className={`bold${owed > 0 ? ' text-danger' : ' text-success'}`}>{owed > 0 ? `${money(owed)} owed` : 'Settled'}</span></div>
        {owing.map((o) => (
          <div key={o.orderId} className="row-flex between small mt-2">
            <span><span className="mono">{o.orderId}</span> · {formatDate(o.orderDate)} · <Badge tone={paymentStatus(o).tone}>{paymentStatus(o).label}</Badge></span>
            <span className="row-flex" style={{ gap: 8 }}><span className="bold">{money(balanceDue(o))}</span><button className="btn btn-secondary btn-sm" onClick={() => setPaying(o)}>Record payment</button></span>
          </div>
        ))}
        {payments.length > 0 && (
          <div className="pay-history">
            <div className="small muted bold mt-3">Recent payments</div>
            {payments.slice(0, 5).map((p) => (
              <div key={p.id} className="row-flex between small"><span>{formatDateTime(p.paidAt)} · {paymentLabel(p.method)} · <span className="mono">{p.orderId}</span></span><span className="bold">{money(p.amount)}</span></div>
            ))}
          </div>
        )}
      </div>
      {paying && <RecordPayment order={paying} customerName={fullName(c)} onClose={() => setPaying(null)} onPaid={() => reload({ silent: true })} />}
      <div className="divider" />
      <h4 style={{ fontSize: 14, marginBottom: 8 }}>Order history</h4>
      {loading ? <Loading /> : (orders || []).length === 0 ? <p className="muted small">No orders yet.</p> : (
        <div className="card">
          {orders.map((o) => (
            <div className="list-row" key={o.orderId} style={{ padding: '10px 14px' }}>
              <div className="grow"><div className="t mono">{o.orderId}</div><div className="s">{formatDate(o.orderDate)} · {(o.orderItems || []).length} item(s)</div></div>
              <div style={{ textAlign: 'right' }}><div className="bold tabular">{money(o.totalAmount)}</div><div className="small muted">{o.paymentMethod ? paymentLabel(o.paymentMethod) : 'Paid'}</div></div>
            </div>
          ))}
        </div>
      )}
    </Drawer>
  );
}
