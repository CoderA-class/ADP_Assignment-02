import { useEffect, useMemo, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import Icon from '../components/Icons';
import CustomerForm from '../components/CustomerForm';
import { APP_NAME } from '../components/Layout';
import { Avatar, EmptyState, ErrorState, Highlight, Loading, Modal, ProductImage } from '../components/ui';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { useApi } from '../hooks/useApi';
import { api } from '../services/api';
import { categoryColor, categoryIcon, colorFor, formatDateTime, fullName, matches, money, stockStatus } from '../utils/format';

/*
 * Point of Sale – "New Sale"
 *  1. Choose the customer from the dropdown (or add a new one right here)
 *  2. Click products (search by name) to add them to the cart
 *  3. Prices, line totals and the grand total are calculated automatically
 *  4. Complete sale → the backend creates the order, items and IDs and reduces stock
 */
export default function PointOfSale() {
  const { user } = useAuth();
  const toast = useToast();
  const location = useLocation();
  const navigate = useNavigate();
  const { data, loading, error, reload, setData } = useApi(async () => {
    const [products, customers] = await Promise.all([api.products.all(), api.customers.all()]);
    return { products, customers };
  });
  const [customerId, setCustomerId] = useState(location.state?.customerId || '');
  const [cart, setCart] = useState([]); // [{ productId, quantity }]
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('All');
  const [addingCustomer, setAddingCustomer] = useState(false);
  const [busy, setBusy] = useState(false);
  const [receipt, setReceipt] = useState(null);
  const searchRef = useRef(null);

  const products = data?.products || [];
  const customers = data?.customers || [];
  const productById = useMemo(() => Object.fromEntries(products.map((p) => [p.id, p])), [products]);
  const customer = customers.find((c) => c.custID === customerId);
  const categories = useMemo(() => ['All', ...[...new Set(products.map((p) => p.category || 'General'))].sort()], [products]);

  const visible = useMemo(() => products
    .filter((p) => category === 'All' || (p.category || 'General') === category)
    .filter((p) => matches(search, p.name, p.id, p.category))
    .sort((a, b) => a.name.localeCompare(b.name)), [products, category, search]);

  const lines = cart.map((l) => {
    const p = productById[l.productId];
    return p ? { ...l, product: p, lineTotal: Math.round(p.price * l.quantity * 100) / 100 } : null;
  }).filter(Boolean);
  const itemCount = lines.reduce((s, l) => s + l.quantity, 0);
  const grandTotal = lines.reduce((s, l) => s + l.lineTotal, 0);

  const qtyInCart = (id) => cart.find((l) => l.productId === id)?.quantity || 0;

  const add = (p) => {
    const inCart = qtyInCart(p.id);
    if (inCart >= p.quantity) {
      toast.warning('Not enough stock', `Only ${p.quantity} × ${p.name} available.`);
      return;
    }
    setCart((c) => (inCart ? c.map((l) => (l.productId === p.id ? { ...l, quantity: l.quantity + 1 } : l)) : [...c, { productId: p.id, quantity: 1 }]));
  };

  const setQty = (id, value) => {
    const p = productById[id];
    let q = parseInt(value, 10);
    if (Number.isNaN(q)) q = 0;
    if (p && q > p.quantity) {
      toast.warning('Not enough stock', `Only ${p.quantity} × ${p.name} available.`);
      q = p.quantity;
    }
    setCart((c) => (q <= 0 ? c.filter((l) => l.productId !== id) : c.map((l) => (l.productId === id ? { ...l, quantity: q } : l))));
  };

  // Enter in the product search adds the first match
  const onSearchKey = (e) => {
    if (e.key === 'Enter' && visible[0]) {
      e.preventDefault();
      if (visible[0].quantity > 0) add(visible[0]);
      setSearch('');
    }
  };

  const complete = async () => {
    if (!customer) { toast.error('Choose a customer', 'Select who is buying (or add a new customer) first.'); return; }
    if (!lines.length) { toast.error('Cart is empty', 'Add at least one product.'); return; }
    setBusy(true);
    try {
      const order = await api.orders.checkout({ customerId, employeeId: user.empID, items: cart });
      setReceipt({ order, customer });
      setCart([]);
      toast.success('Sale completed', `${order.orderId} · ${money(order.totalAmount)}`);
      reload({ silent: true });
    } catch (err) {
      toast.error('Sale not completed', err.message);
      reload({ silent: true });
    } finally {
      setBusy(false);
    }
  };

  useEffect(() => {
    if (!loading) searchRef.current?.focus();
  }, [loading]);

  if (loading) return <Loading />;
  if (error) return <ErrorState error={error} onRetry={reload} />;

  return (
    <>
      <div className="pos">
        {/* ---------------- products ---------------- */}
        <div className="card">
          <div className="toolbar">
            <div className="input-group" style={{ flex: 1, minWidth: 220 }}>
              <span className="lead"><Icon name="search" size={17} /></span>
              <input ref={searchRef} className="input" value={search} onChange={(e) => setSearch(e.target.value)} onKeyDown={onSearchKey}
                placeholder="Search products by name… (Enter adds the first match)" aria-label="Search products" />
            </div>
          </div>
          <div className="toolbar" style={{ paddingTop: 10, paddingBottom: 10 }}>
            <div className="chips">
              {categories.map((c) => <button key={c} className={`chip${category === c ? ' active' : ''}`} onClick={() => setCategory(c)}>{c !== 'All' && <span className="cat-dot" style={{ background: categoryColor(c) }} />}{c}</button>)}
            </div>
          </div>
          {visible.length === 0 ? (
            <EmptyState icon="box" title="No products found" message={products.length ? 'Try a different name.' : 'Add products under Products & Stock first.'} />
          ) : (
            <div className="pos-products">
              {visible.map((p) => {
                const inCart = qtyInCart(p.id);
                const s = stockStatus(p);
                return (
                  <button key={p.id} className="pos-item" onClick={() => add(p)} disabled={p.quantity <= 0} title={p.name}>
                    {inCart > 0 && <span className="in-cart">{inCart} in cart</span>}
                    <div className="thumb"><ProductImage src={p.imageUrl} name={p.name} category={p.category} iconSize={22} /></div>
                    <div className="info">
                      <div className="n"><Highlight text={p.name} query={search} /></div>
                      <div className="p">{money(p.price)}</div>
                      <div className={`q ${s.key === 'ok' ? '' : s.key === 'low' ? 'text-warning' : 'text-danger'}`}>{p.quantity > 0 ? `${p.quantity} available` : 'Out of stock'}</div>
                    </div>
                  </button>
                );
              })}
            </div>
          )}
        </div>

        {/* ---------------- cart ---------------- */}
        <div className="card cart till">
          <div className="till-head">
            <div className="t">Till slip</div>
            <div className="s">{new Date().toLocaleDateString('en-ZA', { weekday: 'short', day: 'numeric', month: 'short' })} · {fullName(user)}</div>
          </div>
          <div className="card-header" style={{ display: 'block' }}>
            <div className="row-flex between" style={{ marginBottom: 10 }}>
              <h3>Customer</h3>
              <button className="btn btn-ghost btn-sm" onClick={() => setAddingCustomer(true)}><Icon name="userPlus" size={15} /> New customer</button>
            </div>
            <CustomerPicker customers={customers} value={customerId} onChange={setCustomerId} onAddNew={() => setAddingCustomer(true)} />
          </div>

          <div className="card-header">
            <h3>Current sale</h3>
            {lines.length > 0 && <button className="btn btn-ghost btn-sm" onClick={() => setCart([])}>Clear</button>}
          </div>
          <div className="cart-lines">
            {lines.length === 0 ? (
              <EmptyState icon="cart" title="The slip is empty" message="Tap a product on the left – or type its name and press Enter." />
            ) : lines.map((l) => (
              <div className="cart-line" key={l.productId}>
                <Avatar src={l.product.imageUrl} name={l.product.name} tint={categoryColor(l.product.category)} icon={categoryIcon(l.product.category)} size={40} square />
                <div style={{ minWidth: 0 }}>
                  <div className="n">{l.product.name}</div>
                  <div className="u">{money(l.product.price)} each</div>
                  <div className="qty">
                    <button onClick={() => setQty(l.productId, l.quantity - 1)} aria-label="Less"><Icon name="minus" size={14} /></button>
                    <input type="number" value={l.quantity} onChange={(e) => setQty(l.productId, e.target.value)} aria-label="Quantity" />
                    <button onClick={() => setQty(l.productId, l.quantity + 1)} aria-label="More"><Icon name="plus" size={14} /></button>
                  </div>
                </div>
                <div>
                  <div className="lt">{money(l.lineTotal)}</div>
                  <button className="icon-btn danger" style={{ width: 28, height: 28, marginTop: 6, marginLeft: 'auto', display: 'grid' }} onClick={() => setQty(l.productId, 0)} aria-label="Remove"><Icon name="trash" size={14} /></button>
                </div>
              </div>
            ))}
          </div>
          <div className="totals">
            <div className="row"><span>Items</span><span>{itemCount}</span></div>
            <div className="row"><span>Subtotal</span><span>{money(grandTotal)}</span></div>
            <div className="row grand"><span>Grand total</span><span>{money(grandTotal)}</span></div>
            <button className="btn btn-primary btn-lg btn-block mt-3" onClick={complete} disabled={busy || !lines.length || !customer}>
              {busy ? 'Completing sale…' : <><Icon name="check" size={18} /> Complete sale · {money(grandTotal)}</>}
            </button>
            {!customer && lines.length > 0 && <div className="small text-warning mt-2 row-flex"><Icon name="alert" size={14} /> Choose a customer to complete the sale.</div>}
          </div>
        </div>
      </div>

      {addingCustomer && (
        <CustomerForm quick onClose={() => setAddingCustomer(false)}
          onSaved={(c) => { setData((d) => ({ ...d, customers: [...d.customers, c] })); setCustomerId(c.custID); }} />
      )}
      {receipt && <ReceiptModal receipt={receipt} cashier={fullName(user)} onClose={() => setReceipt(null)} onViewOrders={() => navigate('/orders')} />}
    </>
  );
}

/* Searchable customer dropdown */
function CustomerPicker({ customers, value, onChange, onAddNew }) {
  const [open, setOpen] = useState(false);
  const [q, setQ] = useState('');
  const [active, setActive] = useState(0);
  const ref = useRef(null);
  const selected = customers.find((c) => c.custID === value);

  useEffect(() => {
    const close = (e) => { if (ref.current && !ref.current.contains(e.target)) setOpen(false); };
    document.addEventListener('mousedown', close);
    return () => document.removeEventListener('mousedown', close);
  }, []);

  const list = customers.filter((c) => matches(q, fullName(c), c.company, c.custID, c.phoneNumber))
    .sort((a, b) => fullName(a).localeCompare(fullName(b))).slice(0, 30);

  const pick = (c) => { onChange(c.custID); setOpen(false); setQ(''); };

  if (selected && !open) {
    return (
      <div className="customer-select">
        <div className="selected">
          <Avatar src={selected.imageUrl} name={fullName(selected)} size={38} />
          <div style={{ flex: 1, minWidth: 0 }}>
            <div className="bold">{fullName(selected)}</div>
            <div className="small muted"><span className="mono">{selected.custID}</span>{selected.company ? ` · ${selected.company}` : ''}</div>
          </div>
          <button className="btn btn-ghost btn-sm" onClick={() => setOpen(true)}>Change</button>
        </div>
      </div>
    );
  }

  return (
    <div className="customer-select" ref={ref}>
      <div className="input-group">
        <span className="lead"><Icon name="user" size={17} /></span>
        <input className="input" value={q} placeholder="Choose customer – type a name…" onFocus={() => setOpen(true)} autoFocus={open}
          onChange={(e) => { setQ(e.target.value); setOpen(true); setActive(0); }}
          onKeyDown={(e) => {
            if (e.key === 'ArrowDown') { e.preventDefault(); setActive((a) => Math.min(a + 1, list.length)); }
            if (e.key === 'ArrowUp') { e.preventDefault(); setActive((a) => Math.max(a - 1, 0)); }
            if (e.key === 'Enter') { e.preventDefault(); if (list[active]) pick(list[active]); else onAddNew(); }
            if (e.key === 'Escape') setOpen(false);
          }} />
        <span style={{ position: 'absolute', right: 10, color: 'var(--faint)', pointerEvents: 'none' }}><Icon name="chevronDown" size={16} /></span>
      </div>
      {open && (
        <div className="suggestions">
          {list.map((c, i) => (
            <div key={c.custID} className={`suggestion${i === active ? ' active' : ''}`} onMouseEnter={() => setActive(i)} onMouseDown={(e) => { e.preventDefault(); pick(c); }}>
              <Avatar src={c.imageUrl} name={fullName(c)} size={32} />
              <div style={{ minWidth: 0 }}>
                <div className="s-title"><Highlight text={fullName(c)} query={q} /></div>
                <div className="s-sub"><span className="mono">{c.custID}</span>{c.company ? ` · ${c.company}` : ''}</div>
              </div>
            </div>
          ))}
          {list.length === 0 && <div className="suggestion-empty">No customer called “{q}”.</div>}
          <div className={`suggestion${active === list.length ? ' active' : ''}`} onMouseDown={(e) => { e.preventDefault(); setOpen(false); onAddNew(); }} style={{ color: 'var(--primary)', borderTop: '1px solid var(--border)', marginTop: 4 }}>
            <Icon name="userPlus" size={18} /> <span className="bold">{q ? `Add “${q}” as a new customer` : 'Add a new customer'}</span>
          </div>
        </div>
      )}
    </div>
  );
}

function ReceiptModal({ receipt, cashier, onClose, onViewOrders }) {
  const { order, customer } = receipt;
  return (
    <Modal title="Sale completed" onClose={onClose} size="sm"
      footer={<>
        <button className="btn btn-secondary" onClick={onViewOrders}>View orders</button>
        <button className="btn btn-secondary" onClick={() => window.print()}><Icon name="printer" size={16} /> Print</button>
        <button className="btn btn-primary" onClick={onClose}>New sale</button>
      </>}>
      <div className="receipt print-area">
        <div className="stamp">PAID</div>
        <div className="r-head">
          <div className="success-check"><Icon name="check" size={28} /></div>
          <h3>{APP_NAME}</h3>
          <div className="muted small">Tax invoice / receipt</div>
          <div className="mono mt-2">{order.orderId}</div>
          <div className="small muted">{formatDateTime(order.orderTime || order.orderDate)}</div>
        </div>
        <div className="row-flex between small"><span className="muted">Customer</span><span className="bold">{fullName(customer)} ({customer.custID})</span></div>
        <div className="row-flex between small mb-4"><span className="muted">Served by</span><span>{cashier}</span></div>
        <table>
          <thead><tr><th>Item</th><th style={{ textAlign: 'center' }}>Qty</th><th style={{ textAlign: 'right' }}>Price</th><th style={{ textAlign: 'right' }}>Total</th></tr></thead>
          <tbody>
            {order.orderItems.map((it) => (
              <tr key={it.orderItemId}>
                <td>{it.productName}</td>
                <td style={{ textAlign: 'center' }}>{it.quantity}</td>
                <td style={{ textAlign: 'right' }}>{money(it.unitPrice)}</td>
                <td style={{ textAlign: 'right' }} className="bold">{money(it.lineTotal)}</td>
              </tr>
            ))}
          </tbody>
        </table>
        <div className="r-total"><span>Grand total</span><span>{money(order.totalAmount)}</span></div>
        <p className="small muted mt-3" style={{ textAlign: 'center' }}>Thank you for your business!</p>
      </div>
    </Modal>
  );
}
