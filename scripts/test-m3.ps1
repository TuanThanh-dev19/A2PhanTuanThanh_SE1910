# Author: Phan Tuan Thanh - CE190595

param(
    [Parameter(Mandatory = $true)]
    [string]$SqlServerInstance
)

$ErrorActionPreference = 'Stop'
if ($env:SQLSERVER_INTEGRATED_SECURITY -ne 'true') {
    throw 'Configure Windows Authentication as described in README before running tests.'
}
if ([string]::IsNullOrWhiteSpace($env:JWT_SECRET)) {
    throw 'Set JWT_SECRET to Base64 of at least 32 random bytes before running tests.'
}
if ($env:DEMO_ENABLED -eq 'true') {
    throw 'Disable DEMO_ENABLED when running tests; seed the demo separately.'
}
$previousM3Database = $env:SQLSERVER_M3_TEST_DATABASE
Push-Location (Split-Path -Parent $PSScriptRoot)
try {
    $suffix = (Get-Date -Format 'yyyyMMdd_HHmmss') + '_' + [guid]::NewGuid().ToString('N').Substring(0, 8)
    $testDatabase = 'A2PhanTuanThanh_SE1910_M3Test_' + $suffix
    & sqlcmd -S $SqlServerInstance -E -C -b -Q "CREATE DATABASE [$testDatabase]"
    if ($LASTEXITCODE -ne 0) { throw 'Could not create isolated M3 test database.' }
    $env:SQLSERVER_M3_TEST_DATABASE = $testDatabase
    Write-Host "M3 API tests: $testDatabase (transactions roll back; database retained)."
    & ./scripts/test-persistence.ps1 -SqlServerInstance $SqlServerInstance
    Write-Host 'M3 and existing tests completed; see target/surefire-reports for results.'
}
finally {
    $env:SQLSERVER_M3_TEST_DATABASE = $previousM3Database
    Pop-Location
}
