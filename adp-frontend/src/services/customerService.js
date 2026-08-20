const API_BASE = '/customers';

export const getCustomers = async () => {
    const res = await fetch(`${API_BASE}/all`);
    if (!res.ok) throw new Error('Failed to fetch customers');
    return res.json();
};

export const createCustomer = async (customer) => {
    const res = await fetch(`${API_BASE}/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(customer),
    });
    if (!res.ok) throw new Error('Failed to create customer');
    return res.json();
};

export const updateCustomer = async (customer) => {
    const res = await fetch(`${API_BASE}/update`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(customer),
    });
    if (!res.ok) throw new Error('Failed to update customer');
    return res.json();
};

export const deleteCustomer = async (custID) => {
    const res = await fetch(`${API_BASE}/delete/${custID}`, {
        method: 'DELETE',
    });
    if (!res.ok) throw new Error('Failed to delete customer');
    return res.json();
};