// Community JavaScript

document.addEventListener('DOMContentLoaded', function() {
    loadCommunityStats();
});

async function loadCommunityStats() {
    try {
        const response = await api.get('/community/stats');
        
        if (response && response.success && response.data) {
            updateCommunityStats(response.data);
        }
    } catch (error) {
        console.error('Error loading community stats:', error);
        // Use default values if API fails
        updateCommunityStats({
            members: 0,
            posts: 0
        });
    }
}

function updateCommunityStats(stats) {
    if (stats.members !== undefined) {
        const element = document.getElementById('communityMembers');
        if (element) element.textContent = stats.members || 0;
    }
    
    if (stats.posts !== undefined) {
        const element = document.getElementById('totalPosts');
        if (element) element.textContent = stats.posts || 0;
    }
}

