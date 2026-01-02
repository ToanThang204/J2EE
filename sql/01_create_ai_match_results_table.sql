-- =====================================================
-- Table: ai_match_results
-- Description: Cache kết quả phân tích AI (24h)
-- =====================================================

-- Check if table exists, drop if needed
IF OBJECT_ID(N'ai_match_results', N'U') IS NOT NULL
BEGIN
    PRINT 'Table ai_match_results already exists. Dropping...';
    DROP TABLE ai_match_results;
END
GO

CREATE TABLE ai_match_results (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    user_id BIGINT NULL,
    job_id BIGINT NOT NULL,
    resume_id BIGINT NULL,
    match_score INT NOT NULL,
    industry_detected VARCHAR(50) NULL,
    analysis_data NVARCHAR(MAX) NOT NULL, -- JSON data
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    
    -- Foreign Keys
    CONSTRAINT FK_ai_match_results_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT FK_ai_match_results_job 
        FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    CONSTRAINT FK_ai_match_results_resume 
        FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE SET NULL,
    
    -- Constraints
    CONSTRAINT CHK_match_score CHECK (match_score >= 0 AND match_score <= 100)
);

PRINT 'Table ai_match_results created successfully';
GO

-- Indexes for performance
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_user_job' AND object_id = OBJECT_ID(N'ai_match_results'))
    CREATE INDEX idx_user_job ON ai_match_results(user_id, job_id);
    
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_created_at' AND object_id = OBJECT_ID(N'ai_match_results'))
    CREATE INDEX idx_created_at ON ai_match_results(created_at);
    
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_match_score' AND object_id = OBJECT_ID(N'ai_match_results'))
    CREATE INDEX idx_match_score ON ai_match_results(match_score);

PRINT 'Indexes created successfully';
GO

-- Comments
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Bảng lưu kết quả phân tích AI độ phù hợp CV-Job', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'ai_match_results';
