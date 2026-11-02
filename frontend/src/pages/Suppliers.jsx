import { useMemo, useState } from 'react';
import Icon from '../components/Icons';
import SearchBar from '../components/SearchBar';
import { Avatar, Badge, ConfirmDialog, Drawer, EmptyState, ErrorState, Field, IdNotice, ImageUpload, Loading, Modal, PageHeader } from '../components/ui';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { useApi } from '../hooks/useApi';
import { api } from '../services/api';
import { matches, money, stockStatus } from '../utils/format';

export default function Suppliers() {
  const { isAdmin } = useAuth();
  const toast = useToast();
  const { data, loading, error, reload } = useApi(async () => {
    const [suppliers, products] = await Promise.all([api.suppliers.all(), api.products.all()]);
    return { suppliers, products };
  });
  const [query, setQuery] = useState({ text: '', mode: 'name' });
  const [editing, setEditing] = useState(null);
  const [viewing, setViewing] = useState(null);
  const [deleting, setDeleting] = useState(null);

  const suppliers = data?.suppliers || [];
  const productsOf = (id) => (data?.products || []).filter((p) => p.supplierId === id);

  const rows = useMemo(() => suppliers
    .filter((s) => (query.mode === 'id' ? matches(query.text, s.supplierId) : matches(query.text, s.name, s.contactPerson, s.email)))
    .sort((a, b) => a.name.localeCompare(b.name)), [suppliers, query]);

  const lookUpId = async (id) => {
    try { setViewing(await api.suppliers.read(id)); } catch { toast.warning('Not found', `There is no supplier with ID ${id}.`); }
  };

  const remove = async (s) => {
    await api.suppliers.remove(s.supplierId);
    toast.success('Supplier deleted', s.name);
    setViewing(null);
    reload({ silent: true });
  };

  if (loading) return <Loading />;
  if (error) return <ErrorState error={error} onRetry={reload} />;

  return (
    <>
      <PageHeader eyebrow="Inventory" title="Suppliers" subtitle="Who you buy your stock from.">
        {isAdmin && <button className="btn btn-primary" onClick={() => setEditing({})}><Icon name="plus" size={17} /> Add supplier</button>}
      </PageHeader>
      <div className="card">
        <div className="toolbar">
          <SearchBar items={suppliers} entityLabel="suppliers" squareAvatar getId={(s) => s.supplierId} getName={(s) => s.name} getSub={(s) => s.contactPerson}
            getImage={(s) => s.imageUrl} placeholder="Search suppliers by name or contact…" idPlaceholder="Enter supplier ID, e.g. SUP-0001"
            onQueryChange={(text, mode) => setQuery({ text, mode })} onSelect={setViewing} onIdSearch={lookUpId} />
        </div>
        {rows.length === 0 ? (
          <EmptyState icon="truck" title={suppliers.length ? 'No matching suppliers' : 'No suppliers yet'} message={suppliers.length ? 'Try another name or the supplier ID.' : 'Add the companies you buy stock from.'}
            action={!suppliers.length && isAdmin && <button className="btn btn-primary" onClick={() => setEditing({})}><Icon name="plus" size={16} /> Add supplier</button>} />
        ) : (
          <div className="table-wrap">
            <table className="table">
              <thead><tr><th>Supplier</th><th>Contact person</th><th>Phone</th><th>Email</th><th className="num">Products</th>{isAdmin && <th />}</tr></thead>
              <tbody>
                {rows.map((s) => {
                  const products = productsOf(s.supplierId);
                  const low = products.filter((p) => stockStatus(p).key !== 'ok').length;
                  return (
                    <tr key={s.supplierId} className="clickable" onClick={() => setViewing(s)}>
                      <td><div className="cell-entity"><Avatar src={s.imageUrl} name={s.name} square icon="truck" size={40} /><div><div className="t">{s.name}</div><div className="s mono">{s.supplierId}</div></div></div></td>
                      <td>{s.contactPerson || '—'}</td>
                      <td>{s.phone || '—'}</td>
                      <td>{s.email || '—'}</td>
                      <td className="num">{products.length} {low > 0 && <Badge tone="amber">{low} low</Badge>}</td>
                      {isAdmin && (
                        <td className="actions" onClick={(e) => e.stopPropagation()}>
                          <button className="icon-btn" title="Edit" onClick={() => setEditing(s)}><Icon name="edit" size={16} /></button>
                          <button className="icon-btn danger" title="Delete" onClick={() => setDeleting(s)}><Icon name="trash" size={16} /></button>
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

      {editing && <SupplierForm supplier={editing} onClose={() => setEditing(null)} onSaved={(s) => { reload({ silent: true }); if (viewing) setViewing(s); }} />}
      {deleting && <ConfirmDialog title="Delete supplier?" message={`${deleting.name} will be removed. Their products stay, but will no longer be linked to a supplier.`} onConfirm={() => remove(deleting)} onClose={() => setDeleting(null)} />}
      {viewing && (
        <Drawer title="Supplier details" onClose={() => setViewing(null)}
          footer={isAdmin && <button className="btn btn-secondary" onClick={() => setEditing(viewing)}><Icon name="edit" size={16} /> Edit</button>}>
          <div className="profile-hero">
            <Avatar src={viewing.imageUrl} name={viewing.name} size={84} square icon="truck" />
            <h3>{viewing.name}</h3>
            <span className="mono muted">{viewing.supplierId}</span>
          </div>
          <dl className="detail-list">
            <dt>Contact person</dt><dd>{viewing.contactPerson || '—'}</dd>
            <dt>Phone</dt><dd>{viewing.phone || '—'}</dd>
            <dt>Email</dt><dd>{viewing.email || '—'}</dd>
            <dt>Address</dt><dd>{viewing.address || '—'}</dd>
          </dl>
          <div className="divider" />
          <h4 style={{ fontSize: 14, marginBottom: 8 }}>Products supplied ({productsOf(viewing.supplierId).length})</h4>
          {productsOf(viewing.supplierId).length === 0 ? <p className="muted small">No products linked yet – choose this supplier when adding a product.</p> : (
            <div className="card">
              {productsOf(viewing.supplierId).map((p) => {
                const st = stockStatus(p);
                return (
                  <div className="list-row" key={p.id} style={{ padding: '10px 14px' }}>
                    <Avatar src={p.imageUrl} name={p.name} size={32} square />
                    <div className="grow"><div className="t">{p.name}</div><div className="s">{money(p.price)} · {p.quantity} in stock</div></div>
                    <Badge tone={st.tone}>{st.label}</Badge>
                  </div>
                );
              })}
            </div>
          )}
        </Drawer>
      )}
    </>
  );
}

function SupplierForm({ supplier, onClose, onSaved }) {
  const toast = useToast();
  const editing = Boolean(supplier?.supplierId);
  const [form, setForm] = useState({ name: '', contactPerson: '', email: '', phone: '', address: '', imageUrl: '', ...supplier });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e?.target ? e.target.value : e }));

  const save = async (e) => {
    e?.preventDefault();
    if (!form.name.trim()) return setError('Please enter the supplier name.');
    setBusy(true);
    setError('');
    try {
      const saved = editing ? await api.suppliers.update(form) : await api.suppliers.create({ ...form, supplierId: '' });
      toast.success(editing ? 'Supplier updated' : 'Supplier added', `${saved.name} · ${saved.supplierId}`);
      onSaved?.(saved);
      onClose();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Modal title={editing ? 'Edit supplier' : 'Add supplier'} subtitle="Only the company name is required." onClose={onClose}
      footer={<>
        <button className="btn btn-secondary" onClick={onClose} disabled={busy}>Cancel</button>
        <button className="btn btn-primary" onClick={save} disabled={busy}>{busy ? 'Saving…' : editing ? 'Save changes' : 'Add supplier'}</button>
      </>}>
      <form className="form-grid" onSubmit={save}>
        <div className="full"><IdNotice id={form.supplierId} prefix="SUP" /></div>
        <Field full label="Logo / picture"><ImageUpload value={form.imageUrl} onChange={set('imageUrl')} name={form.name} onError={(m) => toast.error('Image', m)} /></Field>
        <Field full label="Company name" required><input className="input" value={form.name} onChange={set('name')} placeholder="e.g. Sunrise Beverages" autoFocus /></Field>
        <Field label="Contact person"><input className="input" value={form.contactPerson} onChange={set('contactPerson')} /></Field>
        <Field label="Phone"><input className="input" value={form.phone} onChange={set('phone')} /></Field>
        <Field full label="Email"><input className="input" type="email" value={form.email} onChange={set('email')} /></Field>
        <Field full label="Address"><input className="input" value={form.address} onChange={set('address')} /></Field>
        {error && <div className="full text-danger small">{error}</div>}
        <button type="submit" hidden />
      </form>
    </Modal>
  );
}
