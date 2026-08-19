const BASE_URL = '/api/order-items';

async function handleResponse(response) {
    if (!response.ok) {
        const message = await response.text().catch(() => '');
        throw new Error(message || `Request failed with status ${response.status}`);
    }
    if (response.status === 204) return null;
    return response.json();
}

export async function getOrderItems() {
    const response = await fetch(BASE_URL);
    return handleResponse(response);
}

export async function getOrderItem(orderItemId) {
    const response = await fetch(`${BASE_URL}/${orderItemId}`);
    return handleResponse(response);
}

export async function createOrderItem(orderItem) {
    const response = await fetch(BASE_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(orderItem)
    });
    return handleResponse(response);
}

export async function updateOrderItem(orderItem) {
    const response = await fetch(`${BASE_URL}/${orderItem.orderItemId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(orderItem)
    });
    return handleResponse(response);
}

export async function deleteOrderItem(orderItemId) {
    const response = await fetch(`${BASE_URL}/${orderItemId}`, {
        method: 'DELETE'
    });
    return handleResponse(response);
}