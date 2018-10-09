#!/bin/sh
_BETTY_SCRIPT_BIN_DIR=$(dirname $0)
_BETTY_SCRIPT_BIN_DIR=$(cd $(dirname $0);pwd)
. ${_BETTY_SCRIPT_BIN_DIR}/betty.sh
betty_main ${_BETTY_SCRIPT_BIN_DIR} kill betty-example 

