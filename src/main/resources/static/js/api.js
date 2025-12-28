// API Service Layer

// Get token from localStorage
function getToken() {
    return localStorage.getItem('token');
}

// Make API request
async function apiRequest(endpoint, options = {}) {
    const url = getApiUrl(endpoint);
    const token = getToken();
    
    const defaultHeaders = {
        'Content-Type': 'application/json',
    };
    
    if (token) {
        defaultHeaders['Authorization'] = `Bearer ${token}`;
    }
    
    const config = {
        ...options,
        headers: {
            ...defaultHeaders,
            ...options.headers,
        },
    };
    
    try {
        const response = await fetch(url, config);
        
        // Handle 401 Unauthorized
        if (response.status === 401) {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/login';
            return;
        }
        
        // Handle 403 Forbidden
        if (response.status === 403) {
            showToast('Bạn không có quyền thực hiện thao tác này', 'error');
            return;
        }
        
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || 'Có lỗi xảy ra');
        }
        
        return data;
    } catch (error) {
        console.error('API Error:', error);
        showToast(error.message || 'Có lỗi xảy ra khi kết nối server', 'error');
        throw error;
    }
}

// API Methods
const api = {
    get: (endpoint) => apiRequest(endpoint, { method: 'GET' }),
    
    post: (endpoint, data) => apiRequest(endpoint, {
        method: 'POST',
        body: JSON.stringify(data),
    }),
    
    put: (endpoint, data) => apiRequest(endpoint, {
        method: 'PUT',
        body: JSON.stringify(data),
    }),
    
    patch: (endpoint, data) => apiRequest(endpoint, {
        method: 'PATCH',
        body: JSON.stringify(data),
    }),
    
    delete: (endpoint) => apiRequest(endpoint, { method: 'DELETE' }),
    
    upload: (endpoint, formData) => {
        const token = getToken();
        const url = getApiUrl(endpoint);
        
        return fetch(url, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
            },
            body: formData,
        });
    },
};

