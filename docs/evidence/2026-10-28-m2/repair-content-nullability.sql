-- Author: Phan Tuan Thanh - CE190595

-- One-time repair of schema generated during M2 run-02..04, not application bootstrap.
-- Does not create tables, delete rows, seed data, or modify historical test databases.
USE [A2PhanTuanThanh_SE1910];
SET NOCOUNT ON;
SELECT COUNT(*) AS ArticlesBeforeRepair FROM dbo.NewsArticle;
IF EXISTS (SELECT 1 FROM dbo.NewsArticle WHERE NewsContent IS NULL)
    THROW 50000, 'Repair refused: existing null content requires an explicit data decision', 1;

ALTER TABLE dbo.NewsArticle ALTER COLUMN NewsContent nvarchar(max) NOT NULL;

SELECT name, is_nullable, max_length FROM sys.columns
WHERE object_id = OBJECT_ID(N'dbo.NewsArticle') AND name = N'NewsContent';
SELECT COUNT(*) AS ArticlesAfterRepair FROM dbo.NewsArticle;
