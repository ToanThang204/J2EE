// CV Builder JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initCVBuilder();
    initDynamicSections();
});

function initCVBuilder() {
    const form = document.getElementById('cvForm');
    if (form) {
        form.addEventListener('submit', handleSaveCV);
    }
    
    const previewBtn = document.getElementById('previewBtn');
    if (previewBtn) {
        previewBtn.addEventListener('click', showPreview);
    }
}

function initDynamicSections() {
    const addExperienceBtn = document.getElementById('addExperienceBtn');
    const addEducationBtn = document.getElementById('addEducationBtn');
    const addSkillBtn = document.getElementById('addSkillBtn');
    
    if (addExperienceBtn) {
        addExperienceBtn.addEventListener('click', () => addSection('experience'));
    }
    
    if (addEducationBtn) {
        addEducationBtn.addEventListener('click', () => addSection('education'));
    }
    
    if (addSkillBtn) {
        addSkillBtn.addEventListener('click', () => addSection('skills'));
    }
}

async function handleSaveCV(e) {
    e.preventDefault();
    
    const form = e.target;
    const formData = new FormData(form);
    const data = Object.fromEntries(formData);
    
    try {
        showLoading();
        
        const response = await api.post('/resumes', data);
        
        if (response && response.success) {
            showToast('Lưu CV thành công', 'success');
            setTimeout(() => {
                window.location.href = '/candidate/cv-index';
            }, 1000);
        }
    } catch (error) {
        showToast('Lỗi khi lưu CV', 'error');
    } finally {
        hideLoading();
    }
}

function addSection(type) {
    const container = document.getElementById(`${type}Container`);
    if (!container) return;
    
    const sectionHtml = getSectionTemplate(type);
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = sectionHtml;
    
    container.appendChild(tempDiv.firstElementChild);
}

function getSectionTemplate(type) {
    const templates = {
        experience: `
            <div class="dynamic-section-item">
                <div class="dynamic-section-item-header">
                    <input type="text" class="form-input" placeholder="Tên công ty" name="experience[company][]">
                    <button type="button" class="btn-remove-section" onclick="removeSection(this)">×</button>
                </div>
                <input type="text" class="form-input" placeholder="Vị trí" name="experience[position][]">
                <textarea class="form-input" placeholder="Mô tả" name="experience[description][]" rows="3"></textarea>
            </div>
        `,
        education: `
            <div class="dynamic-section-item">
                <div class="dynamic-section-item-header">
                    <input type="text" class="form-input" placeholder="Tên trường" name="education[school][]">
                    <button type="button" class="btn-remove-section" onclick="removeSection(this)">×</button>
                </div>
                <input type="text" class="form-input" placeholder="Chuyên ngành" name="education[major][]">
                <input type="text" class="form-input" placeholder="Thời gian" name="education[period][]">
            </div>
        `,
        skills: `
            <div class="dynamic-section-item">
                <div class="dynamic-section-item-header">
                    <input type="text" class="form-input" placeholder="Tên kỹ năng" name="skills[]">
                    <button type="button" class="btn-remove-section" onclick="removeSection(this)">×</button>
                </div>
            </div>
        `
    };
    
    return templates[type] || '';
}

function removeSection(btn) {
    const section = btn.closest('.dynamic-section-item');
    if (section) {
        section.remove();
    }
}

function showPreview() {
    // TODO: Implement preview modal
    showToast('Tính năng xem trước đang được phát triển', 'info');
}

