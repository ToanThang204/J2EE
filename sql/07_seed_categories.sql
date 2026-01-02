-- =====================================================
-- SEED DATA: CATEGORIES
-- File: 07_seed_categories.sql
-- Mục đích: Thêm các danh mục công việc mẫu
-- =====================================================

USE J2EECV;
GO

-- Xóa dữ liệu cũ (nếu có)
DELETE FROM dbo.job_categories;
DELETE FROM dbo.categories;
GO

-- =====================================================
-- IT CATEGORIES
-- =====================================================
INSERT INTO dbo.categories (name, description, industry_key, created_at, updated_at) VALUES
('Backend Developer', 'Lập trình Backend/Server-side', 'it', GETDATE(), GETDATE()),
('Frontend Developer', 'Lập trình Frontend/Client-side', 'it', GETDATE(), GETDATE()),
('Full Stack Developer', 'Lập trình Full Stack', 'it', GETDATE(), GETDATE()),
('Mobile Developer', 'Lập trình ứng dụng di động (iOS, Android)', 'it', GETDATE(), GETDATE()),
('DevOps Engineer', 'DevOps, CI/CD, Infrastructure', 'it', GETDATE(), GETDATE()),
('QA/Tester', 'Kiểm thử phần mềm, Quality Assurance', 'it', GETDATE(), GETDATE()),
('Data Engineer', 'Kỹ sư dữ liệu, Big Data', 'it', GETDATE(), GETDATE()),
('System Administrator', 'Quản trị hệ thống', 'it', GETDATE(), GETDATE()),
('Network Engineer', 'Kỹ sư mạng', 'it', GETDATE(), GETDATE()),
('Security Engineer', 'Bảo mật, Cybersecurity', 'it', GETDATE(), GETDATE()),
('UI/UX Designer', 'Thiết kế giao diện, trải nghiệm người dùng', 'it', GETDATE(), GETDATE()),
('Product Manager', 'Quản lý sản phẩm IT', 'it', GETDATE(), GETDATE()),
('Technical Lead', 'Trưởng nhóm kỹ thuật', 'it', GETDATE(), GETDATE()),
('Software Architect', 'Kiến trúc sư phần mềm', 'it', GETDATE(), GETDATE()),
('AI/ML Engineer', 'Kỹ sư AI, Machine Learning', 'it', GETDATE(), GETDATE());

-- =====================================================
-- MARKETING CATEGORIES
-- =====================================================
INSERT INTO dbo.categories (name, description, industry_key, created_at, updated_at) VALUES
('Digital Marketing', 'Marketing số', 'marketing', GETDATE(), GETDATE()),
('Content Marketing', 'Marketing nội dung', 'marketing', GETDATE(), GETDATE()),
('SEO Specialist', 'Chuyên viên SEO', 'marketing', GETDATE(), GETDATE()),
('Social Media Manager', 'Quản lý mạng xã hội', 'marketing', GETDATE(), GETDATE()),
('Performance Marketing', 'Marketing hiệu suất (Ads)', 'marketing', GETDATE(), GETDATE()),
('Brand Manager', 'Quản lý thương hiệu', 'marketing', GETDATE(), GETDATE()),
('Marketing Manager', 'Quản lý Marketing', 'marketing', GETDATE(), GETDATE()),
('Email Marketing', 'Marketing qua Email', 'marketing', GETDATE(), GETDATE()),
('Growth Hacker', 'Chuyên viên tăng trưởng', 'marketing', GETDATE(), GETDATE()),
('PR Specialist', 'Quan hệ công chúng', 'marketing', GETDATE(), GETDATE());

-- =====================================================
-- SALES CATEGORIES
-- =====================================================
INSERT INTO dbo.categories (name, description, industry_key, created_at, updated_at) VALUES
('Sales Executive', 'Nhân viên kinh doanh', 'sales', GETDATE(), GETDATE()),
('Account Manager', 'Quản lý tài khoản khách hàng', 'sales', GETDATE(), GETDATE()),
('Business Development', 'Phát triển kinh doanh', 'sales', GETDATE(), GETDATE()),
('Sales Manager', 'Quản lý bán hàng', 'sales', GETDATE(), GETDATE()),
('Telesales', 'Bán hàng qua điện thoại', 'sales', GETDATE(), GETDATE()),
('Key Account Manager', 'Quản lý tài khoản lớn', 'sales', GETDATE(), GETDATE()),
('Customer Success', 'Chăm sóc khách hàng', 'sales', GETDATE(), GETDATE()),
('Pre-Sales Consultant', 'Tư vấn trước bán hàng', 'sales', GETDATE(), GETDATE());

-- =====================================================
-- FINANCE CATEGORIES
-- =====================================================
INSERT INTO dbo.categories (name, description, industry_key, created_at, updated_at) VALUES
('Accountant', 'Kế toán', 'finance', GETDATE(), GETDATE()),
('Financial Analyst', 'Phân tích tài chính', 'finance', GETDATE(), GETDATE()),
('Auditor', 'Kiểm toán viên', 'finance', GETDATE(), GETDATE()),
('Tax Specialist', 'Chuyên viên thuế', 'finance', GETDATE(), GETDATE()),
('Finance Manager', 'Quản lý tài chính', 'finance', GETDATE(), GETDATE()),
('Treasury Specialist', 'Chuyên viên kho quỹ', 'finance', GETDATE(), GETDATE()),
('Investment Analyst', 'Phân tích đầu tư', 'finance', GETDATE(), GETDATE());

-- =====================================================
-- GENERAL CATEGORIES (Không thuộc ngành cụ thể)
-- =====================================================
INSERT INTO dbo.categories (name, description, industry_key, created_at, updated_at) VALUES
('HR Manager', 'Quản lý nhân sự', 'general', GETDATE(), GETDATE()),
('Recruiter', 'Nhân viên tuyển dụng', 'general', GETDATE(), GETDATE()),
('Office Admin', 'Hành chính văn phòng', 'general', GETDATE(), GETDATE()),
('Executive Assistant', 'Trợ lý điều hành', 'general', GETDATE(), GETDATE()),
('Project Manager', 'Quản lý dự án', 'general', GETDATE(), GETDATE()),
('Operations Manager', 'Quản lý vận hành', 'general', GETDATE(), GETDATE()),
('Legal Specialist', 'Chuyên viên pháp lý', 'general', GETDATE(), GETDATE()),
('Customer Service', 'Dịch vụ khách hàng', 'general', GETDATE(), GETDATE());

GO

-- =====================================================
-- VERIFY
-- =====================================================
PRINT '✅ Đã thêm categories thành công!';
SELECT 
    industry_key,
    COUNT(*) as total
FROM dbo.categories
GROUP BY industry_key
ORDER BY industry_key;

PRINT '';
PRINT 'Tổng số categories: ';
SELECT COUNT(*) as total FROM dbo.categories;
GO
