// Company Members JavaScript

document.addEventListener('DOMContentLoaded', function() {
    loadCompanyMembers();
    initMemberActions();
});

function initMemberActions() {
    const addMemberBtn = document.getElementById('addMemberBtn');
    if (addMemberBtn) {
        addMemberBtn.addEventListener('click', showAddMemberForm);
    }
}

async function loadCompanyMembers() {
    try {
        const companyId = document.getElementById('companyId')?.value ||
                          new URLSearchParams(window.location.search).get('companyId');
        
        if (!companyId) return;
        
        const response = await api.get(`/company-members?companyId=${companyId}`);
        
        if (response && response.success && response.data) {
            displayMembers(response.data);
        }
    } catch (error) {
        console.error('Error loading company members:', error);
    }
}

function displayMembers(members) {
    const container = document.getElementById('membersList');
    if (!container) return;
    
    if (!members || members.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>Chưa có thành viên nào</p></div>';
        return;
    }
    
    container.innerHTML = members.map(member => `
        <div class="member-card">
            <div class="member-avatar">${member.user ? member.user.name.substring(0, 1).toUpperCase() : 'U'}</div>
            <div class="member-info">
                <h3 class="member-name">${member.user ? member.user.name : 'N/A'}</h3>
                <p class="member-email">${member.user ? member.user.email : 'N/A'}</p>
                <span class="member-role">${member.roleInCompany || 'N/A'}</span>
            </div>
            <div class="member-actions">
                <button class="btn-icon edit" onclick="editMember(${member.id})" title="Chỉnh sửa">✏️</button>
                <button class="btn-icon delete" onclick="removeMember(${member.id})" title="Xóa">🗑️</button>
            </div>
        </div>
    `).join('');
}

function showAddMemberForm() {
    // TODO: Implement add member modal
    showToast('Tính năng thêm thành viên đang được phát triển', 'info');
}

function editMember(memberId) {
    window.location.href = `/employer/companies/${getCompanyId()}/members/${memberId}/edit`;
}

async function removeMember(memberId) {
    if (!confirm('Bạn có chắc chắn muốn xóa thành viên này?')) {
        return;
    }
    
    try {
        const response = await api.delete(`/company-members/${memberId}`);
        
        if (response && response.success) {
            showToast('Xóa thành viên thành công', 'success');
            setTimeout(() => window.location.reload(), 1000);
        }
    } catch (error) {
        showToast('Lỗi khi xóa thành viên', 'error');
    }
}

function getCompanyId() {
    return document.getElementById('companyId')?.value ||
           new URLSearchParams(window.location.search).get('companyId');
}

