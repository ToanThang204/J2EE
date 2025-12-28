// Home Page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initHeroSection();
    initJobSearch();
    loadRecentJobs();
});

function initHeroSection() {
    const heroSearchInput = document.getElementById('heroSearchInput');
    const heroSearchBtn = document.getElementById('heroSearchBtn');
    
    if (heroSearchInput && heroSearchBtn) {
        heroSearchBtn.addEventListener('click', () => {
            const searchTerm = heroSearchInput.value.trim();
            if (searchTerm) {
                window.location.href = `/jobs?search=${encodeURIComponent(searchTerm)}`;
            }
        });
        
        heroSearchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                heroSearchBtn.click();
            }
        });
    }
}

function initJobSearch() {
    const searchInput = document.getElementById('jobSearchInput');
    if (searchInput) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                const searchTerm = e.target.value.trim();
                if (searchTerm) {
                    window.location.href = `/jobs?search=${encodeURIComponent(searchTerm)}`;
                }
            }
        });
    }
}

async function loadRecentJobs() {
    try {
        const response = await api.get('/jobs?limit=6');
        if (response && response.success && response.data) {
            displayJobs(response.data);
        }
    } catch (error) {
        console.error('Error loading recent jobs:', error);
    }
}

function displayJobs(jobs) {
    const jobsContainer = document.getElementById('recentJobsContainer');
    if (!jobsContainer || !jobs || jobs.length === 0) return;
    
    jobsContainer.innerHTML = jobs.map(job => `
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

