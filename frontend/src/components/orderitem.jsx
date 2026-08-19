import { useEffect, useMemo, useState } from 'react';
import {
    getOrderItems,
    createOrderItem,
    updateOrderItem,
    deleteOrderItem
} from '../services/orderItemService';

const emptyOrderItem = {
    orderItemId: '',
    orderId: '',
    productId: '',
    quantity: '',
    lineTotal: ''
};

export default function OrderItem() {
    const [orderItems, setOrderItems] = useState([]);
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
        setForm((current) => ({ ...current, [name]: value }));
    };

    const resetForm = () => {
        setForm(emptyOrderItem);
        setEditingId(null);
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        const payload = {
            ...form,
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
        setForm(item);
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
                        <label>
                            Order Item ID
                            <input
                                name="orderItemId"
                                value={form.orderItemId}
                                onChange={handleChange}
                                placeholder="OI-001"
                                disabled={!!editingId}
                                required
                            />
                        </label>
                        <div className="two-column">
                            <label>
                                Order ID
                                <input
                                    name="orderId"
                                    value={form.orderId}
                                    onChange={handleChange}
                                    placeholder="ORD-001"
                                    required
                                />
                            </label>
                            <label>
                                Product ID
                                <input
                                    name="productId"
                                    value={form.productId}
                                    onChange={handleChange}
                                    placeholder="PROD-001"
                                    required
                                />
                            </label>
                        </div>
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
                                Line total
                                <input
                                    type="number"
                                    step="0.01"
                                    name="lineTotal"
                                    value={form.lineTotal}
                                    onChange={handleChange}
                                    placeholder="0.00"
                                    min="0"
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