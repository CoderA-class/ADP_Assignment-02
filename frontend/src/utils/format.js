const currency = new Intl.NumberFormat('en-ZA', { style: 'currency', currency: 'ZAR', minimumFractionDigits: 2 });

/* 15% South African VAT, added on top of selling prices (the server does the same at checkout) */
export const VAT_RATE = 0.15;
const cents = (n) => Math.round((Number(n) || 0) * 100) / 100;
export function withVat(subtotal) {
  const sub = cents(subtotal);
  const vat = cents(sub * VAT_RATE);
  return { subtotal: sub, vat, total: cents(sub + vat) };
}
/* an order's breakdown – orders from before VAT was added have no subtotal, so they show as-is */
export function orderBreakdown(order) {
  if (order?.subtotal == null) return { subtotal: order?.totalAmount || 0, vat: null, total: order?.totalAmount || 0 };
  return { subtotal: order.subtotal, vat: order.vatAmount || 0, total: order.totalAmount };
}

/* how a sale was paid */
export const PAYMENT_METHODS = [
  { key: 'CASH', label: 'Cash', icon: 'money' },
  { key: 'CARD', label: 'Card', icon: 'tag' },
  { key: 'EFT', label: 'EFT', icon: 'building' },
  { key: 'ACCOUNT', label: 'On account', icon: 'clipboard' }
];
export const paymentLabel = (key) => PAYMENT_METHODS.find((m) => m.key === key)?.label || 'Paid';
/* balance still owed on an order (orders from before payments were tracked count as paid) */
export const balanceDue = (o) => (o?.balanceDue != null ? o.balanceDue : 0);
export function paymentStatus(o) {
  const s = o?.paymentStatus || 'PAID';
  return { PAID: { label: 'Paid', tone: 'green' }, PARTIAL: { label: 'Part paid', tone: 'amber' }, UNPAID: { label: 'Unpaid', tone: 'red' } }[s] || { label: 'Paid', tone: 'green' };
}

export const money = (value) => currency.format(Number(value) || 0).replace('ZAR', 'R');

export const number = (value) => new Intl.NumberFormat('en-ZA').format(Number(value) || 0);

export function toDate(value) {
  if (!value) return null;
  if (value instanceof Date) return value;
  // "2026-11-02" should be treated as a local date, not UTC midnight
  if (/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    const [y, m, d] = value.split('-').map(Number);
    return new Date(y, m - 1, d);
  }
  return new Date(value);
}

export function formatDate(value, opts = { day: 'numeric', month: 'short', year: 'numeric' }) {
  const d = toDate(value);
  return d ? d.toLocaleDateString('en-ZA', opts) : '—';
}

export function formatDateTime(value) {
  const d = toDate(value);
  return d ? `${d.toLocaleDateString('en-ZA', { day: 'numeric', month: 'short' })}, ${d.toLocaleTimeString('en-ZA', { hour: '2-digit', minute: '2-digit' })}` : '—';
}

export function formatTime(value) {
  const d = toDate(value);
  return d ? d.toLocaleTimeString('en-ZA', { hour: '2-digit', minute: '2-digit' }) : '—';
}

