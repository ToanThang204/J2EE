// WebCV API Configuration
const API_CONFIG = {
    BASE_URL: '/api',
    TIMEOUT: 30000,
    RETRY_ATTEMPTS: 3
};

// Helper to get full API URL
function getApiUrl(endpoint) {
    return `${API_CONFIG.BASE_URL}${endpoint}`;
}

