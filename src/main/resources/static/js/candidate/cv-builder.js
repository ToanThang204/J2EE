// Global variables
let currentTemplate = 'simple';
let currentColor = '#28a745';
let currentFont = 'Arial';
let currentFontSize = 11;
let editMode = false;
let cvData = {
    title: '',
    objective: '',
    header: {
        fullName: '',
        title: '',
        birthday: '',
        gender: '',
        phone: '',
        email: '',
        website: '',
        address: ''
    },
    educations: [],
    experiences: [],
    skills: [],
    certifications: [],
    awards: [],
    activities: [],
    projects: [],
    references: [],
    hobbies: [],
    additional: []
};
let currentResumeId = null;

// Initialize on page load
document.addEventListener('DOMContentLoaded', function () {
    initializeFormElements();
    updateCVPreview();
    // If an id is provided in the query string, load the resume for editing
    const params = new URLSearchParams(window.location.search);
    const resumeId = params.get('id');
    if (resumeId) {
        loadResume(resumeId);
    }
});

// Initialize form elements
function initializeFormElements() {
    // Add initial education form
    addEducation();
    // Add initial experience form
    addExperience();
    // Add initial skill form
    addSkill();
}

// Select CV Template
function selectTemplate(templateName) {
    currentTemplate = templateName;

    // Update active state
    document.querySelectorAll('.template-item').forEach(item => {
        item.classList.remove('active');
    });
    document.querySelector(`[data-template="${templateName}"]`).classList.add('active');

    updateCVPreview();
}