export function timeAgo(value) {
  const d = toDate(value);
  if (!d) return '';
  const s = Math.floor((Date.now() - d.getTime()) / 1000);
  if (s < 60) return 'just now';
  const m = Math.floor(s / 60);
  if (m < 60) return `${m} min ago`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h} hr${h > 1 ? 's' : ''} ago`;
  const days = Math.floor(h / 24);
  if (days < 7) return `${days} day${days > 1 ? 's' : ''} ago`;
  return formatDate(d);
}

export function daysSince(value) {
  const d = toDate(value);
  if (!d) return null;
  const start = new Date(); start.setHours(0, 0, 0, 0);
  const then = new Date(d); then.setHours(0, 0, 0, 0);
  return Math.round((start - then) / 86400000);
}

export const todayISO = () => {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};

export const minutesToHours = (mins) => `${Math.floor((mins || 0) / 60)}h ${String((mins || 0) % 60).padStart(2, '0')}m`;

export function initials(name = '') {
  const parts = String(name).trim().split(/\s+/).filter((w) => /^[A-Za-zÀ-ÿ]/.test(w));
  if (!parts.length) return '?';
  return (parts[0][0] + (parts.length > 1 ? parts[parts.length - 1][0] : '')).toUpperCase();
}

export const PALETTE = ['#1f4f99', '#2f7bd6', '#c2410c', '#7a5af5', '#1f9d61', '#d6453d', '#b26f00', '#c03862', '#0e7490', '#4b5c6d'];
export function colorFor(key = '') {
  let hash = 0;
  for (const ch of String(key)) hash = (hash * 31 + ch.charCodeAt(0)) >>> 0;
  return PALETTE[hash % PALETTE.length];
}

/* Fixed colours for common wholesale categories so they're always easy to tell apart */
const CATEGORY_COLORS = {
  bakery: '#b45309', dairy: '#0891b2', beverages: '#2251cc', drinks: '#2251cc', pantry: '#15803d', groceries: '#15803d',
  snacks: '#c2410c', frozen: '#0e7490', cleaning: '#7c3aed', 'personal care': '#be185d', airtime: '#4f46e5', general: '#475569'
};
/* Categories the admin created (with their own icon / colour) – filled in from /categories */
const CUSTOM = {};
export function registerCategories(list = []) {
  Object.keys(CUSTOM).forEach((k) => delete CUSTOM[k]);
  list.forEach((c) => { if (c?.name) CUSTOM[c.name.trim().toLowerCase()] = c; });
  window.dispatchEvent(new Event('categories-changed'));
}
const key = (category) => String(category || 'General').trim().toLowerCase();
export const categoryColor = (category) => CUSTOM[key(category)]?.color || CATEGORY_COLORS[key(category)] || colorFor(category);

const CATEGORY_ICONS = {
  bakery: 'bread', dairy: 'cheese', beverages: 'bottle', drinks: 'bottle', pantry: 'jar', groceries: 'jar', snacks: 'cookie',
  frozen: 'snowflake', cleaning: 'spray', 'personal care': 'soap', airtime: 'mobile', general: 'box', produce: 'carrot', eggs: 'egg', grains: 'wheat'
};
export const categoryIcon = (category) => CUSTOM[key(category)]?.icon || CATEGORY_ICONS[key(category)] || 'box';
export const CATEGORY_ICON_CHOICES = ['box', 'bread', 'cheese', 'bottle', 'jar', 'cookie', 'snowflake', 'spray', 'soap', 'mobile', 'carrot', 'egg', 'wheat', 'coffee', 'gift', 'truck', 'tag', 'fire', 'lightbulb', 'store'];
export const CATEGORY_COLOR_CHOICES = ['#1f4f99', '#2563b8', '#7a5af5', '#c03862', '#c2410c', '#b26f00', '#15803d', '#0e7490', '#4b5c6d', '#be185d'];

export const fullName = (p) => `${p?.firstName || ''} ${p?.lastName || ''}`.trim();

export const titleCase = (s = '') => String(s).toLowerCase().replace(/(^|[\s_])\w/g, (m) => m.replace('_', ' ').toUpperCase());

/* Case-insensitive "does any of these fields contain the text" */
export function matches(text, ...fields) {
  const q = String(text || '').trim().toLowerCase();
  if (!q) return true;
  return fields.some((f) => String(f ?? '').toLowerCase().includes(q));
}

export function stockStatus(product) {
  const qty = product?.quantity ?? 0;
  const reorder = product?.reorderLevel ?? 10;
  if (qty <= 0) return { label: 'Out of stock', tone: 'red', key: 'out' };
  if (qty <= reorder) return { label: 'Low stock', tone: 'amber', key: 'low' };
  return { label: 'In stock', tone: 'green', key: 'ok' };
}
