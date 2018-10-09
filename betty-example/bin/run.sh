#!/bin/sh
_BETTY_SCRIPT_BIN_DIR=$(cd $(dirname $0);pwd)
_BETTY_SCRIPT_JVM_ARGS="-Xms512m -Xmx2048m"
_BETTY_SCRIPT_APP_ARGS=""
. ${_BETTY_SCRIPT_BIN_DIR}/betty.sh
betty_main ${_BETTY_SCRIPT_BIN_DIR} run betty-example 0.0.1-SNAPSHOT 

