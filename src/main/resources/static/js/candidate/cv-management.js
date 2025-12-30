// CV Management (Index) JavaScript

document.addEventListener('DOMContentLoaded', function () {
    // Page is rendered with SSR, no need to load via API
});

function displayResumes(resumes) {
    const grid = document.querySelector('.cv-grid');
    if (!grid) return;

    if (!resumes || resumes.length === 0) {
        grid.innerHTML = '<div class="empty-state"><p>Chưa có CV nào</p></div>';
        return;
    }

    grid.innerHTML = resumes.map(resume => `
        <div class="cv-card" onclick="window.location.href='/candidate/resumes/${resume.id}'">
            <div class="cv-card-header">
                <div>
                    <h3 class="cv-card-title">${resume.title || 'CV chưa có tiêu đề'}</h3>
                    <p class="cv-card-meta">Cập nhật: ${formatDate(resume.updatedAt)}</p>
                </div>
                <div class="cv-card-actions" onclick="event.stopPropagation()">
                    <button class="btn-icon edit" onclick="editResume(${resume.id})" title="Chỉnh sửa">✏️</button>
                    <button class="btn-icon delete" onclick="deleteResume(${resume.id})" title="Xóa">🗑️</button>
                </div>
            </div>
        </div>
    `).join('');
}

function editResume(resumeId) {
    window.location.href = `/candidate/resumes/${resumeId}/edit`;
}

async function deleteResume(resumeId) {
    if (!confirm('Bạn có chắc chắn muốn xóa CV này?')) {
        return;
    }

    try {
        const response = await api.delete(`/resumes/${resumeId}`);

        if (response && response.success) {
            showToast('Xóa CV thành công', 'success');
            setTimeout(() => window.location.reload(), 1000);
        }
    } catch (error) {
        showToast('Lỗi khi xóa CV', 'error');
    }
}

