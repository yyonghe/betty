#!/bin/sh

_appname="${betty:app.appname}"
_version="${betty:app.version}"
_jvmargs="${betty:app.jvmargs}"
_appargs="${betty:app.appargs}"

betty_resourvle_classspath()
{
	for lib in $*
	do
		tmp=`find ${_PWD_DIR}/lib -name *.jar |grep "${lib}-[^a-zA-Z_]\{1,\}"`
		if [ -z "${_CLASSPATH}" ];then
			_CLASSPATH="${tmp}"
		else
			_CLASSPATH="${_CLASSPATH}:${tmp}"
		fi
	done
}

betty_start()
{
	cd ${_BIN_DIR}
	_pid=`pgrep -f ^.*java.*${_appname}.*jar.*$`
	if [ -n "${_pid}" ];then
		echo "Process ${_appname} is running."
		exit 0
	fi
	betty_resourvle_classspath betty-server betty-common
	nohup ${JAVA_HOME}/bin/java -classpath ${_CLASSPATH} ${_jvmargs} -Dbetty.work.dir=${_PWD_DIR} -Dbetty.appname=${_appname} io.betty.server.bootstrap.BettyServerBootStrap ${_appargs} >> ${PWD_DIR}/logs/nohup.out 2>&1 &
	_pid=`pgrep -f ^.*java.*${_appname}.*jar.*$`
	echo "Started process ${_appname}-${_version} with pid ${_pid}"
}

betty_kill()
{
	_pid=`pgrep -f ^.*java.*${_appname}.*jar.*$`
	if [ -n "${_pid}" ];then
		echo "Killing process ${_appname} with pid ${_pid}."
		kill -9 ${_pid}
		echo "Killed process ${_appname} with pid ${_pid}."
	else
		echo "No process to kill"
	fi
}

betty_run()
{
	#firstly try to kill
	betty_kill

	cd ${_BIN_DIR}
	betty_resourvle_classspath betty-common betty-server
	${JAVA_HOME}/bin/java -classpath ${_CLASSPATH} ${_jvmargs} -Dbetty.work.dir=${_PWD_DIR} -Dbetty.appname=${_appname} io.betty.server.bootstrap.BettyServerBootStrap ${_appargs}
}

betty_main()
{
	_BIN_DIR=$1
	_CMD=$2
	_PWD_DIR=$(cd $_BIN_DIR;cd ..;pwd)
	_CLASSPATH=""
	
	if [ "start" = "${_CMD}" ];then
		betty_start
	elif [ "kill" = "${_CMD}" ];then
		betty_kill
	elif [ "run" = "${_CMD}" ];then
		betty_run
	else
		echo "Unknown command: ${_CMD}"
		exit 1
	fi
}

