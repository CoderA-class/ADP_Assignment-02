import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Icon from './Icons';
import { StickyNote } from './ui';

const COLORS = ['yellow', 'pink', 'mint', 'sky', 'peach', 'lilac'];
const TILTS = [-2.5, 2, -1.5, 3, -2, 1.5];
const storageKey = (userId) => `stockflow.notes.${userId || 'me'}`;

function loadNotes(userId) {
  try { return JSON.parse(localStorage.getItem(storageKey(userId))) || []; } catch { return []; }
}

/* The dashboard notice board: automatic notes from the system plus the user's own sticky notes (kept in this browser) */
export default function NoticeBoard({ userId, autoNotes = [] }) {
  const [notes, setNotes] = useState(() => loadNotes(userId));

  useEffect(() => {
    try { localStorage.setItem(storageKey(userId), JSON.stringify(notes)); } catch { /* storage unavailable */ }
  }, [notes, userId]);

  const add = () => setNotes((n) => [...n, { id: Date.now(), text: '', color: COLORS[n.length % COLORS.length] }]);
  const update = (id, patch) => setNotes((n) => n.map((x) => (x.id === id ? { ...x, ...patch } : x)));
  const remove = (id) => setNotes((n) => n.filter((x) => x.id !== id));

  return (
    <div className="sticky-board">
      {autoNotes.map((a, i) => (
        <StickyNote key={a.key} color={a.color} tilt={TILTS[i % TILTS.length]} pinned icon={a.icon} title={a.title}
          foot={a.link && <Link to={a.link} state={a.state}>{a.linkLabel || 'Open'} <Icon name="arrowRight" size={11} /></Link>}>
          <p>{a.text}</p>
        </StickyNote>
      ))}
      {notes.map((n, i) => (
        <StickyNote key={n.id} color={n.color} tilt={TILTS[(i + autoNotes.length) % TILTS.length]}
          foot={<>
            <span className="color-dots">
              {COLORS.map((c) => (
                <button key={c} type="button" aria-label={`Make note ${c}`} onClick={() => update(n.id, { color: c })}
                  style={{ background: `var(--note-${c})`, outline: n.color === c ? '2px solid rgba(0,0,0,.35)' : 'none' }} />
              ))}
            </span>
            <button type="button" onClick={() => remove(n.id)} aria-label="Remove note"><Icon name="trash" size={13} /></button>
          </>}>
          <textarea value={n.text} placeholder="Write a reminder…" onChange={(e) => update(n.id, { text: e.target.value })} aria-label="Note text" />
        </StickyNote>
      ))}
      <button type="button" className="sticky-add" onClick={add}>
        <span className="row-flex"><Icon name="plus" size={14} /> Add note</span>
      </button>
    </div>
  );
}
