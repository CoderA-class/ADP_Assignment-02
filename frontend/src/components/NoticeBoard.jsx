import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Icon from './Icons';
import { StickyNote } from './ui';

const COLORS = ['yellow', 'pink', 'mint', 'sky', 'peach', 'lilac'];
/* stickers a user can put on their own notes */
const STICKERS = ['note', 'lightbulb', 'sparkle', 'fire', 'phone', 'truck', 'cart', 'calendar', 'coffee', 'gift', 'rocket', 'smile', 'bell', 'money'];
const STICKER_LABELS = { note: 'Note', lightbulb: 'Idea', sparkle: 'Important', fire: 'Urgent', phone: 'Call', truck: 'Delivery', cart: 'Order', calendar: 'Date', coffee: 'Break', gift: 'Special', rocket: 'Goal', smile: 'Nice', bell: 'Reminder', money: 'Payment' };
const TILTS = [-2.5, 2, -1.5, 3, -2, 1.5];
const storageKey = (userId) => `stockflow.notes.${userId || 'me'}`;

function loadNotes(userId) {
  try { return JSON.parse(localStorage.getItem(storageKey(userId))) || []; } catch { return []; }
}

/* The dashboard notice board: automatic notes from the system plus the user's own sticky notes (kept in this browser) */
export default function NoticeBoard({ userId, autoNotes = [] }) {
  const [notes, setNotes] = useState(() => loadNotes(userId));
  const [picking, setPicking] = useState(null); // id of the note whose sticker picker is open

  useEffect(() => {
    try { localStorage.setItem(storageKey(userId), JSON.stringify(notes)); } catch { /* storage unavailable */ }
  }, [notes, userId]);

  const add = () => {
    const id = Date.now();
    setNotes((n) => [...n, { id, title: '', text: '', color: COLORS[n.length % COLORS.length], icon: STICKERS[n.length % STICKERS.length], pinned: false, done: false }]);
    setPicking(id);
  };
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
        <StickyNote key={n.id} color={n.color} pinned={n.pinned} className={`own${n.done ? ' done' : ''}`} tilt={TILTS[(i + autoNotes.length) % TILTS.length]}
          foot={<>
            <span className="color-dots">
              {COLORS.map((c) => (
                <button key={c} type="button" aria-label={`Make note ${c}`} onClick={() => update(n.id, { color: c })}
                  style={{ background: `var(--note-${c})`, outline: n.color === c ? '2px solid rgba(0,0,0,.35)' : 'none' }} />
              ))}
            </span>
            <span className="note-tools">
              <button type="button" className={n.done ? 'on' : ''} onClick={() => update(n.id, { done: !n.done })} title={n.done ? 'Mark as not done' : 'Mark as done'} aria-label="Toggle done"><Icon name="check" size={13} /></button>
              <button type="button" className={n.pinned ? 'on' : ''} onClick={() => update(n.id, { pinned: !n.pinned })} title={n.pinned ? 'Use tape' : 'Pin it'} aria-label="Toggle pin"><Icon name="pinNote" size={13} /></button>
              <button type="button" onClick={() => remove(n.id)} title="Remove note" aria-label="Remove note"><Icon name="trash" size={13} /></button>
            </span>
          </>}>
          <div className="note-head">
            <button type="button" className="sticker" onClick={() => setPicking(picking === n.id ? null : n.id)} title="Choose a sticker" aria-label="Choose a sticker">
              <Icon name={n.icon || 'note'} size={16} />
            </button>
            <input className="note-title" value={n.title || ''} maxLength={40} placeholder={STICKER_LABELS[n.icon] || 'Title'} onChange={(e) => update(n.id, { title: e.target.value })} aria-label="Note title" />
          </div>
          {picking === n.id && (
            <div className="sticker-picker" role="listbox" aria-label="Stickers">
              {STICKERS.map((ic) => (
                <button key={ic} type="button" className={n.icon === ic ? 'on' : ''} title={STICKER_LABELS[ic]} aria-label={STICKER_LABELS[ic]}
                  onClick={() => { update(n.id, { icon: ic }); setPicking(null); }}><Icon name={ic} size={15} /></button>
              ))}
            </div>
          )}
          <textarea value={n.text} placeholder="Write a reminder…" onChange={(e) => update(n.id, { text: e.target.value })} aria-label="Note text" />
          {n.done && <span className="done-stamp" aria-hidden="true">DONE</span>}
        </StickyNote>
      ))}
      <button type="button" className="sticky-add" onClick={add}>
        <span className="row-flex"><Icon name="plus" size={14} /> Add note</span>
      </button>
    </div>
  );
}
