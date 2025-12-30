// Candidate Applications JavaScript

document.addEventListener('DOMContentLoaded', function () {
    initApplicationFilters();
    // loadApplications(); // Removed as data is now SSR-ed
});

function initApplicationFilters() {
    const tabs = document.querySelectorAll('.application-tab');
    tabs.forEach(tab => {
        tab.addEventListener('click', function () {
            const status = this.dataset.status || 'all';
            filterApplications(status);
        });
    });
}

function filterApplications(status) {
    const url = new URL(window.location.href);
    if (status === 'all') {
        url.searchParams.delete('status');
    } else {
        url.searchParams.set('status', status);
    }
    window.location.href = url.toString();
}

async function loadApplications() {
    try {
        const urlParams = new URLSearchParams(window.location.search);
        const status = urlParams.get('status');

        const endpoint = status ? `/applications?status=${status}` : '/applications';
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
        <div class="application-card-candidate ${app.status ? app.status.toLowerCase() : ''}">
            <div class="application-card-candidate-header">
                <div>
                    <h3 class="application-card-candidate-title">${app.job ? app.job.title : 'N/A'}</h3>
                    <p class="application-card-candidate-company">${app.job && app.job.company ? app.job.company.companyName : 'N/A'}</p>
                </div>
                <span class="status-badge ${app.status ? app.status.toLowerCase() : ''}">${app.status || 'N/A'}</span>
            </div>
            <div class="application-card-candidate-footer">
                <span class="application-card-candidate-date">Ứng tuyển: ${formatDate(app.createdAt)}</span>
                <div class="action-buttons">
                    <a href="/jobs/${app.job ? app.job.id : ''}" class="btn btn-secondary">Xem việc làm</a>
                    ${app.resume ? `<button class="btn btn-secondary" onclick="viewResume(${app.resume.id})">Xem CV</button>` : ''}
                </div>
            </div>
        </div>
    `).join('');
}

