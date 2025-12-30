// API Service Layer

// Get token from localStorage
function getToken() {
    return localStorage.getItem('token');
}

// Make API request
async function apiRequest(endpoint, options = {}) {
    const url = getApiUrl(endpoint);
    const token = getToken();

    const defaultHeaders = {};

    // Auto handle Content-Type for JSON
    if (!(options.body instanceof FormData)) {
        defaultHeaders['Content-Type'] = 'application/json';
    }

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

    // Auto stringify body if not FormData
    if (config.body && !(config.body instanceof FormData) && typeof config.body !== 'string') {
        config.body = JSON.stringify(config.body);
    }

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

        // Create a smarter response handler
        const contentType = response.headers.get('content-type') || '';
        let data = null;

        if (contentType.includes('application/json')) {
            data = await response.json();
        } else {
            // If not JSON (e.g. HTML error page), get text for debugging
            const text = await response.text();
            console.error('Non-JSON response received:', response.status, text);

            // Try to extract error reasoning from HTML if possible or just throw generic
            throw new Error(`Server returned ${response.status} (Non-JSON). Check console logs.`);
        }

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
        body: data,
    }),

    put: (endpoint, data) => apiRequest(endpoint, {
        method: 'PUT',
        body: data,
    }),

    patch: (endpoint, data) => apiRequest(endpoint, {
        method: 'PATCH',
        body: data,
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

