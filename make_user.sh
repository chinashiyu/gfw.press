
#
# 这个脚本生成100个用户的用户文件user.tx_，每次生成的密码是不相同的，请谨慎使用
#
# 每行第一列为端口号，第二列为密码
#
# 生成后，先查看一下，如果确定使用，覆盖现有的user.txt文件即可，命令：cp user.tx_ user.txt
#
yum install epel-release -y -q > /dev/null ;
yum install pwgen -y -q > /dev/null ;
cd `dirname $0` ;
for ((i=10001; i<10101; ++i)); do pwgen -n -s -B -c 10 | sed "s/^/$i /"; done > user.tx_ ;
