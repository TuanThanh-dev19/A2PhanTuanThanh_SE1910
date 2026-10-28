-- Author: Phan Tuan Thanh - CE190595

-- Read-only snapshot. The caller selects the database with sqlcmd -d.
SET NOCOUNT ON;
SELECT DB_NAME() AS DatabaseName, CONVERT(varchar(30), SERVERPROPERTY('ProductVersion')) AS ServerVersion;

SELECT t.name AS TableName, c.name AS ColumnName, TYPE_NAME(c.user_type_id) AS SqlType,
       c.max_length AS MaxBytes, c.is_nullable AS Nullable, c.is_identity AS IsIdentity
FROM sys.tables t
JOIN sys.columns c ON c.object_id = t.object_id
WHERE t.is_ms_shipped = 0
ORDER BY t.name, c.column_id;

SELECT t.name AS TableName, i.name AS KeyName, i.is_primary_key AS IsPrimaryKey,
       i.is_unique_constraint AS IsUniqueConstraint, c.name AS ColumnName, ic.key_ordinal AS KeyPosition
FROM sys.tables t
JOIN sys.indexes i ON i.object_id = t.object_id
JOIN sys.index_columns ic ON ic.object_id = i.object_id AND ic.index_id = i.index_id
JOIN sys.columns c ON c.object_id = ic.object_id AND c.column_id = ic.column_id
WHERE i.is_primary_key = 1 OR i.is_unique_constraint = 1
ORDER BY t.name, i.name, ic.key_ordinal;

SELECT fk.name AS ForeignKeyName, OBJECT_NAME(f.parent_object_id) AS ChildTable,
       COL_NAME(f.parent_object_id, f.parent_column_id) AS ChildColumn,
       OBJECT_NAME(f.referenced_object_id) AS ParentTable,
       COL_NAME(f.referenced_object_id, f.referenced_column_id) AS ParentColumn,
       fk.delete_referential_action_desc AS DeleteAction, fk.is_disabled, fk.is_not_trusted
FROM sys.foreign_key_columns f
JOIN sys.foreign_keys fk ON fk.object_id = f.constraint_object_id
ORDER BY fk.name;

SELECT name AS CheckName, definition AS CheckDefinition FROM sys.check_constraints ORDER BY name;

SELECT N'SystemAccount' AS TableName, COUNT(*) AS TotalRows FROM dbo.SystemAccount
UNION ALL SELECT N'Category', COUNT(*) FROM dbo.Category
UNION ALL SELECT N'NewsArticle', COUNT(*) FROM dbo.NewsArticle
UNION ALL SELECT N'Tag', COUNT(*) FROM dbo.Tag
UNION ALL SELECT N'NewsTag', COUNT(*) FROM dbo.NewsTag;

-- No account password/hash is included in evidence.
SELECT NewsArticleID, NewsTitle, CategoryID, CreatedByID, UpdatedByID, NewsStatus, CreatedDate, ModifiedDate
FROM dbo.NewsArticle ORDER BY NewsArticleID;

SELECT NewsArticleID, DATALENGTH(NewsContent) AS ContentBytes,
       CONVERT(varchar(64), HASHBYTES('SHA2_256', NewsContent), 2) AS ContentSHA256
FROM dbo.NewsArticle ORDER BY NewsArticleID;
