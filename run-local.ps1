$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
if (-not $env:JWT_SECRET_BASE64) {
    $secret = New-Object byte[] 32
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($secret) } finally { $rng.Dispose() }
    $env:JWT_SECRET_BASE64 = [Convert]::ToBase64String($secret)
    Write-Host 'Generated temporary JWT secret for local use.'
}
Write-Host 'Starting API at http://127.0.0.1:8080 ...'
& "$PSScriptRoot\mvnw.cmd" spring-boot:run
exit $LASTEXITCODE
