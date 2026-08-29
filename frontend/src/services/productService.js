import { requestJson } from './api';

const API_BASE_URL = '/products';

export async function getProducts() {
    return requestJson(`${API_BASE_URL}/all`);
}

export async function createProduct(product) {
    return requestJson(`${API_BASE_URL}/create`, {
        method: 'POST',
        body: JSON.stringify(product)
    });
}

export async function updateProduct(product) {
    return requestJson(`${API_BASE_URL}/update`, {
        method: 'PUT',
        body: JSON.stringify(product)
    });
}

export async function deleteProduct(id) {
    return requestJson(`${API_BASE_URL}/delete/${id}`, {
        method: 'DELETE'
    });
}