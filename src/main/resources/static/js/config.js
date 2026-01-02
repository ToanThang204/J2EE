// WebCV API Configuration
const API_CONFIG = {
    BASE_URL: '/api',
    TIMEOUT: 30000,
    RETRY_ATTEMPTS: 3
};

// Helper to get full API URL
function getApiUrl(endpoint) {
    if (!endpoint) return API_CONFIG.BASE_URL;
    // If endpoint already contains the base, return as-is
    if (endpoint.startsWith(API_CONFIG.BASE_URL)) return endpoint;
    // Ensure single slash between base and endpoint
    if (endpoint.startsWith('/')) {
        return API_CONFIG.BASE_URL + endpoint;
    }
    return API_CONFIG.BASE_URL + '/' + endpoint;
}