// Show form tab
function showFormTab(tabName) {
    // Hide all tabs
    document.querySelectorAll('.form-tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    // Show selected tab
    document.getElementById('form-' + tabName + '-tab').classList.add('active');
    document.getElementById('tab-' + tabName).classList.add('active');
}

// Toggle edit mode
function toggleEditMode() {
    editMode = !editMode;
    const btn = document.getElementById('edit-mode-btn');

    if (editMode) {
        btn.classList.add('active');
        btn.innerHTML = '<i class="fas fa-eye"></i> Xem trước';
    } else {
        btn.classList.remove('active');
        btn.innerHTML = '<i class="fas fa-edit"></i> Chế độ chỉnh sửa';
    }
}

// Toggle export dropdown
function toggleExportDropdown() {
    const menu = document.getElementById('export-menu');
    menu.style.display = menu.style.display === 'none' ? 'block' : 'none';
}

// Export CV to PDF
function exportCV(format) {
    if (format === 'pdf') {
        window.print();
    }
}

// Update CV Preview
function updateCVPreview() {
    // Collect form data
    collectFormData();

    // Render CV based on template
    const cvHTML = renderCV();
    document.getElementById('cv-template').innerHTML = cvHTML;
}

// Collect form data from inputs
function collectFormData() {
    cvData.title = document.getElementById('cv-title').value || '';
    cvData.objective = document.getElementById('cv-objective').value || '';

    cvData.header.fullName = document.getElementById('header-full-name').value || '';
    cvData.header.title = document.getElementById('header-title').value || '';
    cvData.header.birthday = document.getElementById('header-birthday').value || '';
    cvData.header.gender = document.getElementById('header-gender').value || '';
    cvData.header.phone = document.getElementById('header-phone').value || '';
    cvData.header.email = document.getElementById('header-email').value || '';
    cvData.header.website = document.getElementById('header-website').value || '';
    cvData.header.address = document.getElementById('header-address').value || '';

    // Collect educations
    cvData.educations = [];
    document.querySelectorAll('[data-type="education"]').forEach(item => {
        cvData.educations.push({
            school: item.querySelector('[name="education-school"]').value,
            degree: item.querySelector('[name="education-degree"]').value,
            major: item.querySelector('[name="education-major"]').value,
            startDate: item.querySelector('[name="education-start"]').value,
            endDate: item.querySelector('[name="education-end"]').value
        });
    });

    // Collect experiences
    cvData.experiences = [];
    document.querySelectorAll('[data-type="experience"]').forEach(item => {
        cvData.experiences.push({
            company: item.querySelector('[name="experience-company"]').value,
            position: item.querySelector('[name="experience-position"]').value,
            startDate: item.querySelector('[name="experience-start"]').value,
            endDate: item.querySelector('[name="experience-end"]').value,
            description: item.querySelector('[name="experience-description"]').value
        });
    });

    // Collect skills
    cvData.skills = [];
    document.querySelectorAll('[data-type="skill"]').forEach(item => {
        const skillText = item.querySelector('[name="skill-text"]').value;
        if (skillText) cvData.skills.push({ text: skillText });
    });

    // Collect certifications
    cvData.certifications = [];
    document.querySelectorAll('[data-type="certification"]').forEach(item => {
        cvData.certifications.push({
            name: item.querySelector('[name="certification-name"]').value,
            issuer: item.querySelector('[name="certification-issuer"]').value,
            date: item.querySelector('[name="certification-date"]').value
        });
    });

    // Collect awards
    cvData.awards = [];
    document.querySelectorAll('[data-type="award"]').forEach(item => {
        cvData.awards.push({
            name: item.querySelector('[name="award-name"]').value,
            date: item.querySelector('[name="award-date"]').value
        });
    });

    // Collect activities
    cvData.activities = [];
    document.querySelectorAll('[data-type="activity"]').forEach(item => {
        cvData.activities.push({
            organization: item.querySelector('[name="activity-org"]').value,
            role: item.querySelector('[name="activity-role"]').value,
            startDate: item.querySelector('[name="activity-start"]').value,
            endDate: item.querySelector('[name="activity-end"]').value,
            description: item.querySelector('[name="activity-desc"]').value
        });
    });

    // Collect projects
    cvData.projects = [];
    document.querySelectorAll('[data-type="project"]').forEach(item => {
        cvData.projects.push({
            name: item.querySelector('[name="project-name"]').value,
            role: item.querySelector('[name="project-role"]').value,
            startDate: item.querySelector('[name="project-start"]').value,
            endDate: item.querySelector('[name="project-end"]').value,
            description: item.querySelector('[name="project-desc"]').value
        });
    });

    // Collect references
    cvData.references = [];
    document.querySelectorAll('[data-type="reference"]').forEach(item => {
        cvData.references.push({
            name: item.querySelector('[name="ref-name"]').value,
            company: item.querySelector('[name="ref-company"]').value,
            phone: item.querySelector('[name="ref-phone"]').value,
            email: item.querySelector('[name="ref-email"]').value
        });
    });

    // Collect hobbies
    cvData.hobbies = [];
    document.querySelectorAll('[data-type="hobby"]').forEach(item => {
        const text = item.querySelector('[name="hobby-text"]').value;
        if (text) cvData.hobbies.push({ text });
    });

    // Collect additional info
    cvData.additional = [];
    document.querySelectorAll('[data-type="additional"]').forEach(item => {
        const text = item.querySelector('[name="additional-text"]').value;
        if (text) cvData.additional.push({ text });
    });
}

// Render CV HTML
function renderCV() {
    if (currentTemplate === 'simple') {
        return renderSimpleCV();
    } else if (currentTemplate === 'professional') {
        return renderProfessionalCV();
    } else if (currentTemplate === 'sidebar') {
        return renderSidebarCV();
    }
    return renderSimpleCV();
}

// Render Simple CV
function renderSimpleCV() {
    let html = `<div class="cv-content simple">`;

    // Header
    if (cvData.header.fullName || cvData.header.title) {
        html += `<div class="cv-header">
            <div class="cv-avatar"><i class="fas fa-user"></i></div>
            <div class="cv-header-content">
                <h1 class="cv-name">${cvData.header.fullName || 'Họ và tên'}</h1>
                <p class="cv-title">${cvData.header.title || 'Chức danh'}</p>
                <div class="cv-contact">
                    ${cvData.header.phone ? `<div class="cv-contact-item"><i class="fas fa-phone"></i> ${cvData.header.phone}</div>` : ''}
                    ${cvData.header.email ? `<div class="cv-contact-item"><i class="fas fa-envelope"></i> ${cvData.header.email}</div>` : ''}
                    ${cvData.header.address ? `<div class="cv-contact-item"><i class="fas fa-map-marker-alt"></i> ${cvData.header.address}</div>` : ''}
                </div>
            </div>
        </div>`;
    }

    // Objective
    if (cvData.objective) {
        html += `<div class="cv-section">
            <h2 class="cv-section-title">Mục tiêu</h2>
            <p class="cv-item-description">${cvData.objective}</p>
        </div>`;
    }

    // Experience
    if (cvData.experiences.length > 0 && cvData.experiences.some(e => e.company)) {
        html += `<div class="cv-section">
            <h2 class="cv-section-title">Kinh nghiệm làm việc</h2>`;
        cvData.experiences.forEach(exp => {
            if (exp.company) {
                html += `<div class="cv-item">
                    <div class="cv-item-header">
                        <div>
                            <div class="cv-item-title">${exp.position || 'Vị trí'}</div>
                            <div class="cv-item-subtitle">${exp.company}</div>
                        </div>
                        <div class="cv-item-date">${formatDateRange(exp.startDate, exp.endDate)}</div>
                    </div>
                    ${exp.description ? `<div class="cv-item-description">${exp.description}</div>` : ''}
                </div>`;
            }
        });
        html += `</div>`;
    }

    // Education
    if (cvData.educations.length > 0 && cvData.educations.some(e => e.school)) {
        html += `<div class="cv-section">
            <h2 class="cv-section-title">Học vấn</h2>`;
        cvData.educations.forEach(edu => {
            if (edu.school) {
                html += `<div class="cv-item">
                    <div class="cv-item-header">
                        <div>
                            <div class="cv-item-title">${edu.degree || 'Bằng cấp'}</div>
                            <div class="cv-item-subtitle">${edu.school}</div>
                        </div>
                        <div class="cv-item-date">${formatDateRange(edu.startDate, edu.endDate)}</div>
                    </div>
                    ${edu.major ? `<div class="cv-item-description">Ngành: ${edu.major}</div>` : ''}
                </div>`;
            }
        });
        html += `</div>`;
    }

    // Skills
    if (cvData.skills.length > 0 && cvData.skills.some(s => s.text)) {
        html += `<div class="cv-section">
            <h2 class="cv-section-title">Kỹ năng</h2>
            <div class="cv-skills-list">`;
        cvData.skills.forEach(skill => {
            if (skill.text) {
                html += `<span class="cv-skill-tag">${skill.text}</span>`;
            }
        });
        html += `</div></div>`;
    }

    // Certifications
    if (cvData.certifications.length > 0 && cvData.certifications.some(c => c.name)) {
        html += `<div class="cv-section">
            <h2 class="cv-section-title">Chứng chỉ - Giải thưởng</h2>`;
        cvData.certifications.forEach(cert => {
            if (cert.name) {
                html += `<div class="cv-item">
                    <div class="cv-item-header">
                        <div class="cv-item-title">${cert.name}</div>
                        <div class="cv-item-date">${formatDate(cert.date)}</div>
                    </div>
                    ${cert.issuer ? `<div class="cv-item-subtitle">${cert.issuer}</div>` : ''}
                </div>`;
            }
        });
        html += `</div>`;
    }

    // Awards
    if (cvData.awards && cvData.awards.some(a => a.name)) {
        html += `<div class="cv-section"><h2 class="cv-section-title">Giải thưởng</h2>`;
        cvData.awards.forEach(item => {
            if (item.name) html += `<div class="cv-item"><div class="cv-item-header"><div class="cv-item-title">${item.name}</div><div class="cv-item-date">${formatDate(item.date)}</div></div></div>`;
        });
        html += `</div>`;
    }

    // Activities
    if (cvData.activities && cvData.activities.some(a => a.organization)) {
        html += `<div class="cv-section"><h2 class="cv-section-title">Hoạt động</h2>`;
        cvData.activities.forEach(item => {
            if (item.organization) {
                html += `<div class="cv-item">
                    <div class="cv-item-header">
                        <div>
                            <div class="cv-item-title">${item.role || 'Thành viên'}</div>
                            <div class="cv-item-subtitle">${item.organization}</div>
                        </div>
                        <div class="cv-item-date">${formatDateRange(item.startDate, item.endDate)}</div>
                    </div>
                    ${item.description ? `<div class="cv-item-description">${item.description}</div>` : ''}
                </div>`;
            }
        });
        html += `</div>`;
    }

    // Projects
    if (cvData.projects && cvData.projects.some(a => a.name)) {
        html += `<div class="cv-section"><h2 class="cv-section-title">Dự án</h2>`;
        cvData.projects.forEach(item => {
            if (item.name) {
                html += `<div class="cv-item">
                    <div class="cv-item-header">
                        <div>
                            <div class="cv-item-title">${item.name}</div>
                            <div class="cv-item-subtitle">${item.role}</div>
                        </div>
                        <div class="cv-item-date">${formatDateRange(item.startDate, item.endDate)}</div>
                    </div>
                    ${item.description ? `<div class="cv-item-description">${item.description}</div>` : ''}
                </div>`;
            }
        });
        html += `</div>`;
    }

    // References
    if (cvData.references && cvData.references.some(a => a.name)) {
        html += `<div class="cv-section"><h2 class="cv-section-title">Người tham khảo</h2>`;
        cvData.references.forEach(item => {
            if (item.name) {
                html += `<div class="cv-item">
                    <div class="cv-item-title">${item.name}</div>
                    <div class="cv-item-subtitle">${item.company}</div>
                    <div class="cv-item-description"><i class="fas fa-phone"></i> ${item.phone} | <i class="fas fa-envelope"></i> ${item.email}</div>
                </div>`;
            }
        });
        html += `</div>`;
    }

    // Hobbies
    if (cvData.hobbies && cvData.hobbies.some(a => a.text)) {
        html += `<div class="cv-section"><h2 class="cv-section-title">Sở thích</h2><div class="cv-skills-list">`;
        cvData.hobbies.forEach(item => {
            if (item.text) html += `<span class="cv-skill-tag">${item.text}</span>`;
        });
        html += `</div></div>`;
    }

    // Additional Info
    if (cvData.additional && cvData.additional.some(a => a.text)) {
        html += `<div class="cv-section"><h2 class="cv-section-title">Thông tin thêm</h2>`;
        cvData.additional.forEach(item => {
            if (item.text) html += `<div class="cv-item-description">${item.text}</div>`;
        });
        html += `</div>`;
    }

    html += `</div>`;
    return html;
}

// Render Professional CV
function renderProfessionalCV() {
    let html = `<div class="cv-content professional">
        <div class="cv-sidebar">
            <div style="text-align: center; margin-bottom: 30px;">
                <h1 class="cv-name">${cvData.header.fullName || 'Họ và tên'}</h1>
                <p class="cv-title">${cvData.header.title || 'Chức danh'}</p>
            </div>
            
            ${cvData.header.phone || cvData.header.email ? `<div class="cv-section">
                <h2 class="cv-section-title">Liên hệ</h2>
                ${cvData.header.phone ? `<div style="font-size: 12px; margin-bottom: 8px;"><i class="fas fa-phone" style="margin-right: 6px;"></i>${cvData.header.phone}</div>` : ''}
                ${cvData.header.email ? `<div style="font-size: 12px; margin-bottom: 8px;"><i class="fas fa-envelope" style="margin-right: 6px;"></i>${cvData.header.email}</div>` : ''}
                ${cvData.header.address ? `<div style="font-size: 12px;"><i class="fas fa-map-marker-alt" style="margin-right: 6px;"></i>${cvData.header.address}</div>` : ''}
            </div>` : ''}
            
            ${cvData.skills.length > 0 && cvData.skills.some(s => s.text) ? `<div class="cv-section">
                <h2 class="cv-section-title">Kỹ năng</h2>
                ${cvData.skills.map(skill => skill.text ? `<div style="font-size: 11px; margin-bottom: 6px; padding: 6px; background: rgba(255,255,255,0.1); border-radius: 3px;">${skill.text}</div>` : '').join('')}
            </div>` : ''}
        </div>
        
        <div class="cv-main">`;

    // Objective
    if (cvData.objective) {
        html += `<div class="cv-section">
            <h2 class="cv-section-title">Mục tiêu</h2>
            <p class="cv-item-description">${cvData.objective}</p>
        </div>`;
    }

    // Experience
    if (cvData.experiences.length > 0 && cvData.experiences.some(e => e.company)) {
        html += `<div class="cv-section">
            <h2 class="cv-section-title">Kinh nghiệm</h2>`;
        cvData.experiences.forEach(exp => {
            if (exp.company) {
                html += `<div class="cv-item">
                    <div class="cv-item-header">
                        <div class="cv-item-title">${exp.position || 'Vị trí'}</div>
                        <div class="cv-item-date">${formatDateRange(exp.startDate, exp.endDate)}</div>
                    </div>
                    <div class="cv-item-subtitle">${exp.company}</div>
                    ${exp.description ? `<div class="cv-item-description">${exp.description}</div>` : ''}
                </div>`;
            }
        });
        html += `</div>`;
    }

    // Education
    if (cvData.educations.length > 0 && cvData.educations.some(e => e.school)) {
        html += `<div class="cv-section">
            <h2 class="cv-section-title">Học vấn</h2>`;
        cvData.educations.forEach(edu => {
            if (edu.school) {
                html += `<div class="cv-item">
                    <div class="cv-item-header">
                        <div class="cv-item-title">${edu.degree || 'Bằng cấp'}</div>
                        <div class="cv-item-date">${formatDateRange(edu.startDate, edu.endDate)}</div>
                    </div>
                    <div class="cv-item-subtitle">${edu.school}</div>
                </div>`;
            }
        });
        html += `</div>`;
    }

    // Certifications
    if (cvData.certifications.length > 0 && cvData.certifications.some(c => c.name)) {
        html += `<div class="cv-section">
            <h2 class="cv-section-title">Chứng chỉ</h2>`;
        cvData.certifications.forEach(cert => {
            if (cert.name) {
                html += `<div class="cv-item">
                    <div class="cv-item-title">${cert.name}</div>
                    ${cert.issuer ? `<div class="cv-item-subtitle">${cert.issuer}</div>` : ''}
                </div>`;
            }
        });
        html += `</div>`;
    }

    html += `</div></div>`;
    return html;
}

// Render Sidebar CV (same as professional for now)
function renderSidebarCV() {
    return renderProfessionalCV();
}

// Add education form
function addEducation() {
    const container = document.getElementById('educations-container');
    const index = container.children.length + 1;
    const html = `<div class="form-item-container" data-type="education">
        <h3 class="item-title">Học vấn ${index}</h3>
        <button type="button" class="btn-remove" onclick="this.parentElement.remove(); updateCVPreview()"><i class="fas fa-times"></i></button>
        <div class="form-group">
            <label>Trường học</label>
            <input type="text" name="education-school" placeholder="VD: Đại học Bách Khoa" class="form-control" onchange="updateCVPreview()">
        </div>
        <div class="form-row">
            <div class="form-group">
                <label>Bằng cấp</label>
                <input type="text" name="education-degree" placeholder="VD: Cử nhân" class="form-control" onchange="updateCVPreview()">
            </div>
            <div class="form-group">
                <label>Chuyên ngành</label>
                <input type="text" name="education-major" placeholder="VD: Công nghệ Thông tin" class="form-control" onchange="updateCVPreview()">
            </div>
        </div>
        <div class="form-group">
            <label>Năm bắt đầu</label>
            <input type="date" name="education-start" class="form-control" onchange="updateCVPreview()">
        </div>
        <div class="form-group">
            <label>Năm kết thúc</label>
            <input type="date" name="education-end" class="form-control" onchange="updateCVPreview()">
        </div>
    </div>`;
    container.insertAdjacentHTML('beforeend', html);
}

// Add experience form
function addExperience() {
    const container = document.getElementById('experiences-container');
    const index = container.children.length + 1;
    const html = `<div class="form-item-container" data-type="experience">
        <h3 class="item-title">Kinh nghiệm ${index}</h3>
        <button type="button" class="btn-remove" onclick="this.parentElement.remove(); updateCVPreview()"><i class="fas fa-times"></i></button>
        <div class="form-group">
            <label>Tên công ty</label>
            <input type="text" name="experience-company" placeholder="VD: Công ty XYZ" class="form-control" onchange="updateCVPreview()">
        </div>
        <div class="form-group">
            <label>Vị trí</label>
            <input type="text" name="experience-position" placeholder="VD: Frontend Developer" class="form-control" onchange="updateCVPreview()">
        </div>
        <div class="form-group">
            <label>Ngày bắt đầu</label>
            <input type="date" name="experience-start" class="form-control" onchange="updateCVPreview()">
        </div>
        <div class="form-group">
            <label>Ngày kết thúc</label>
            <input type="date" name="experience-end" class="form-control" onchange="updateCVPreview()">
        </div>
        <div class="form-group">
            <label>Mô tả</label>
            <textarea name="experience-description" rows="3" placeholder="Mô tả kinh nghiệm..." class="form-control" onchange="updateCVPreview()"></textarea>
        </div>
    </div>`;
    container.insertAdjacentHTML('beforeend', html);
}

// Add skill form
function addSkill() {
    const container = document.getElementById('skills-container');
    const index = container.children.length + 1;
    const html = `<div class="form-item-container" data-type="skill">
        <h3 class="item-title">Kỹ năng ${index}</h3>
        <button type="button" class="btn-remove" onclick="this.parentElement.remove(); updateCVPreview()"><i class="fas fa-times"></i></button>
        <div class="form-group">
            <label>Tên kỹ năng</label>
            <input type="text" name="skill-text" placeholder="VD: JavaScript, React" class="form-control" onchange="updateCVPreview()">
        </div>
    </div>`;
    container.insertAdjacentHTML('beforeend', html);
}

// Add certification form
function addCertification() {
    const container = document.getElementById('certifications-container');
    const index = container.children.length + 1;
    const html = `<div class="form-item-container" data-type="certification">
        <h3 class="item-title">Chứng chỉ ${index}</h3>
        <button type="button" class="btn-remove" onclick="this.parentElement.remove(); updateCVPreview()"><i class="fas fa-times"></i></button>
        <div class="form-group">
            <label>Tên chứng chỉ</label>
            <input type="text" name="certification-name" placeholder="VD: TOEIC 800" class="form-control" onchange="updateCVPreview()">
        </div>
        <div class="form-group">
            <label>Cơ quan cấp</label>
            <input type="text" name="certification-issuer" placeholder="VD: IIG" class="form-control" onchange="updateCVPreview()">
        </div>
        <div class="form-group">
            <label>Ngày cấp</label>
            <input type="date" name="certification-date" class="form-control" onchange="updateCVPreview()">
        </div>
    </div>`;
    container.insertAdjacentHTML('beforeend', html);
}

// Add Award
function addAward() {
    const container = document.getElementById('awards-container');
    const index = container.children.length + 1;
    const html = `<div class="form-item-container" data-type="award">
        <h3 class="item-title">Giải thưởng ${index}</h3>
        <button type="button" class="btn-remove" onclick="this.parentElement.remove(); updateCVPreview()"><i class="fas fa-times"></i></button>
        <div class="form-group">
            <label>Tên giải thưởng</label>
            <input type="text" name="award-name" placeholder="VD: Giải nhất Hackathon" class="form-control" onchange="updateCVPreview()">
        </div>
        <div class="form-group">
            <label>Ngày nhận</label>
            <input type="date" name="award-date" class="form-control" onchange="updateCVPreview()">
        </div>
    </div>`;
    container.insertAdjacentHTML('beforeend', html);
}

// Add Activity
function addActivity() {
    const container = document.getElementById('activities-container');
    const index = container.children.length + 1;
    const html = `<div class="form-item-container" data-type="activity">
        <h3 class="item-title">Hoạt động ${index}</h3>
        <button type="button" class="btn-remove" onclick="this.parentElement.remove(); updateCVPreview()"><i class="fas fa-times"></i></button>
        <div class="form-group">
            <label>Tên tổ chức</label>
            <input type="text" name="activity-org" placeholder="VD: CLB Tin học" class="form-control" onchange="updateCVPreview()">
        </div>
        <div class="form-group">
            <label>Vai trò</label>
            <input type="text" name="activity-role" placeholder="VD: Trưởng ban" class="form-control" onchange="updateCVPreview()">
        </div>
        <div class="form-group">
            <label>Ngày bắt đầu</label>
            <input type="date" name="activity-start" class="form-control" onchange="updateCVPreview()">
        </div>
        <div class="form-group">
            <label>Ngày kết thúc</label>
            <input type="date" name="activity-end" class="form-control" onchange="updateCVPreview()">
        </div>
        <div class="form-group">
            <label>Mô tả</label>
            <textarea name="activity-desc" rows="2" placeholder="Mô tả hoạt động..." class="form-control" onchange="updateCVPreview()"></textarea>
        </div>
    </div>`;
    container.insertAdjacentHTML('beforeend', html);
}

// Add Project
function addProject() {
    const container = document.getElementById('projects-container');
    const index = container.children.length + 1;
    const html = `<div class="form-item-container" data-type="project">
        <h3 class="item-title">Dự án ${index}</h3>
        <button type="button" class="btn-remove" onclick="this.parentElement.remove(); updateCVPreview()"><i class="fas fa-times"></i></button>
        <div class="form-group">
            <label>Tên dự án</label>
            <input type="text" name="project-name" placeholder="VD: Website Bán hàng" class="form-control" onchange="updateCVPreview()">
        </div>
        <div class="form-group">
            <label>Vai trò</label>
            <input type="text" name="project-role" placeholder="VD: Backend Developer" class="form-control" onchange="updateCVPreview()">
        </div>
        <div class="form-group">
            <label>Ngày bắt đầu</label>
            <input type="date" name="project-start" class="form-control" onchange="updateCVPreview()">
        </div>
        <div class="form-group">
            <label>Ngày kết thúc</label>
            <input type="date" name="project-end" class="form-control" onchange="updateCVPreview()">
        </div>
        <div class="form-group">
            <label>Mô tả dự án</label>
            <textarea name="project-desc" rows="3" placeholder="Công nghệ sử dụng, chức năng chính..." class="form-control" onchange="updateCVPreview()"></textarea>
        </div>
    </div>`;
    container.insertAdjacentHTML('beforeend', html);
}

// Add Reference
function addReference() {
    const container = document.getElementById('references-container');
    const index = container.children.length + 1;
    const html = `<div class="form-item-container" data-type="reference">
        <h3 class="item-title">Người tham khảo ${index}</h3>
        <button type="button" class="btn-remove" onclick="this.parentElement.remove(); updateCVPreview()"><i class="fas fa-times"></i></button>
        <div class="form-group">
            <label>Họ và tên</label>
            <input type="text" name="ref-name" placeholder="VD: Nguyễn Văn A" class="form-control" onchange="updateCVPreview()">
        </div>
        <div class="form-group">
            <label>Công ty/Tổ chức</label>
            <input type="text" name="ref-company" placeholder="VD: Công ty XYZ" class="form-control" onchange="updateCVPreview()">
        </div>
        <div class="form-row">
            <div class="form-group">
                <label>Số điện thoại</label>
                <input type="text" name="ref-phone" class="form-control" onchange="updateCVPreview()">
            </div>
            <div class="form-group">
                <label>Email</label>
                <input type="email" name="ref-email" class="form-control" onchange="updateCVPreview()">
            </div>
        </div>
    </div>`;
    container.insertAdjacentHTML('beforeend', html);
}

// Add Hobby
function addHobby() {
    const container = document.getElementById('hobbies-container');
    const index = container.children.length + 1;
    const html = `<div class="form-item-container" data-type="hobby">
        <h3 class="item-title">Sở thích ${index}</h3>
        <button type="button" class="btn-remove" onclick="this.parentElement.remove(); updateCVPreview()"><i class="fas fa-times"></i></button>
        <div class="form-group">
            <label>Tên sở thích</label>
            <input type="text" name="hobby-text" placeholder="VD: Đọc sách, Đá bóng" class="form-control" onchange="updateCVPreview()">
        </div>
    </div>`;
    container.insertAdjacentHTML('beforeend', html);
}

// Add Additional Info
function addAdditionalInfo() {
    const container = document.getElementById('additional-container');
    const index = container.children.length + 1;
    const html = `<div class="form-item-container" data-type="additional">
        <h3 class="item-title">Thông tin ${index}</h3>
        <button type="button" class="btn-remove" onclick="this.parentElement.remove(); updateCVPreview()"><i class="fas fa-times"></i></button>
        <div class="form-group">
            <label>Nội dung</label>
            <textarea name="additional-text" rows="2" placeholder="Thông tin khác..." class="form-control" onchange="updateCVPreview()"></textarea>
        </div>
    </div>`;
    container.insertAdjacentHTML('beforeend', html);
}

// Handle form submission
function handleSubmitCV(event) {
    event.preventDefault();
    saveCV();
}

// Save CV
// Save CV
function saveCV() {
    try {
        collectFormData();

        const cvTitle = document.getElementById('cv-title').value || 'CV của tôi';

        const htmlContent = document.getElementById('cv-template').innerHTML;
        
        // Build payload with all fields to match Resume entity
        const resumePayload = {
            title: cvTitle,
            fullName: cvData.header.fullName || '',
            email: cvData.header.email || '',
            phone: cvData.header.phone || '',
            address: cvData.header.address || '',
            summary: cvData.objective || '',
            personalInfo: htmlContent,
            skillsSummary: cvData.skills ? cvData.skills.map(s => s.text).join(', ') : ''
        };

        console.log('Saving CV:', resumePayload);

        const saveBtn = document.querySelector('button[onclick="saveCV()"]');
        if (saveBtn) {
            saveBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang lưu...';
            saveBtn.disabled = true;
        }

        if (typeof api !== 'undefined') {
            const request = currentResumeId ? api.put(`/resumes/${currentResumeId}`, resumePayload) : api.post('/resumes', resumePayload);
            request
                .then(response => {
                    showToast('Lưu CV thành công!', 'success');
                    setTimeout(() => window.location.href = '/candidate/cv-index', 1000);
                })
                .catch(err => {
                    console.error('Save error:', err);
                    showToast(err.message || 'Lỗi khi lưu CV', 'error');
                })
                .finally(() => {
                    if (saveBtn) {
                        saveBtn.innerHTML = '<i class="fas fa-save"></i> Lưu CV';
                        saveBtn.disabled = false;
                    }
                });
        }

    } catch (error) {
        console.error('System error:', error);
        showToast('Có lỗi xảy ra: ' + error.message, 'error');
    }
}

// Load an existing resume into the builder for editing
function loadResume(id) {
    if (typeof api === 'undefined') return;
    api.get(`/resumes/${id}`)
        .then(response => {
            console.log('Raw API response:', response);
            
            // Check if response is wrapped in a data property
            const resume = response.data || response;
            
            if (!resume) return;
            
            console.log('Loaded resume data:', resume);
            
            currentResumeId = resume.id;
            
            // Populate simple fields
            if (document.getElementById('cv-title')) {
                document.getElementById('cv-title').value = resume.title || '';
                console.log('Set title:', resume.title);
            }
            
            // Populate header fields from resume data
            if (document.getElementById('header-full-name')) {
                document.getElementById('header-full-name').value = resume.fullName || '';
                console.log('Set fullName:', resume.fullName);
            }
            if (document.getElementById('header-email')) {
                document.getElementById('header-email').value = resume.email || '';
                console.log('Set email:', resume.email);
            }
            if (document.getElementById('header-phone')) {
                document.getElementById('header-phone').value = resume.phone || '';
                console.log('Set phone:', resume.phone);
            }
            if (document.getElementById('header-address')) {
                document.getElementById('header-address').value = resume.address || '';
                console.log('Set address:', resume.address);
            }
            if (document.getElementById('cv-objective')) {
                document.getElementById('cv-objective').value = resume.summary || '';
                console.log('Set summary:', resume.summary);
            }
            
            // If resume.personalInfo contains the rendered HTML, inject into preview
            if (resume.personalInfo) {
                document.getElementById('cv-template').innerHTML = resume.personalInfo;
            }
            
            // Also try to populate header from nested header object if it exists
            if (resume.header) {
                console.log('Found nested header:', resume.header);
                const h = resume.header;
                if (document.getElementById('header-full-name') && h.fullName) {
                    document.getElementById('header-full-name').value = h.fullName;
                }
                if (document.getElementById('header-email') && h.email) {
                    document.getElementById('header-email').value = h.email;
                }
                if (document.getElementById('header-phone') && h.phone) {
                    document.getElementById('header-phone').value = h.phone;
                }
                if (document.getElementById('header-title') && h.title) {
                    document.getElementById('header-title').value = h.title;
                }
                if (document.getElementById('header-address') && h.address) {
                    document.getElementById('header-address').value = h.address;
                }
            }
            
            updateCVPreview();
            showToast('Đã tải CV để chỉnh sửa', 'success');
        })
        .catch(err => {
            console.error('Load resume error:', err);
            showToast('Không thể tải CV để chỉnh sửa', 'error');
        });
}

// Load Candidate Profile
function loadCandidateProfile() {
    if (typeof api !== 'undefined') {
        // ProfileController is at /api/candidate/profile
        api.get('/candidate/profile')
            .then(res => {
                if (res && res.data) {
                    const user = res.data;
                    document.getElementById('header-full-name').value = user.name || '';
                    document.getElementById('header-email').value = user.email || '';
                    document.getElementById('header-phone').value = user.phone || '';
                    document.getElementById('header-address').value = user.address || '';

                    if (user.profile) {
                        document.getElementById('header-title').value = user.profile.title || '';
                        document.getElementById('cv-objective').value = user.profile.bio || '';
                        document.getElementById('header-website').value = user.profile.website || '';
                    }
                    updateCVPreview();
                    showToast('Đã tải thông tin cá nhân', 'info');
                }
            })
            .catch(err => console.log('Profile load error:', err));
    }
}
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', loadCandidateProfile);
} else {
    loadCandidateProfile();
}

// Helper functions (kept from original)
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', { year: 'numeric', month: 'long', day: 'numeric' });
}

function formatDateRange(startDate, endDate) {
    const start = startDate ? formatDate(startDate) : '';
    const end = endDate ? formatDate(endDate) : 'Hiện tại';
    if (!start && !end) return '';
    return `${start} - ${end}`;
}

