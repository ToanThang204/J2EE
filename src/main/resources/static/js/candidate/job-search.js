// Candidate Job Search JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initJobSearch();
    initFilters();
    loadJobs();
});

function initJobSearch() {
    const searchInput = document.getElementById('searchInput');
    const searchBtn = document.getElementById('searchBtn');
    
    if (searchInput && searchBtn) {
        searchBtn.addEventListener('click', searchJobs);
        
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                searchJobs();
            }
        });
    }
}

function initFilters() {
    const categoryFilter = document.getElementById('categoryFilter');
    const locationFilter = document.getElementById('locationFilter');
    const salaryFilter = document.getElementById('salaryFilter');
    
    [categoryFilter, locationFilter, salaryFilter].forEach(filter => {
        if (filter) {
            filter.addEventListener('change', searchJobs);
        }
    });
}

function searchJobs() {
    const searchInput = document.getElementById('searchInput');
    const categoryFilter = document.getElementById('categoryFilter');
    const locationFilter = document.getElementById('locationFilter');
    const salaryFilter = document.getElementById('salaryFilter');
    
    const params = new URLSearchParams();
    
    if (searchInput && searchInput.value.trim()) {
        params.append('search', searchInput.value.trim());
    }
    
    if (categoryFilter && categoryFilter.value) {
        params.append('category', categoryFilter.value);
    }
    
    if (locationFilter && locationFilter.value.trim()) {
        params.append('location', locationFilter.value.trim());
    }
    
    if (salaryFilter && salaryFilter.value) {
        params.append('salary', salaryFilter.value);
    }
    
    const queryString = params.toString();
    window.location.href = queryString ? `/candidate/job-search?${queryString}` : '/candidate/job-search';
}

async function loadJobs() {
    try {
        const urlParams = new URLSearchParams(window.location.search);
        const params = new URLSearchParams();
        
        ['search', 'category', 'location', 'salary'].forEach(key => {
            const value = urlParams.get(key);
            if (value) params.append(key, value);
        });
        
        const endpoint = params.toString() ? `/jobs?${params.toString()}` : '/jobs';
        const response = await api.get(endpoint);
        
        if (response && response.success && response.data) {
            displayJobs(response.data);
        }
    } catch (error) {
        console.error('Error loading jobs:', error);
    }
}

function displayJobs(jobs) {
    const grid = document.getElementById('jobsGrid');
    const emptyState = document.getElementById('emptyState');
    
    if (!grid) return;
    
    if (!jobs || jobs.length === 0) {
        grid.style.display = 'none';
        if (emptyState) emptyState.style.display = 'block';
        return;
    }
    
    grid.style.display = 'grid';
    if (emptyState) emptyState.style.display = 'none';
    
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

