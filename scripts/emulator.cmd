@echo off
rem Start the Android Automotive emulator if it is not running, then wait until it has booted.
rem   emulator.cmd          start and wait
rem   emulator.cmd nowait   start only
setlocal
call "%~dp0env.cmd"

"%ADB%" start-server >nul 2>nul
"%ADB%" get-state >nul 2>nul
if not errorlevel 1 (
    echo [emulator] already running
    goto wait
)
if not exist "%ANDROID_HOME%\emulator\emulator.exe" (
    echo [emulator] not found: %ANDROID_HOME%\emulator\emulator.exe
    exit /b 1
)
echo [emulator] starting %AVD_NAME% ...
start "Android Emulator" "%ANDROID_HOME%\emulator\emulator.exe" -avd %AVD_NAME% -gpu auto -no-snapshot-save

:wait
if /i "%~1"=="nowait" exit /b 0
echo [emulator] waiting for boot ...
"%ADB%" wait-for-device
:boot
set "BOOT="
for /f "delims=" %%b in ('"%ADB%" shell getprop sys.boot_completed 2^>nul') do set "BOOT=%%b"
if not "%BOOT%"=="1" (
    ping -n 3 127.0.0.1 >nul
    goto boot
)
echo [emulator] ready
exit /b 0
