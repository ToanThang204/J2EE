// Job Detail Page JavaScript

let currentJobId = null;

document.addEventListener('DOMContentLoaded', function() {
    currentJobId = document.getElementById('jobId')?.value || 
                   new URLSearchParams(window.location.pathname.split('/').pop()).get('id');
    
    initJobDetail();
    loadJobDetails();
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

