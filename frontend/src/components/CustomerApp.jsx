import { useEffect, useState } from 'react';
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
      </header>

      <main className="content-grid">
        <section className="panel form-panel">
          <h2>{editingId ? 'Update customer' : 'Add customer'}</h2>
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
          <div className="table-header">
            <h2>Customer list</h2>
          </div>

          {loading ? (
            <p>Loading customers...</p>
          ) : (
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
                {customers.length === 0 ? (
                  <tr>
                    <td colSpan="6">No customers found.</td>
                  </tr>
                ) : (
                  customers.map((customer) => (
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
          )}
        </section>
      </main>
    </div>
  );
}
