import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getCustomers } from '../services/customerService';
import { getEmployees } from '../services/employeeService';
import { getOrders } from '../services/orderService';
import { getOrderItems } from '../services/orderItemService';
import { getProducts } from '../services/productService';
import { getSuppliers } from '../services/supplierService';

const modules = [
  {
    path: '/customers',
    title: 'Customers',
    description: 'Customer records',
    tone: 'lime',
    load: getCustomers,
  },
  {
    path: '/orders',
    title: 'Orders',
    description: 'Sales orders',
    tone: 'blue',
    load: getOrders,
  },
  {
    path: '/products',
    title: 'Products',
    description: 'Product catalogue',
    tone: 'orange',
    load: getProducts,
  },
  {
    path: '/suppliers',
    title: 'Suppliers',
    description: 'Supplier records',
    tone: 'pink',
    load: getSuppliers,
  },
  {
    path: '/employees',
    title: 'Employees',
    description: 'People and roles',
    tone: 'violet',
    load: getEmployees,
  },
  {
    path: '/order-items',
    title: 'Order items',
    description: 'Order line items',
    tone: 'teal',
    load: getOrderItems,
  },
];

export default function Home() {
  const [counts, setCounts] = useState({});
  const [connectionStatus, setConnectionStatus] = useState('Loading records');

  useEffect(() => {
    let active = true;

    Promise.allSettled(modules.map((module) => module.load())).then((results) => {
      if (!active) return;
      setCounts(Object.fromEntries(results.map((result, index) => [
        modules[index].title,
        result.status === 'fulfilled' && Array.isArray(result.value) ? result.value.length : null,
      ])));
      setConnectionStatus(results.every((result) => result.status === 'fulfilled')
        ? 'Connected to backend'
        : 'Some records unavailable');
    });

    return () => {
      active = false;
    };
  }, []);

  return (
    <main className="page-shell dashboard-shell">
      <header className="dashboard-heading">
        <div>
          <p className="eyebrow">Nexus control centre</p>
          <h1>Operations dashboard</h1>
          <p className="dashboard-subtitle">A live overview of the records managed across your workspace.</p>
        </div>
        <div className="header-pill">
          <span className={`status-dot ${connectionStatus === 'Some records unavailable' ? 'warning' : ''}`} />
          {connectionStatus}
        </div>
      </header>

        <section className="module-section" aria-labelledby="module-heading">
          <div className="module-heading-row">
            <div>
              <p className="mini-label">Workspace overview</p>
              <h2 id="module-heading">Record totals</h2>
            </div>
            <p className="module-count">Select a section to manage records</p>
          </div>
          <div className="module-grid">
            {modules.map((module) => (
              <Link to={module.path} className={`module-card dashboard-module ${module.tone}`} key={module.path}>
                <div className="module-card-top">
                  <span>{module.description}</span>
                  <span className="module-arrow" aria-hidden="true">&rarr;</span>
                </div>
                <div className="dashboard-module-value">
                  <span>{counts[module.title] ?? '--'}</span>
                  <h3>{module.title}</h3>
                </div>
                <span className="module-card-line" />
              </Link>
            ))}
          </div>
        </section>
    </main>
  );
}
