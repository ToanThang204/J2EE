-- =====================================================
-- Alter Table: categories
-- Description: Thêm industry_key để map với industry_contexts
-- =====================================================

-- Check if column exists before adding
IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'categories') 
    AND name = 'industry_key'
)
BEGIN
    ALTER TABLE categories 
    ADD industry_key VARCHAR(50) NULL;
    
    PRINT 'Column industry_key added successfully';
END
ELSE
BEGIN
    PRINT 'Column industry_key already exists';
END
GO

-- Add index
IF NOT EXISTS (
    SELECT * FROM sys.indexes 
    WHERE name = 'idx_industry_key' 
    AND object_id = OBJECT_ID(N'categories')
)
BEGIN
    CREATE INDEX idx_industry_key ON categories(industry_key);
    PRINT 'Index idx_industry_key created successfully';
END
GO

-- Add comment
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Key tham chiếu đến industry_contexts.key', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'categories',
    @level2type = N'COLUMN', @level2name = N'industry_key';
GO

-- Seed some default mappings (nếu có categories)
-- UPDATE categories SET industry_key = 'it' WHERE name LIKE N'%Công nghệ%' OR name LIKE N'%IT%' OR name LIKE N'%Lập trình%';
-- UPDATE categories SET industry_key = 'marketing' WHERE name LIKE N'%Marketing%';
-- UPDATE categories SET industry_key = 'sales' WHERE name LIKE N'%Kinh doanh%' OR name LIKE N'%Sales%';
-- UPDATE categories SET industry_key = 'finance' WHERE name LIKE N'%Tài chính%' OR name LIKE N'%Kế toán%';
