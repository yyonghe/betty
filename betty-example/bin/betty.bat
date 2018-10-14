
:: for test ....
::set _appname=betty-example
::set _version=0.0.1-SNAPSHOT
::set _jvmargs=-Xms512m -Xmx2048m
::set _appargs=

set _appname=betty-example
set _version=0.0.1-SNAPSHOT
set _jvmargs=-Xms512m -Xmx2048m
set _appargs=
set _CLASSPATH=

goto betty_main

:betty_resourvle_classspath
	for /f "delims=" %%i in ('dir /s /b %_PWD_DIR%\lib\betty-server-*.jar') do (set tmp=%%i)
	if "" == "%_CLASSPATH%" (
		set "_CLASSPATH=%tmp%"
	) else (
		set "_CLASSPATH=%_CLASSPATH%;%tmp%"
	)
	for /f "delims=" %%i in ('dir /s /b %_PWD_DIR%\lib\betty-common-*.jar') do (set tmp=%%i)
	if "" == "%_CLASSPATH%" (
		set "_CLASSPATH=%tmp%"
	) else (
		set "_CLASSPATH=%_CLASSPATH%;%tmp%"
	)
goto:eof

:betty_run
	call:betty_resourvle_classspath
	%JAVA_HOME%\bin\java.exe -classpath %_CLASSPATH% %_jvmargs% -Dbetty.work.dir=%_PWD_DIR% -Dbetty.appname=%_appname% io.betty.server.bootstrap.BettyServerBootStrap %_appargs%
goto:eof

:betty_main
	if "start" == "%_CMD%" (
		goto betty_run
	) else (
		if "run" == "%_CMD%" (
			goto betty_run
		) else (
			if "kill" == "%_CMD%" (
				echo "Unsuport operation kill, no process kill"
			) else (
				if "restart" == "%_CMD%" (
					goto betty_run
				) else (
					echo "Unknown command: %_CMD%"
				)
			)
		)
	)
goto:eof

