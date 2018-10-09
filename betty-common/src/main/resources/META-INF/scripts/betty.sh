#!/bin/sh

betty_resourvle_classspath()
{
	for lib in $*
	do
		tmp=`find ${PWD_DIR}/lib -name *.jar |grep "${lib}-[^a-zA-Z_]\{1,\}"`
		if [ -z "${_CLASSPATH}" ];then
			_CLASSPATH="${tmp}"
		else
			_CLASSPATH="${_CLASSPATH}:${tmp}"
		fi
	done
}

betty_start()
{
	_appname=$3
	_version=$4
	cd ${BIN_DIR}
	_pid=`pgrep -f ^.*java.*${_appname}.*jar.*$`
	if [ -n "${_pid}" ];then
		echo "Process ${_appname} is running."
		exit 0
	fi
	betty_resourvle_classspath betty-server betty-common
	nohup ${JAVA_HOME}/bin/java -classpath ${_CLASSPATH} ${_BETTY_SCRIPT_JVM_ARGS} -Dbetty.work.dir=${PWD_DIR} -Dbetty.appname=${_appname} io.betty.server.bootstrap.BettyServerBootStrap ${_BETTY_SCRIPT_APP_ARGS} >> ${PWD_DIR}/logs/nohup.out 2>&1 &
	_pid=`pgrep -f ^.*java.*${_appname}.*jar.*$`
	echo "Started process ${_appname}-${_version} with pid ${_pid}"
}

betty_kill()
{
	_appname=$3
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
	betty_kill $* #firstly try to kill
	_appname=$3
	_version=$4
	cd ${BIN_DIR}
	betty_resourvle_classspath betty-common betty-server
	${JAVA_HOME}/bin/java -classpath ${_CLASSPATH} ${_BETTY_SCRIPT_JVM_ARGS} -Dbetty.work.dir=${PWD_DIR} -Dbetty.appname=${_appname} io.betty.server.bootstrap.BettyServerBootStrap ${_BETTY_SCRIPT_APP_ARGS}
}

betty_main()
{
	BIN_DIR=$1
	_CMD=$2
	PWD_DIR=$(cd $BIN_DIR;cd ..;pwd)
	_CLASSPATH=""
	
	if [ "start" = "${_CMD}" ];then
		betty_start $*
	elif [ "kill" = "${_CMD}" ];then
		betty_kill $*
	elif [ "run" = "${_CMD}" ];then
		betty_run $*
	else
		echo "Unknown command: ${_CMD}"
		exit 1
	fi
}

