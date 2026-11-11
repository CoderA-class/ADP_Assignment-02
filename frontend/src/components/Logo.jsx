/* Nexus logo: an "N" drawn as a small network – three links joining four nodes,
   with one node highlighted. Stands for everything in the business being connected. */
export default function Logo({ size = 22, accent = '#8fb4ff' }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path d="M6 18V6l12 12V6" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" />
      <circle cx="6" cy="6" r="2.4" fill="currentColor" />
      <circle cx="6" cy="18" r="2.4" fill="currentColor" />
      <circle cx="18" cy="18" r="2.4" fill="currentColor" />
      <circle cx="18" cy="6" r="2.9" fill={accent} />
    </svg>
  );
}
