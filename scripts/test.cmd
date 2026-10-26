@echo off
rem Run the unit tests (business rules, demo scenario, voice flow, phrase length, screen rendering).
setlocal
call "%~dp0env.cmd"
cd /d "%~dp0.."
call "%~dp0..\gradlew.bat" :app:testDebugUnitTest --console=plain
