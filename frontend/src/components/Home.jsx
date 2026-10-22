import { Link } from 'react-router-dom';

const modules = [
  {
    path: '/customers',
    label: '01 / Relationships',
    title: 'Customers',
    description: 'Maintain customer profiles, contact details, and account records.',
    tone: 'lime',
  },
  {
    path: '/orders',
    label: '02 / Revenue',
    title: 'Orders',
    description: 'Track order value, dates, and the customers behind every sale.',
    tone: 'blue',
  },
  {
    path: '/products',
    label: '03 / Inventory',
    title: 'Products',
    description: 'Keep your catalogue, pricing, quantities, and categories current.',
    tone: 'orange',
  },
  {
    path: '/suppliers',
    label: '04 / Procurement',
    title: 'Suppliers',
    description: 'Organise the businesses and contacts that keep operations moving.',
    tone: 'pink',
  },
  {
    path: '/employees',
    label: '05 / People',
    title: 'Employees',
    description: 'Manage staff details, roles, contact information, and status.',
    tone: 'violet',
  },
  {
    path: '/order-items',
    label: '06 / Fulfilment',
    title: 'Order items',
    description: 'Review the individual products connected to each order.',
    tone: 'teal',
  },
];

export default function Home() {
  return (
    <div className="home-shell">
      <header className="home-nav">
        <Link to="/" className="brand-mark" aria-label="Nexus home">
          <span className="brand-mark-icon">N</span>
          <span>NEXUS<span className="brand-mark-dot">.</span></span>
        </Link>
        <div className="home-nav-meta">
          <span className="status-dot" />
          <span>Operations workspace</span>
        </div>
      </header>

      <main>
        <section className="home-hero">
          <div className="hero-copy">
            <p className="eyebrow">Business operations / 2026</p>
            <h1>Everything your team needs to <em>move forward.</em></h1>
            <p className="hero-description">
              One focused workspace for the people, products, partners, and orders that power your business.
            </p>
          </div>
          <div className="hero-orbit" aria-hidden="true">
            <div className="orbit-ring orbit-ring-one" />
            <div className="orbit-ring orbit-ring-two" />
            <div className="orbit-core">N</div>
            <span className="orbit-label orbit-label-top">connect</span>
            <span className="orbit-label orbit-label-bottom">operate</span>
          </div>
        </section>

        <section className="module-section" aria-labelledby="module-heading">
          <div className="module-heading-row">
            <div>
              <p className="mini-label">Your toolkit</p>
              <h2 id="module-heading">Choose a workspace</h2>
            </div>
            <p className="module-count">06 modules / one source of truth</p>
          </div>
          <div className="module-grid">
            {modules.map((module) => (
              <Link to={module.path} className={`module-card ${module.tone}`} key={module.path}>
                <div className="module-card-top">
                  <span>{module.label}</span>
                  <span className="module-arrow" aria-hidden="true">-&gt;</span>
                </div>
                <div>
                  <h3>{module.title}</h3>
                  <p>{module.description}</p>
                </div>
                <span className="module-card-line" />
              </Link>
            ))}
          </div>
        </section>
      </main>

      <footer className="home-footer">
        <span>NEXUS OPERATIONS PLATFORM</span>
        <span>System ready</span>
      </footer>
    </div>
  );
}
