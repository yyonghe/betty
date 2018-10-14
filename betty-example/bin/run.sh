#!/bin/sh

_BIN_DIR=$(cd $(dirname $0);pwd)
_PWD_DIR=$(cd $_BIN_DIR;cd ..;pwd)
_CMD=run

. ${_BIN_DIR}/betty.sh

betty_main

