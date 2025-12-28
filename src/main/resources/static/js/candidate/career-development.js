// Career Development JavaScript

document.addEventListener('DOMContentLoaded', function() {
    loadCareerStats();
    loadSuggestions();
});

async function loadCareerStats() {
    try {
        const response = await api.get('/candidate/career-stats');
        
        if (response && response.success && response.data) {
            updateCareerStats(response.data);
        }
    } catch (error) {
        console.error('Error loading career stats:', error);
    }
}

function updateCareerStats(stats) {
    if (stats.totalApplications !== undefined) {
        const element = document.getElementById('totalApplications');
        if (element) element.textContent = stats.totalApplications || 0;
    }
    
    if (stats.approvedApplications !== undefined) {
        const element = document.getElementById('approvedApplications');
        if (element) element.textContent = stats.approvedApplications || 0;
    }
    
    if (stats.pendingApplications !== undefined) {
        const element = document.getElementById('pendingApplications');
        if (element) element.textContent = stats.pendingApplications || 0;
    }
}

async function loadSuggestions() {
    // Suggestions are static for now
    // Can be enhanced with AI-powered suggestions
}

