#!/bin/sh

_BIN_DIR=$(cd $(dirname $0);pwd)

. ${_BIN_DIR}/betty.sh

betty_main ${_BIN_DIR} kill 

