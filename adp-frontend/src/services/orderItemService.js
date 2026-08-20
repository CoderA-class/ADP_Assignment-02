const API_BASE = '/orderitems';

export const getOrderItems = async () => {
    const res = await fetch(`${API_BASE}/all`);
    if (!res.ok) throw new Error('Failed to fetch order items');
    return res.json();
};

export const createOrderItem = async (orderItem) => {
    const res = await fetch(`${API_BASE}/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(orderItem),
    });
    if (!res.ok) throw new Error('Failed to create order item');
    return res.json();
};

export const updateOrderItem = async (orderItem) => {
    const res = await fetch(`${API_BASE}/update`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(orderItem),
    });
    if (!res.ok) throw new Error('Failed to update order item');
    return res.json();
};

export const deleteOrderItem = async (orderItemId) => {
    const res = await fetch(`${API_BASE}/delete/${orderItemId}`, {
        method: 'DELETE',
    });
    if (!res.ok) throw new Error('Failed to delete order item');
    return res.json();
};