import { useMemo, useState } from 'react';
import { useLocation } from 'react-router-dom';
import Icon from '../components/Icons';
import SearchBar from '../components/SearchBar';
import { Avatar, Badge, ConfirmDialog, Drawer, EmptyState, ErrorState, Field, IdNotice, ImageUpload, Loading, Modal, PageHeader, ProductImage, StatCard, StockMeter } from '../components/ui';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { useApi } from '../hooks/useApi';
import { api } from '../services/api';
import { categoryColor, categoryIcon, colorFor, matches, money, number, stockStatus } from '../utils/format';
import { storage } from '../utils/storage';

const CATEGORY_SUGGESTIONS = ['Bakery', 'Beverages', 'Dairy', 'Pantry', 'Snacks', 'Cleaning', 'Frozen', 'Personal Care', 'Airtime', 'General'];

export default function Products() {
  const { isAdmin } = useAuth();
  const toast = useToast();
  const location = useLocation();
  const { data, loading, error, reload } = useApi(async () => {
    const [products, suppliers] = await Promise.all([api.products.all(), api.suppliers.all()]);
    return { products, suppliers };
  });
  const [query, setQuery] = useState({ text: '', mode: 'name' });
  const [stockFilter, setStockFilter] = useState(location.state?.filter || 'all');
  const [category, setCategory] = useState('All');
  const [view, setView] = useState(() => storage.get('stockflow.productView', 'grid'));
  const [editing, setEditing] = useState(null);
  const [viewing, setViewing] = useState(null);
  const [restocking, setRestocking] = useState(null);
  const [deleting, setDeleting] = useState(null);

  const products = data?.products || [];
  const suppliers = data?.suppliers || [];
  const supplierName = (id) => suppliers.find((s) => s.supplierId === id)?.name;

  const categories = useMemo(() => ['All', ...[...new Set(products.map((p) => p.category || 'General'))].sort()], [products]);
  const counts = useMemo(() => ({
    all: products.length,
    low: products.filter((p) => stockStatus(p).key === 'low').length,
    out: products.filter((p) => stockStatus(p).key === 'out').length
  }), [products]);

  const rows = useMemo(() => products
    .filter((p) => stockFilter === 'all' || stockStatus(p).key === stockFilter)
    .filter((p) => category === 'All' || (p.category || 'General') === category)
    .filter((p) => (query.mode === 'id' ? matches(query.text, p.id) : matches(query.text, p.name, p.category, supplierName(p.supplierId))))
    .sort((a, b) => a.name.localeCompare(b.name)), [products, stockFilter, category, query]); // eslint-disable-line react-hooks/exhaustive-deps

  const setViewMode = (v) => { setView(v); storage.set('stockflow.productView', v); };

  const lookUpId = async (id) => {
    try { setViewing(await api.products.read(id)); } catch { toast.warning('Not found', `There is no product with ID ${id}.`); }
  };

  const remove = async (p) => {
    await api.products.remove(p.id);
    toast.success('Product deleted', p.name);
    setViewing(null);
    reload({ silent: true });
  };

  if (loading) return <Loading />;
  if (error) return <ErrorState error={error} onRetry={reload} />;

  const stockValue = products.reduce((s, p) => s + p.price * p.quantity, 0);

  return (
    <>
      <PageHeader eyebrow="Inventory" title="Products & Stock" subtitle={isAdmin ? 'Add products with a name, price and quantity – the ID is created for you.' : 'See what is in stock and what is running low.'}>
        {isAdmin && <button className="btn btn-primary" onClick={() => setEditing({})}><Icon name="plus" size={17} /> Add product</button>}
      </PageHeader>

      <div className="grid grid-4 mb-4">
        <StatCard icon="box" tone="blue" label="Products" value={number(products.length)} foot={`${categories.length - 1} categories`} />
        <StatCard icon="money" tone="green" label="Stock value" value={money(stockValue)} />
        <StatCard icon="alert" tone="amber" label="Low stock" value={counts.low} foot="At or below re-order level" />
        <StatCard icon="x" tone="red" label="Out of stock" value={counts.out} />
      </div>

      <div className="card">
        <div className="toolbar">
          <SearchBar items={products} entityLabel="products" squareAvatar getId={(p) => p.id} getName={(p) => p.name} getSub={(p) => p.category}
            getImage={(p) => p.imageUrl} getRight={(p) => <><div className="bold" style={{ color: 'var(--text)' }}>{money(p.price)}</div><div>{p.quantity} in stock</div></>}
            placeholder="Search products by name or category…" idPlaceholder="Enter product ID, e.g. PRD-0001"
            onQueryChange={(text, mode) => setQuery({ text, mode })} onSelect={setViewing} onIdSearch={lookUpId} />
          <div className="chips">
            {[['all', 'All'], ['low', 'Low stock'], ['out', 'Out of stock']].map(([k, l]) => (
              <button key={k} className={`chip${stockFilter === k ? ' active' : ''}`} onClick={() => setStockFilter(k)}>{l} <span className="count">{counts[k]}</span></button>
            ))}
          </div>
          <select className="select" style={{ width: 170, height: 34 }} value={category} onChange={(e) => setCategory(e.target.value)} aria-label="Category">
            {categories.map((c) => <option key={c} value={c}>{c === 'All' ? 'All categories' : c}</option>)}
          </select>
          <div className="view-toggle" style={{ marginLeft: 'auto' }}>
            <button className={view === 'grid' ? 'active' : ''} onClick={() => setViewMode('grid')} title="Grid view"><Icon name="grid" size={16} /></button>
            <button className={view === 'list' ? 'active' : ''} onClick={() => setViewMode('list')} title="List view"><Icon name="list" size={16} /></button>
          </div>
        </div>

        {rows.length === 0 ? (
          <EmptyState icon="box" title={products.length ? 'No matching products' : 'No products yet'}
            message={products.length ? 'Try another name or filter, or search by ID.' : 'Add your first product to start selling.'}
            action={!products.length && isAdmin && <button className="btn btn-primary" onClick={() => setEditing({})}><Icon name="plus" size={16} /> Add product</button>} />
        ) : view === 'grid' ? (
          <div className="product-grid">
            {rows.map((p) => {
              const s = stockStatus(p);
              return (
                <div key={p.id} className="product-card" onClick={() => setViewing(p)}>
                  <div className="thumb"><ProductImage src={p.imageUrl} name={p.name} category={p.category} /><Badge tone={s.tone}>{s.label}</Badge></div>
                  <div className="body">
                    <div className="name">{p.name}</div>
                    <div className="meta"><span className="mono">{p.id}</span> · {p.category}</div>
                    <div className="row">
                      <span className="price">{money(p.price)}</span>
                      <span className={`small bold ${s.key === 'ok' ? 'muted' : s.key === 'low' ? 'text-warning' : 'text-danger'}`}>{p.quantity} in stock</span>
                    </div>
                    <div className="mt-1"><StockMeter product={p} /></div>
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="table-wrap">
            <table className="table">
              <thead><tr><th>Product</th><th>Category</th><th>Supplier</th><th className="num">Price</th><th className="num">In stock</th><th>Status</th>{isAdmin && <th />}</tr></thead>
              <tbody>
                {rows.map((p) => {
                  const s = stockStatus(p);
                  return (
                    <tr key={p.id} className="clickable" onClick={() => setViewing(p)}>
                      <td><div className="cell-entity"><Avatar src={p.imageUrl} name={p.name} tint={categoryColor(p.category)} icon={categoryIcon(p.category)} square size={40} /><div><div className="t">{p.name}</div><div className="s mono">{p.id}</div></div></div></td>
                      <td>{p.category}</td>
                      <td>{supplierName(p.supplierId) || <span className="muted">—</span>}</td>
                      <td className="num">{money(p.price)}</td>
                      <td className="num bold" style={{ minWidth: 110 }}>{p.quantity}<div className="mt-1"><StockMeter product={p} /></div></td>
                      <td><Badge tone={s.tone}>{s.label}</Badge></td>
                      {isAdmin && (
                        <td className="actions" onClick={(e) => e.stopPropagation()}>
                          <button className="btn btn-secondary btn-sm" onClick={() => setRestocking(p)}><Icon name="plus" size={14} /> Restock</button>
                          <button className="icon-btn" title="Edit" onClick={() => setEditing(p)}><Icon name="edit" size={16} /></button>
                          <button className="icon-btn danger" title="Delete" onClick={() => setDeleting(p)}><Icon name="trash" size={16} /></button>
                        </td>
                      )}
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {editing && <ProductForm product={editing} suppliers={suppliers} categories={categories.filter((c) => c !== 'All')} onClose={() => setEditing(null)} onSaved={(p) => { reload({ silent: true }); if (viewing) setViewing(p); }} />}
      {restocking && <RestockModal product={restocking} onClose={() => setRestocking(null)} onSaved={(p) => { reload({ silent: true }); if (viewing) setViewing(p); }} />}
      {deleting && <ConfirmDialog title="Delete product?" message={`${deleting.name} (${deleting.id}) will be removed from your catalogue.`} onConfirm={() => remove(deleting)} onClose={() => setDeleting(null)} />}
      {viewing && (
        <Drawer title="Product details" onClose={() => setViewing(null)}
          footer={isAdmin && <>
            <button className="btn btn-soft-danger" onClick={() => setDeleting(viewing)}><Icon name="trash" size={16} /></button>
            <button className="btn btn-secondary" onClick={() => setEditing(viewing)}><Icon name="edit" size={16} /> Edit</button>
            <button className="btn btn-primary" onClick={() => setRestocking(viewing)}><Icon name="plus" size={16} /> Restock</button>
          </>}>
          <div style={{ borderRadius: 12, overflow: 'hidden', aspectRatio: '16 / 10', background: 'var(--surface-2)', marginBottom: 16 }}>
            <ProductImage src={viewing.imageUrl} name={viewing.name} category={viewing.category} iconSize={44} />
          </div>
          <div className="row-flex between"><h3 style={{ fontSize: 18 }}>{viewing.name}</h3><Badge tone={stockStatus(viewing).tone}>{stockStatus(viewing).label}</Badge></div>
          <div className="mono muted mt-1">{viewing.id}</div>
          {viewing.description && <p className="mt-3">{viewing.description}</p>}
          <div className="grid grid-2 mt-4">
            <div className="card stat-card" style={{ padding: 14 }}><div><div className="stat-label">Price</div><div className="stat-value" style={{ fontSize: 20 }}>{money(viewing.price)}</div></div></div>
            <div className="card stat-card" style={{ padding: 14 }}><div><div className="stat-label">In stock</div><div className="stat-value" style={{ fontSize: 20 }}>{viewing.quantity}</div></div></div>
          </div>
          <dl className="detail-list mt-4">
            <dt>Category</dt><dd>{viewing.category}</dd>
            <dt>Supplier</dt><dd>{supplierName(viewing.supplierId) || '—'}</dd>
            <dt>Re-order level</dt><dd>{viewing.reorderLevel} units</dd>
            <dt>Stock value</dt><dd>{money(viewing.price * viewing.quantity)}</dd>
          </dl>
        </Drawer>
      )}
    </>
  );
}

function ProductForm({ product, suppliers, categories, onClose, onSaved }) {
  const toast = useToast();
  const editing = Boolean(product?.id);
  const [form, setForm] = useState({ name: '', price: '', quantity: '', category: '', description: '', supplierId: '', reorderLevel: 10, imageUrl: '', ...product });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e?.target ? e.target.value : e }));
  const allCategories = [...new Set([...categories, ...CATEGORY_SUGGESTIONS])];

  const save = async (e) => {
    e?.preventDefault();
    if (!form.name.trim()) return setError('Please enter the product name.');
    if (form.price === '' || Number(form.price) < 0) return setError('Please enter a valid price.');
    if (form.quantity === '' || Number(form.quantity) < 0) return setError('Please enter how many are in stock.');
    setBusy(true);
    setError('');
    try {
      const payload = { ...form, price: Number(form.price), quantity: parseInt(form.quantity, 10), reorderLevel: parseInt(form.reorderLevel || 10, 10), supplierId: form.supplierId || null };
      const saved = editing ? await api.products.update(payload) : await api.products.create({ ...payload, id: '' });
      toast.success(editing ? 'Product updated' : 'Product added', `${saved.name} · ${saved.id}`);
      onSaved?.(saved);
      onClose();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Modal title={editing ? 'Edit product' : 'Add product'} subtitle="Name, price and quantity are required." onClose={onClose} size="lg"
      footer={<>
        <button className="btn btn-secondary" onClick={onClose} disabled={busy}>Cancel</button>
        <button className="btn btn-primary" onClick={save} disabled={busy}>{busy ? 'Saving…' : editing ? 'Save changes' : 'Add product'}</button>
      </>}>
      <form className="form-grid" onSubmit={save}>
        <div className="full"><IdNotice id={form.id} prefix="PRD" /></div>
        <Field full label="Picture"><ImageUpload value={form.imageUrl} onChange={set('imageUrl')} name={form.name} onError={(m) => toast.error('Image', m)} /></Field>
        <Field full label="Product name" required><input className="input" value={form.name} onChange={set('name')} placeholder="e.g. White Sugar 10kg" autoFocus /></Field>
        <Field label="Selling price" required><div className="input-prefix"><span>R</span><input className="input" type="number" min="0" step="0.01" value={form.price} onChange={set('price')} placeholder="0.00" /></div></Field>
        <Field label="Quantity in stock" required><input className="input" type="number" min="0" step="1" value={form.quantity} onChange={set('quantity')} placeholder="0" /></Field>
        <Field label="Category" hint="Pick one or type a new one">
          <input className="input" list="category-list" value={form.category} onChange={set('category')} placeholder="e.g. Pantry" />
          <datalist id="category-list">{allCategories.map((c) => <option key={c} value={c} />)}</datalist>
        </Field>
        <Field label="Re-order level" hint="You'll be notified when stock drops to this"><input className="input" type="number" min="0" value={form.reorderLevel} onChange={set('reorderLevel')} /></Field>
        <Field full label="Supplier">
          <select className="select" value={form.supplierId || ''} onChange={set('supplierId')}>
            <option value="">— No supplier —</option>
            {suppliers.map((s) => <option key={s.supplierId} value={s.supplierId}>{s.name}</option>)}
          </select>
        </Field>
        <Field full label="Description"><textarea className="textarea" value={form.description || ''} onChange={set('description')} placeholder="Optional notes, pack size, etc." /></Field>
        {error && <div className="full text-danger small">{error}</div>}
        <button type="submit" hidden />
      </form>
    </Modal>
  );
}

function RestockModal({ product, onClose, onSaved }) {
  const toast = useToast();
  const [amount, setAmount] = useState('');
  const [busy, setBusy] = useState(false);
  const save = async (e) => {
    e?.preventDefault();
    const n = parseInt(amount, 10);
    if (!n || n <= 0) return toast.error('Restock', 'Enter how many units arrived.');
    setBusy(true);
    try {
      const saved = await api.products.restock(product.id, n);
      toast.success('Stock updated', `${saved.name} now has ${saved.quantity} in stock.`);
      onSaved?.(saved);
      onClose();
    } catch (err) {
      toast.error('Restock failed', err.message);
    } finally {
      setBusy(false);
    }
  };
  return (
    <Modal title={`Restock ${product.name}`} subtitle={`Currently ${product.quantity} in stock`} onClose={onClose} size="sm"
      footer={<>
        <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
        <button className="btn btn-primary" onClick={save} disabled={busy}>{busy ? 'Saving…' : 'Add stock'}</button>
      </>}>
      <form onSubmit={save} className="stack">
        <Field label="Units received">
          <input className="input" type="number" min="1" value={amount} onChange={(e) => setAmount(e.target.value)} autoFocus placeholder="e.g. 24" />
        </Field>
        <div className="chips">{[6, 12, 24, 50, 100].map((n) => <button type="button" key={n} className="chip" onClick={() => setAmount(String(n))}>+{n}</button>)}</div>
        {amount > 0 && <p className="small muted">New stock level: <strong>{product.quantity + parseInt(amount, 10)}</strong></p>}
      </form>
    </Modal>
  );
}
