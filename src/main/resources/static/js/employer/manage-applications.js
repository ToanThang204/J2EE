// Employer Manage Applications JavaScript

document.addEventListener('DOMContentLoaded', function() {
    loadApplications();
    initApplicationFilters();
});

function initApplicationFilters() {
    const searchInput = document.getElementById('searchInput');
    const searchBtn = document.getElementById('searchBtn');
    
    if (searchInput && searchBtn) {
        searchBtn.addEventListener('click', searchApplications);
        
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                searchApplications();
            }
        });
    }
}

function searchApplications() {
    const searchInput = document.getElementById('searchInput');
    const searchTerm = searchInput ? searchInput.value.trim() : '';
    
    if (searchTerm) {
        window.location.href = `/employer/manage-applications?search=${encodeURIComponent(searchTerm)}`;
    } else {
        window.location.href = '/employer/manage-applications';
    }
}

async function loadApplications() {
    try {
        const urlParams = new URLSearchParams(window.location.search);
        const search = urlParams.get('search');
        
        const endpoint = search ? `/employer/applications?search=${encodeURIComponent(search)}` : '/employer/applications';
        const response = await api.get(endpoint);
        
        if (response && response.success && response.data) {
            displayApplications(response.data);
        }
    } catch (error) {
        console.error('Error loading applications:', error);
    }
}

function displayApplications(applications) {
    const container = document.getElementById('applicationsList');
    if (!container) return;
    
    if (!applications || applications.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>Chưa có đơn ứng tuyển nào</p></div>';
        return;
    }
    
    container.innerHTML = applications.map(app => `
        <div class="application-item">
            <div class="application-item-header">
                <div class="application-candidate-info">
                    <h3 class="application-candidate-name">${app.user ? app.user.name : 'N/A'}</h3>
                    <p class="application-job-title">${app.job ? app.job.title : 'N/A'}</p>
                    <span class="application-date">Ứng tuyển: ${formatDate(app.createdAt)}</span>
                </div>
                <span class="status-badge ${app.status ? app.status.toLowerCase() : ''}">${app.status || 'N/A'}</span>
            </div>
            <div class="application-actions">
                <button class="btn btn-primary" onclick="viewApplication(${app.id})">Xem chi tiết</button>
                ${app.resume ? `<a href="/candidate/resumes/${app.resume.id}" class="application-resume-link">Xem CV</a>` : ''}
                <select class="application-status-select" onchange="updateApplicationStatus(${app.id}, this.value)">
                    <option value="PENDING" ${app.status === 'PENDING' ? 'selected' : ''}>Chờ duyệt</option>
                    <option value="REVIEWED" ${app.status === 'REVIEWED' ? 'selected' : ''}>Đã xem</option>
                    <option value="INTERVIEW" ${app.status === 'INTERVIEW' ? 'selected' : ''}>Phỏng vấn</option>
                    <option value="REJECTED" ${app.status === 'REJECTED' ? 'selected' : ''}>Từ chối</option>
                    <option value="HIRED" ${app.status === 'HIRED' ? 'selected' : ''}>Trúng tuyển</option>
                </select>
            </div>
        </div>
    `).join('');
}

function viewApplication(applicationId) {
    window.location.href = `/employer/applications/${applicationId}`;
}

async function updateApplicationStatus(applicationId, status) {
    try {
        const response = await api.put(`/applications/${applicationId}/status`, { status });
        
        if (response && response.success) {
            showToast('Cập nhật trạng thái thành công', 'success');
            setTimeout(() => window.location.reload(), 1000);
        }
    } catch (error) {
        showToast('Lỗi khi cập nhật trạng thái', 'error');
    }
}

