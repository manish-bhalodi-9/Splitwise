# Building Without Android Studio

This guide is for advanced users who want to build the app using only command-line tools.

## Prerequisites

1. **Java JDK 17+** ✅ (You already have Java 21)
2. **Android SDK Command Line Tools** ❌ (Need to install)
3. **Android Platform & Build Tools** ❌ (Need to install)

---

## Step 1: Install Android SDK Command Line Tools

### Download Command Line Tools

1. Go to: https://developer.android.com/studio#command-line-tools-only
2. Download "Command line tools only" for Windows
3. Extract to: `C:\Android\cmdline-tools\latest\`

The structure should be:
```
C:\Android\
  └── cmdline-tools\
      └── latest\
          ├── bin\
          ├── lib\
          └── ...
```

---

## Step 2: Set Environment Variables

Open PowerShell as Administrator and run:

```powershell
# Set ANDROID_HOME
[System.Environment]::SetEnvironmentVariable('ANDROID_HOME', 'C:\Android', 'User')

# Add to PATH
$currentPath = [System.Environment]::GetEnvironmentVariable('Path', 'User')
$newPath = "$currentPath;C:\Android\cmdline-tools\latest\bin;C:\Android\platform-tools"
[System.Environment]::SetEnvironmentVariable('Path', $newPath, 'User')

# Refresh environment in current session
$env:ANDROID_HOME = 'C:\Android'
$env:Path = "$env:Path;C:\Android\cmdline-tools\latest\bin;C:\Android\platform-tools"
```

**Close and reopen PowerShell** for changes to take effect.

---

## Step 3: Install Android SDK Components

```powershell
# Accept licenses
sdkmanager --licenses

# Install required components
sdkmanager "platform-tools" "platforms;android-35" "build-tools;34.0.0"

# Install additional tools
sdkmanager "emulator" "system-images;android-35;google_apis;x86_64"
```

---

## Step 4: Create local.properties

Create `local.properties` in the project root:

```powershell
@"
sdk.dir=C:\\Android
"@ | Out-File -FilePath "local.properties" -Encoding utf8
```

---

## Step 5: Build the App

```powershell
# Navigate to project directory
cd D:\Dev\Splitwise

# Clean build
.\gradlew clean

# Build debug APK
.\gradlew assembleDebug

# The APK will be at: app\build\outputs\apk\debug\app-debug.apk
```

---

## Step 6: Install on Device

### Via USB (ADB)

1. **Enable USB Debugging** on your Android device:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times to enable Developer Options
   - Go to Settings → Developer Options
   - Enable "USB Debugging"

2. **Connect device via USB**

3. **Verify connection:**
```powershell
adb devices
```

4. **Install APK:**
```powershell
adb install app\build\outputs\apk\debug\app-debug.apk
```

### Via Emulator

```powershell
# Create AVD
avdmanager create avd -n Pixel_7 -k "system-images;android-35;google_apis;x86_64" -d pixel_7

# Start emulator
emulator -avd Pixel_7

# Install APK
adb install app\build\outputs\apk\debug\app-debug.apk
```

---

## Quick Reference Commands

```powershell
# Build debug APK
.\gradlew assembleDebug

# Build release APK
.\gradlew assembleRelease

# Install on connected device
.\gradlew installDebug

# Run tests
.\gradlew test

# Clean build artifacts
.\gradlew clean

# Check for connected devices
adb devices

# View logcat (app logs)
adb logcat | Select-String "ExpenseSplitter"
```

---

## Troubleshooting

### "ANDROID_HOME not set"
- Make sure you set the environment variable and restarted PowerShell

### "SDK location not found"
- Create `local.properties` file with `sdk.dir=C:\\Android`

### "Failed to find Build Tools"
- Run: `sdkmanager "build-tools;34.0.0"`

### "License not accepted"
- Run: `sdkmanager --licenses` and accept all

### Gradle build fails
- Check internet connection (needs to download dependencies)
- Run: `.\gradlew clean` then try again

---

## Why Android Studio is Better

- ⏱️ **Setup Time**: 5 minutes vs 30+ minutes
- 🔧 **Maintenance**: Auto-updates vs manual updates
- 🐛 **Debugging**: Visual debugger vs command-line only
- 📱 **Testing**: Built-in emulator vs separate setup
- 💡 **Development**: Code completion, live preview, profiling tools

**Recommendation**: Unless you have a specific reason to avoid it, **use Android Studio** for a much better experience.
