// Employer manage-applications.js removed — placeholder to avoid 404
// manage-applications.js - frontend for employer applications management
document.addEventListener('DOMContentLoaded', () => {
	const companySelect = document.getElementById('companySelect');
	const btnRefresh = document.getElementById('btnRefresh');

	// Load companies from API and populate the select
	async function populateCompanies() {
		if (!companySelect) return;
		try {
			// Try to get companies available to the current user
			let resp;
			if (typeof api !== 'undefined' && typeof api.get === 'function') {
				resp = await api.get('/api/companies');
			} else {
				const r = await fetch('/api/companies');
				resp = await r.json();
			}

			const companies = resp && resp.data ? resp.data : resp;
			const userStr = localStorage.getItem('user');
			let user = null;
			try { user = userStr ? JSON.parse(userStr) : null; } catch(e){}

			// Filter similarly to server logic: company.user.id == user.id OR (company.user == null && company.status=='ACTIVE')
			const filtered = (companies || []).filter(c => {
				if (!c) return false;
				if (c.user && user && c.user.id === user.id) return true;
				if (!c.user && c.status === 'ACTIVE') return true;
				return false;
			});

			// Populate select
			companySelect.innerHTML = '<option value="">Chọn công ty</option>' + (filtered.map(c => `<option value="${c.id}">${escapeHtml(c.companyName || c.company_name || c.companyName)}</option>`).join(''));

			// If only one company, auto-select and load
			if (filtered.length === 1) {
				companySelect.value = filtered[0].id;
				loadForSelectedCompany();
			}
		} catch (e) {
			console.error('Failed to load companies', e);
		}
	}

	function loadForSelectedCompany() {
		const companyId = companySelect ? companySelect.value : null;
		if (!companyId) {
			document.getElementById('appsList').innerHTML = '<div class="empty-state">Vui lòng chọn công ty.</div>';
			document.getElementById('appsLoading').style.display = 'none';
			return;
		}
		loadApplicationsByCompany(companyId);
	}

	if (btnRefresh) btnRefresh.addEventListener('click', loadForSelectedCompany);
	if (companySelect) companySelect.addEventListener('change', loadForSelectedCompany);

	// initial populate companies and load
	setTimeout(() => { populateCompanies().then(() => setTimeout(loadForSelectedCompany, 50)); }, 50);
});

async function loadApplicationsByCompany(companyId) {
	const listEl = document.getElementById('appsList');
	const loadingEl = document.getElementById('appsLoading');
	if (loadingEl) loadingEl.style.display = 'block';
	if (listEl) listEl.innerHTML = '';

	try {
		// Use backend API: /api/applications/company/{companyId}
		const resp = await api.get(`/api/applications/company/${companyId}`);
		// The project's `api.get` may return raw data or {success,data}
		const apps = resp && resp.data ? resp.data : resp;

		if (!apps || apps.length === 0) {
			listEl.innerHTML = '<div class="empty-state">Chưa có đơn ứng tuyển cho công ty này.</div>';
			return;
		}

		listEl.innerHTML = apps.map(renderApplicationCard).join('');
	} catch (err) {
		console.error('Error loading applications', err);
		listEl.innerHTML = '<div class="empty-state">Lỗi khi tải đơn ứng tuyển.</div>';
	} finally {
		if (loadingEl) loadingEl.style.display = 'none';
	}
}

function renderApplicationCard(app) {
	const candidate = app.userName || (app.user ? app.user.name : 'N/A');
	const jobTitle = app.job ? app.job.title : (app.jobTitle || 'N/A');
	const companyName = app.job && app.job.company ? app.job.company.companyName : (app.companyName || 'N/A');
	const created = app.createdAt ? formatDateTime(app.createdAt) : '';
	const status = app.status || 'N/A';
	const resumeLink = app.resume && app.resume.id ? `/candidate/resumes/${app.resume.id}` : null;

	return `
		<div class="application-card">
			<div style="display:flex;justify-content:space-between;align-items:center;">
				<div>
					<h3 style="margin:0">${escapeHtml(candidate)}</h3>
					<div style="color:#666">${escapeHtml(jobTitle)} • ${escapeHtml(companyName)}</div>
				</div>
				<div style="text-align:right">
					<div class="status-badge">${escapeHtml(status)}</div>
					<div style="font-size:12px;color:#888">${escapeHtml(created)}</div>
				</div>
			</div>
			<div style="margin-top:10px;display:flex;gap:8px;align-items:center">
				${resumeLink ? `<a class="btn btn-secondary" href="${resumeLink}">Xem CV</a>` : ''}
				<button class="btn btn-primary" onclick="viewApplication(${app.id})">Xem</button>
				<select onchange="changeStatus(${app.id}, this.value)">
					<option value="PENDING" ${status==='PENDING'?'selected':''}>Chờ duyệt</option>
					<option value="REVIEWED" ${status==='REVIEWED'?'selected':''}>Đã xem</option>
					<option value="INTERVIEW" ${status==='INTERVIEW'?'selected':''}>Phỏng vấn</option>
					<option value="REJECTED" ${status==='REJECTED'?'selected':''}>Từ chối</option>
					<option value="HIRED" ${status==='HIRED'?'selected':''}>Trúng tuyển</option>
				</select>
			</div>
		</div>
	`;
}

function escapeHtml(s){ if(!s && s!==0) return ''; return String(s).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;'); }

function formatDateTime(iso){
	try{ const d=new Date(iso); return d.toLocaleString(); }catch(e){return iso}
}

function viewApplication(id){ window.location.href = `/candidate/applications/${id}`; }

async function changeStatus(applicationId, status){
	try{
		// PATCH /api/applications/{id}/status?status=INTERVIEW
		const resp = await api.patch(`/api/applications/${applicationId}/status?status=${encodeURIComponent(status)}`);
		// If api returns envelope, handle it; otherwise refresh
		if(resp && resp.success===false){ showToast('Lỗi cập nhật trạng thái','error'); }
		else { showToast('Cập nhật trạng thái thành công','success'); setTimeout(()=>location.reload(),700); }
	}catch(e){ console.error(e); showToast('Lỗi khi cập nhật trạng thái','error'); }
}

