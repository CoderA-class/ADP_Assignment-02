import { money } from '../utils/format';

/* Simple responsive bar chart (no chart library needed) */
export function BarChart({ data, height = 240, valueFormat = money }) {
  const width = 640;
  const pad = { top: 16, right: 8, bottom: 28, left: 56 };
  const max = Math.max(1, ...data.map((d) => d.value));
  const nice = niceMax(max);
  const innerW = width - pad.left - pad.right;
  const innerH = height - pad.top - pad.bottom;
  const step = innerW / data.length;
  const barW = Math.min(36, step * 0.62);
  const ticks = [0, 0.25, 0.5, 0.75, 1].map((t) => t * nice);

  return (
    <svg className="bar-chart" viewBox={`0 0 ${width} ${height}`} role="img" aria-label="Sales chart">
      {ticks.map((t) => {
        const y = pad.top + innerH - (t / nice) * innerH;
        return (
          <g key={t}>
            <line className="grid-line" x1={pad.left} x2={width - pad.right} y1={y} y2={y} />
            <text x={pad.left - 8} y={y + 4} textAnchor="end">{shortMoney(t)}</text>
          </g>
        );
      })}
      {data.map((d, i) => {
        const h = (d.value / nice) * innerH;
        const x = pad.left + i * step + (step - barW) / 2;
        const y = pad.top + innerH - h;
        return (
          <g key={d.label + i}>
            <rect className={`bar${d.highlight ? ' today' : ''}`} x={x} y={y} width={barW} height={Math.max(h, d.value > 0 ? 2 : 0)} rx="4">
              <title>{`${d.full || d.label}: ${valueFormat(d.value)}`}</title>
            </rect>
            {(data.length <= 14 || i % 2 === 0) && <text x={x + barW / 2} y={height - 8} textAnchor="middle">{d.label}</text>}
          </g>
        );
      })}
    </svg>
  );
}

/* Small area sparkline. data = array of numbers */
export function Sparkline({ data = [], width = 240, height = 56, color = 'var(--accent)', fill = true }) {
  if (!data.length) return null;
  const max = Math.max(...data, 1);
  const step = width / Math.max(data.length - 1, 1);
  const pts = data.map((v, i) => [i * step, height - 4 - (v / max) * (height - 10)]);
  const line = pts.map((p, i) => `${i ? 'L' : 'M'}${p[0].toFixed(1)} ${p[1].toFixed(1)}`).join(' ');
  const id = `sg${Math.random().toString(36).slice(2, 8)}`;
  return (
    <svg className="spark" viewBox={`0 0 ${width} ${height}`} aria-hidden="true">
      <defs><linearGradient id={id} x1="0" x2="0" y1="0" y2="1"><stop offset="0" stopColor={color} stopOpacity=".35" /><stop offset="1" stopColor={color} stopOpacity="0" /></linearGradient></defs>
      {fill && <path d={`${line} L${width} ${height} L0 ${height} Z`} fill={`url(#${id})`} />}
      <path className="line" d={line} fill="none" stroke={color} strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" />
      <circle cx={pts[pts.length - 1][0]} cy={pts[pts.length - 1][1]} r="3.5" fill={color} />
    </svg>
  );
}

/* Donut chart with legend */
export function Donut({ data, size = 150, thickness = 22, centerLabel, centerValue }) {
  const total = data.reduce((s, d) => s + d.value, 0) || 1;
  const r = (size - thickness) / 2;
  const c = 2 * Math.PI * r;
  let offset = 0;
  return (
    <div className="row-flex" style={{ gap: 20, alignItems: 'center' }}>
      <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} style={{ flexShrink: 0 }}>
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="#eef2f7" strokeWidth={thickness} />
        {data.map((d) => {
          const len = (d.value / total) * c;
          const el = (
            <circle key={d.label} cx={size / 2} cy={size / 2} r={r} fill="none" stroke={d.color} strokeWidth={thickness}
              strokeDasharray={`${Math.max(len - 2, 0)} ${c - Math.max(len - 2, 0)}`} strokeDashoffset={-offset}
              transform={`rotate(-90 ${size / 2} ${size / 2})`}>
              <title>{`${d.label}: ${d.value}`}</title>
            </circle>
          );
          offset += len;
          return el;
        })}
        <text x="50%" y="47%" textAnchor="middle" style={{ fontSize: 20, fontWeight: 900, fill: 'var(--ink)' }}>{centerValue}</text>
        <text x="50%" y="62%" textAnchor="middle" style={{ fontSize: 11, fill: 'var(--muted)' }}>{centerLabel}</text>
      </svg>
      <div className="legend" style={{ flex: 1, minWidth: 0 }}>
        {data.map((d) => (
          <div className="legend-row" key={d.label}>
            <span className="sw" style={{ background: d.color }} />
            <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{d.label}</span>
            <span className="v">{d.display ?? d.value}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

function niceMax(v) {
  const p = Math.pow(10, Math.floor(Math.log10(v)));
  const n = v / p;
  const m = n <= 1 ? 1 : n <= 2 ? 2 : n <= 5 ? 5 : 10;
  return m * p;
}

function shortMoney(v) {
  if (v >= 1_000_000) return `R${(v / 1_000_000).toFixed(1)}m`;
  if (v >= 1000) return `R${(v / 1000).toFixed(v >= 10000 ? 0 : 1)}k`;
  return `R${Math.round(v)}`;
}
