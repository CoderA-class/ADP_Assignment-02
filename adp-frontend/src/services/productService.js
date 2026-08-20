const API_BASE = '/products';

export const getProducts = async () => {
    const res = await fetch(`${API_BASE}/all`);
    if (!res.ok) throw new Error('Failed to fetch products');
    return res.json();
};

export const createProduct = async (product) => {
    const res = await fetch(`${API_BASE}/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(product),
    });
    if (!res.ok) throw new Error('Failed to create product');
    return res.json();
};

export const updateProduct = async (product) => {
    const res = await fetch(`${API_BASE}/update`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(product),
    });
    if (!res.ok) throw new Error('Failed to update product');
    return res.json();
};

export const deleteProduct = async (id) => {
    const res = await fetch(`${API_BASE}/delete/${id}`, {
        method: 'DELETE',
    });
    if (!res.ok) throw new Error('Failed to delete product');
    return res.json();
};