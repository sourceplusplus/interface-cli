@REM Installation (Note: you need to start cmd or powershell in administrator mode.)
@echo off
setlocal ENABLEDELAYEDEXPANSION

@REM  Get the latest version of spp-cli.
set FLAG="FALSE"
set VERSION= UNKNOWN
curl -s https://api.github.com/repos/sourceplusplus/interface-cli/releases/latest > response.txt
FOR /F "tokens=*" %%g IN ('FIND "tag_name" "response.txt"') do set result=%%g
set "tag_name=%result:"tag_name": "=%"
set "VERSION=%tag_name:",=%"
@echo The latest version of spp-cli is %VERSION%

@REM Download the binary package.
curl -LO "https://github.com/sourceplusplus/interface-cli/releases/download/%VERSION%/spp-cli-%VERSION%-win64.zip"
if EXIST "spp-cli-%VERSION%-win64.zip" (
    tar -xf ".\spp-cli-%VERSION%-win64.zip"

    mkdir "C:\Program Files\spp-cli"

    @REM Add spp-cli to the environment variable PATH.
    copy ".\spp-cli.exe" "C:\Program Files\spp-cli\spp-cli.exe"
    setx "Path" "C:\Program Files\spp-cli\;%path%" /m

    @REM Delete unnecessary files.
    del ".\response.txt"
    del ".\spp-cli-%VERSION%-win64.zip"

    @echo Reopen the terminal and type 'spp-cli --help' to get more information.
) else (
    @echo Failed to download spp-cli-%VERSION%-win64.zip
)
