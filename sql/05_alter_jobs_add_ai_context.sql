-- =====================================================
-- Alter Table: jobs
-- Description: Thêm ai_context JSON
-- =====================================================

-- Check if column exists before adding
IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'jobs') 
    AND name = 'ai_context'
)
BEGIN
    ALTER TABLE jobs 
    ADD ai_context NVARCHAR(MAX) NULL;
    
    PRINT 'Column ai_context added successfully';
END
ELSE
BEGIN
    PRINT 'Column ai_context already exists';
END
GO

-- Add comment
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'JSON context cho AI matching (skills, weights, red flags)', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'jobs',
    @level2type = N'COLUMN', @level2name = N'ai_context';
GO

-- Example structure:
-- {
--   "must_have_skills": ["Laravel", "MySQL", "REST API"],
--   "nice_to_have_skills": ["Vue.js", "Docker", "AWS"],
--   "min_experience_years": 3,
--   "weights": {
--     "skills": 40,
--     "experience": 30,
--     "education": 10,
--     "soft_skills": 20
--   },
--   "red_flags": ["Không có kinh nghiệm backend", "CV quá chung chung"]
-- }
