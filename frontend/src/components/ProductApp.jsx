// src/components/ProductApp.jsx
import { useEffect, useMemo, useState } from 'react';
import {
    getProducts,
    createProduct,
    updateProduct,
    deleteProduct,
} from '../services/productService';

const emptyProduct = {
    id: '',
    name: '',
    price: '',
    quantity: '',
    category: '',
};

export default function ProductApp() {
    const [products, setProducts] = useState([]);
    const [form, setForm] = useState(emptyProduct);
    const [editingId, setEditingId] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [search, setSearch] = useState('');

    const fetchProducts = async () => {
        try {
            setLoading(true);
            const data = await getProducts();
            setProducts(data);
            setError('');
        } catch (err) {
            setError('Unable to load products from the backend.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchProducts();
    }, []);

    // Filter products by search term
    const filteredProducts = useMemo(() => {
        const term = search.trim().toLowerCase();
        if (!term) return products;

        return products.filter((product) => {
            return (
                product.id.toLowerCase().includes(term) ||
                product.name.toLowerCase().includes(term) ||
                product.category.toLowerCase().includes(term)
            );
        });
    }, [products, search]);

    // Statistics
    const productStats = useMemo(() => {
        const total = products.length;
        const totalValue = products.reduce(
            (sum, p) => sum + p.price * p.quantity,
            0
        );
        const avgPrice = total ? products.reduce((s, p) => s + p.price, 0) / total : 0;
        const categories = new Set(products.map((p) => p.category)).size;

        return {
            total,
            totalValue: totalValue.toFixed(2),
            avgPrice: avgPrice.toFixed(2),
            categories,
        };
    }, [products]);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setForm((current) => ({ ...current, [name]: value }));
    };

    const resetForm = () => {
        setForm(emptyProduct);
        setEditingId(null);
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        const payload = {
            ...form,
            price: parseFloat(form.price),
            quantity: parseInt(form.quantity, 10),
        };

        try {
            if (editingId) {
                await updateProduct(payload);
            } else {
                await createProduct(payload);
            }
            resetForm();
            await fetchProducts();
        } catch (err) {
            setError(err.message || 'Product save failed.');
        }
    };

    const handleEdit = (product) => {
        setForm(product);
        setEditingId(product.id);
    };

    const handleDelete = async (id) => {
        try {
            await deleteProduct(id);
            await fetchProducts();
            if (editingId === id) resetForm();
        } catch (err) {
            setError(err.message || 'Product deletion failed.');
        }
    };

    return (
        <div className="page-shell">
            <header className="topbar">
                <div>
                    <p className="eyebrow">Retail / Business Management</p>
                    <h1>Product Management</h1>
                </div>
                <div className="header-pill">
                    <span className="status-dot" />
                    System online
                </div>
            </header>

            <section className="stats-grid">
                <article className="stat-card accent">
                    <div className="stat-label">Total products</div>
                    <div className="stat-value">{productStats.total}</div>
                    <div className="stat-foot">Unique SKUs</div>
                </article>
                <article className="stat-card">
                    <div className="stat-label">Total inventory value</div>
                    <div className="stat-value">R{productStats.totalValue}</div>
                    <div className="stat-foot">Price × Quantity</div>
                </article>
                <article className="stat-card">
                    <div className="stat-label">Average price</div>
                    <div className="stat-value">R{productStats.avgPrice}</div>
                    <div className="stat-foot">Per product</div>
                </article>
                <article className="stat-card">
                    <div className="stat-label">Categories</div>
                    <div className="stat-value">{productStats.categories}</div>
                    <div className="stat-foot">Distinct product types</div>
                </article>
            </section>

            <main className="content-grid">
                <section className="panel form-panel">
                    <div className="section-header">
                        <div>
                            <p className="mini-label">Product record</p>
                            <h2>{editingId ? 'Update product' : 'Add product'}</h2>
                        </div>
                    </div>

                    <form onSubmit={handleSubmit} className="customer-form">
                        <label>
                            Product ID
                            <input
                                name="id"
                                value={form.id}
                                onChange={handleChange}
                                placeholder="PROD-001"
                                disabled={!!editingId}
                                required
                            />
                        </label>
                        <label>
                            Product name
                            <input
                                name="name"
                                value={form.name}
                                onChange={handleChange}
                                placeholder="Wireless Mouse"
                                required
                            />
                        </label>
                        <div className="two-column">
                            <label>
                                Price (R)
                                <input
                                    type="number"
                                    step="0.01"
                                    name="price"
                                    value={form.price}
                                    onChange={handleChange}
                                    placeholder="0.00"
                                    min="0"
                                    required
                                />
                            </label>
                            <label>
                                Quantity
                                <input
                                    type="number"
                                    name="quantity"
                                    value={form.quantity}
                                    onChange={handleChange}
                                    placeholder="0"
                                    min="0"
                                    required
                                />
                            </label>
                        </div>
                        <label>
                            Category
                            <input
                                name="category"
                                value={form.category}
                                onChange={handleChange}
                                placeholder="Electronics"
                                required
                            />
                        </label>

                        <div className="button-row">
                            <button type="submit" className="primary-btn">
                                {editingId ? 'Save Changes' : 'Add Product'}
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
                            <h2>Product list</h2>
                        </div>
                        <div className="search-box">
                            <input
                                type="text"
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                                placeholder="Search products"
                                aria-label="Search products"
                            />
                        </div>
                    </div>

                    {loading ? (
                        <div className="loading-box">Loading products...</div>
                    ) : (
                        <div className="table-wrap">
                            <table>
                                <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Name</th>
                                    <th>Price</th>
                                    <th>Qty</th>
                                    <th>Category</th>
                                    <th>Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                {filteredProducts.length === 0 ? (
                                    <tr>
                                        <td colSpan="6" className="empty-state">
                                            No matching products found.
                                        </td>
                                    </tr>
                                ) : (
                                    filteredProducts.map((product) => (
                                        <tr key={product.id}>
                                            <td>{product.id}</td>
                                            <td>{product.name}</td>
                                            <td>R{Number(product.price).toFixed(2)}</td>
                                            <td>{product.quantity}</td>
                                            <td>{product.category}</td>
                                            <td className="action-cell">
                                                <button
                                                    type="button"
                                                    className="secondary-btn"
                                                    onClick={() => handleEdit(product)}
                                                >
                                                    Edit
                                                </button>
                                                <button
                                                    type="button"
                                                    className="danger-btn"
                                                    onClick={() => handleDelete(product.id)}
                                                >
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