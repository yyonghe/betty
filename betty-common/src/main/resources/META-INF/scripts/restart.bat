@echo off

set "_BIN_DIR=%~dp0"

set "CURRENT_DIR=%cd%"
cd /d %_BIN_DIR%
cd ..
set _PWD_DIR=%cd%
cd /d %CURRENT_DIR%

set "_CMD=restart"

::echo _BIN_DIR: %_BIN_DIR%
::echo _PWD_DIR: %_PWD_DIR%

%_BIN_DIR%/betty.bat
