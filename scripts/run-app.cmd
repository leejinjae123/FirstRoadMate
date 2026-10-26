@echo off
rem Build the debug APK, install it on the emulator and launch the app.
rem   run-app.cmd         keep app data
rem   run-app.cmd fresh   clear app data first (demo from the beginning)
setlocal
call "%~dp0env.cmd"
cd /d "%~dp0.."

rem Boot the emulator while Gradle builds.
call "%~dp0emulator.cmd" nowait || exit /b 1

echo [build] assembleDebug ...
call "%~dp0..\gradlew.bat" :app:assembleDebug --console=plain -q || exit /b 1

call "%~dp0emulator.cmd" || exit /b 1

echo [install] %APP_ID%
"%ADB%" install -r -g app\build\outputs\apk\debug\app-debug.apk
if errorlevel 1 (
    rem Usually a different debug signing key (built on another PC/location): reinstall from scratch.
    echo [install] install failed - uninstalling the old build and retrying
    "%ADB%" uninstall %APP_ID% >nul 2>nul
    "%ADB%" install -g app\build\outputs\apk\debug\app-debug.apk || exit /b 1
)
if /i "%~1"=="fresh" (
    echo [install] clearing app data
    "%ADB%" shell pm clear %APP_ID% >nul
)
"%ADB%" shell pm grant %APP_ID% android.permission.RECORD_AUDIO >nul 2>nul

echo [run] %APP_ID%/%APP_ACTIVITY%
"%ADB%" shell am start -n %APP_ID%/%APP_ACTIVITY% >nul || exit /b 1
echo [run] done
exit /b 0
