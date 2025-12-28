// Company Info JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initCompanyInfo();
});

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

