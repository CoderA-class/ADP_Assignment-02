import { useMemo, useState } from 'react';
import { useLocation } from 'react-router-dom';
import Icon from '../components/Icons';
import SearchBar from '../components/SearchBar';
import { Avatar, Badge, ConfirmDialog, Drawer, EmptyState, ErrorState, Field, IdNotice, ImageUpload, Loading, Modal, PageHeader, ProductImage, StatCard, StockMeter, useCategoryVersion } from '../components/ui';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { useApi } from '../hooks/useApi';
import { api } from '../services/api';
import { categoryColor, categoryIcon, colorFor, matches, money, number, registerCategories, stockStatus, timeAgo } from '../utils/format';
import { storage } from '../utils/storage';
import { AlertAdminButton, AlertAdminModal, AlertPill, ContactSupplier, MyAlertsPanel, StaffAlertsPanel, activeAlertFor } from '../components/StockAlerts';
import CategoryManager from '../components/CategoryManager';


export default function Products() {
  useCategoryVersion();   // tiles follow the category colours the admin picked
  const { user, isAdmin } = useAuth();
  const toast = useToast();
  const location = useLocation();
  const { data, loading, error, reload } = useApi(async () => {
    const [products, suppliers, alerts, categoryList] = await Promise.all([
      api.products.all(), api.suppliers.all(), api.stockAlerts.list().catch(() => []), api.categories.all().catch(() => [])
    ]);
    registerCategories(categoryList);
    return { products, suppliers, alerts, categoryList };
  });
  const [query, setQuery] = useState({ text: '', mode: 'name' });
  const [stockFilter, setStockFilter] = useState(location.state?.filter || 'all');
  const [category, setCategory] = useState('All');
  const [view, setView] = useState(() => storage.get('stockflow.productView', 'grid'));
  const [editing, setEditing] = useState(null);
  const [viewing, setViewing] = useState(null);
  const [restocking, setRestocking] = useState(null);
  const [deleting, setDeleting] = useState(null);
  const [alerting, setAlerting] = useState(null);
  const [managingCategories, setManagingCategories] = useState(false);

  const products = data?.products || [];
  const suppliers = data?.suppliers || [];
  const alerts = data?.alerts || [];
  const supplierName = (id) => suppliers.find((s) => s.supplierId === id)?.name;

  const categoryList = data?.categoryList || [];
  const categories = useMemo(() => ['All', ...[...new Set([...categoryList.map((c) => c.name), ...products.map((p) => p.category || 'General')])].sort()], [products, categoryList]);
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
        {isAdmin && <button className="btn btn-secondary" onClick={() => setManagingCategories(true)}><Icon name="tag" size={15} /> Categories</button>}
        {isAdmin && <button className="btn btn-primary" onClick={() => setEditing({})}><Icon name="plus" size={17} /> Add product</button>}
      </PageHeader>

      <div className="grid grid-4 mb-4">
        <StatCard icon="box" tone="blue" label="Products" value={number(products.length)} foot={`${categories.length - 1} categories`} />
        <StatCard icon="money" tone="green" label="Stock value" value={money(stockValue)} />
        <StatCard icon="alert" tone="amber" label="Low stock" value={counts.low} foot="At or below re-order level" />
        <StatCard icon="x" tone="red" label="Out of stock" value={counts.out} />
      </div>

      {isAdmin
        ? <StaffAlertsPanel alerts={alerts} products={products} suppliers={suppliers} onRestock={setRestocking} onChanged={() => reload({ silent: true })} />
        : <MyAlertsPanel alerts={alerts} employeeId={user?.empID} />}

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
                <div key={p.id} className={`product-stack pc-${s.key}${activeAlertFor(alerts, p.id) ? ' pc-alerted' : ''}`} style={{ '--cat': categoryColor(p.category) }}>
                  <div className="product-card" onClick={() => setViewing(p)}>
                    <div className="pc-tags">
                      <span className="tag"><span className="cat-dot" style={{ background: categoryColor(p.category) }} /> {p.category || 'General'}</span>
                      <span className="tag"><Icon name="bolt" size={10} /> {p.id}</span>
                    </div>
                    <div className="thumb"><ProductImage src={p.imageUrl} name={p.name} category={p.category} /><Badge tone={s.tone}>{s.label}</Badge></div>
                    <div className="pc-title">
                      <div style={{ minWidth: 0 }}>
                        <div className="pc-sub">{p.quantity} in stock</div>
                        <div className="name">{p.name}</div>
                      </div>
                      <span className="round-btn" aria-hidden="true"><Icon name="arrowUpRight" size={13} /></span>
                    </div>
                  </div>
                  <div className="pc-pill" onClick={() => setViewing(p)}>
                    <span className="pc-ico"><Icon name="tag" size={14} /></span>
                    <div className="grow"><div className="pc-label">Selling price</div><div className="pc-value">{money(p.price)}</div></div>
                  </div>
                  <div className="pc-pill" onClick={() => setViewing(p)}>
                    <span className="pc-ico"><Icon name="boxes" size={14} /></span>
                    <div className="grow">
                      <div className="pc-label">Stock level · re-order at {p.reorderLevel}</div>
                      <div className={`pc-value ${s.key === 'ok' ? '' : s.key === 'low' ? 'text-warning' : 'text-danger'}`}>{s.label}</div>
                      <div className="mt-1"><StockMeter product={p} /></div>
                    </div>
                  </div>
                  {isAdmin
                    ? (activeAlertFor(alerts, p.id)
                      ? <button className="btn btn-cta btn-block" onClick={() => setRestocking(p)}><Icon name="bullhorn" size={13} /> Staff alert · Restock</button>
                      : s.key !== 'ok' && <button className="btn btn-cta btn-block" onClick={() => setRestocking(p)}><Icon name="plus" size={13} /> Restock</button>)
                    : s.key !== 'ok' && <AlertAdminButton product={p} alert={activeAlertFor(alerts, p.id)} onClick={setAlerting} block />}
                </div>
              );
            })}
          </div>
        ) : (
          <div className="table-wrap">
            <table className="table">
              <thead><tr><th>Product</th><th>Category</th><th>Supplier</th><th className="num">Price</th><th className="num">In stock</th><th>Status</th><th /></tr></thead>
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
                      <td><Badge tone={s.tone}>{s.label}</Badge>{isAdmin && activeAlertFor(alerts, p.id) && <div className="mt-1"><AlertPill alert={activeAlertFor(alerts, p.id)} isAdmin /></div>}</td>
                      {!isAdmin && <td className="actions" onClick={(e) => e.stopPropagation()}><AlertAdminButton product={p} alert={activeAlertFor(alerts, p.id)} onClick={setAlerting} /></td>}
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

      {managingCategories && <CategoryManager categories={categoryList} products={products} onClose={() => setManagingCategories(false)} onChanged={() => reload({ silent: true })} />}
      {editing && <ProductForm product={editing} suppliers={suppliers} categories={categories.filter((c) => c !== 'All')} onCategoryAdded={() => reload({ silent: true })} onClose={() => setEditing(null)} onSaved={(p) => { reload({ silent: true }); if (viewing) setViewing(p); }} />}
      {restocking && <RestockModal product={restocking} onClose={() => setRestocking(null)} onSaved={(p) => { reload({ silent: true }); if (viewing) setViewing(p); }} />}
      {alerting && <AlertAdminModal product={alerting} onClose={() => setAlerting(null)} onSent={() => reload({ silent: true })} />}
      {deleting && <ConfirmDialog title="Delete product?" message={`${deleting.name} (${deleting.id}) will be removed from your catalogue.`} onConfirm={() => remove(deleting)} onClose={() => setDeleting(null)} />}
      {viewing && (
        <Drawer title="Product details" onClose={() => setViewing(null)}
          footer={isAdmin ? <>
            <button className="btn btn-soft-danger" onClick={() => setDeleting(viewing)}><Icon name="trash" size={16} /></button>
            <button className="btn btn-secondary" onClick={() => setEditing(viewing)}><Icon name="edit" size={16} /> Edit</button>
            <button className="btn btn-primary" onClick={() => setRestocking(viewing)}><Icon name="plus" size={16} /> Restock</button>
          </> : stockStatus(viewing).key !== 'ok' && <AlertAdminButton product={viewing} alert={activeAlertFor(alerts, viewing.id)} onClick={setAlerting} size="lg" block />}>
          <div style={{ borderRadius: 12, overflow: 'hidden', aspectRatio: '16 / 10', background: 'var(--surface-2)', marginBottom: 16 }}>
            <ProductImage src={viewing.imageUrl} name={viewing.name} category={viewing.category} iconSize={44} />
          </div>
          <div className="row-flex between"><h3 style={{ fontSize: 18 }}>{viewing.name}</h3><Badge tone={stockStatus(viewing).tone}>{stockStatus(viewing).label}</Badge></div>
          <div className="mono muted mt-1">{viewing.id}</div>
          {activeAlertFor(alerts, viewing.id) && (
            <div className="tip-note mt-3" style={{ maxWidth: 'none' }}><Icon name="bullhorn" size={14} /><div>
              <strong>{activeAlertFor(alerts, viewing.id).raisedByName}</strong> alerted the admins {timeAgo(activeAlertFor(alerts, viewing.id).createdAt)}
              {activeAlertFor(alerts, viewing.id).note ? ` – “${activeAlertFor(alerts, viewing.id).note}”` : '.'}
            </div></div>
          )}
          {viewing.description && <p className="mt-3">{viewing.description}</p>}
          <div className="grid grid-2 mt-4">
            <div className="card stat-card" style={{ padding: 14 }}><div><div className="stat-label">Price</div><div className="stat-value" style={{ fontSize: 20 }}>{money(viewing.price)}</div></div></div>
            <div className="card stat-card" style={{ padding: 14 }}><div><div className="stat-label">In stock</div><div className="stat-value" style={{ fontSize: 20 }}>{viewing.quantity}</div></div></div>
          </div>
          <dl className="detail-list mt-4">
            <dt>Category</dt><dd>{viewing.category}</dd>
            <dt>Supplier</dt><dd>{supplierName(viewing.supplierId) || '—'}
              {isAdmin && suppliers.find((s) => s.supplierId === viewing.supplierId) && (
                <div className="mt-1"><ContactSupplier supplier={suppliers.find((s) => s.supplierId === viewing.supplierId)} product={viewing} /></div>
              )}
            </dd>
            <dt>Re-order level</dt><dd>{viewing.reorderLevel} units</dd>
            <dt>Stock value</dt><dd>{money(viewing.price * viewing.quantity)}</dd>
          </dl>
        </Drawer>
      )}
    </>
  );
}

