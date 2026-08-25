import { useEffect, useMemo, useState } from 'react';
import {
    getCustomers,
    createCustomer,
    updateCustomer,
    deleteCustomer
} from '../services/customerService';

const emptyCustomer = {
    custID: '',
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    address: ''
};

export default function CustomerApp() {
    const [customers, setCustomers] = useState([]);
    const [form, setForm] = useState(emptyCustomer);
    const [editingId, setEditingId] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [search, setSearch] = useState('');

    const fetchCustomers = async () => {
        try {
            setLoading(true);
            const data = await getCustomers();
            setCustomers(data);
            setError('');
        } catch (err) {
            setError('Unable to load customers from the backend.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCustomers();
    }, []);

    const filteredCustomers = useMemo(() => {
        const term = search.trim().toLowerCase();
        if (!term) return customers;

        return customers.filter((customer) => {
            const fullName = `${customer.firstName} ${customer.lastName}`.toLowerCase();
            return (
                customer.custID.toLowerCase().includes(term) ||
                fullName.includes(term) ||
                customer.email.toLowerCase().includes(term) ||
                customer.phoneNumber.toLowerCase().includes(term)
            );
        });
    }, [customers, search]);

    const customerStats = useMemo(() => {
        const total = customers.length;
        const active = customers.filter((customer) => customer.email && customer.phoneNumber).length;
        const avg = total ? Math.round((active / total) * 100) : 0;

        return {
            total,
            active,
            percent: avg,
            uniqueIds: total
        };
    }, [customers]);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setForm((current) => ({ ...current, [name]: value }));
    };

    const resetForm = () => {
        setForm(emptyCustomer);
        setEditingId(null);
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        try {
            if (editingId) {
                await updateCustomer(form);
            } else {
                await createCustomer(form);
            }

            resetForm();
            await fetchCustomers();
        } catch (err) {
            setError(err.message || 'Customer save failed.');
        }
    };

    const handleEdit = (customer) => {
        setForm(customer);
        setEditingId(customer.custID);
    };

    const handleDelete = async (custID) => {
        try {
            await deleteCustomer(custID);
            await fetchCustomers();
            if (editingId === custID) {
                resetForm();
            }
        } catch (err) {
            setError(err.message || 'Customer deletion failed.');
        }
    };

    return (
        <div className="page-shell">
            <header className="topbar">
                <div>
                    <p className="eyebrow">Retail / Business Management</p>
                    <h1>Customer Management</h1>
                </div>
                <div className="header-pill">
                    <span className="status-dot" />
                    System online
                </div>
            </header>

            <section className="stats-grid">
                <article className="stat-card accent">
                    <div className="stat-label">Total customers</div>
                    <div className="stat-value">{customerStats.total}</div>
                    <div className="stat-foot">Across all active records</div>
                </article>
                <article className="stat-card">
                    <div className="stat-label">Active profiles</div>
                    <div className="stat-value">{customerStats.active}</div>
                    <div className="stat-foot">Valid contact details</div>
                </article>
                <article className="stat-card">
                    <div className="stat-label">Coverage</div>
                    <div className="stat-value">{customerStats.percent}%</div>
                    <div className="stat-foot">Contact completeness</div>
                </article>
                <article className="stat-card">
                    <div className="stat-label">Unique IDs</div>
                    <div className="stat-value">{customerStats.uniqueIds}</div>
                    <div className="stat-foot">Customer identifiers</div>
                </article>
            </section>

            <main className="content-grid">
                <section className="panel form-panel">
                    <div className="section-header">
                        <div>
                            <p className="mini-label">Customer record</p>
                            <h2>{editingId ? 'Update customer' : 'Add customer'}</h2>
                        </div>
                    </div>

                    <form onSubmit={handleSubmit} className="customer-form">
                        <label>
                            Customer ID
                            <input
                                name="custID"
                                value={form.custID}
                                onChange={handleChange}
                                placeholder="CUST-001"
                                required
                            />
                        </label>
                        <div className="two-column">
                            <label>
                                First name
                                <input
                                    name="firstName"
                                    value={form.firstName}
                                    onChange={handleChange}
                                    placeholder="John"
                                    required
                                />
                            </label>
                            <label>
                                Last name
                                <input
                                    name="lastName"
                                    value={form.lastName}
                                    onChange={handleChange}
                                    placeholder="Smith"
                                    required
                                />
                            </label>
                        </div>
                        <label>
                            Email
                            <input
                                type="email"
                                name="email"
                                value={form.email}
                                onChange={handleChange}
                                placeholder="john@example.com"
                                required
                            />
                        </label>
                        <label>
                            Phone number
                            <input
                                name="phoneNumber"
                                value={form.phoneNumber}
                                onChange={handleChange}
                                placeholder="0821234567"
                                required
                            />
                        </label>
                        <label>
                            Address
                            <input
                                name="address"
                                value={form.address}
                                onChange={handleChange}
                                placeholder="123 Main Road"
                                required
                            />
                        </label>

                        <div className="button-row">
                            <button type="submit" className="primary-btn">
                                {editingId ? 'Save Changes' : 'Add Customer'}
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
                            <h2>Customer list</h2>
                        </div>
                        <div className="search-box">
                            <input
                                type="text"
                                value={search}
                                onChange={(event) => setSearch(event.target.value)}
                                placeholder="Search customers"
                                aria-label="Search customers"
                            />
                        </div>
                    </div>

                    {loading ? (
                        <div className="loading-box">Loading customers...</div>
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
                                {filteredCustomers.length === 0 ? (
                                    <tr>
                                        <td colSpan="6" className="empty-state">No matching customers found.</td>
                                    </tr>
                                ) : (
                                    filteredCustomers.map((customer) => (
                                        <tr key={customer.custID}>
                                            <td>{customer.custID}</td>
                                            <td>{`${customer.firstName} ${customer.lastName}`}</td>
                                            <td>{customer.email}</td>
                                            <td>{customer.phoneNumber}</td>
                                            <td>{customer.address}</td>
                                            <td className="action-cell">
                                                <button type="button" className="secondary-btn" onClick={() => handleEdit(customer)}>
                                                    Edit
                                                </button>
                                                <button type="button" className="danger-btn" onClick={() => handleDelete(customer.custID)}>
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