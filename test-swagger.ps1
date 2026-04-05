$wc = New-Object System.Net.WebClient
$wc.Encoding = [System.Text.UTF8Encoding]::new()
try {
    $resp = Invoke-WebRequest -Uri "http://localhost:8080/swagger-ui.html" -UseBasicParsing -ErrorAction Stop
    Write-Host "SUCCESS! Swagger UI Status: $($resp.StatusCode)"
} catch {
    if ($_.Exception.Response) {
        Write-Host "HTTP Error:" $_.Exception.Response.StatusCode
    } else {
        Write-Host "Connection failed"
    }
}
