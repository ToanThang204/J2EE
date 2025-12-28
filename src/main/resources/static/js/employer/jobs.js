// Employer Jobs Management JavaScript

document.addEventListener('DOMContentLoaded', function() {
    loadEmployerJobs();
    initJobActions();
});

function initJobActions() {
    const createBtn = document.getElementById('createJobBtn');
    if (createBtn) {
        createBtn.addEventListener('click', () => {
            window.location.href = '/employer/jobs/create';
        });
    }
}

async function loadEmployerJobs() {
    try {
        const response = await api.get('/employer/jobs');
        
        if (response && response.success && response.data) {
            displayJobs(response.data);
        }
    } catch (error) {
        console.error('Error loading employer jobs:', error);
    }
}

function displayJobs(jobs) {
    const container = document.getElementById('jobsList');
    if (!container) return;
    
    if (!jobs || jobs.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>Chưa có việc làm nào</p></div>';
        return;
    }
    
    container.innerHTML = jobs.map(job => `
        <div class="job-card-employer">
            <div class="job-card-employer-header">
                <div>
                    <h3 class="job-card-employer-title">${job.title || 'Job Title'}</h3>
                    <div class="job-card-employer-meta">
                        <span>📍 ${job.location || 'N/A'}</span>
                        <span>💰 ${job.salaryRange || 'Cạnh tranh'}</span>
                    </div>
                </div>
                <span class="job-status-badge ${job.status ? job.status.toLowerCase() : 'active'}">${job.status || 'ACTIVE'}</span>
            </div>
            <div class="job-card-employer-footer">
                <span class="job-applications-count">${job.applicationsCount || 0} đơn ứng tuyển</span>
                <div class="job-actions">
                    <a href="/jobs/${job.id}" class="btn btn-secondary">Xem</a>
                    <a href="/employer/jobs/${job.id}/edit" class="btn btn-primary">Chỉnh sửa</a>
                    <button class="btn-icon delete" onclick="deleteJob(${job.id})" title="Xóa">🗑️</button>
                </div>
            </div>
        </div>
    `).join('');
}

async function deleteJob(jobId) {
    if (!confirm('Bạn có chắc chắn muốn xóa việc làm này?')) {
        return;
    }
    
    try {
        const response = await api.delete(`/jobs/${jobId}`);
        
        if (response && response.success) {
            showToast('Xóa việc làm thành công', 'success');
            setTimeout(() => window.location.reload(), 1000);
        }
    } catch (error) {
        showToast('Lỗi khi xóa việc làm', 'error');
    }
}

