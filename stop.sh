#!/bin/bash

cd `dirname $0`

export JAVA_HOME=./jdk

export JRE_HOME=$JAVA_HOME

export PATH=$JAVA_HOME/bin:$PATH

_pack="press.gfw"

kill -9 `jps -l |grep $_pack.Server |awk '{print $1}'` > null 2>&1

rm -f server.lock
