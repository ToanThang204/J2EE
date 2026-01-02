// Job Detail Page JavaScript

let currentJobId = null;

document.addEventListener('DOMContentLoaded', function() {
    currentJobId = document.getElementById('jobId')?.value || 
                   new URLSearchParams(window.location.pathname.split('/').pop()).get('id');
    
    initJobDetail();
    loadJobDetails();
    loadResumes();
    // Ensure header auth UI is initialized/updated on this page as well
    try {
        if (typeof updateHeaderAuth === 'function') updateHeaderAuth();
        if (typeof initializeUserMenuToggle === 'function') initializeUserMenuToggle();
    } catch (e) {
        console.warn('Could not initialize header auth from job-detail.js', e);
    }
    // If header functions are not yet available due to script order, poll briefly and call when ready
    (function waitForHeaderInit(maxWaitMs = 2000, intervalMs = 100) {
        const start = Date.now();
        const id = setInterval(() => {
            if (typeof updateHeaderAuth === 'function') {
                try { updateHeaderAuth(); } catch (e) { console.warn(e); }
            }
            if (typeof initializeUserMenuToggle === 'function') {
                try { initializeUserMenuToggle(); } catch (e) { console.warn(e); }
            }
            if (typeof updateHeaderAuth === 'function' || Date.now() - start > maxWaitMs) {
                clearInterval(id);
            }
        }, intervalMs);
    })();
});

function initJobDetail() {
    const applyBtn = document.getElementById('applyJobBtn');
    const favoriteBtn = document.getElementById('favoriteBtn');
    const analyzeBtn = document.getElementById('analyzeBtn');
    
    if (applyBtn) {
        applyBtn.addEventListener('click', handleApplyJob);
    }
    
    if (favoriteBtn) {
        favoriteBtn.addEventListener('click', toggleFavorite);
    }
    
    if (analyzeBtn) {
        analyzeBtn.addEventListener('click', handleAnalyze);
    }
}

async function loadJobDetails() {
    if (!currentJobId) return;
    
    try {
        const response = await api.get(`/jobs/${currentJobId}`);
        if (response && response.success && response.data) {
            updateJobDetailUI(response.data);
        }
    } catch (error) {
        console.error('Error loading job details:', error);
    }
}

function updateJobDetailUI(job) {
    // Update favorite button state
    const favoriteBtn = document.getElementById('favoriteBtn');
    if (favoriteBtn && job.isFavorite) {
        favoriteBtn.classList.add('active');
    }
}

async function loadResumes() {
    const select = document.getElementById('resumeSelect');
    if (!select) return;
    // Only load resumes for logged-in users (candidates/admins).
    const userStr = localStorage.getItem('user');
    if (!userStr) return; // not logged in

    let user;
    try {
        user = JSON.parse(userStr);
    } catch (e) {
        return;
    }

    // Only candidates or admins should fetch resumes for a user
    if (!user || (user.role !== 'CANDIDATE' && user.role !== 'ADMIN')) return;

    try {
        const userId = user.id || user.userId;
        if (!userId) return;

        const resp = await api.get(`/resumes/user/${userId}`);
        // support both envelope {success,data} and direct array
        let resumes = [];
        if (!resp) {
            resumes = [];
        } else if (Array.isArray(resp)) {
            resumes = resp;
        } else if (resp.data && Array.isArray(resp.data)) {
            resumes = resp.data;
        } else if (resp.items && Array.isArray(resp.items)) {
            resumes = resp.items;
        } else {
            // try to handle single-object list
            resumes = Array.isArray(resp) ? resp : [];
        }

        // populate
        resumes.forEach(r => {
            const opt = document.createElement('option');
            opt.value = r.id;
            opt.textContent = r.title || (`CV ${r.id}`);
            select.appendChild(opt);
        });
    } catch (error) {
        console.error('Could not load resumes:', error);
    }
}

// wire side apply button to same handler
document.addEventListener('click', function (e) {
    if (e.target && e.target.id === 'applyJobBtnSide') {
        const resumeSelect = document.getElementById('resumeSelect');
        const resumeId = resumeSelect ? resumeSelect.value : null;
        if (!resumeId) {
            showToast('Vui lòng chọn CV để ứng tuyển', 'error');
            return;
        }
        applyJob(currentJobId, resumeId);
    }
});

async function handleApplyJob() {
    if (!currentJobId) {
        showToast('Không tìm thấy thông tin việc làm', 'error');
        return;
    }
    
    // Get selected resume
    const resumeSelect = document.getElementById('resumeSelect');
    const resumeId = resumeSelect ? resumeSelect.value : null;
    
    if (!resumeId) {
        showToast('Vui lòng chọn CV để ứng tuyển', 'error');
        return;
    }
    
    await applyJob(currentJobId, resumeId);
}

async function toggleFavorite() {
    if (!currentJobId) return;
    
    try {
        const response = await api.post(`/saved-jobs?jobId=${currentJobId}`, {});
        
        if (response && response.success) {
            const favoriteBtn = document.getElementById('favoriteBtn');
            if (favoriteBtn) {
                favoriteBtn.classList.toggle('active');
                const isFavorite = favoriteBtn.classList.contains('active');
                showToast(isFavorite ? 'Đã lưu việc làm' : 'Đã bỏ lưu', 'success');
            }
        }
    } catch (error) {
        showToast('Lỗi khi lưu việc làm', 'error');
    }
}

async function handleAnalyze() {
    if (!currentJobId) {
        showToast('Không tìm thấy thông tin việc làm', 'error');
        return;
    }
    
    const resumeSelect = document.getElementById('resumeSelect');
    const resumeId = resumeSelect ? resumeSelect.value : null;
    
    if (!resumeId) {
        showToast('Vui lòng chọn CV để phân tích', 'error');
        return;
    }
    
    await analyzeMatch(currentJobId, resumeId);
}

