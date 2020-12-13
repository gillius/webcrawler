@echo off
IF "%1"=="" (
	set ARGS="-h"
) ELSE (
	set ARGS=%*
)

gradlew.bat -q run --args="%ARGS%"