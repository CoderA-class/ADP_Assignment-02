import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import './styles.css';
import CustomerApp from './components/CustomerApp';
import OrderItem from './components/orderitem.jsx';
import EmployeeApp from './components/EmployeeApp';
import ProductApp from './components/ProductApp';
import SupplierApp from './components/SupplierApp';

function App() {
    return (
        <BrowserRouter basename="/adp-assignment">
            <div className="app-container">
                <nav style={{ padding: '1rem', background: '#f0f0f0', marginBottom: '1rem', display: 'flex', gap: '1rem' }}>
                    <Link to="/" style={{ fontWeight: 'bold', textDecoration: 'none', color: '#333' }}>Customers</Link>
                    <Link to="/employees" style={{ fontWeight: 'bold', textDecoration: 'none', color: '#333' }}>Employees</Link>
                    <Link to="/order-items" style={{ fontWeight: 'bold', textDecoration: 'none', color: '#333' }}>Order Items</Link>
                    <Link to="/products" style={{ fontWeight: 'bold', textDecoration: 'none', color: '#333' }}>Products</Link>
                    <Link to="/suppliers" style={{ fontWeight: 'bold', textDecoration: 'none', color: '#333' }}>Suppliers</Link>
                </nav>

                <div style={{ padding: '1rem' }}>
                    <Routes>
                        <Route path="/" element={<CustomerApp />} />
                        <Route path="/employees" element={<EmployeeApp />} />
                        <Route path="/order-items" element={<OrderItem />} />
                        <Route path="/products" element={<ProductApp />} />
                        <Route path="/suppliers" element={<SupplierApp />} />
                    </Routes>
                </div>
            </div>
        </BrowserRouter>
    );
}

ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
);