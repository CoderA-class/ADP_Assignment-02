import { requestJson } from './api';

const BASE_URL = '/suppliers';

export async function getSuppliers() {
    return requestJson(`${BASE_URL}/all`);
}

export async function getSupplier(supplierId) {
    return requestJson(`${BASE_URL}/read/${supplierId}`);
}

export async function createSupplier(supplier) {
    return requestJson(`${BASE_URL}/create`, {
        method: 'POST',
        body: JSON.stringify(supplier)
    });
}

export async function updateSupplier(supplier) {
    return requestJson(`${BASE_URL}/update`, {
        method: 'PUT',
        body: JSON.stringify(supplier)
    });
}

export async function deleteSupplier(supplierId) {
    return requestJson(`${BASE_URL}/delete/${supplierId}`, {
        method: 'DELETE'
    });
}