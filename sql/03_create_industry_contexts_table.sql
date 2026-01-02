-- =====================================================
-- Table: industry_contexts
-- Description: Cấu hình prompt AI theo ngành nghề
-- =====================================================

-- Check if table exists, drop if needed
IF OBJECT_ID(N'industry_contexts', N'U') IS NOT NULL
BEGIN
    PRINT 'Table industry_contexts already exists. Dropping...';
    DROP TABLE industry_contexts;
END
GO

CREATE TABLE industry_contexts (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    [key] VARCHAR(50) UNIQUE NOT NULL,
    name NVARCHAR(255) NOT NULL,
    description NVARCHAR(MAX) NULL,
    config NVARCHAR(MAX) NOT NULL, -- JSON data
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

PRINT 'Table industry_contexts created successfully';
GO

-- Index
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_key' AND object_id = OBJECT_ID(N'industry_contexts'))
    CREATE INDEX idx_key ON industry_contexts([key]);
    
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_is_active' AND object_id = OBJECT_ID(N'industry_contexts'))
    CREATE INDEX idx_is_active ON industry_contexts(is_active);

PRINT 'Indexes created successfully';
GO

-- Seed data (only if table is empty)
IF NOT EXISTS (SELECT 1 FROM industry_contexts)
BEGIN
    PRINT 'Inserting seed data...';
    
    INSERT INTO industry_contexts ([key], name, description, config, is_active) VALUES
    ('it', N'Công nghệ thông tin', N'Ngành IT/Tech', N'{
      "role": "Chuyên gia Tuyển dụng IT/Tech",
      "focus_areas": [
        "Tech Stack và công nghệ cụ thể (frameworks, languages, tools)",
        "Kinh nghiệm thực tế với dự án, codebase scale",
        "Kỹ năng giải quyết vấn đề kỹ thuật (algorithms, design patterns)",
        "Portfolio/GitHub"
      ],
      "key_metrics": "Số năm kinh nghiệm với từng tech stack, project complexity, code quality",
      "red_flags": "Thiếu hands-on experience, không có portfolio/GitHub, CV quá chung chung"
    }', 1),

    ('marketing', N'Marketing', N'Ngành Marketing & Digital Marketing', N'{
      "role": "Chuyên gia Tuyển dụng Marketing",
      "focus_areas": [
        "Kênh marketing (digital, social media, SEO/SEM, content)",
        "Tools (Google Analytics, Ads Manager, CRM)",
        "Metrics và KPIs (CTR, ROI, conversion rate)",
        "Case study và campaign thực tế"
      ],
      "key_metrics": "Budget quản lý, traffic growth, conversion rate, campaign results",
      "red_flags": "Không có số liệu cụ thể, thiếu case study, metrics không rõ ràng"
    }', 1),

    ('sales', N'Kinh doanh', N'Ngành Sales & Business Development', N'{
      "role": "Chuyên gia Tuyển dụng Sales",
      "focus_areas": [
        "Kinh nghiệm sales (B2B, B2C, Enterprise)",
        "Số liệu đạt được (revenue, quota, growth)",
        "Kỹ năng đàm phán và closing",
        "Network và quan hệ khách hàng"
      ],
      "key_metrics": "Revenue generated, quota achievement %, số deal đóng thành công",
      "red_flags": "Không có số liệu revenue, thiếu track record, CV quá chung chung"
    }', 1),

    ('finance', N'Tài chính', N'Ngành Finance & Accounting', N'{
      "role": "Chuyên gia Tuyển dụng Finance",
      "focus_areas": [
        "Chứng chỉ (CPA, CFA, ACCA)",
        "Kinh nghiệm với báo cáo tài chính, audit, tax",
        "Công cụ (Excel, SAP, QuickBooks)",
        "Kiến thức pháp lý và compliance"
      ],
      "key_metrics": "Số năm kinh nghiệm, chứng chỉ, quy mô budget/project quản lý",
      "red_flags": "Thiếu chứng chỉ bắt buộc, không có kinh nghiệm audit, thiếu technical skills"
    }', 1),

    ('general', N'Tổng quát', N'Ngành nghề khác', N'{
      "role": "Chuyên gia Tuyển dụng",
      "focus_areas": [
        "Kinh nghiệm làm việc liên quan",
        "Kỹ năng cứng và mềm",
        "Học vấn và chứng chỉ",
        "Thành tích và đóng góp"
      ],
      "key_metrics": "Số năm kinh nghiệm, thành tích nổi bật",
      "red_flags": "Thiếu kinh nghiệm cần thiết, CV không rõ ràng"
    }', 1);
    
    PRINT 'Seed data inserted successfully';
END
ELSE
BEGIN
    PRINT 'Seed data already exists. Skipping...';
END
GO

-- Comments
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Bảng cấu hình prompt AI theo ngành nghề', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'industry_contexts';
