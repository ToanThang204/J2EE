/**
 * AI Match functionality for Home Page
 */

let currentJobId = null;

/**
 * Check AI match for a job
 */
async function checkAiMatch(jobId) {
    currentJobId = jobId;
    
    // Show modal with loading state
    const modal = document.getElementById('aiMatchModal');
    const modalBody = document.getElementById('aiModalBody');
    
    modal.style.display = 'flex';
    modalBody.innerHTML = `
        <div class="ai-loading">
            <div class="spinner"></div>
            <p>AI đang phân tích... Vui lòng đợi</p>
        </div>
    `;
    
    try {
        // Get current user info to fetch their resumes
        const token = localStorage.getItem('token');
        if (!token) {
            showAiMatchError('Vui lòng đăng nhập trước khi sử dụng AI matching');
            return;
        }
        
        // Parse JWT to get userId (try multiple field names)
        const payload = JSON.parse(atob(token.split('.')[1]));
        console.log('JWT Payload:', payload); // Debug
        
        // Try different possible field names for userId
        const userId = payload.id || payload.userId || payload.sub || payload.user_id;
        
        if (!userId) {
            console.error('Cannot find userId in JWT payload:', payload);
            showAiMatchError('Không thể xác định user. Vui lòng đăng nhập lại');
            return;
        }
        
        console.log('Using userId:', userId); // Debug
        
        // Get user's resumes
        const resumesResponse = await fetch(`/api/resumes/user/${userId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (!resumesResponse.ok) {
            showAiMatchError('Không thể lấy danh sách CV. Vui lòng thử lại');
            return;
        }
        
        const resumes = await resumesResponse.json();
        
        if (!resumes || resumes.length === 0) {
            showAiMatchError('Bạn chưa có CV nào. Vui lòng tạo CV trước khi sử dụng AI matching');
            return;
        }
        
        // Show CV selection UI
        showResumeSelection(resumes, jobId, token);
        
        
    } catch (error) {
        console.error('AI Match error:', error);
        showAiMatchError('Lỗi kết nối: ' + error.message);
    }
}

/**
 * Show AI match result
 */
function showAiMatchResult(data) {
    const modalBody = document.getElementById('aiModalBody');
    
    // Parse analysis if it's a string
    let analysis = data.analysis;
    if (typeof analysis === 'string') {
        try {
            analysis = JSON.parse(analysis);
        } catch (e) {
            // Keep as string if parsing fails
        }
    }
    
    // Get score color
    const score = data.match_score || 0;
    let scoreColor = '#dc3545'; // red
    if (score >= 70) scoreColor = '#28a745'; // green
    else if (score >= 50) scoreColor = '#ffc107'; // yellow
    
    modalBody.innerHTML = `
        <div class="ai-result-success">
            <div class="ai-score" style="color: ${scoreColor};">
                ${score}/100
            </div>
            
            <div class="ai-details">
                <div class="ai-detail-row">
                    <strong>🏭 Ngành nghề:</strong>
                    <span>${data.industry_detected || 'N/A'}</span>
                </div>
                <div class="ai-detail-row">
                    <strong>📝 Nguồn:</strong>
                    <span>${data.source === 'gemini' ? '✨ AI Analysis' : '💾 Cached'}</span>
                </div>
                <div class="ai-detail-row">
                    <strong>⏱️ Số lần còn lại:</strong>
                    <span>${data.remaining_usage} lần</span>
                </div>
            </div>
            
            <div class="ai-analysis">
                <h4>📊 Phân tích chi tiết:</h4>
                <div class="ai-analysis-content">
                    ${formatAnalysis(analysis)}
                </div>
            </div>
            
            <div class="ai-actions">
                <button class="btn btn-primary" onclick="window.location.href='/jobs/${currentJobId}'">
                    Xem chi tiết công việc
                </button>
                <button class="btn btn-secondary" onclick="closeAiModal()">
                    Đóng
                </button>
            </div>
        </div>
    `;
}

/**
 * Format analysis content
 */
function formatAnalysis(analysis) {
    if (typeof analysis === 'string') {
        return `<pre>${analysis}</pre>`;
    } else if (typeof analysis === 'object') {
        let html = '';
        for (const [key, value] of Object.entries(analysis)) {
            const label = key.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
            html += `<div class="analysis-item">
                <strong>${label}:</strong>
                <p>${typeof value === 'object' ? JSON.stringify(value, null, 2) : value}</p>
            </div>`;
        }
        return html;
    }
    return '<p>Không có dữ liệu phân tích</p>';
}

/**
 * Show AI match error
 */
function showAiMatchError(message) {
    const modalBody = document.getElementById('aiModalBody');
    
    modalBody.innerHTML = `
        <div class="ai-error">
            <div class="ai-error-icon">❌</div>
            <p>${message}</p>
            <div class="ai-error-actions">
                ${!localStorage.getItem('token') ? 
                    `<button class="btn btn-primary" onclick="window.location.href='/auth/login'">
                        Đăng nhập
                    </button>` : 
                    `<button class="btn btn-primary" onclick="window.location.href='/candidate/resumes/create'">
                        Tạo CV ngay
                    </button>`
                }
                <button class="btn btn-secondary" onclick="closeAiModal()">
                    Đóng
                </button>
            </div>
        </div>
    `;
}

/**
 * Close AI modal
 */
function closeAiModal() {
    const modal = document.getElementById('aiMatchModal');
    modal.style.display = 'none';
    currentJobId = null;
}

// Close modal when clicking outside
document.addEventListener('click', function(event) {
    const modal = document.getElementById('aiMatchModal');
    if (event.target === modal) {
        closeAiModal();
    }
});

/**
 * Show resume selection UI
 */
function showResumeSelection(resumes, jobId, token) {
    const modalBody = document.getElementById('aiModalBody');
    
    let resumeOptionsHtml = resumes.map(resume => `
        <div class="resume-option" onclick="selectResumeAndAnalyze(${resume.id}, ${jobId}, '${token}')">
            <div class="resume-info">
                <h4>${resume.title || 'CV của bạn'}</h4>
                <p class="resume-meta">
                    <span>📧 ${resume.email || 'N/A'}</span>
                    <span>📱 ${resume.phone || 'N/A'}</span>
                </p>
                ${resume.skillsSummary ? `<p class="resume-skills">🎯 ${resume.skillsSummary.substring(0, 100)}...</p>` : ''}
                <p class="resume-date">Cập nhật: ${new Date(resume.updatedAt || resume.createdAt).toLocaleDateString('vi-VN')}</p>
            </div>
            <div class="resume-action">
                <button class="btn-select-resume">
                    Chọn CV này
                </button>
            </div>
        </div>
    `).join('');
    
    modalBody.innerHTML = `
        <div class="resume-selection">
            <h3>📋 Chọn CV để phân tích</h3>
            <p class="selection-hint">Chọn CV bạn muốn AI phân tích độ phù hợp với công việc này</p>
            <div class="resume-list">
                ${resumeOptionsHtml}
            </div>
        </div>
    `;
}

/**
 * Select resume and start analysis
 */
async function selectResumeAndAnalyze(resumeId, jobId, token) {
    const modalBody = document.getElementById('aiModalBody');
    
    // Show loading
    modalBody.innerHTML = `
        <div class="ai-loading">
            <div class="spinner"></div>
            <p>AI đang phân tích CV của bạn... Vui lòng đợi</p>
        </div>
    `;
    
    try {
        // Call AI matching API
        const matchResponse = await fetch('/api/ai/check-match', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                jobId: parseInt(jobId),
                resumeId: parseInt(resumeId)
            })
        });
        
        const matchData = await matchResponse.json();
        
        if (matchResponse.ok && matchData.success) {
            showAiMatchResult(matchData.data);
        } else {
            showAiMatchError(matchData.message || 'Có lỗi xảy ra khi phân tích');
        }
        
    } catch (error) {
        console.error('AI Match error:', error);
        showAiMatchError('Lỗi kết nối: ' + error.message);
    }
}
