import { requestJson } from './api';

const API_BASE_URL = '/employees';

export async function getEmployees() {
    return requestJson(`${API_BASE_URL}/all`);
}

export async function createEmployee(employee) {
    return requestJson(`${API_BASE_URL}/create`, {
        method: 'POST',
        body: JSON.stringify(employee)
    });
}

export async function updateEmployee(employee) {
    return requestJson(`${API_BASE_URL}/update`, {
        method: 'PUT',
        body: JSON.stringify(employee)
    });
}

export async function deleteEmployee(empID) {
    return requestJson(`${API_BASE_URL}/delete/${empID}`, {
        method: 'DELETE'
    });
}