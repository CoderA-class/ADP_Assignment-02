import { useEffect, useMemo, useState } from 'react';
import {
    getOrderItems,
    createOrderItem,
    updateOrderItem,
    deleteOrderItem
} from '../services/orderItemService';
import { getOrders } from '../services/orderService';
import { getProducts } from '../services/productService';
import { getCustomers } from '../services/customerService';

const emptyOrderItem = {
    orderItemId: '',
    orderId: '',
    productId: '',
    productName: '',
    customerId: '',
    customerName: '',
    quantity: '',
    lineTotal: ''
};

export default function OrderItem() {
    const [orderItems, setOrderItems] = useState([]);
    const [orders, setOrders] = useState([]);
    const [products, setProducts] = useState([]);
    const [customers, setCustomers] = useState([]);
    const [form, setForm] = useState(emptyOrderItem);
    const [editingId, setEditingId] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [search, setSearch] = useState('');

    const fetchOrderItems = async () => {
        try {
            setLoading(true);
            const data = await getOrderItems();
            setOrderItems(data);
            setError('');
        } catch (err) {
            setError('Unable to load order items from the backend.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchOrderItems();
        Promise.all([getOrders(), getProducts(), getCustomers()])
            .then(([orderData, productData, customerData]) => {
                setOrders(orderData);
                setProducts(productData);
                setCustomers(customerData);
            })
            .catch(() => setError('Unable to load products, customers, or orders for sales entry.'));
    }, []);

    const filteredOrderItems = useMemo(() => {
        const term = search.trim().toLowerCase();
        if (!term) return orderItems;

        return orderItems.filter((item) => {
            return (
                item.orderItemId.toLowerCase().includes(term) ||
                item.orderId.toLowerCase().includes(term) ||
                item.productId.toLowerCase().includes(term)
            );
        });
    }, [orderItems, search]);

    const orderItemStats = useMemo(() => {
        const total = orderItems.length;
        const totalQuantity = orderItems.reduce((sum, item) => sum + Number(item.quantity || 0), 0);
        const totalValue = orderItems.reduce((sum, item) => sum + Number(item.lineTotal || 0), 0);
        const avgLineTotal = total ? totalValue / total : 0;

        return {
            total,
            totalQuantity,
            totalValue: totalValue.toFixed(2),
            avgLineTotal: avgLineTotal.toFixed(2)
        };
    }, [orderItems]);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setForm((current) => {
            const next = { ...current, [name]: value };
            if (name === 'productName') {
                const product = products.find((item) =>
                    item.id.toLowerCase() === value.trim().toLowerCase() ||
                    item.name.toLowerCase() === value.trim().toLowerCase()
                );
                next.productId = product ? product.id : '';
                next.lineTotal = product && current.quantity
                    ? (product.price * Number(current.quantity)).toFixed(2)
                    : '';
            }
            if (name === 'quantity' && current.productId) {
                const product = products.find((item) => item.id === current.productId);
                next.lineTotal = product && value ? (product.price * Number(value)).toFixed(2) : '';
            }
            if (name === 'customerName') {
                const customer = customers.find((item) => {
                    const fullName = `${item.firstName} ${item.lastName}`.toLowerCase();
                    return fullName === value.trim().toLowerCase() ||
                        item.email.toLowerCase() === value.trim().toLowerCase();
                });
                next.customerId = customer ? customer.custID : '';
                next.customerName = customer ? `${customer.firstName} ${customer.lastName}` : value;
                const customerOrders = customer
                    ? orders.filter((order) => order.customerId === customer.custID)
                    : [];
                if (customerOrders.length === 1) {
                    next.orderId = customerOrders[0].orderId;
                }
            }
            return next;
        });
    };

    const resetForm = () => {
        setForm(emptyOrderItem);
        setEditingId(null);
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        if (!form.productId || !form.customerId) {
            setError('Choose a customer and a listed item name or barcode before saving.');
            return;
        }

        const payload = {
            orderItemId: form.orderItemId,
            orderId: form.orderId,
            productId: form.productId,
            quantity: Number(form.quantity),
            lineTotal: Number(form.lineTotal)
        };

        try {
            if (editingId) {
                await updateOrderItem(payload);
            } else {
                await createOrderItem(payload);
            }

            resetForm();
            await fetchOrderItems();
        } catch (err) {
            setError(err.message || 'Order item save failed.');
        }
    };

    const handleEdit = (item) => {
        const product = products.find((candidate) => candidate.id === item.productId);
        const order = orders.find((candidate) => candidate.orderId === item.orderId);
        const customer = order && customers.find((candidate) => candidate.custID === order.customerId);
        setForm({
            ...item,
            productName: product ? product.name : item.productId,
            customerId: customer ? customer.custID : '',
            customerName: customer ? `${customer.firstName} ${customer.lastName}` : ''
        });
        setEditingId(item.orderItemId);
    };

    const handleDelete = async (orderItemId) => {
        try {
            await deleteOrderItem(orderItemId);
            await fetchOrderItems();
            if (editingId === orderItemId) {
                resetForm();
            }
        } catch (err) {
            setError(err.message || 'Order item deletion failed.');
        }
    };

    return (
        <div className="page-shell">
            <header className="topbar">
                <div>
                    <p className="eyebrow">Retail / Business Management</p>
                    <h1>Order Item Management</h1>
                </div>
                <div className="header-pill">
                    <span className="status-dot" />
                    System online
                </div>
            </header>

            <section className="stats-grid">
                <article className="stat-card accent">
                    <div className="stat-label">Total order items</div>
                    <div className="stat-value">{orderItemStats.total}</div>
                    <div className="stat-foot">Across all orders</div>
                </article>
                <article className="stat-card">
                    <div className="stat-label">Total quantity</div>
                    <div className="stat-value">{orderItemStats.totalQuantity}</div>
                    <div className="stat-foot">Units ordered</div>
                </article>
                <article className="stat-card">
                    <div className="stat-label">Total value</div>
                    <div className="stat-value">R{orderItemStats.totalValue}</div>
                    <div className="stat-foot">Sum of line totals</div>
                </article>
                <article className="stat-card">
                    <div className="stat-label">Avg line total</div>
                    <div className="stat-value">R{orderItemStats.avgLineTotal}</div>
                    <div className="stat-foot">Per order item</div>
                </article>
            </section>

            <main className="content-grid">
                <section className="panel form-panel">
                    <div className="section-header">
                        <div>
                            <p className="mini-label">Order item record</p>
                            <h2>{editingId ? 'Update order item' : 'Add order item'}</h2>
                        </div>
                    </div>

                    <form onSubmit={handleSubmit} className="orderitem-form">
                        {!editingId && <p className="form-hint">The line ID is generated automatically.</p>}
                        <div className="two-column">
                            <label>
                                Customer
                                <input
                                    name="customerName"
                                    value={form.customerName}
                                    onChange={handleChange}
                                    list="customer-options"
                                    placeholder="Type a customer name or email"
                                    required
                                />
                                <datalist id="customer-options">
                                    {customers.map((customer) => (
                                        <option key={customer.custID} value={`${customer.firstName} ${customer.lastName}`}>
                                            {customer.email}
                                        </option>
                                    ))}
                                </datalist>
                            </label>
                            <label>
                                Order
                                <select
                                    name="orderId"
                                    value={form.orderId}
                                    onChange={handleChange}
                                    required
                                >
                                    <option value="">Choose an order</option>
                                    {orders
                                        .filter((order) => !form.customerId || order.customerId === form.customerId)
                                        .map((order) => (
                                        <option key={order.orderId} value={order.orderId}>
                                            {order.orderId} - {order.orderDate}
                                        </option>
                                        ))}
                                </select>
                            </label>
                        </div>
                        {form.customerId && <p className="field-note">Matched customer: {form.customerId}</p>}
                        <label>
                            Item name or barcode
                            <input
                                name="productName"
                                value={form.productName}
                                onChange={handleChange}
                                list="product-options"
                                placeholder="Type an item name, or scan its barcode"
                                required
                            />
                            <datalist id="product-options">
                                {products.map((product) => (
                                    <option key={product.id} value={product.name}>{product.id}</option>
                                ))}
                            </datalist>
                            {form.productId && <span className="field-note">Matched item barcode: {form.productId}</span>}
                        </label>
                        <div className="two-column">
                            <label>
                                Quantity
                                <input
                                    type="number"
                                    name="quantity"
                                    value={form.quantity}
                                    onChange={handleChange}
                                    placeholder="1"
                                    min="1"
                                    required
                                />
                            </label>
                            <label>
                                Line total (calculated)
                                <input
                                    type="number"
                                    step="0.01"
                                    name="lineTotal"
                                    value={form.lineTotal}
                                    onChange={handleChange}
                                    placeholder="0.00"
                                    min="0"
                                    readOnly
                                    required
                                />
                            </label>
                        </div>

                        <div className="button-row">
                            <button type="submit" className="primary-btn">
                                {editingId ? 'Save Changes' : 'Add Order Item'}
                            </button>
                            {editingId && (
                                <button type="button" className="secondary-btn" onClick={resetForm}>
                                    Cancel
                                </button>
                            )}
                        </div>
                    </form>

                    {error && <p className="error-message">{error}</p>}
                </section>

                <section className="panel table-panel">
                    <div className="section-header">
                        <div>
                            <p className="mini-label">Directory</p>
                            <h2>Order item list</h2>
                        </div>
                        <div className="search-box">
                            <input
                                type="text"
                                value={search}
                                onChange={(event) => setSearch(event.target.value)}
                                placeholder="Search order items"
                                aria-label="Search order items"
                            />
                        </div>
                    </div>

                    {loading ? (
                        <div className="loading-box">Loading order items...</div>
                    ) : (
                        <div className="table-wrap">
                            <table>
                                <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Order ID</th>
                                    <th>Product ID</th>
                                    <th>Quantity</th>
                                    <th>Line Total</th>
                                    <th>Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                {filteredOrderItems.length === 0 ? (
                                    <tr>
                                        <td colSpan="6" className="empty-state">No matching order items found.</td>
                                    </tr>
                                ) : (
                                    filteredOrderItems.map((item) => (
                                        <tr key={item.orderItemId}>
                                            <td>{item.orderItemId}</td>
                                            <td>{item.orderId}</td>
                                            <td>{item.productId}</td>
                                            <td>{item.quantity}</td>
                                            <td>R{Number(item.lineTotal).toFixed(2)}</td>
                                            <td className="action-cell">
                                                <button type="button" className="secondary-btn" onClick={() => handleEdit(item)}>
                                                    Edit
                                                </button>
                                                <button type="button" className="danger-btn" onClick={() => handleDelete(item.orderItemId)}>
                                                    Delete
                                                </button>
                                            </td>
                                        </tr>
                                    ))
                                )}
                                </tbody>
                            </table>
                        </div>
                    )}
                </section>
            </main>
        </div>
    );
}