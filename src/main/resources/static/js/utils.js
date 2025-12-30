// Utility Functions

// Format date
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });
}

// Format currency
function formatCurrency(amount) {
    if (!amount) return '0 VNĐ';
    return new Intl.NumberFormat('vi-VN').format(amount) + ' VNĐ';
}

// Validate email
function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

// Validate password
function validatePassword(password) {
    return password && password.length >= 6;
}

// Show loading spinner
function showLoading(element) {
    if (element) {
        element.innerHTML = '<div class="spinner"></div>';
    }
}

// Hide loading spinner
function hideLoading(element) {
    if (element) {
        element.innerHTML = '';
    }
}

// Show toast notification
function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('show');
    }, 100);

    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => {
            document.body.removeChild(toast);
        }, 300);
    }, 3000);
}

// Debounce function
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Throttle function
function throttle(func, limit) {
    let inThrottle;
    return function (...args) {
        if (!inThrottle) {
            func.apply(this, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}

// Logout function
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    sessionStorage.clear();
    window.location.href = '/logout';
}

// Toggle mobile menu
function toggleMobileMenu() {
    const sidebar = document.querySelector('.sidebar');
    const overlay = document.getElementById('mobileMenuOverlay');

    if (sidebar && overlay) {
        sidebar.classList.toggle('mobile-open');
        overlay.classList.toggle('show');
    }
}

// Load notification and favorite counts
async function loadBadgeCounts() {
    const token = localStorage.getItem('token');
    if (!token) return;

    try {
        // Load notification count
        const notifResponse = await api.get('/notifications/unread-count');
        if (notifResponse && notifResponse.success && notifResponse.data !== null) {
            const count = notifResponse.data;
            const notifBadge = document.getElementById('notificationCount');
            if (notifBadge) {
                notifBadge.textContent = count > 99 ? '99+' : count;
                notifBadge.style.display = count > 0 ? 'flex' : 'none';
            }
        }

        // Load favorite count (if endpoint exists)
        // const favResponse = await api.get('/saved-jobs/count');
        // Similar implementation
    } catch (error) {
        console.error('Error loading badge counts:', error);
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function () {
    loadBadgeCounts();

    // Set interval to refresh counts every 30 seconds
    setInterval(loadBadgeCounts, 30000);
});

