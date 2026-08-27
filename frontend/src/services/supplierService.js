const BASE_URL = '/suppliers';

async function handleResponse(response) {
    if (!response.ok) {
        const message = await response.text().catch(() => '');
        throw new Error(message || `Request failed with status ${response.status}`);
    }
    if (response.status === 204) return null;
    return response.json();
}

export async function getSuppliers() {
    const response = await fetch(`${BASE_URL}/all`);
    return handleResponse(response);
}

export async function getSupplier(supplierId) {
    const response = await fetch(`${BASE_URL}/read/${supplierId}`);
    return handleResponse(response);
}

export async function createSupplier(supplier) {
    const response = await fetch(`${BASE_URL}/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(supplier)
    });
    return handleResponse(response);
}

export async function updateSupplier(supplier) {
    const response = await fetch(`${BASE_URL}/update`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(supplier)
    });
    return handleResponse(response);
}

export async function deleteSupplier(supplierId) {
    const response = await fetch(`${BASE_URL}/delete/${supplierId}`, {
        method: 'DELETE'
    });
    return handleResponse(response);
}
