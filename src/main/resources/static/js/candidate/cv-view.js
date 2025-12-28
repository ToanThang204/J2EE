// CV View JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initCVView();
});

function initCVView() {
    const editBtn = document.getElementById('editCVBtn');
    const printBtn = document.getElementById('printCVBtn');
    const downloadBtn = document.getElementById('downloadPDFBtn');
    
    if (editBtn) {
        editBtn.addEventListener('click', () => {
            const resumeId = document.getElementById('resumeId')?.value;
            if (resumeId) {
                window.location.href = `/candidate/resumes/${resumeId}/edit`;
            }
        });
    }
    
    if (printBtn) {
        printBtn.addEventListener('click', () => {
            window.print();
        });
    }
    
    if (downloadBtn) {
        downloadBtn.addEventListener('click', downloadPDF);
    }
}

async function downloadPDF() {
    const resumeId = document.getElementById('resumeId')?.value;
    if (!resumeId) {
        showToast('Không tìm thấy CV', 'error');
        return;
    }
    
    try {
        window.location.href = `/api/resumes/${resumeId}/pdf`;
    } catch (error) {
        showToast('Lỗi khi tải PDF', 'error');
    }
}

