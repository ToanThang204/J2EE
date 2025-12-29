// Employer Upgrade JavaScript

let selectedLogo = null;

document.addEventListener('DOMContentLoaded', function() {
    const upgradeForm = document.getElementById('upgradeForm');
    if (upgradeForm) {
        upgradeForm.addEventListener('submit', handleUpgrade);
    }
});

async function handleUpgrade(e) {
    e.preventDefault();
    
    const form = e.target;
    const submitBtn = form.querySelector('button[type="submit"]');
    const btnText = submitBtn.querySelector('.btn-text');
    const btnLoading = submitBtn.querySelector('.btn-loading');
    
    // Clear previous errors
    clearErrors();
    
    // Get form data
    const formData = {
        companyName: form.companyName.value.trim(),
        email: form.email.value.trim(),
        phone: form.phone.value.trim(),
        address: form.address.value.trim(),
        website: form.website.value.trim(),
        description: form.description.value.trim(),
        logo: selectedLogo // Base64 encoded logo
    };
    
    // Validate
    let hasError = false;
    
    if (!formData.companyName || formData.companyName.length < 2) {
        showError('companyNameError', 'Tên công ty phải có ít nhất 2 ký tự');
        hasError = true;
    }
    
    if (!formData.address) {
        showError('addressError', 'Vui lòng nhập địa chỉ công ty');
        hasError = true;
    }
    
    if (formData.website && !isValidUrl(formData.website)) {
        showError('websiteError', 'Website không hợp lệ');
        hasError = true;
    }
    
    if (formData.email && !isValidEmail(formData.email)) {
        showError('emailError', 'Email không hợp lệ');
        hasError = true;
    }
    
    if (!formData.description || formData.description.length < 20) {
        showError('descriptionError', 'Mô tả phải có ít nhất 20 ký tự');
        hasError = true;
    }
    
    if (hasError) return;
    
    // Show loading
    submitBtn.disabled = true;
    btnText.style.display = 'none';
    btnLoading.style.display = 'inline-block';
    
    // Debug logging
    console.log('Form data being sent:', {
        ...formData,
        logo: formData.logo ? `${formData.logo.substring(0, 50)}... (length: ${formData.logo.length})` : 'null'
    });
    
    try {
        const response = await api.post('/users/upgrade-to-employer', formData);
        
        if (response && response.success) {
            showToast('Gửi yêu cầu nâng cấp thành công! Vui lòng chờ admin phê duyệt.', 'success');
            
            // Redirect to homepage
            setTimeout(() => {
                window.location.href = '/';
            }, 2000);
        }
    } catch (error) {
        showToast(error.message || 'Có lỗi xảy ra khi nâng cấp tài khoản', 'error');
        
        // Reset button
        submitBtn.disabled = false;
        btnText.style.display = 'inline-block';
        btnLoading.style.display = 'none';
    }
}

function isValidUrl(string) {
    try {
        new URL(string);
        return true;
    } catch (_) {
        return false;
    }
}

function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function handleLogoUpload(event) {
    const file = event.target.files[0];
    if (!file) return;
    
    // Validate file size (5MB)
    if (file.size > 5 * 1024 * 1024) {
        showError('logoError', 'Kích thước file không được vượt quá 5MB');
        return;
    }
    
    // Validate file type
    if (!file.type.startsWith('image/')) {
        showError('logoError', 'Vui lòng chọn file ảnh');
        return;
    }
    
    const reader = new FileReader();
    reader.onload = function(e) {
        selectedLogo = e.target.result; // Base64 string with data URL
        
        // Show preview
        const preview = document.getElementById('logoPreview');
        const previewImg = document.getElementById('logoPreviewImg');
        const uploadArea = document.getElementById('logoUploadArea');
        
        previewImg.src = selectedLogo;
        preview.style.display = 'block';
        uploadArea.style.display = 'none';
        
        // Clear error
        const errorEl = document.getElementById('logoError');
        if (errorEl) {
            errorEl.classList.remove('show');
            errorEl.textContent = '';
        }
    };
    reader.readAsDataURL(file);
}

function removeLogo() {
    selectedLogo = null;
    
    const preview = document.getElementById('logoPreview');
    const previewImg = document.getElementById('logoPreviewImg');
    const uploadArea = document.getElementById('logoUploadArea');
    const fileInput = document.getElementById('logoInput');
    
    previewImg.src = '';
    preview.style.display = 'none';
    uploadArea.style.display = 'flex';
    fileInput.value = '';
}

function showError(elementId, message) {
    const errorElement = document.getElementById(elementId);
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.classList.add('show');
        const input = document.querySelector(`#${elementId.replace('Error', '')}`);
        if (input) {
            input.classList.add('error');
        }
    }
}

function clearErrors() {
    const errorElements = document.querySelectorAll('.error-message');
    errorElements.forEach(el => {
        el.textContent = '';
        el.classList.remove('show');
    });
    
    const inputs = document.querySelectorAll('.form-input-modern');
    inputs.forEach(input => input.classList.remove('error'));
}
