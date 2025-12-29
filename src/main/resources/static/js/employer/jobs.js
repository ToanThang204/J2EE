// Employer Jobs Management JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Jobs are already rendered by Thymeleaf, no need to load via API
    console.log('Employer jobs page loaded');
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

