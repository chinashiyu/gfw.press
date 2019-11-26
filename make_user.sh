
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
for x in `for ((i=0;i<30;i++)); do expr $RANDOM % 20000 + 10000 ; done | sort -u`; do echo $x `pwgen -c -n -s -B 10 1`; done > /gfw.press/user.tx_ ;
