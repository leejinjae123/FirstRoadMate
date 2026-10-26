@echo off
rem Show logs of the running app only (Ctrl+C to stop).
setlocal
call "%~dp0env.cmd"
set "PID="
for /f "delims=" %%p in ('"%ADB%" shell pidof %APP_ID% 2^>nul') do set "PID=%%p"
if not defined PID (
    echo [logcat] %APP_ID% is not running. Start the app first.
    exit /b 1
)
echo [logcat] pid %PID%
"%ADB%" logcat -v time --pid=%PID%
