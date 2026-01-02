-- =====================================================
-- Alter Table: companies
-- Description: Thêm company_context JSON
-- =====================================================

-- Check if column exists before adding
IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'companies') 
    AND name = 'company_context'
)
BEGIN
    ALTER TABLE companies 
    ADD company_context NVARCHAR(MAX) NULL;
    
    PRINT 'Column company_context added successfully';
END
ELSE
BEGIN
    PRINT 'Column company_context already exists';
END
GO

-- Add comment
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'JSON context về văn hóa, giá trị công ty', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'companies',
    @level2type = N'COLUMN', @level2name = N'company_context';
GO

-- Example structure:
-- {
--   "culture": "Startup năng động, học hỏi nhanh",
--   "values": ["Innovation", "Teamwork", "Customer-first"],
--   "work_style": "Agile, Remote-friendly",
--   "general_requirements": "Tự chủ cao, chủ động học hỏi"
-- }
