const API_BASE = '/products';

export const getProducts = async () => {
    const response = await fetch(`${API_BASE}/all`);
    if (!response.ok) throw new Error('Failed to fetch products');
    return response.json();
};

export const createProduct = async (product) => {
    const response = await fetch(`${API_BASE}/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(product),
    });
    if (!response.ok) throw new Error('Failed to create product');
    return response.json();
};

export const updateProduct = async (product) => {
    const response = await fetch(`${API_BASE}/update`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(product),
    });
    if (!response.ok) throw new Error('Failed to update product');
    return response.json();
};

export const deleteProduct = async (id) => {
    const response = await fetch(`${API_BASE}/delete/${id}`, { method: 'DELETE' });
    if (!response.ok) throw new Error('Failed to delete product');
    return response.json();
};
