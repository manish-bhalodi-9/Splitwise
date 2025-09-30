# Initialize Gradle Wrapper
# Run this script to set up the Gradle wrapper properly

Write-Host "Initializing Gradle Wrapper..." -ForegroundColor Green

# Download gradle-wrapper.jar if needed
$wrapperJar = "gradle\wrapper\gradle-wrapper.jar"
if (-not (Test-Path $wrapperJar) -or (Get-Item $wrapperJar).Length -lt 1000) {
    Write-Host "Downloading gradle-wrapper.jar..." -ForegroundColor Yellow
    Invoke-WebRequest -Uri "https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar" -OutFile $wrapperJar
    Write-Host "✓ Downloaded gradle-wrapper.jar" -ForegroundColor Green
}

# Verify wrapper properties exist
$wrapperProps = "gradle\wrapper\gradle-wrapper.properties"
if (Test-Path $wrapperProps) {
    Write-Host "✓ gradle-wrapper.properties exists" -ForegroundColor Green
} else {
    Write-Host "✗ gradle-wrapper.properties missing" -ForegroundColor Red
    exit 1
}

# Verify gradlew.bat exists
if (Test-Path "gradlew.bat") {
    Write-Host "✓ gradlew.bat exists" -ForegroundColor Green
} else {
    Write-Host "✗ gradlew.bat missing" -ForegroundColor Red
    exit 1
}

Write-Host "`nGradle wrapper is ready!" -ForegroundColor Green
Write-Host "`nNext steps:" -ForegroundColor Cyan
Write-Host "1. In Android Studio: File → Sync Project with Gradle Files" -ForegroundColor White
Write-Host "2. Wait for sync to complete" -ForegroundColor White
Write-Host "3. The 'app' module should appear" -ForegroundColor White
Write-Host "4. Run button should become enabled" -ForegroundColor White
