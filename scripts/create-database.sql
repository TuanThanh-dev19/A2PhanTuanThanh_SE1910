-- Run using an authorized SQL Server identity; leaves an existing database intact.
USE [master];
GO
IF DB_ID(N'A2PhanTuanThanh_SE1910') IS NULL
    CREATE DATABASE [A2PhanTuanThanh_SE1910];
GO
