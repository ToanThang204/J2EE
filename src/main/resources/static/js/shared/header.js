// Header Authentication UI Handler

function initHeaderAuth() {
    try {
        console.log('Header auth: Initializing...');
        if (typeof updateHeaderAuth === 'function') updateHeaderAuth();
        if (typeof initializeUserMenuToggle === 'function') initializeUserMenuToggle();
    } catch (e) {
        console.warn('Header auth: initialization failed', e);
    }
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initHeaderAuth);
} else {
    // Document already loaded — initialize immediately
    initHeaderAuth();
}

// Also ensure auth UI updates on full window load and when localStorage changes (other tabs/logins)
window.addEventListener('load', function() {
    try { initHeaderAuth(); } catch (e) {}
});

window.addEventListener('storage', function(e) {
    if (!e) return;
    if (e.key === 'user' || e.key === 'token') {
        try { updateHeaderAuth(); } catch (err) {}
    }
});

function initializeUserMenuToggle() {
    const userProfile = document.querySelector('.user-profile');
    const userDropdown = document.querySelector('.user-dropdown');
    
    if (!userProfile || !userDropdown) return;
    
    let hideTimeout = null;
    
    // Show dropdown on hover
    userProfile.addEventListener('mouseenter', function(e) {
        if (hideTimeout) {
            clearTimeout(hideTimeout);
            hideTimeout = null;
        }
        userDropdown.style.display = 'block';
        userDropdown.style.opacity = '1';
    });
    
    // Delay hide when leaving user profile
    userProfile.addEventListener('mouseleave', function(e) {
        hideTimeout = setTimeout(() => {
            userDropdown.style.display = 'none';
            userDropdown.style.opacity = '0';
        }, 300);
    });
    
    // Cancel hide when entering dropdown
    userDropdown.addEventListener('mouseenter', function(e) {
        if (hideTimeout) {
            clearTimeout(hideTimeout);
            hideTimeout = null;
        }
    });
    
    // Hide when leaving dropdown
    userDropdown.addEventListener('mouseleave', function(e) {
        hideTimeout = setTimeout(() => {
            userDropdown.style.display = 'none';
            userDropdown.style.opacity = '0';
        }, 300);
    });
    
    // Toggle dropdown on click
    userProfile.addEventListener('click', function(e) {
        e.stopPropagation();
        if (hideTimeout) {
            clearTimeout(hideTimeout);
            hideTimeout = null;
        }
        const isVisible = userDropdown.style.display === 'block';
        userDropdown.style.display = isVisible ? 'none' : 'block';
        userDropdown.style.opacity = isVisible ? '0' : '1';
    });
    
    // Close dropdown when clicking outside
    document.addEventListener('click', function(e) {
        if (!userProfile.contains(e.target) && !userDropdown.contains(e.target)) {
            if (hideTimeout) {
                clearTimeout(hideTimeout);
                hideTimeout = null;
            }
            userDropdown.style.display = 'none';
            userDropdown.style.opacity = '0';
        }
    });
    
    // Allow clicking menu items
    const dropdownItems = userDropdown.querySelectorAll('a');
    dropdownItems.forEach(item => {
        item.addEventListener('click', function(e) {
            if (hideTimeout) {
                clearTimeout(hideTimeout);
                hideTimeout = null;
            }
            // Let the link navigate
            setTimeout(() => {
                userDropdown.style.display = 'none';
                userDropdown.style.opacity = '0';
            }, 100);
        });
    });
}

function updateHeaderAuth() {
    const token = localStorage.getItem('token');
    const userStr = localStorage.getItem('user');

    console.log('Header auth: Token exists?', !!token);
    console.log('Header auth: User exists?', !!userStr);

    if (!token) {
        console.log('Header auth: No token — showing auth buttons');
        showAuthButtons();
        return;
    }

    if (!userStr) {
        // Token exists but no user stored — try to fetch current user
        console.log('Header auth: Token present but no user in localStorage — attempting to fetch user');
        attemptFetchCurrentUser().then(fetchedUser => {
            if (fetchedUser) {
                showUserProfile(fetchedUser);
            } else {
                showAuthButtons();
            }
        }).catch(err => {
            console.warn('Header auth: Failed to fetch current user', err);
            showAuthButtons();
        });
        return;
    }

    try {
        const user = JSON.parse(userStr);
        console.log('Header auth: User data:', user);
        // User is logged in - show user profile
        showUserProfile(user);
    } catch (error) {
        console.error('Header auth: Error parsing user data:', error);
        showAuthButtons();
    }
}
// Safety: if user data present in localStorage but UI still hidden (race/ordering), force update after small delay
setTimeout(function() {
    try {
        const token = localStorage.getItem('token');
        const userStr = localStorage.getItem('user');
        if (token && userStr) {
            // If user-profile still hidden, try to show it
            const userProfile = document.querySelector('.user-profile');
            if (userProfile && (userProfile.style.display === 'none' || window.getComputedStyle(userProfile).display === 'none')) {
                try {
                    const user = JSON.parse(userStr);
                    showUserProfile(user);
                    console.log('Header auth: Forced showUserProfile after delay');
                } catch (e) {
                    // ignore
                }
            }
        }
    } catch (e) {}
}, 500);
function showAuthButtons() {
    // Hide user-related elements
    const userProfile = document.querySelector('.user-profile');
    const headerBadges = document.querySelector('.header-badges');
    const headerNav = document.querySelector('.header-nav');
    const btnUpgrade = document.querySelector('.btn-upgrade');

    if (userProfile) userProfile.style.display = 'none';
    if (headerBadges) headerBadges.style.display = 'none';
    if (headerNav) headerNav.style.display = 'none';
    if (btnUpgrade) btnUpgrade.style.display = 'none';

    // Show auth buttons
    const authButtons = document.querySelector('.auth-buttons');
    if (authButtons) {
        authButtons.style.display = 'flex';
    }
}

