import React from 'react';
import ReactDOM from 'react-dom/client';
import './styles.css';
import CustomerApp from './components/CustomerApp';
import OrderItem from './components/orderitem.jsx';

ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <CustomerApp />
        <OrderItem />
    </React.StrictMode>
);