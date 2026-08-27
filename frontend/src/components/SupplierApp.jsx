import { useEffect, useMemo, useState } from 'react';
import {
    getSuppliers,
    createSupplier,
    updateSupplier,
    deleteSupplier
} from '../services/supplierService';

const emptySupplier = {
    supplierId: '',
    name: '',
    email: '',
    phone: '',
    address: ''
};

export default function Supplier() {
    const [suppliers, setSuppliers] = useState([]);
    const [form, setForm] = useState(emptySupplier);
    const [editingId, setEditingId] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [search, setSearch] = useState('');

    const fetchSuppliers = async () => {
        try {
            setLoading(true);
            const data = await getSuppliers();
            setSuppliers(data);
            setError('');
        } catch (err) {
            setError('Unable to load suppliers from the backend.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchSuppliers();
    }, []);

    const filteredSuppliers = useMemo(() => {
        const term = search.trim().toLowerCase();
        if (!term) return suppliers;

        return suppliers.filter((supplier) => {
            return (
                supplier.supplierId.toLowerCase().includes(term) ||
                supplier.name.toLowerCase().includes(term) ||
                supplier.email.toLowerCase().includes(term)
            );
        });
    }, [suppliers, search]);

    const supplierStats = useMemo(() => {
        const total = suppliers.length;
        const withPhone = suppliers.filter((supplier) => Boolean(supplier.phone)).length;
        const uniqueDomains = new Set(
            suppliers
                .map((supplier) => supplier.email?.split('@')[1])
                .filter(Boolean)
        ).size;

        return { total, withPhone, uniqueDomains };
    }, [suppliers]);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setForm((current) => ({ ...current, [name]: value }));
    };

    const resetForm = () => {
        setForm(emptySupplier);
        setEditingId(null);
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        try {
            if (editingId) {
                await updateSupplier(form);
            } else {
                await createSupplier(form);
            }

            resetForm();
            await fetchSuppliers();
        } catch (err) {
            setError(err.message || 'Supplier save failed.');
        }
    };

    const handleEdit = (supplier) => {
        setForm(supplier);
        setEditingId(supplier.supplierId);
    };

    const handleDelete = async (supplierId) => {
        try {
            await deleteSupplier(supplierId);
            await fetchSuppliers();
            if (editingId === supplierId) {
                resetForm();
            }
        } catch (err) {
            setError(err.message || 'Supplier deletion failed.');
        }
    };

    return (
        <div className="page-shell">
            <header className="topbar">
                <div>
                    <p className="eyebrow">Procurement / Business Management</p>
                    <h1>Supplier Management</h1>
                </div>
                <div className="header-pill">
                    <span className="status-dot" />
                    System online
                </div>
            </header>

            <section className="stats-grid">
                <article className="stat-card accent">
                    <div className="stat-label">Total suppliers</div>
                    <div className="stat-value">{supplierStats.total}</div>
                    <div className="stat-foot">On file</div>
                </article>
                <article className="stat-card">
                    <div className="stat-label">With phone number</div>
                    <div className="stat-value">{supplierStats.withPhone}</div>
                    <div className="stat-foot">Contactable by phone</div>
                </article>
                <article className="stat-card">
                    <div className="stat-label">Unique email domains</div>
                    <div className="stat-value">{supplierStats.uniqueDomains}</div>
                    <div className="stat-foot">Distinct organisations</div>
                </article>
            </section>

            <main className="content-grid">
                <section className="panel form-panel">
                    <div className="section-header">
                        <div>
                            <p className="mini-label">Supplier record</p>
                            <h2>{editingId ? 'Update supplier' : 'Add supplier'}</h2>
                        </div>
                    </div>

                    <form onSubmit={handleSubmit} className="supplier-form">
                        <label>
                            Supplier ID
                            <input
                                name="supplierId"
                                value={form.supplierId}
                                onChange={handleChange}
                                placeholder="SUP-001"
                                disabled={!!editingId}
                                required
                            />
                        </label>
                        <label>
                            Name
                            <input
                                name="name"
                                value={form.name}
                                onChange={handleChange}
                                placeholder="Acme Supplies"
                                required
                            />
                        </label>
                        <div className="two-column">
                            <label>
                                Email
                                <input
                                    type="email"
                                    name="email"
                                    value={form.email}
                                    onChange={handleChange}
                                    placeholder="orders@acme.co.za"
                                    required
                                />
                            </label>
                            <label>
                                Phone
                                <input
                                    name="phone"
                                    value={form.phone}
                                    onChange={handleChange}
                                    placeholder="021 555 0123"
                                    required
                                />
                            </label>
                        </div>
                        <label>
                            Address
                            <input
                                name="address"
                                value={form.address}
                                onChange={handleChange}
                                placeholder="12 Main Road, Paarl"
                                required
                            />
                        </label>

                        <div className="button-row">
                            <button type="submit" className="primary-btn">
                                {editingId ? 'Save Changes' : 'Add Supplier'}
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
                            <h2>Supplier list</h2>
                        </div>
                        <div className="search-box">
                            <input
                                type="text"
                                value={search}
                                onChange={(event) => setSearch(event.target.value)}
                                placeholder="Search suppliers"
                                aria-label="Search suppliers"
                            />
                        </div>
                    </div>

                    {loading ? (
                        <div className="loading-box">Loading suppliers...</div>
                    ) : (
                        <div className="table-wrap">
                            <table>
                                <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Name</th>
                                    <th>Email</th>
                                    <th>Phone</th>
                                    <th>Address</th>
                                    <th>Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                {filteredSuppliers.length === 0 ? (
                                    <tr>
                                        <td colSpan="6" className="empty-state">No matching suppliers found.</td>
                                    </tr>
                                ) : (
                                    filteredSuppliers.map((supplier) => (
                                        <tr key={supplier.supplierId}>
                                            <td>{supplier.supplierId}</td>
                                            <td>{supplier.name}</td>
                                            <td>{supplier.email}</td>
                                            <td>{supplier.phone}</td>
                                            <td>{supplier.address}</td>
                                            <td className="action-cell">
                                                <button type="button" className="secondary-btn" onClick={() => handleEdit(supplier)}>
                                                    Edit
                                                </button>
                                                <button type="button" className="danger-btn" onClick={() => handleDelete(supplier.supplierId)}>
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
