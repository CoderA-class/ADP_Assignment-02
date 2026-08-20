import { useEffect, useMemo, useState } from 'react';
import {
    getEmployees,
    createEmployee,
    updateEmployee,
    deleteEmployee
} from '../services/employeeService';

const emptyEmployee = {
    empID: '',
    fName: '',
    lName: '',
    salary: '',
    email: '',
    phone: '',
    role: '',
    empStatus: 'ACTIVE'
};

export default function EmployeeApp() {
    const [employees, setEmployees] = useState([]);
    const [form, setForm] = useState(emptyEmployee);
    const [editingId, setEditingId] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [search, setSearch] = useState('');

    const fetchEmployees = async () => {
        try {
            setLoading(true);
            const data = await getEmployees();
            setEmployees(data);
            setError('');
        } catch (err) {
            setError('Unable to load employees from the backend.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchEmployees();
    }, []);

    const filteredEmployees = useMemo(() => {
        const term = search.trim().toLowerCase();
        if (!term) return employees;

        return employees.filter((employee) => {
            const fullName = `${employee.fName} ${employee.lName}`.toLowerCase();
            return (
                employee.empID.toLowerCase().includes(term) ||
                fullName.includes(term) ||
                employee.email.toLowerCase().includes(term) ||
                employee.phone.toLowerCase().includes(term) ||
                employee.role.toLowerCase().includes(term)
            );
        });
    }, [employees, search]);

    const employeeStats = useMemo(() => {
        const total = employees.length;
        const active = employees.filter((employee) => employee.empStatus === 'ACTIVE').length;
        const avg = total ? Math.round((active / total) * 100) : 0;

        return {
            total,
            active,
            percent: avg,
            uniqueIds: total
        };
    }, [employees]);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setForm((current) => ({ ...current, [name]: value }));
    };

    const resetForm = () => {
        setForm(emptyEmployee);
        setEditingId(null);
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        try {
            // Ensure numeric formatting for salary if required by backend
            const payload = {
                ...form,
                salary: parseFloat(form.salary) || 0
            };

            if (editingId) {
                await updateEmployee(payload);
            } else {
                await createEmployee(payload);
            }

            resetForm();
            await fetchEmployees();
        } catch (err) {
            setError(err.message || 'Employee save failed.');
        }
    };

    const handleEdit = (employee) => {
        setForm(employee);
        setEditingId(employee.empID);
    };

    const handleDelete = async (empID) => {
        try {
            await deleteEmployee(empID);
            await fetchEmployees();
            if (editingId === empID) {
                resetForm();
            }
        } catch (err) {
            setError(err.message || 'Employee deletion failed.');
        }
    };

    return (
        <div className="page-shell">
            <header className="topbar">
                <div>
                    <p className="eyebrow">HR Management System</p>
                    <h1>Employee Management</h1>
                </div>
                <div className="header-pill">
                    <span className="status-dot" />
                    System online
                </div>
            </header>

            <section className="stats-grid">
                <article className="stat-card accent">
                    <div className="stat-label">Total employees</div>
                    <div className="stat-value">{employeeStats.total}</div>
                    <div className="stat-foot">Across all active records</div>
                </article>
                <article className="stat-card">
                    <div className="stat-label">Active staff</div>
                    <div className="stat-value">{employeeStats.active}</div>
                    <div className="stat-foot">Status set to ACTIVE</div>
                </article>
                <article className="stat-card">
                    <div className="stat-label">Activity Rate</div>
                    <div className="stat-value">{employeeStats.percent}%</div>
                    <div className="stat-foot">Active vs total ratio</div>
                </article>
                <article className="stat-card">
                    <div className="stat-label">Unique IDs</div>
                    <div className="stat-value">{employeeStats.uniqueIds}</div>
                    <div className="stat-foot">Employee identifiers</div>
                </article>
            </section>

            <main className="content-grid">
                <section className="panel form-panel">
                    <div className="section-header">
                        <div>
                            <p className="mini-label">Employee record</p>
                            <h2>{editingId ? 'Update employee' : 'Add employee'}</h2>
                        </div>
                    </div>

                    <form onSubmit={handleSubmit} className="customer-form">
                        <label>
                            Employee ID
                            <input
                                name="empID"
                                value={form.empID}
                                onChange={handleChange}
                                placeholder="EMP-001"
                                required
                                disabled={editingId !== null} // Usually ID is immutable during updates
                            />
                        </label>
                        <div className="two-column">
                            <label>
                                First name
                                <input
                                    name="fName"
                                    value={form.fName}
                                    onChange={handleChange}
                                    placeholder="Robyn"
                                    required
                                />
                            </label>
                            <label>
                                Last name
                                <input
                                    name="lName"
                                    value={form.lName}
                                    onChange={handleChange}
                                    placeholder="Stevens"
                                    required
                                />
                            </label>
                        </div>
                        <label>
                            Email address
                            <input
                                type="email"
                                name="email"
                                value={form.email}
                                onChange={handleChange}
                                placeholder="robyn@example.com"
                                required
                            />
                        </label>
                        <label>
                            Phone number
                            <input
                                name="phone"
                                value={form.phone}
                                onChange={handleChange}
                                placeholder="0821234567"
                                required
                            />
                        </label>
                        <div className="two-column">
                            <label>
                                Salary ($)
                                <input
                                    type="number"
                                    step="0.01"
                                    name="salary"
                                    value={form.salary}
                                    onChange={handleChange}
                                    placeholder="50000"
                                    required
                                />
                            </label>
                            <label>
                                Status
                                <select
                                    name="empStatus"
                                    value={form.empStatus}
                                    onChange={handleChange}
                                    style={{ width: '100%', padding: '12px 14px', borderRadius: '12px', border: '1px solid var(--line)', background: '#f8fafc', color: 'var(--navy)' }}
                                    required
                                >
                                    <option value="ACTIVE">ACTIVE</option>
                                    <option value="SUSPENDED">SUSPENDED</option>
                                    <option value="TERMINATED">TERMINATED</option>
                                </select>
                            </label>
                        </div>
                        <label>
                            Role
                            <input
                                name="role"
                                value={form.role}
                                onChange={handleChange}
                                placeholder="Software Engineer"
                                required
                            />
                        </label>

                        <div className="button-row">
                            <button type="submit" className="primary-btn">
                                {editingId ? 'Save Changes' : 'Add Employee'}
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
                            <h2>Employee list</h2>
                        </div>
                        <div className="search-box">
                            <input
                                type="text"
                                value={search}
                                onChange={(event) => setSearch(event.target.value)}
                                placeholder="Search employees..."
                                aria-label="Search employees"
                            />
                        </div>
                    </div>

                    {loading ? (
                        <div className="loading-box">Loading employees...</div>
                    ) : (
                        <div className="table-wrap">
                            <table>
                                <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Name</th>
                                    <th>Role</th>
                                    <th>Salary</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                {filteredEmployees.length === 0 ? (
                                    <tr>
                                        <td colSpan="6" className="empty-state">No matching employees found.</td>
                                    </tr>
                                ) : (
                                    filteredEmployees.map((employee) => (
                                        <tr key={employee.empID}>
                                            <td>{employee.empID}</td>
                                            <td>{`${employee.fName} ${employee.lName}`}</td>
                                            <td>{employee.role}</td>
                                            <td>${Number(employee.salary).toLocaleString()}</td>
                                            <td>
                          <span style={{
                              padding: '4px 8px',
                              borderRadius: '6px',
                              fontSize: '0.75rem',
                              fontWeight: 'bold',
                              background: employee.empStatus === 'ACTIVE' ? '#d1fae5' : '#fee2e2',
                              color: employee.empStatus === 'ACTIVE' ? '#065f46' : '#991b1b'
                          }}>
                            {employee.empStatus}
                          </span>
                                            </td>
                                            <td className="action-cell">
                                                <button type="button" className="secondary-btn" onClick={() => handleEdit(employee)}>
                                                    Edit
                                                </button>
                                                <button type="button" className="danger-btn" onClick={() => handleDelete(employee.empID)}>
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