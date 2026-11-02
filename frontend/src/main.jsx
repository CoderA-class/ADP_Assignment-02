import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter, NavLink, Routes, Route } from 'react-router-dom';
import './styles.css';
import Home from './components/Home';
import CustomerApp from './components/CustomerApp';
import OrderItem from './components/orderitem.jsx';
import EmployeeApp from './components/EmployeeApp';
import OrderApp from './components/OrderApp';
import ProductApp from './components/ProductApp';
import SupplierApp from './components/SupplierApp';

const navigation = [
    { path: '/', label: 'Dashboard', end: true },
    { path: '/customers', label: 'Customers' },
    { path: '/employees', label: 'Employees' },
    { path: '/orders', label: 'Orders' },
    { path: '/order-items', label: 'Order items' },
    { path: '/products', label: 'Products' },
    { path: '/suppliers', label: 'Suppliers' }
];

function App() {
    return (
        <BrowserRouter basename="/adp-assignment">
            <div className="app-container">
                <header className="app-nav">
                    <NavLink to="/" className="app-brand" aria-label="Nexus dashboard">
                        <span className="app-brand-mark">N</span>
                        <span>NEXUS <span>OPERATIONS</span></span>
                    </NavLink>
                    <nav className="app-nav-links" aria-label="Main navigation">
                        {navigation.map((item) => (
                            <NavLink
                                key={item.path}
                                to={item.path}
                                end={item.end}
                                className={({ isActive }) => isActive ? 'app-nav-link active' : 'app-nav-link'}
                            >
                                {item.label}
                            </NavLink>
                        ))}
                    </nav>
                </header>
                <Routes>
                    <Route path="/" element={<Home />} />
                    <Route path="/customers" element={<CustomerApp />} />
                    <Route path="/employees" element={<EmployeeApp />} />
                    <Route path="/orders" element={<OrderApp />} />
                    <Route path="/order-items" element={<OrderItem />} />
                    <Route path="/products" element={<ProductApp />} />
                    <Route path="/suppliers" element={<SupplierApp />} />
                </Routes>
            </div>
        </BrowserRouter>
    );
}

ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
);