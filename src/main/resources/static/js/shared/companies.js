// Companies List JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initCompanySearch();
    loadCompanies();
});

function initCompanySearch() {
    const searchInput = document.getElementById('companySearchInput');
    const searchBtn = document.getElementById('companySearchBtn');
    
    if (searchInput && searchBtn) {
        searchBtn.addEventListener('click', searchCompanies);
        
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                searchCompanies();
            }
        });
    }
}

function searchCompanies() {
    const searchInput = document.getElementById('companySearchInput');
    const searchTerm = searchInput ? searchInput.value.trim() : '';
    
    if (searchTerm) {
        window.location.href = `/pages/companies?search=${encodeURIComponent(searchTerm)}`;
    } else {
        window.location.href = '/pages/companies';
    }
}

async function loadCompanies() {
    try {
        const urlParams = new URLSearchParams(window.location.search);
        const search = urlParams.get('search');
        
        const endpoint = search ? `/companies?search=${encodeURIComponent(search)}` : '/companies';
        const response = await api.get(endpoint);
        
        if (response && response.success && response.data) {
            displayCompanies(response.data);
        }
    } catch (error) {
        console.error('Error loading companies:', error);
    }
}

function displayCompanies(companies) {
    const container = document.getElementById('companiesContainer');
    if (!container) return;
    
    if (!companies || companies.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>Không tìm thấy công ty nào</p></div>';
        return;
    }
    
    container.innerHTML = companies.map(company => `
        <div class="company-card-large" onclick="window.location.href='/companies/${company.id}'">
            <div class="company-logo">${company.companyName ? company.companyName.substring(0, 2).toUpperCase() : 'CO'}</div>
            <div class="company-info">
                <h3 class="company-name">${company.companyName || 'Company Name'}</h3>
                <p class="company-description">${company.description ? (company.description.length > 150 ? company.description.substring(0, 150) + '...' : company.description) : 'Chưa có mô tả'}</p>
                <div class="company-meta">
                    <span>📍 ${company.address || 'N/A'}</span>
                    <span class="company-jobs-count">${company.jobs ? company.jobs.length : 0} việc làm</span>
                </div>
            </div>
        </div>
    `).join('');
}

