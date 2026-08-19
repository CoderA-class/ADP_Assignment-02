import { useEffect, useMemo, useState } from 'react';
import {
    getOrders,
    createOrder,
    updateOrder,
    deleteOrder
} from '../services/orderService';

const emptyOrder = {
    orderId: '',
    customerId: '',
    orderDate: '',
    totalAmount: ''
};

export default function OrderApp() {
    const [orders, setOrders] = useState([]);
    const [form, setForm] = useState(emptyOrder);
    const [editingId, setEditingId] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [search, setSearch] = useState('');

    const fetchOrders = async () => {
        try {
            setLoading(true);
            const data = await getOrders();
            setOrders(data);
            setError('');
        } catch (err) {
            setError('Unable to load orders from the backend.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchOrders();
    }, []);

    const filteredOrders = useMemo(() => {
        const term = search.trim().toLowerCase();
        if (!term) return orders;

        return orders.filter((order) => {
            return (
                order.orderId.toLowerCase().includes(term) ||
                order.customerId.toLowerCase().includes(term)
            );
        });
    }, [orders, search]);

    const orderStats = useMemo(() => {
        const total = orders.length;
        const totalValue = orders.reduce((sum, order) => sum + Number(order.totalAmount || 0), 0);
        const avgValue = total ? totalValue / total : 0;
        const uniqueCustomers = new Set(orders.map((order) => order.customerId)).size;

        return {
            total,
            totalValue: totalValue.toFixed(2),
            avgValue: avgValue.toFixed(2),
            uniqueCustomers
        };
    }, [orders]);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setForm((current) => ({ ...current, [name]: value }));
    };

    const resetForm = () => {
        setForm(emptyOrder);
        setEditingId(null);
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        const payload = {
            ...form,
            totalAmount: Number(form.totalAmount || 0)
        };

        try {
            if (editingId) {
                await updateOrder(payload);
            } else {
                await createOrder(payload);
            }

            resetForm();
            await fetchOrders();
        } catch (err) {
            setError(err.message || 'Order save failed.');
        }
    };

    const handleEdit = (order) => {
        setForm({
            orderId: order.orderId,
            customerId: order.customerId,
            orderDate: order.orderDate,
            totalAmount: order.totalAmount
        });
        setEditingId(order.orderId);
    };

    const handleDelete = async (orderId) => {
        try {
            await deleteOrder(orderId);
            await fetchOrders();
            if (editingId === orderId) {
                resetForm();
            }
        } catch (err) {
            setError(err.message || 'Order deletion failed.');
        }
    };

    return (
        <div className="page-shell">
            <header className="topbar">
                <div>
                    <p className="eyebrow">Retail / Business Management</p>
                    <h1>Order Management</h1>
                </div>
                <div className="header-pill">
                    <span className="status-dot" />
                    System online
                </div>
            </header>

            <section className="stats-grid">
                <article className="stat-card accent">
                    <div className="stat-label">Total orders</div>
                    <div className="stat-value">{orderStats.total}</div>
                    <div className="stat-foot">Across all customers</div>
                </article>
                <article className="stat-card">
                    <div className="stat-label">Total order value</div>
                    <div className="stat-value">R{orderStats.totalValue}</div>
                    <div className="stat-foot">Sum of all order totals</div>
                </article>
                <article className="stat-card">
                    <div className="stat-label">Average order value</div>
                    <div className="stat-value">R{orderStats.avgValue}</div>
                    <div className="stat-foot">Per order</div>
                </article>
                <article className="stat-card">
                    <div className="stat-label">Customers with orders</div>
                    <div className="stat-value">{orderStats.uniqueCustomers}</div>
                    <div className="stat-foot">Unique customer IDs</div>
                </article>
            </section>

            <main className="content-grid">
                <section className="panel form-panel">
                    <div className="section-header">
                        <div>
                            <p className="mini-label">Order record</p>
                            <h2>{editingId ? 'Update order' : 'Add order'}</h2>
                        </div>
                    </div>

                    <form onSubmit={handleSubmit} className="order-form">
                        <label>
                            Order ID
                            <input
                                name="orderId"
                                value={form.orderId}
                                onChange={handleChange}
                                placeholder="ORD-001"
                                disabled={!!editingId}
                                required
                            />
                        </label>
                        <label>
                            Customer ID
                            <input
                                name="customerId"
                                value={form.customerId}
                                onChange={handleChange}
                                placeholder="CUST-001"
                                required
                            />
                        </label>
                        <div className="two-column">
                            <label>
                                Order date
                                <input
                                    type="date"
                                    name="orderDate"
                                    value={form.orderDate}
                                    onChange={handleChange}
                                    required
                                />
                            </label>
                            <label>
                                Total amount
                                <input
                                    type="number"
                                    step="0.01"
                                    name="totalAmount"
                                    value={form.totalAmount}
                                    onChange={handleChange}
                                    placeholder="0.00"
                                    min="0"
                                    required
                                />
                            </label>
                        </div>

                        <div className="button-row">
                            <button type="submit" className="primary-btn">
                                {editingId ? 'Save Changes' : 'Add Order'}
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
                            <h2>Order list</h2>
                        </div>
                        <div className="search-box">
                            <input
                                type="text"
                                value={search}
                                onChange={(event) => setSearch(event.target.value)}
                                placeholder="Search orders"
                                aria-label="Search orders"
                            />
                        </div>
                    </div>

                    {loading ? (
                        <div className="loading-box">Loading orders...</div>
                    ) : (
                        <div className="table-wrap">
                            <table>
                                <thead>
                                <tr>
                                    <th>Order ID</th>
                                    <th>Customer ID</th>
                                    <th>Order Date</th>
                                    <th>Total Amount</th>
                                    <th>Items</th>
                                    <th>Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                {filteredOrders.length === 0 ? (
                                    <tr>
                                        <td colSpan="6" className="empty-state">No matching orders found.</td>
                                    </tr>
                                ) : (
                                    filteredOrders.map((order) => (
                                        <tr key={order.orderId}>
                                            <td>{order.orderId}</td>
                                            <td>{order.customerId}</td>
                                            <td>{order.orderDate}</td>
                                            <td>R{Number(order.totalAmount).toFixed(2)}</td>
                                            <td>{order.orderItems ? order.orderItems.length : 0}</td>
                                            <td className="action-cell">
                                                <button type="button" className="secondary-btn" onClick={() => handleEdit(order)}>
                                                    Edit
                                                </button>
                                                <button type="button" className="danger-btn" onClick={() => handleDelete(order.orderId)}>
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
