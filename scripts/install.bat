@REM Installation (Note: you need to start cmd or powershell in administrator mode.)
@echo off
setlocal ENABLEDELAYEDEXPANSION

@REM curl https://api.github.com/repos/%Owner%/%Repo%/releases/latest > response.txt
@REM FOR /F "tokens=*" %%g IN ('FIND "tag_name" "response.txt"') do set result=%%g
@REM set "tag_name=%result:"tag_name": "=%"
@REM set "tag_name=%tag_name:",=%"

@REM  Get the latest version of spp-cli.
set FLAG="FALSE"
set VERSION= UNKNOWN
curl -LO "https://raw.githubusercontent.com/apache/skywalking-website/master/data/releases.yml"
if EXIST "releases.yml" (
    for /F "tokens=1,2,*" %%i in ('FINDSTR "name version" "./releases.yml"') do (
        if !FLAG! EQU "TRUE" (
            set FLAG="FALSE"
            set VERSION=%%k
        )
        if "%%k" == "SkyWalking CLI" (set FLAG="TRUE")
    )
)
set VERSION=%VERSION:~1%
@echo The latest version of spp-cli is %VERSION%

@REM Download the binary package.
curl -LO "https://github.com/sourceplusplus/interface-cli/releases/download/%VERSION%/spp-cli-%VERSION%-win64.zip"
if EXIST "spp-cli-%VERSION%-win64.zip" (
    tar -xf ".\spp-cli-%VERSION%-win64.zip"

    mkdir "C:\Program Files\spp-cli"
    @REM %USERPROFILE%\bin

    @REM Add spp-cli to the environment variable PATH.
    copy ".\spp-cli.exe" "C:\Program Files\swctl-cli\swctl.exe"
    setx "Path" "C:\Program Files\swctl-cli\;%path%" /m

    @REM Delete unnecessary files.
    del ".\skywalking-cli-%VERSION%-bin.tgz" ".\verify.txt"
    del ".\skywalking-cli-%VERSION%-bin.tgz.sha512" ".\releases.yml"
    rd /S /Q ".\skywalking-cli-%VERSION%-bin"

    @echo Reopen the terminal and type 'spp-cli --help' to get more information.
) else (
    @echo Failed to download spp-cli-%VERSION%-win64.zip
)
