// Sidebar authentication check
document.addEventListener('DOMContentLoaded', function() {
    updateSidebarAuth();
});

function updateSidebarAuth() {
    const token = localStorage.getItem('token');
    const userStr = localStorage.getItem('user');
    
    console.log('Sidebar.js: Checking auth...', { token: !!token, userStr });
    
    if (!token || !userStr) {
        // Not logged in - show candidate sidebar (or hide sidebar)
        console.log('Sidebar.js: Not logged in');
        return;
    }
    
    try {
        const user = JSON.parse(userStr);
        const role = user.role;
        
        console.log('Sidebar.js: User role =', role);
        
        // Hide all sidebar sections first
        document.querySelectorAll('.sidebar-nav-admin, .sidebar-nav-employer, .sidebar-nav-candidate').forEach(block => {
            block.style.display = 'none';
        });
        
        // Show appropriate sidebar based on role
        if (role === 'ADMIN') {
            const adminBlock = document.querySelector('.sidebar-nav-admin');
            console.log('Sidebar.js: Admin block found?', !!adminBlock);
            if (adminBlock) adminBlock.style.display = 'block';
            
            // Update sidebar header
            const subtitle = document.querySelector('.sidebar-subtitle');
            if (subtitle) subtitle.textContent = 'Admin Dashboard';
            
            const sidebar = document.querySelector('.sidebar');
            if (sidebar) {
                sidebar.classList.add('sidebar-dark');
                sidebar.classList.remove('sidebar-light');
            }
        } else if (role === 'EMPLOYER') {
            const employerBlock = document.querySelector('.sidebar-nav-employer');
            if (employerBlock) employerBlock.style.display = 'block';
            
            const subtitle = document.querySelector('.sidebar-subtitle');
            if (subtitle) subtitle.textContent = 'Employer Dashboard';
        } else {
            const candidateBlock = document.querySelector('.sidebar-nav-candidate');
            if (candidateBlock) candidateBlock.style.display = 'block';
            
            const subtitle = document.querySelector('.sidebar-subtitle');
            if (subtitle) subtitle.textContent = 'Candidate Dashboard';
        }
    } catch (e) {
        console.error('Error parsing user data:', e);
    }
}
