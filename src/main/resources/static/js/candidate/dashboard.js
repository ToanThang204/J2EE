// Candidate Dashboard JavaScript

document.addEventListener('DOMContentLoaded', function() {
    loadDashboardData();
    initQuickActions();
});

async function loadDashboardData() {
    try {
        const [applicationsRes, savedJobsRes] = await Promise.all([
            api.get('/applications?limit=5'),
            api.get('/saved-jobs?limit=5')
        ]);
        
        if (applicationsRes && applicationsRes.success && applicationsRes.data) {
            displayRecentApplications(applicationsRes.data);
        }
        
        if (savedJobsRes && savedJobsRes.success && savedJobsRes.data) {
            updateSavedJobsCount(savedJobsRes.data.length);
        }
    } catch (error) {
        console.error('Error loading dashboard data:', error);
    }
}

function displayRecentApplications(applications) {
    const container = document.getElementById('recentApplicationsContainer');
    if (!container) return;
    
    if (!applications || applications.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>Chưa có đơn ứng tuyển nào</p></div>';
        return;
    }
    
    container.innerHTML = applications.map(app => `
        <div class="application-card" onclick="window.location.href='/candidate/applications/${app.id}'">
            <div class="application-card-header">
                <div>
                    <h3 class="application-card-title">${app.job ? app.job.title : 'N/A'}</h3>
                    <p class="application-card-company">${app.job && app.job.company ? app.job.company.companyName : 'N/A'}</p>
                </div>
                <span class="status-badge ${app.status ? app.status.toLowerCase() : ''}">${app.status || 'N/A'}</span>
            </div>
            <div class="application-card-footer">
                <span class="application-date">Ứng tuyển: ${formatDate(app.createdAt)}</span>
                <a href="/jobs/${app.job ? app.job.id : ''}" class="btn btn-secondary">Xem chi tiết</a>
            </div>
        </div>
    `).join('');
}

function updateSavedJobsCount(count) {
    const countElement = document.getElementById('savedJobsCount');
    if (countElement) {
        countElement.textContent = count || 0;
    }
}

function initQuickActions() {
    const quickActionBtns = document.querySelectorAll('.quick-action-btn');
    quickActionBtns.forEach(btn => {
        btn.addEventListener('click', function(e) {
            const href = this.getAttribute('href');
            if (href) {
                window.location.href = href;
            }
        });
    });
}

