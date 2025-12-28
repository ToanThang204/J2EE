// Application Shared JavaScript

async function applyJob(jobId, resumeId = null) {
    if (!jobId) {
        showToast('Không tìm thấy thông tin việc làm', 'error');
        return;
    }
    
    try {
        showLoading();
        
        const data = {};
        if (resumeId) {
            data.resumeId = resumeId;
        }
        
        const response = await api.post(`/applications?jobId=${jobId}`, data);
        
        if (response && response.success) {
            showToast('Ứng tuyển thành công!', 'success');
            setTimeout(() => {
                window.location.href = '/candidate/applications';
            }, 1000);
        }
    } catch (error) {
        showToast(error.message || 'Lỗi khi ứng tuyển', 'error');
    } finally {
        hideLoading();
    }
}

async function cancelApplication(applicationId) {
    if (!confirm('Bạn có chắc chắn muốn hủy đơn ứng tuyển này?')) {
        return;
    }
    
    try {
        const response = await api.delete(`/applications/${applicationId}`);
        
        if (response && response.success) {
            showToast('Hủy đơn ứng tuyển thành công', 'success');
            setTimeout(() => window.location.reload(), 1000);
        }
    } catch (error) {
        showToast('Lỗi khi hủy đơn ứng tuyển', 'error');
    }
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

function viewApplication(applicationId) {
    window.location.href = `/candidate/applications/${applicationId}`;
}

function viewResume(resumeId) {
    if (resumeId) {
        window.location.href = `/candidate/resumes/${resumeId}`;
    } else {
        showToast('CV không tồn tại', 'error');
    }
}

