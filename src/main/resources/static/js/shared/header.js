// Header Authentication UI Handler

document.addEventListener('DOMContentLoaded', function() {
    console.log('Header auth: Initializing...');
    updateHeaderAuth();
});

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
        const candidateLink = userProfile.querySelector('.profile-link-candidate');
        const employerLink = userProfile.querySelector('.profile-link-employer');
        const adminLink = userProfile.querySelector('.profile-link-admin');
        
        if (candidateLink) candidateLink.style.display = user.role === 'CANDIDATE' ? 'block' : 'none';
        if (employerLink) employerLink.style.display = user.role === 'EMPLOYER' ? 'block' : 'none';
        if (adminLink) adminLink.style.display = user.role === 'ADMIN' ? 'block' : 'none';
    }
    
    // Show upgrade button only for candidates
    if (btnUpgrade && user.role === 'CANDIDATE') {
        btnUpgrade.style.display = 'inline-block';
    } else if (btnUpgrade) {
        btnUpgrade.style.display = 'none';
    }
}
