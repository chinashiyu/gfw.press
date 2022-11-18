#!/bin/bash

cd `dirname $0`

export JAVA_HOME=./jdk

export JRE_HOME=$JAVA_HOME

export PATH=$JAVA_HOME/bin:$PATH

_java="java --enable-preview -Dfile.encoding=utf-8 -Dsun.jnu.encoding=utf-8 -Duser.timezone=Asia/Shanghai  -Xms512M -Xmx512M -classpath `find ./lib/*.jar | xargs echo | sed 's/ /:/g'`:./bin"

_pack="press.gfw"

kill -9 `jps -l |grep $_pack.Server |awk '{print $1}'` > /dev/null 2>&1

rm -f server.lock

nohup $_java $_pack.Server >> server.log 2>&1 &
