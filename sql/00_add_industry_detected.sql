-- =====================================================
-- ADD MISSING COLUMN: industry_detected
-- =====================================================

USE J2EEDatabase;
GO

-- Thêm cột industry_detected vào ai_match_results
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
    PRINT '○ Column already exists';
END
GO

-- Verify
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'ai_match_results'
AND COLUMN_NAME = 'industry_detected';
GO

PRINT '✓ Done! Now you have all required columns.';
GO
