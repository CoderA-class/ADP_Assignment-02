import { useState } from 'react';
import Icon from './Icons';
import { Field, Modal } from './ui';
import { useToast } from '../context/ToastContext';
import { api } from '../services/api';
import { CATEGORY_COLOR_CHOICES, CATEGORY_ICON_CHOICES, categoryColor, categoryIcon } from '../utils/format';

/* Admin: add, rename, re-style and remove stock categories (e.g. "Electronics") */
export default function CategoryManager({ categories, products, onClose, onChanged }) {
  const toast = useToast();
  const [editing, setEditing] = useState(null);   // { id?, name, icon, color }
  const [busy, setBusy] = useState(false);
  const count = (name) => products.filter((p) => (p.category || '').toLowerCase() === name.toLowerCase()).length;

  const save = async (e) => {
    e?.preventDefault();
    if (!editing.name.trim()) return toast.error('Category', 'Please enter a name.');
    setBusy(true);
    try {
      const payload = { name: editing.name.trim(), icon: editing.icon || null, color: editing.color || null };
      const saved = editing.id ? await api.categories.update(editing.id, payload) : await api.categories.create(payload);
      toast.success(editing.id ? 'Category updated' : 'Category added', saved.name);
      setEditing(null);
      onChanged?.();
    } catch (err) {
      toast.error('Could not save the category', err.message);
    } finally {
      setBusy(false);
    }
  };

  const remove = async (c) => {
    try {
      await api.categories.remove(c.id);
      toast.success('Category removed', c.name);
      onChanged?.();
    } catch (err) {
      toast.error('Could not remove the category', err.message);
    }
  };

  const preview = editing && { icon: editing.icon || categoryIcon(editing.name), color: editing.color || categoryColor(editing.name || 'New') };

  return (
    <Modal title="Stock categories" subtitle="Group your products, e.g. Electronics, Bakery or Cleaning." onClose={onClose}
      footer={editing
        ? <><button className="btn btn-secondary" onClick={() => setEditing(null)} disabled={busy}>Back</button><button className="btn btn-primary" onClick={save} disabled={busy}>{busy ? 'Saving…' : editing.id ? 'Save changes' : 'Add category'}</button></>
        : <><button className="btn btn-secondary" onClick={onClose}>Close</button><button className="btn btn-primary" onClick={() => setEditing({ name: '', icon: '', color: '' })}><Icon name="plus" size={15} /> New category</button></>}>
      {editing ? (
        <form onSubmit={save} className="stack" style={{ gap: 16 }}>
          <div className="cat-preview">
            <span className="cat-badge" style={{ background: `${preview.color}22`, color: preview.color }}><Icon name={preview.icon} size={22} /></span>
            <div><div className="bold">{editing.name || 'New category'}</div><div className="small muted">This is how its products look without a picture</div></div>
          </div>
          <Field label="Name" required><input className="input" value={editing.name} maxLength={40} onChange={(e) => setEditing((c) => ({ ...c, name: e.target.value }))} placeholder="e.g. Electronics" autoFocus /></Field>
          <Field label="Icon">
            <div className="icon-choices">
              {CATEGORY_ICON_CHOICES.map((ic) => (
                <button type="button" key={ic} className={preview.icon === ic ? 'on' : ''} onClick={() => setEditing((c) => ({ ...c, icon: ic }))} aria-label={ic}><Icon name={ic} size={16} /></button>
              ))}
            </div>
          </Field>
          <Field label="Colour">
            <div className="color-choices">
              {CATEGORY_COLOR_CHOICES.map((col) => (
                <button type="button" key={col} className={preview.color === col ? 'on' : ''} style={{ background: col }} onClick={() => setEditing((c) => ({ ...c, color: col }))} aria-label={`Colour ${col}`} />
              ))}
            </div>
          </Field>
          {editing.id && <p className="small muted">Renaming moves its {count(categories.find((x) => x.id === editing.id)?.name || '')} product(s) to the new name.</p>}
          <button type="submit" hidden />
        </form>
      ) : (
        <div className="cat-list">
          {categories.length === 0 && <p className="muted">No categories yet – add your first one.</p>}
          {categories.map((c) => (
            <div className="cat-row" key={c.id}>
              <span className="cat-badge" style={{ background: `${categoryColor(c.name)}22`, color: categoryColor(c.name) }}><Icon name={categoryIcon(c.name)} size={17} /></span>
              <div className="grow"><div className="bold">{c.name}</div><div className="small muted">{count(c.name)} product{count(c.name) === 1 ? '' : 's'}</div></div>
              <button className="icon-btn" title="Edit" onClick={() => setEditing({ id: c.id, name: c.name, icon: c.icon || '', color: c.color || '' })}><Icon name="edit" size={15} /></button>
              <button className="icon-btn danger" title={count(c.name) ? 'Move its products first' : 'Remove'} disabled={count(c.name) > 0} onClick={() => remove(c)}><Icon name="trash" size={15} /></button>
            </div>
          ))}
        </div>
      )}
    </Modal>
  );
}
