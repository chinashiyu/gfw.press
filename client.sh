cd `dirname $0`

_java="java -Dfile.encoding=utf-8 -Dsun.jnu.encoding=utf-8 -Duser.timezone=Asia/Shanghai -Xms256M -Xmx256M -classpath `find ./lib/*.jar | xargs echo | sed 's/ /:/g'`:./bin"

_pack="press.gfw"

nohup $_java $_pack.Windows  >> client.log &
