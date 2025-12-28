// Category Jobs JavaScript

document.addEventListener('DOMContentLoaded', function() {
    loadCategoryJobs();
});

async function loadCategoryJobs() {
    try {
        const urlParams = new URLSearchParams(window.location.search);
        const categoryId = urlParams.get('categoryId');
        
        if (!categoryId) {
            showToast('Không tìm thấy danh mục', 'error');
            return;
        }
        
        const response = await api.get(`/jobs?categoryId=${categoryId}`);
        
        if (response && response.success && response.data) {
            displayJobs(response.data);
        }
    } catch (error) {
        console.error('Error loading category jobs:', error);
    }
}

function displayJobs(jobs) {
    const grid = document.querySelector('.jobs-grid');
    if (!grid) return;
    
    if (!jobs || jobs.length === 0) {
        grid.innerHTML = '<div class="empty-state"><p>Không có việc làm nào trong danh mục này</p></div>';
        return;
    }
    
    grid.innerHTML = jobs.map(job => `
        <div class="job-card" onclick="window.location.href='/jobs/${job.id}'">
            <div class="job-card-header">
                <div class="company-logo-small">${job.company ? job.company.companyName.substring(0, 2).toUpperCase() : 'CO'}</div>
                <div class="company-name-small">${job.company ? job.company.companyName : 'Company'}</div>
            </div>
            <h3 class="job-title">${job.title || 'Job Title'}</h3>
            <div class="job-meta">
                <div class="job-meta-item">
                    <span>📍</span>
                    <span>${job.location || 'N/A'}</span>
                </div>
            </div>
            <div class="job-footer">
                <span class="salary-badge">${job.salaryRange || 'Cạnh tranh'}</span>
            </div>
        </div>
    `).join('');
}

