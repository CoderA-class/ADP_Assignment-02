import { useEffect, useMemo, useRef, useState } from 'react';
import Icon from './Icons';
import { Avatar, Highlight } from './ui';

/*
 * Search box used on every list page.
 *  - "Name" mode (default): type a name, matching records pop up underneath as you type.
 *  - "ID" mode (from the dropdown): type or paste an auto-generated ID (e.g. CUS-0004) and press Enter.
 *
 * props:
 *   items         – records to search
 *   getName(item) – display name, getId(item) – the ID, getSub(item) – grey text under the name
 *   getImage(item)– optional picture
 *   onQueryChange(query, mode) – so the page can filter its table too
 *   onSelect(item)– user picked a suggestion
 *   onIdSearch(id)– user pressed Enter in ID mode (page can look it up on the server)
 */
export default function SearchBar({
  items = [], getName, getId, getSub, getImage, getRight, placeholder = 'Search by name…', idPlaceholder = 'Enter ID, e.g. CUS-0001',
  onQueryChange, onSelect, onIdSearch, entityLabel = 'records', squareAvatar = false
}) {
  const [mode, setMode] = useState('name');
  const [query, setQuery] = useState('');
  const [open, setOpen] = useState(false);
  const [active, setActive] = useState(0);
  const wrapRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => { onQueryChange?.(query, mode); }, [query, mode]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    const close = (e) => { if (wrapRef.current && !wrapRef.current.contains(e.target)) setOpen(false); };
    document.addEventListener('mousedown', close);
    return () => document.removeEventListener('mousedown', close);
  }, []);

  const results = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return [];
    return items.filter((item) => {
      if (mode === 'id') return String(getId(item) || '').toLowerCase().includes(q);
      return String(getName(item) || '').toLowerCase().includes(q) || String(getSub?.(item) || '').toLowerCase().includes(q);
    }).slice(0, 8);
  }, [items, query, mode, getId, getName, getSub]);

  useEffect(() => { setActive(0); }, [query, mode]);

  const choose = (item) => {
    setOpen(false);
    onSelect?.(item);
  };

  const onKeyDown = (e) => {
    if (e.key === 'ArrowDown') { e.preventDefault(); setOpen(true); setActive((a) => Math.min(a + 1, results.length - 1)); }
    else if (e.key === 'ArrowUp') { e.preventDefault(); setActive((a) => Math.max(a - 1, 0)); }
    else if (e.key === 'Escape') { setOpen(false); }
    else if (e.key === 'Enter') {
      e.preventDefault();
      if (open && results[active]) choose(results[active]);
      else if (mode === 'id' && query.trim()) { setOpen(false); onIdSearch?.(query.trim().toUpperCase()); }
    }
  };

  return (
    <div className="searchbar" ref={wrapRef}>
      <select className="mode" value={mode} aria-label="Search by"
        onChange={(e) => { setMode(e.target.value); setQuery(''); inputRef.current?.focus(); }}>
        <option value="name">Name</option>
        <option value="id">ID</option>
      </select>
      <div className="field-wrap">
        <Icon name={mode === 'id' ? 'hash' : 'search'} size={17} />
        <input ref={inputRef} value={query} placeholder={mode === 'id' ? idPlaceholder : placeholder}
          onChange={(e) => { setQuery(e.target.value); setOpen(true); }}
          onFocus={() => query && setOpen(true)} onKeyDown={onKeyDown} aria-label="Search" />
        {query && (
          <button className="icon-btn clear" style={{ width: 28, height: 28 }} onClick={() => { setQuery(''); inputRef.current?.focus(); }} aria-label="Clear search">
            <Icon name="x" size={15} />
          </button>
        )}
      </div>
      {open && query.trim() && (
        <div className="suggestions">
          {results.length === 0 ? (
            <div className="suggestion-empty">
              No {entityLabel} found for “{query}”.
              {mode === 'name'
                ? <div className="mt-2"><button className="btn btn-secondary btn-sm" onClick={() => { setMode('id'); setQuery(''); inputRef.current?.focus(); }}><Icon name="hash" size={14} /> Search by ID instead</button></div>
                : onIdSearch && <div className="mt-2"><button className="btn btn-secondary btn-sm" onClick={() => { setOpen(false); onIdSearch(query.trim().toUpperCase()); }}>Look up “{query.trim().toUpperCase()}” on the server</button></div>}
            </div>
          ) : results.map((item, i) => (
            <div key={getId(item)} className={`suggestion${i === active ? ' active' : ''}`} onMouseEnter={() => setActive(i)} onMouseDown={(e) => { e.preventDefault(); choose(item); }}>
              <Avatar src={getImage?.(item)} name={getName(item)} size={34} square={squareAvatar} />
              <div style={{ minWidth: 0 }}>
                <div className="s-title"><Highlight text={getName(item)} query={mode === 'name' ? query : ''} /></div>
                <div className="s-sub"><span className="mono"><Highlight text={getId(item)} query={mode === 'id' ? query : ''} /></span>{getSub?.(item) ? ` · ${getSub(item)}` : ''}</div>
              </div>
              {getRight && <div className="s-right">{getRight(item)}</div>}
            </div>
          ))}
          {results.length > 0 && <div className="suggestion-footer">↑ ↓ to move · Enter to open · {mode === 'name' ? 'Switch the dropdown to “ID” to search by ID' : 'Searching by ID'}</div>}
        </div>
      )}
    </div>
  );
}
