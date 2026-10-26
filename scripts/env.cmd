@echo off
rem Tool locations for the emulator / build scripts.
rem Override FRM_SDK (or any variable below) before calling to use another location.
if not defined FRM_SDK set "FRM_SDK=C:\sdk"
set "JAVA_HOME=%FRM_SDK%\jdk-21"
set "ANDROID_HOME=%FRM_SDK%\android"
set "ANDROID_SDK_ROOT=%FRM_SDK%\android"
set "ANDROID_AVD_HOME=%FRM_SDK%\avd"
set "ANDROID_USER_HOME=%FRM_SDK%\android-user"
set "GRADLE_USER_HOME=%FRM_SDK%\gradle-home"
if not defined AVD_NAME set "AVD_NAME=FirstRoadMate_AAOS34"
set "ADB=%ANDROID_HOME%\platform-tools\adb.exe"
set "APP_ID=com.firstroad.mate.debug"
set "APP_ACTIVITY=com.firstroad.mate.MainActivity"
