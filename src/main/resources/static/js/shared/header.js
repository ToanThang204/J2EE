// Header Authentication UI Handler

document.addEventListener('DOMContentLoaded', function () {
    console.log('Header auth: Initializing...');
    updateHeaderAuth();
    initializeUserMenuToggle();
});

function initializeUserMenuToggle() {
    const userProfile = document.querySelector('.user-profile');
    const userDropdown = document.querySelector('.user-dropdown');
    
    if (!userProfile || !userDropdown) return;
    
    // Toggle dropdown on click
    userProfile.addEventListener('click', function(e) {
        e.stopPropagation();
        const isVisible = userDropdown.style.display === 'block';
        userDropdown.style.display = isVisible ? 'none' : 'block';
        userDropdown.style.opacity = isVisible ? '0' : '1';
    });
    
    // Close dropdown when clicking outside
    document.addEventListener('click', function(e) {
        if (!userProfile.contains(e.target) && !userDropdown.contains(e.target)) {
            userDropdown.style.display = 'none';
            userDropdown.style.opacity = '0';
        }
    });
    
    // Allow clicking menu items
    const dropdownItems = userDropdown.querySelectorAll('a');
    dropdownItems.forEach(item => {
        item.addEventListener('click', function(e) {
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

    if (!token || !userStr) {
        // User not logged in - show auth buttons
        console.log('Header auth: Showing auth buttons');
        showAuthButtons();
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
