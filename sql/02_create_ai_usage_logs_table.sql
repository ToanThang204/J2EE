-- =====================================================
-- Table: ai_usage_logs
-- Description: Log usage cho rate limiting
-- =====================================================

-- Check if table exists, drop if needed
IF OBJECT_ID(N'ai_usage_logs', N'U') IS NOT NULL
BEGIN
    PRINT 'Table ai_usage_logs already exists. Dropping...';
    DROP TABLE ai_usage_logs;
END
GO

CREATE TABLE ai_usage_logs (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    user_id BIGINT NULL,
    ip_address VARCHAR(45) NULL,
    feature_name VARCHAR(50) NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    
    -- Foreign Keys
    CONSTRAINT FK_ai_usage_logs_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

PRINT 'Table ai_usage_logs created successfully';
GO

-- Indexes for rate limiting queries
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_user_feature_time' AND object_id = OBJECT_ID(N'ai_usage_logs'))
    CREATE INDEX idx_user_feature_time ON ai_usage_logs(user_id, feature_name, created_at);
    
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_ip_feature_time' AND object_id = OBJECT_ID(N'ai_usage_logs'))
    CREATE INDEX idx_ip_feature_time ON ai_usage_logs(ip_address, feature_name, created_at);

PRINT 'Indexes created successfully';
GO

-- Comments
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Bảng log usage AI để rate limiting', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'ai_usage_logs';
