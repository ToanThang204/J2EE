// Company Detail Page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    loadCompanyDetails();
    loadCompanyJobs();
});

async function loadCompanyDetails() {
    try {
        const companyId = getCompanyIdFromUrl();
        if (!companyId) return;
        
        const response = await api.get(`/companies/${companyId}`);
        
        if (response && response.success && response.data) {
            displayCompanyDetails(response.data);
        }
    } catch (error) {
        console.error('Error loading company details:', error);
    }
}

async function loadCompanyJobs() {
    try {
        const companyId = getCompanyIdFromUrl();
        if (!companyId) return;
        
        const response = await api.get(`/jobs?companyId=${companyId}`);
        
        if (response && response.success && response.data) {
            displayCompanyJobs(response.data);
        }
    } catch (error) {
        console.error('Error loading company jobs:', error);
    }
}

function displayCompanyDetails(company) {
    // Update company header
    const logoElement = document.querySelector('.company-detail-logo');
    if (logoElement && company.companyName) {
        logoElement.textContent = company.companyName.substring(0, 2).toUpperCase();
    }
    
    const nameElement = document.querySelector('.company-detail-info h1');
    if (nameElement) {
        nameElement.textContent = company.companyName || 'Company Name';
    }
    
    const descElement = document.querySelector('.company-detail-description');
    if (descElement) {
        descElement.textContent = company.description || 'Chưa có mô tả';
    }
}

function displayCompanyJobs(jobs) {
    const container = document.getElementById('companyJobsContainer');
    if (!container) return;
    
    if (!jobs || jobs.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>Công ty này chưa có việc làm nào</p></div>';
        return;
    }
    
    container.innerHTML = jobs.map(job => `
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

function getCompanyIdFromUrl() {
    const pathParts = window.location.pathname.split('/');
    const companiesIndex = pathParts.indexOf('companies');
    if (companiesIndex !== -1 && pathParts[companiesIndex + 1]) {
        return pathParts[companiesIndex + 1];
    }
    return null;
}

