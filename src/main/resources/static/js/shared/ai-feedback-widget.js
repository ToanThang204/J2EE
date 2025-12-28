// AI Feedback Widget JavaScript

async function loadAIFeedback(jobId, resumeId) {
    if (!jobId || !resumeId) return;
    
    try {
        const response = await api.post(`/ai/check-match?jobId=${jobId}&resumeId=${resumeId}`, {});
        
        if (response && response.success && response.data) {
            displayAIFeedback(response.data);
        }
    } catch (error) {
        console.error('Error loading AI feedback:', error);
    }
}

function displayAIFeedback(data) {
    const widget = document.getElementById('aiFeedbackWidget');
    if (!widget) return;
    
    const score = data.score || 0;
    const content = data.feedback || data.content || 'Chưa có phản hồi';
    
    widget.innerHTML = `
        <div class="ai-feedback-content">
            <div class="ai-feedback-score">
                <div class="score-circle" style="--score: ${score}">
                    <span>${score}%</span>
                </div>
                <p>Điểm khớp</p>
            </div>
            <div class="ai-feedback-text">
                <p>${content}</p>
            </div>
            <button class="btn btn-secondary" onclick="refreshAIFeedback()">Làm mới</button>
        </div>
    `;
}

function refreshAIFeedback() {
    const jobId = document.getElementById('currentJobId')?.value;
    const resumeId = document.getElementById('currentResumeId')?.value;
    
    if (jobId && resumeId) {
        loadAIFeedback(jobId, resumeId);
    }
}

