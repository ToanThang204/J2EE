// Company Info JavaScript

document.addEventListener('DOMContentLoaded', function() {
    loadCompanyInfo();
    initCompanyInfo();
});

async function loadCompanyInfo() {
    // Check if company info is already rendered from server-side
    const logoSection = document.querySelector('.company-logo-section');
    const infoSection = document.querySelector('.info-section');
    
    // If already rendered (visible), skip API call
    if (logoSection && logoSection.style.display !== 'none') {
        console.log('Company info already rendered from server');
        return;
    }
    
    // Otherwise, try loading from API
    try {
        const company = await api.get('/companies/my-company');
        
        if (company) {
            displayCompanyInfo(company);
        } else {
            showEmptyState();
        }
    } catch (error) {
        console.log('No company found or error loading:', error);
        showEmptyState();
    }
}

function showEmptyState() {
    const emptyState = document.getElementById('emptyState');
    if (emptyState) emptyState.style.display = 'block';
    
    const logoSection = document.querySelector('.company-logo-section');
    const infoSection = document.querySelector('.info-section');
    if (logoSection) logoSection.style.display = 'none';
    if (infoSection) infoSection.style.display = 'none';
}

function displayCompanyInfo(company) {
    console.log('Displaying company:', company);
    
    // Hide empty state first
    const emptyState = document.getElementById('emptyState');
    if (emptyState) emptyState.style.display = 'none';
    
    // Show company logo section
    const logoSection = document.querySelector('.company-logo-section');
    if (logoSection) {
        logoSection.style.display = 'flex';
        
        const logoDiv = logoSection.querySelector('.company-logo');
        if (logoDiv) {
            // If company has logo, display image; otherwise show initials
            if (company.logo) {
                logoDiv.innerHTML = `<img src="${company.logo}" alt="${company.companyName}" style="width: 100%; height: 100%; object-fit: cover; border-radius: 16px;">`;
            } else if (company.companyName) {
                logoDiv.innerHTML = `<span style="font-size: 48px; font-weight: 700;">${company.companyName.substring(0, 2).toUpperCase()}</span>`;
            }
        }
        
        const nameEl = logoSection.querySelector('.company-name');
        if (nameEl) {
            nameEl.textContent = company.companyName || 'N/A';
        }
    }
    
    // Update and show description section
    const descSection = document.querySelector('.info-section');
    if (descSection) {
        descSection.style.display = 'block';
        const descText = document.querySelector('.info-description');
        if (descText) {
            descText.textContent = company.description || 'Chưa có mô tả';
        }
    }
    
    // Show contact section
    const contactSection = document.getElementById('contactSection');
    if (contactSection) {
        contactSection.style.display = 'block';
    }
    
    // Update contact values
    updateContactValue('address', company.address);
    updateContactValue('email', company.email);
    updateContactValue('phone', company.phone);
    updateContactValue('website', company.website);
}

function updateContactValue(field, value) {
    const elements = document.querySelectorAll(`[data-company-${field}]`);
    elements.forEach(el => {
        if (field === 'website' && value) {
            const link = el.querySelector('a');
            if (link) {
                link.href = value;
                link.textContent = value;
            }
        } else if (value) {
            el.textContent = value;
        }
    });
}

function initCompanyInfo() {
    const editBtn = document.getElementById('editCompanyBtn');
    const form = document.getElementById('companyInfoForm');
    
    if (editBtn) {
        editBtn.addEventListener('click', () => {
            window.location.href = '/employer/company/info/edit';
        });
    }
    
    if (form) {
        form.addEventListener('submit', handleSaveCompanyInfo);
    }
}

async function handleSaveCompanyInfo(e) {
    e.preventDefault();
    
    const form = e.target;
    const formData = new FormData(form);
    const data = Object.fromEntries(formData);
    
    try {
        const companyId = document.getElementById('companyId')?.value;
        const endpoint = companyId ? `/companies/${companyId}` : '/companies';
        const method = companyId ? 'put' : 'post';
        
        const response = await api[method](endpoint, data);
        
        if (response && response.success) {
            showToast('Lưu thông tin công ty thành công', 'success');
            setTimeout(() => window.location.reload(), 1000);
        }
    } catch (error) {
        showToast('Lỗi khi lưu thông tin', 'error');
    }
}

