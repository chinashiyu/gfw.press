cd `dirname $0`

#gfw.press server configure -Xmn16G -Xms20G -Xmx20G
_java="java -Dfile.encoding=utf-8 -Dsun.jnu.encoding=utf-8 -Duser.timezone=Asia/Shanghai -Xmn1024M -Xms1280M -Xmx1280M -classpath `find ./lib/*.jar | xargs echo | sed 's/ /:/g'`:./bin"

_pack="press.gfw"

nohup $_java $_pack.Server >> server.log &
