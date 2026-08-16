import { requestJson } from './api';

const API_BASE_URL = '/customers';

export async function getCustomers() {
  return requestJson(`${API_BASE_URL}/all`);
}

export async function createCustomer(customer) {
  return requestJson(`${API_BASE_URL}/create`, {
    method: 'POST',
    body: JSON.stringify(customer)
  });
}

export async function updateCustomer(customer) {
  return requestJson(`${API_BASE_URL}/update`, {
    method: 'PUT',
    body: JSON.stringify(customer)
  });
}

export async function deleteCustomer(custID) {
  return requestJson(`${API_BASE_URL}/delete/${custID}`, {
    method: 'DELETE'
  });
}
