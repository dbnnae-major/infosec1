$ErrorActionPreference = 'Stop'
$baseUrl = 'http://127.0.0.1:8080'
$username = 'student-' + [Guid]::NewGuid().ToString('N').Substring(0, 8)
$password = 'StrongPassword123!'

Write-Host '1. Check GET /api/data without token (expect HTTP 401)'
try {
    Invoke-WebRequest -Uri "$baseUrl/api/data" -UseBasicParsing | Out-Null
    throw 'FAIL: API returned success without token'
} catch [System.Net.WebException] {
    $statusCode = [int]$_.Exception.Response.StatusCode
    if ($statusCode -ne 401) { throw "FAIL: expected 401, got $statusCode" }
    Write-Host 'PASS: HTTP 401'
}

Write-Host '2. Register user'
$payload = @{ username = $username; password = $password } | ConvertTo-Json
$registration = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -ContentType 'application/json' -Body $payload
Write-Host "PASS: $($registration.message) ($username)"

Write-Host '3. Log in and obtain JWT'
$login = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -ContentType 'application/json' -Body $payload
if (-not $login.token) { throw 'FAIL: no JWT in login response' }
Write-Host "PASS: got JWT, expires in $($login.expiresInSeconds) seconds"

Write-Host '4. Access protected API with JWT'
$data = Invoke-RestMethod -Uri "$baseUrl/api/data" -Headers @{ Authorization = "Bearer $($login.token)" }
if ($data.users -notcontains $username) { throw 'FAIL: user not found in result' }
Write-Host "PASS: $($data | ConvertTo-Json -Compress)"
Write-Host 'All smoke checks passed.'
