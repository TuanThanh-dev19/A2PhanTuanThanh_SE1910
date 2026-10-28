# Author: Phan Tuan Thanh - CE190595

param(
    [Parameter(Mandatory = $true)]
    [string]$SqlServerInstance,
    [string]$EvidenceDirectory = ''
)

$ErrorActionPreference = 'Stop'

function Start-PackagedApplication {
    param([string]$OutputPrefix, [string]$DataSourceUrl = '')

    $javaArguments = @('-jar', 'target/A2PhanTuanThanh_SE1910-0.0.1-SNAPSHOT.jar', '--server.port=0')
    if ($DataSourceUrl -ne '') {
        $javaArguments += "--spring.datasource.url=$DataSourceUrl"
    }
    $process = Start-Process -FilePath 'java' -ArgumentList $javaArguments -PassThru -WindowStyle Hidden `
        -RedirectStandardOutput "$OutputPrefix-start.txt" -RedirectStandardError "$OutputPrefix-stderr.txt"
    try {
        $httpPort = $null
        for ($attempt = 0; $attempt -lt 90; $attempt++) {
            if ($process.HasExited) { throw 'Packaged application exited before startup.' }
            $startupLog = Get-Content -LiteralPath "$OutputPrefix-start.txt" -Raw -ErrorAction SilentlyContinue
            if ($startupLog -match 'Tomcat started on port (\d+)' -and
                $startupLog -match 'Started A2PhanTuanThanhSe1910Application') {
                $portMatch = [regex]::Match($startupLog, 'Tomcat started on port (\d+)')
                $httpPort = $portMatch.Groups[1].Value
                break
            }
            Start-Sleep -Milliseconds 500
        }
        if ($null -eq $httpPort) { throw 'Packaged application did not become ready within 45 seconds.' }
        $httpResponse = Invoke-WebRequest -Uri "http://localhost:$httpPort/" -SkipHttpErrorCheck
        if ($httpResponse.StatusCode -ne 404) { throw 'Expected HTTP 404 without controllers.' }
        "PASS: packaged Boot 3 application started; GET / = $($httpResponse.StatusCode); port=$httpPort; PID=$($process.Id)" |
            Set-Content -LiteralPath "$OutputPrefix-smoke.txt" -Encoding utf8
        return $process
    }
    catch {
        if (!$process.HasExited) { Stop-Process -Id $process.Id }
        throw
    }
}

$projectRoot = Split-Path -Parent $PSScriptRoot
$previousTestDatabase = $env:SQLSERVER_TEST_DATABASE
$applicationProcess = $null
Push-Location $projectRoot
try {
    # The runner uses the Windows identity explicitly selected for this project.
    if ($env:SQLSERVER_INTEGRATED_SECURITY -ne 'true') {
        throw 'This runner requires configured Windows Authentication; it does not guess SQL login credentials.'
    }
    $databaseSuffix = (Get-Date -Format 'yyyyMMdd_HHmmss') + '_' + [guid]::NewGuid().ToString('N').Substring(0, 8)
    $testDatabase = 'A2PhanTuanThanh_SE1910_M2Test_' + $databaseSuffix
    if ([string]::IsNullOrWhiteSpace($EvidenceDirectory)) {
        $EvidenceDirectory = "tmp/m2-results/$databaseSuffix"
    }
    if ((Test-Path -LiteralPath $EvidenceDirectory) -and
        (Get-ChildItem -LiteralPath $EvidenceDirectory -Force | Measure-Object).Count -gt 0) {
        throw 'Evidence directory must be new or empty; previous runs must be preserved.'
    }
    New-Item -ItemType Directory -Force -Path $EvidenceDirectory | Out-Null
    $evidencePath = (Resolve-Path -LiteralPath $EvidenceDirectory).Path
    @(
        "Recorded at: $((Get-Date).ToString('o'))",
        'Runner: Codex/tool or local operator; not student IntelliJ evidence.',
        "Git base: $(git rev-parse HEAD)",
        "SQL instance: $SqlServerInstance",
        "Isolated database: $testDatabase",
        'Authentication: Windows integrated. No credentials recorded.',
        'Database is retained for inspection; no database is dropped or reset.'
    ) | Set-Content -LiteralPath "$evidencePath/environment.txt" -Encoding utf8

    # Only create a fresh database; existing names are refused.
    $createQuery = "IF DB_ID(N'$testDatabase') IS NOT NULL THROW 50000, 'Test database already exists', 1; CREATE DATABASE [$testDatabase];"
    & sqlcmd -S $SqlServerInstance -E -C -b -W -w 200 -Q $createQuery 2>&1 |
        Tee-Object -FilePath "$evidencePath/database-create.txt"
    if ($LASTEXITCODE -ne 0) { throw 'Could not create the isolated test database.' }
    $env:SQLSERVER_TEST_DATABASE = $testDatabase

    & sqlcmd -S $SqlServerInstance -E -C -b -W -w 200 -d A2PhanTuanThanh_SE1910 -Q 'SET NOCOUNT ON; SELECT DB_NAME() AS DatabaseName; SELECT name FROM sys.tables WHERE is_ms_shipped=0;' |
        Set-Content -LiteralPath "$evidencePath/assignment-before.txt" -Encoding utf8
    if ($LASTEXITCODE -ne 0) { throw 'Could not inspect the assignment database.' }

    & .\mvnw.cmd -B clean verify 2>&1 | Tee-Object -FilePath "$evidencePath/build-test.txt"
    $buildExitCode = $LASTEXITCODE
    if (Test-Path -LiteralPath target/surefire-reports) {
        Copy-Item -Path target/surefire-reports/*.txt -Destination $evidencePath
    }
    if ($buildExitCode -ne 0) { throw 'Build/test failed; see build-test.txt. Database and logs retained.' }

    & sqlcmd -S $SqlServerInstance -E -C -b -W -w 240 -f 65001 -d $testDatabase -i scripts/inspect-persistence.sql -o "$evidencePath/isolated-schema-data.txt"
    if ($LASTEXITCODE -ne 0) { throw 'Could not inspect the test schema.' }

    $applicationProcess = Start-PackagedApplication -OutputPrefix "$evidencePath/application"

    & sqlcmd -S $SqlServerInstance -E -C -b -W -w 240 -f 65001 -d A2PhanTuanThanh_SE1910 -i scripts/inspect-persistence.sql -o "$evidencePath/assignment-schema-data.txt"
    if ($LASTEXITCODE -ne 0) { throw 'Could not inspect the assignment schema.' }
    & sqlcmd -S $SqlServerInstance -E -C -b -W -w 200 -Q "SET NOCOUNT ON; SELECT s.program_name, DB_NAME(s.database_id) AS DatabaseName, c.net_transport, c.auth_scheme, c.encrypt_option FROM sys.dm_exec_sessions s JOIN sys.dm_exec_connections c ON c.session_id=s.session_id WHERE s.program_name='A2PhanTuanThanh_SE1910';" |
        Set-Content -LiteralPath "$evidencePath/sqlserver-application-session.txt" -Encoding utf8
    if ($LASTEXITCODE -ne 0) { throw 'Could not inspect the application session.' }
    Stop-Process -Id $applicationProcess.Id
    $applicationProcess.WaitForExit()
    $applicationProcess = $null

    # Restart the packaged JAR in separate JVMs against the committed test fixture.
    $urlProperty = Get-Content src/main/resources/application.properties |
        Where-Object { $_.StartsWith('spring.datasource.url=') }
    $testUrl = $urlProperty.Substring('spring.datasource.url='.Length).Replace(
        'databaseName=A2PhanTuanThanh_SE1910;', "databaseName=$testDatabase;")
    for ($restart = 1; $restart -le 2; $restart++) {
        $applicationProcess = Start-PackagedApplication -OutputPrefix "$evidencePath/restart-$restart" -DataSourceUrl $testUrl
        Stop-Process -Id $applicationProcess.Id
        $applicationProcess.WaitForExit()
        $applicationProcess = $null
    }
    & sqlcmd -S $SqlServerInstance -E -C -b -W -w 240 -f 65001 -d $testDatabase -i scripts/inspect-persistence.sql -o "$evidencePath/isolated-after-process-restarts.txt"
    if ($LASTEXITCODE -ne 0) { throw 'Could not inspect data after process restarts.' }
    $beforeHash = (Get-FileHash -LiteralPath "$evidencePath/isolated-schema-data.txt" -Algorithm SHA256).Hash
    $afterHash = (Get-FileHash -LiteralPath "$evidencePath/isolated-after-process-restarts.txt" -Algorithm SHA256).Hash
    if ($beforeHash -ne $afterHash) { throw 'Schema/data snapshot changed across packaged process restarts.' }
    @(
        'PASS TC-DB-02: committed fixture unchanged across two independent packaged JVM starts/stops.',
        'Compared schema, keys, FKs, checks, counts, article/audit data and Unicode content byte lengths/SHA256.',
        "Before SHA256: $beforeHash", "After SHA256: $afterHash"
    ) | Set-Content -LiteralPath "$evidencePath/process-restart-verification.txt" -Encoding utf8
}
finally {
    if ($null -ne $applicationProcess -and !$applicationProcess.HasExited) {
        Stop-Process -Id $applicationProcess.Id
        $applicationProcess.WaitForExit()
    }
    $env:SQLSERVER_TEST_DATABASE = $previousTestDatabase
    Pop-Location
}
