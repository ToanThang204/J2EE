// Candidate Settings JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initSettingsTabs();
    initAccountForm();
    initPasswordForm();
    initNotificationSettings();
    initPrivacySettings();
});

function initSettingsTabs() {
    const tabs = document.querySelectorAll('.settings-tab');
    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const tabName = this.dataset.tab;
            switchTab(tabName);
        });
    });
}

function switchTab(tabName) {
    document.querySelectorAll('.settings-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    
    document.querySelectorAll('.settings-tab-content').forEach(content => {
        content.classList.remove('active');
    });
    
    const activeTab = document.querySelector(`[data-tab="${tabName}"]`);
    const activeContent = document.getElementById(`${tabName}Tab`);
    
    if (activeTab) activeTab.classList.add('active');
    if (activeContent) activeContent.classList.add('active');
}

function initAccountForm() {
    const form = document.getElementById('accountForm');
    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const data = {
                name: document.getElementById('name').value
            };
            
            try {
                const response = await api.put('/profile', data);
                if (response && response.success) {
                    showToast('Cập nhật thông tin thành công', 'success');
                }
            } catch (error) {
                showToast('Lỗi khi cập nhật', 'error');
            }
        });
    }
}

function initPasswordForm() {
    const form = document.getElementById('passwordForm');
    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const currentPassword = document.getElementById('currentPassword').value;
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            
            if (newPassword !== confirmPassword) {
                showToast('Mật khẩu mới không khớp', 'error');
                return;
            }
            
            try {
                const response = await api.put('/profile/password', {
                    currentPassword,
                    newPassword
                });
                
                if (response && response.success) {
                    showToast('Đổi mật khẩu thành công', 'success');
                    form.reset();
                }
            } catch (error) {
                showToast('Lỗi khi đổi mật khẩu', 'error');
            }
        });
    }
}

function initNotificationSettings() {
    const form = document.getElementById('notificationSettingsForm');
    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const data = {
                emailNotifications: document.getElementById('emailNotifications').checked,
                jobAlerts: document.getElementById('jobAlerts').checked,
                applicationUpdates: document.getElementById('applicationUpdates').checked
            };
            
            try {
                const response = await api.put('/profile/notification-settings', data);
                if (response && response.success) {
                    showToast('Lưu cài đặt thông báo thành công', 'success');
                }
            } catch (error) {
                showToast('Lỗi khi lưu cài đặt', 'error');
            }
        });
    }
}

function initPrivacySettings() {
    const form = document.getElementById('privacySettingsForm');
    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const data = {
                profileVisibility: document.getElementById('profileVisibility').checked,
                allowMessages: document.getElementById('allowMessages').checked
            };
            
            try {
                const response = await api.put('/profile/privacy-settings', data);
                if (response && response.success) {
                    showToast('Lưu cài đặt bảo mật thành công', 'success');
                }
            } catch (error) {
                showToast('Lỗi khi lưu cài đặt', 'error');
            }
        });
    }
}

