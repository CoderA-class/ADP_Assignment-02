import { requestJson } from './api';

const API_BASE_URL = '/orders';

export async function getOrders() {
    return requestJson(`${API_BASE_URL}/all`);
}

export async function createOrder(order) {
    return requestJson(`${API_BASE_URL}/create`, {
        method: 'POST',
        body: JSON.stringify(order)
    });
}

export async function updateOrder(order) {
    return requestJson(`${API_BASE_URL}/update`, {
        method: 'PUT',
        body: JSON.stringify(order)
    });
}

export async function deleteOrder(orderId) {
    return requestJson(`${API_BASE_URL}/delete/${orderId}`, {
        method: 'DELETE'
    });
}