function showUserProfile(user) {
    // Hide auth buttons
    const authButtons = document.querySelector('.auth-buttons');
    if (authButtons) {
        authButtons.style.display = 'none';
    }

    // Show user-related elements
    const userProfile = document.querySelector('.user-profile');
    const headerBadges = document.querySelector('.header-badges');
    const headerNav = document.querySelector('.header-nav');
    const btnUpgrade = document.querySelector('.btn-upgrade');

    if (headerNav) headerNav.style.display = 'flex';
    if (headerBadges) headerBadges.style.display = 'flex';

    // Update user profile info
    if (userProfile) {
        userProfile.style.display = 'flex';

        const userName = userProfile.querySelector('.user-name');
        const userRole = userProfile.querySelector('.user-role');

        if (userName) userName.textContent = user.name || 'User';
        if (userRole) {
            let roleText = 'Ứng Viên';
            if (user.role === 'EMPLOYER') roleText = 'Nhà Tuyển Dụng';
            else if (user.role === 'ADMIN') roleText = 'Admin';
            userRole.textContent = roleText;
        }

        // Show/hide profile links based on role
        // Show/hide profile links based on role
        const candidateLinks = userProfile.querySelectorAll('.profile-link-candidate');
        const employerLinks = userProfile.querySelectorAll('.profile-link-employer');
        const adminLinks = userProfile.querySelectorAll('.profile-link-admin');

        candidateLinks.forEach(link => link.style.display = user.role === 'CANDIDATE' ? 'flex' : 'none');
        employerLinks.forEach(link => link.style.display = user.role === 'EMPLOYER' ? 'flex' : 'none');
        adminLinks.forEach(link => link.style.display = user.role === 'ADMIN' ? 'flex' : 'none');
    }

    // Show upgrade button only for candidates
    if (btnUpgrade && user.role === 'CANDIDATE') {
        btnUpgrade.style.display = 'inline-block';
    } else if (btnUpgrade) {
        btnUpgrade.style.display = 'none';
    }

    // Update dropdown header info if exists
    const dropdownHeaderName = document.querySelector('.dropdown-header-name');
    const dropdownHeaderRole = document.querySelector('.dropdown-header-role');
    if (dropdownHeaderName) dropdownHeaderName.textContent = user.name || 'User';
    if (dropdownHeaderRole) {
        let roleText = 'Ứng Viên';
        if (user.role === 'EMPLOYER') roleText = 'Nhà Tuyển Dụng';
        else if (user.role === 'ADMIN') roleText = 'Admin';
        dropdownHeaderRole.textContent = roleText;
    }
}

function openSettings() {
    console.log('Opening settings...');
    // This can be expanded to open a settings modal or navigate to settings page
    window.location.href = '/candidate/settings';
}

// Try a few common endpoints to get current user when token exists but localStorage lacks `user`.
async function attemptFetchCurrentUser() {
    const endpoints = ['/auth/me', '/api/auth/me', '/users/me', '/api/users/me'];
    for (let ep of endpoints) {
        try {
            if (typeof api !== 'undefined' && typeof api.get === 'function') {
                const resp = await api.get(ep);
                // handle envelope {success, data} or direct user object
                const user = resp && resp.data ? resp.data : (resp && resp.user ? resp.user : resp);
                if (user && (user.name || user.email || user.id)) {
                    try { localStorage.setItem('user', JSON.stringify(user)); } catch (e) {}
                    return user;
                }
            } else {
                // fallback fetch with Authorization header if token present
                const token = localStorage.getItem('token');
                if (!token) break;
                const r = await fetch(ep, { headers: { 'Authorization': 'Bearer ' + token } });
                if (!r.ok) continue;
                const j = await r.json();
                const user = j && j.data ? j.data : j;
                if (user && (user.name || user.email || user.id)) {
                    try { localStorage.setItem('user', JSON.stringify(user)); } catch (e) {}
                    return user;
                }
            }
        } catch (e) {
            // try next endpoint
        }
    }
    return null;
}
