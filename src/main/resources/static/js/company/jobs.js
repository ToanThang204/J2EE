// Company Jobs JavaScript

document.addEventListener('DOMContentLoaded', function() {
    loadCompanyJobs();
});

async function loadCompanyJobs() {
    try {
        const companyId = getCompanyId();
        if (!companyId) return;
        
        const response = await api.get(`/jobs?companyId=${companyId}`);
        
        if (response && response.success && response.data) {
            displayJobs(response.data);
        }
    } catch (error) {
        console.error('Error loading company jobs:', error);
    }
}

function displayJobs(jobs) {
    const container = document.getElementById('companyJobsList');
    if (!container) return;
    
    if (!jobs || jobs.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>Công ty này chưa có việc làm nào</p></div>';
        return;
    }
    
    container.innerHTML = jobs.map(job => `
        <div class="job-card" onclick="window.location.href='/jobs/${job.id}'">
            <h3 class="job-title">${job.title || 'Job Title'}</h3>
            <div class="job-meta">
                <span>📍 ${job.location || 'N/A'}</span>
                <span>💰 ${job.salaryRange || 'Cạnh tranh'}</span>
            </div>
        </div>
    `).join('');
}

function getCompanyId() {
    return document.getElementById('companyId')?.value ||
           new URLSearchParams(window.location.search).get('companyId');
}

