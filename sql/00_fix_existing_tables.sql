-- =====================================================
-- FIX EXISTING TABLES
-- Description: Sửa lại cấu trúc 2 bảng AI đã tồn tại
-- =====================================================

PRINT 'Starting to fix existing AI tables...';
GO

-- =====================================================
-- 1. FIX TABLE: ai_match_results
-- =====================================================

-- Thêm cột industry_detected nếu chưa có
IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'ai_match_results') 
    AND name = 'industry_detected'
)
BEGIN
    ALTER TABLE ai_match_results 
    ADD industry_detected VARCHAR(50) NULL;
    PRINT '✓ Added column: ai_match_results.industry_detected';
END
ELSE
BEGIN
    PRINT '○ Column ai_match_results.industry_detected already exists';
END
GO

-- Đổi tên cột analysis → analysis_data (nếu chưa đúng)
IF EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'ai_match_results') 
    AND name = 'analysis'
)
AND NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'ai_match_results') 
    AND name = 'analysis_data'
)
BEGIN
    EXEC sp_rename 'ai_match_results.analysis', 'analysis_data', 'COLUMN';
    PRINT '✓ Renamed column: analysis → analysis_data';
END
ELSE IF EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'ai_match_results') 
    AND name = 'analysis_data'
)
BEGIN
    PRINT '○ Column ai_match_results.analysis_data already correct';
END
GO

-- Xóa cột ip_address (không cần thiết)
IF EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'ai_match_results') 
    AND name = 'ip_address'
)
BEGIN
    ALTER TABLE ai_match_results DROP COLUMN ip_address;
    PRINT '✓ Dropped column: ai_match_results.ip_address (not needed)';
END
ELSE
BEGIN
    PRINT '○ Column ai_match_results.ip_address already removed';
END
GO

PRINT '========================================';
PRINT 'ai_match_results table fixed!';
PRINT '========================================';
GO

-- =====================================================
-- 2. FIX TABLE: ai_usage_logs
-- =====================================================

-- Đổi tên cột feature_type → feature_name
IF EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'ai_usage_logs') 
    AND name = 'feature_type'
)
AND NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'ai_usage_logs') 
    AND name = 'feature_name'
)
BEGIN
    EXEC sp_rename 'ai_usage_logs.feature_type', 'feature_name', 'COLUMN';
    PRINT '✓ Renamed column: feature_type → feature_name';
END
ELSE IF EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'ai_usage_logs') 
    AND name = 'feature_name'
)
BEGIN
    PRINT '○ Column ai_usage_logs.feature_name already correct';
END
GO

-- Xóa các cột không cần thiết
IF EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'ai_usage_logs') 
    AND name = 'request_data'
)
BEGIN
    ALTER TABLE ai_usage_logs DROP COLUMN request_data;
    PRINT '✓ Dropped column: ai_usage_logs.request_data';
END
GO

IF EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'ai_usage_logs') 
    AND name = 'success'
)
BEGIN
    ALTER TABLE ai_usage_logs DROP COLUMN success;
    PRINT '✓ Dropped column: ai_usage_logs.success';
END
GO

IF EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'ai_usage_logs') 
    AND name = 'error_message'
)
BEGIN
    ALTER TABLE ai_usage_logs DROP COLUMN error_message;
    PRINT '✓ Dropped column: ai_usage_logs.error_message';
END
GO

PRINT '========================================';
PRINT 'ai_usage_logs table fixed!';
PRINT '========================================';
GO

-- =====================================================
-- 3. VERIFY FINAL STRUCTURE
-- =====================================================

PRINT '';
PRINT '========================================';
PRINT 'FINAL TABLE STRUCTURES:';
PRINT '========================================';
PRINT '';

PRINT 'ai_match_results columns:';
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'ai_match_results'
ORDER BY ORDINAL_POSITION;
GO

PRINT '';
PRINT 'ai_usage_logs columns:';
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'ai_usage_logs'
ORDER BY ORDINAL_POSITION;
GO

PRINT '';
PRINT '========================================';
PRINT '✓ ALL FIXES COMPLETED SUCCESSFULLY!';
PRINT '========================================';
PRINT '';
PRINT 'NEXT STEPS:';
PRINT '1. Run script: 03_create_industry_contexts_table.sql';
PRINT '2. Run script: 04_alter_companies_add_company_context.sql';
PRINT '3. Run script: 05_alter_jobs_add_ai_context.sql';
PRINT '4. Run script: 06_alter_categories_add_industry_key.sql';
GO
