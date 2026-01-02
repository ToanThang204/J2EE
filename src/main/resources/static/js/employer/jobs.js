// Employer Jobs Management JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Jobs are already rendered by Thymeleaf, no need to load via API
    console.log('Employer jobs page loaded');
    // Attach click handler for view buttons (delegation)
    document.body.addEventListener('click', function(e) {
        const target = e.target.closest && e.target.closest('.view-job');
        if (target) {
            const jobId = target.getAttribute('data-job-id');
            if (jobId) showJobDetail(jobId);
        }
        if (e.target.closest && e.target.closest('.job-detail-close')) {
            closeJobModal();
        }
        if (e.target.closest && e.target.closest('.job-detail-close-btn')) {
            closeJobModal();
        }
    });
});

async function deleteJob(jobId) {
    if (!confirm('Bạn có chắc chắn muốn xóa việc làm này?')) {
        return;
    }
    
    try {
        const response = await api.delete(`/jobs/${jobId}`);
        
        showToast('Xóa việc làm thành công', 'success');
        setTimeout(() => window.location.reload(), 1000);
    } catch (error) {
        console.error('Error deleting job:', error);
        showToast('Lỗi khi xóa việc làm', 'error');
    }
}

async function showJobDetail(jobId) {
    try {
        const res = await api.get(`/jobs/${jobId}`);
        if (!res || !res.ok) {
            // if api wrapper returns object
            const data = await res.json();
            if (!data) throw new Error('Job not found');
        }
    } catch (err) {
        // attempt direct fetch if api wrapper not available
    }

    try {
        const response = await fetch(`/api/jobs/${jobId}`);
        if (!response.ok) throw new Error('Không tải được job');
        const job = await response.json();
        renderJobModal(job);
    } catch (error) {
        console.error('Error fetching job detail:', error);
        showToast('Không thể tải chi tiết công việc', 'error');
    }
}

function renderJobModal(job) {
    const modal = document.getElementById('jobDetailModal');
    if (!modal) return;
    const titleEl = modal.querySelector('.job-detail-title');
    const companyEl = modal.querySelector('.job-detail-company');
    const locationEl = modal.querySelector('.job-detail-location');
    const bodyEl = modal.querySelector('.job-detail-body');
    const editLink = modal.querySelector('.job-detail-edit');

    titleEl.textContent = job.title || 'N/A';
    companyEl.textContent = job.company && job.company.companyName ? job.company.companyName : '';
    locationEl.textContent = job.location ? '📍 ' + job.location : '';
    bodyEl.innerHTML = '';
    if (job.description) bodyEl.innerHTML += `<div class="job-desc"><h4>Mô tả</h4><p>${escapeHtml(job.description)}</p></div>`;
    if (job.requirements) bodyEl.innerHTML += `<div class="job-req"><h4>Yêu cầu</h4><p>${escapeHtml(job.requirements)}</p></div>`;
    if (job.salary) bodyEl.innerHTML += `<div class="job-salary"><strong>Mức lương:</strong> ${escapeHtml(job.salary)}</div>`;
    if (job.createdAt) bodyEl.innerHTML += `<div class="job-posted">Đăng: ${new Date(job.createdAt).toLocaleString()}</div>`;

    if (editLink) {
        editLink.setAttribute('href', `/employer/jobs/${job.id}/edit`);
    }

    modal.style.display = 'block';
    setTimeout(() => modal.classList.add('open'), 10);
}

function closeJobModal() {
    const modal = document.getElementById('jobDetailModal');
    if (!modal) return;
    modal.classList.remove('open');
    setTimeout(() => modal.style.display = 'none', 220);
}

function escapeHtml(unsafe) {
    if (!unsafe) return '';
    return unsafe
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');
}

