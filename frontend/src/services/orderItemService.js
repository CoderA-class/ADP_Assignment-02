import { requestJson } from './api';

const BASE_URL = '/order-items';

export async function getOrderItems() {
    return requestJson(`${BASE_URL}/all`);
}

export async function getOrderItem(orderItemId) {
    return requestJson(`${BASE_URL}/read/${orderItemId}`);
}

export async function createOrderItem(orderItem) {
    return requestJson(`${BASE_URL}/create`, {
        method: 'POST',
        body: JSON.stringify(orderItem)
    });
}

export async function updateOrderItem(orderItem) {
    return requestJson(`${BASE_URL}/update`, {
        method: 'PUT',
        body: JSON.stringify(orderItem)
    });
}

export async function deleteOrderItem(orderItemId) {
    return requestJson(`${BASE_URL}/delete/${orderItemId}`, {
        method: 'DELETE'
    });
}