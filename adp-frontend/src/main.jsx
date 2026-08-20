import React from 'react';
import ReactDOM from 'react-dom/client';
import './styles.css';
import CustomerApp from './components/CustomerApp';
import OrderItem from './components/OrderItem';
import ProductApp from './components/ProductApp';

ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <CustomerApp />
        <OrderItem />
        <ProductApp />
    </React.StrictMode>
);
