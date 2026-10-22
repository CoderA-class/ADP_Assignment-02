import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import './styles.css';
import Home from './components/Home';
import CustomerApp from './components/CustomerApp';
import OrderItem from './components/orderitem.jsx';
import EmployeeApp from './components/EmployeeApp';
import OrderApp from './components/OrderApp';
import ProductApp from './components/ProductApp';
import SupplierApp from './components/SupplierApp';

function App() {
    return (
        <BrowserRouter basename="/adp-assignment">
            <div className="app-container">
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