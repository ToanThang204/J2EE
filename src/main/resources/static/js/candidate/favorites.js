// Candidate Favorites/Saved Jobs JavaScript

document.addEventListener('DOMContentLoaded', function() {
    loadSavedJobs();
});

async function loadSavedJobs() {
    try {
        const response = await api.get('/saved-jobs');
        
        if (response && response.success && response.data) {
            displaySavedJobs(response.data);
        }
    } catch (error) {
        console.error('Error loading saved jobs:', error);
    }
}

function displaySavedJobs(savedJobs) {
    const container = document.getElementById('savedJobsContainer');
    if (!container) return;
    
    if (!savedJobs || savedJobs.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>Chưa có việc làm nào được lưu</p></div>';
        return;
    }
    
    container.innerHTML = savedJobs.map(savedJob => `
        <div class="saved-job-card">
            <div class="saved-job-info">
                <h3 class="saved-job-title">${savedJob.job ? savedJob.job.title : 'N/A'}</h3>
                <p class="saved-job-company">${savedJob.job && savedJob.job.company ? savedJob.job.company.companyName : 'N/A'}</p>
                <span class="saved-job-date">Đã lưu: ${formatDate(savedJob.createdAt)}</span>
            </div>
            <div class="saved-job-actions">
                <a href="/jobs/${savedJob.job ? savedJob.job.id : ''}" class="btn btn-primary">Xem chi tiết</a>
                <button class="btn btn-secondary" onclick="removeFavorite(${savedJob.id})">Bỏ lưu</button>
            </div>
        </div>
    `).join('');
}

async function removeFavorite(savedJobId) {
    if (!confirm('Bạn có chắc chắn muốn bỏ lưu việc làm này?')) {
        return;
    }
    
    try {
        const response = await api.delete(`/saved-jobs/${savedJobId}`);
        
        if (response && response.success) {
            showToast('Đã bỏ lưu việc làm', 'success');
            setTimeout(() => window.location.reload(), 1000);
        }
    } catch (error) {
        showToast('Lỗi khi bỏ lưu', 'error');
    }
}

