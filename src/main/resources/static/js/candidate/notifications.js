// Candidate Notifications JavaScript

document.addEventListener('DOMContentLoaded', function() {
    loadNotifications();
    initNotificationActions();
});

async function loadNotifications() {
    try {
        const response = await api.get('/notifications');
        
        if (response && response.success && response.data) {
            displayNotifications(response.data.content || response.data);
        }
    } catch (error) {
        console.error('Error loading notifications:', error);
    }
}

function displayNotifications(notifications) {
    const container = document.getElementById('notificationsList');
    if (!container) return;
    
    if (!notifications || notifications.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>Chưa có thông báo nào</p></div>';
        return;
    }
    
    container.innerHTML = notifications.map(notif => `
        <div class="notification-card ${notif.read ? '' : 'unread'}">
            <div class="notification-card-header">
                <div>
                    <h3 class="notification-title">${notif.title || 'Thông báo'}</h3>
                    <p class="notification-message">${notif.message || 'N/A'}</p>
                </div>
                ${!notif.read ? '<span class="notification-badge-new">Mới</span>' : ''}
            </div>
            <div class="notification-footer">
                <span class="notification-date">${formatDate(notif.createdAt)}</span>
                <div class="notification-actions">
                    ${!notif.read ? `<button class="btn btn-secondary" onclick="markAsRead(${notif.id})">Đánh dấu đã đọc</button>` : ''}
                    <button class="btn-icon delete" onclick="deleteNotification(${notif.id})" title="Xóa">🗑️</button>
                </div>
            </div>
        </div>
    `).join('');
}

function initNotificationActions() {
    const markAllBtn = document.getElementById('markAllAsReadBtn');
    if (markAllBtn) {
        markAllBtn.addEventListener('click', markAllAsRead);
    }
}

async function markAsRead(notificationId) {
    try {
        const response = await api.post(`/notifications/${notificationId}/read`, {});
        
        if (response && response.success) {
            showToast('Đã đánh dấu đã đọc', 'success');
            setTimeout(() => window.location.reload(), 500);
        }
    } catch (error) {
        showToast('Lỗi khi đánh dấu đã đọc', 'error');
    }
}

async function markAllAsRead() {
    try {
        const response = await api.post('/notifications/mark-all-read', {});
        
        if (response && response.success) {
            showToast('Đã đánh dấu tất cả đã đọc', 'success');
            setTimeout(() => window.location.reload(), 500);
        }
    } catch (error) {
        showToast('Lỗi khi đánh dấu đã đọc', 'error');
    }
}

async function deleteNotification(notificationId) {
    if (!confirm('Bạn có chắc chắn muốn xóa thông báo này?')) {
        return;
    }
    
    try {
        const response = await api.delete(`/notifications/${notificationId}`);
        
        if (response && response.success) {
            showToast('Xóa thông báo thành công', 'success');
            setTimeout(() => window.location.reload(), 500);
        }
    } catch (error) {
        showToast('Lỗi khi xóa thông báo', 'error');
    }
}

