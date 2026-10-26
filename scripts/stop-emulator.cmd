@echo off
rem Shut down the running emulator.
setlocal
call "%~dp0env.cmd"
"%ADB%" emu kill
