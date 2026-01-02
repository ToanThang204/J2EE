// AI Analysis JavaScript

async function analyzeMatch(jobId, resumeId, userId = null) {
    if (!jobId || !resumeId) {
        showToast('Vui lòng chọn việc làm và CV để phân tích', 'error');
        return;
    }
    
    try {
        showLoading();
        
        const params = new URLSearchParams({
            jobId: jobId,
            resumeId: resumeId
        });
        
        if (userId) {
            params.append('userId', userId);
        }
        
        const response = await api.post(`/ai/check-match?${params.toString()}`, {});
        
        if (response && response.success && response.data) {
            displayAnalysisResult(response.data);
            showToast('Phân tích thành công!', 'success');
        }
    } catch (error) {
        showToast(error.message || 'Lỗi khi phân tích', 'error');
    } finally {
        hideLoading();
    }
}

function displayAnalysisResult(data) {
    const resultContainer = document.getElementById('aiAnalysisResult');
    if (!resultContainer) return;
    
    const score = data.score || 0;
    const content = data.feedback || data.content || 'Không có phản hồi';
    
    resultContainer.innerHTML = `
        <div class="ai-analysis-card">
            <div class="ai-analysis-header">
                <h3>Kết quả phân tích AI</h3>
                <div class="ai-score">
                    <span class="ai-score-value">${score}%</span>
                    <span class="ai-score-label">Điểm khớp</span>
                </div>
            </div>
            <div class="ai-analysis-content">
                <p>${content}</p>
            </div>
            <div class="ai-analysis-actions">
                <button class="btn btn-secondary" onclick="closeAnalysisResult()">Đóng</button>
                <button class="btn btn-primary" onclick="applyJobAfterAnalysis()">Ứng tuyển ngay</button>
            </div>
        </div>
    `;
    
    resultContainer.style.display = 'block';
}

function closeAnalysisResult() {
    const resultContainer = document.getElementById('aiAnalysisResult');
    if (resultContainer) {
        resultContainer.style.display = 'none';
    }
}

function applyJobAfterAnalysis() {
    const jobId = document.getElementById('currentJobId')?.value;

    // Redirect user to job detail page so they can choose CV and confirm application
    if (jobId) {
        window.location.href = `/jobs/${jobId}`;
    } else {
        showToast('Không tìm thấy việc làm để ứng tuyển', 'error');
    }
}