function ProductForm({ product, suppliers, categories, onClose, onSaved, onCategoryAdded }) {
  const toast = useToast();
  const editing = Boolean(product?.id);
  const [form, setForm] = useState({ name: '', price: '', quantity: '', category: '', description: '', supplierId: '', reorderLevel: 10, imageUrl: '', ...product });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e?.target ? e.target.value : e }));
  const allCategories = [...new Set([...categories, ...(form.category ? [form.category] : [])])].sort();
  const [newCategory, setNewCategory] = useState(null);   // text while adding a category from this form
  const addCategory = async () => {
    const name = (newCategory || '').trim();
    if (!name) return;
    try {
      const created = await api.categories.create({ name });
      setForm((f) => ({ ...f, category: created.name }));
      setNewCategory(null);
      onCategoryAdded?.();
      toast.success('Category added', created.name);
    } catch (err) {
      toast.error('Could not add the category', err.message);
    }
  };

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
        <Field label="Category" hint={newCategory === null ? 'Choose one, or add a new category' : 'Press Enter or Add to save the new category'}>
          {newCategory === null ? (
            <select className="select" value={form.category || ''} onChange={(e) => (e.target.value === '__new' ? setNewCategory('') : set('category')(e))}>
              <option value="">Choose a category</option>
              {allCategories.map((c) => <option key={c} value={c}>{c}</option>)}
              <option value="__new">+ New category…</option>
            </select>
          ) : (
            <div className="row-flex" style={{ gap: 6 }}>
              <input className="input" value={newCategory} onChange={(e) => setNewCategory(e.target.value)} placeholder="e.g. Electronics" autoFocus
                onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); addCategory(); } if (e.key === 'Escape') setNewCategory(null); }} />
              <button type="button" className="btn btn-primary btn-sm" onClick={addCategory}>Add</button>
              <button type="button" className="icon-btn" title="Cancel" onClick={() => setNewCategory(null)}><Icon name="x" size={14} /></button>
            </div>
          )}
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
