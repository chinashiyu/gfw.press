echo ;
echo ;
echo 正在安装 GFW.Press 服务器，请稍候.......... ;
echo ;

cd / ;
yum install java-1.8.0-openjdk-devel  -y -q > /dev/null ;
yum install squid -y -q > /dev/null ;
if [ -e "/etc/squid/squid.conf" ] ; then
        sed -i "s/shutdown_lifetime 3 seconds//g" /etc/squid/squid.conf ;
        sed -i "s/access_log none//g" /etc/squid/squid.conf ;
        sed -i "s/cache_log \/dev\/null//g" /etc/squid/squid.conf ;
        sed -i "s/logfile_rotate 0//g" /etc/squid/squid.conf ;
        sed -i "s/cache deny all//g" /etc/squid/squid.conf ;
        echo "shutdown_lifetime 3 seconds" >> /etc/squid/squid.conf ;
        echo "access_log none" >> /etc/squid/squid.conf ;
        echo "cache_log /dev/null" >> /etc/squid/squid.conf ;
        echo "logfile_rotate 0" >> /etc/squid/squid.conf ;
        echo "cache deny all" >> /etc/squid/squid.conf ;
fi;
chkconfig squid on > /dev/null ;
service squid restart > /dev/null ;
yum install git  -y -q > /dev/null ;

if [ -e "/gfw.press" ] ; then

        echo "目录 /gfw.press 已经存在，安装退出" ;
        echo ;
        echo "如需重新安装，请先把目录 /gfw.press 改名或删除" ;

else

        git clone https://github.com/chinashiyu/gfw.press.git ;

        chmod a+x /gfw.press/server.sh ;
        chmod a+x /gfw.press/stop.sh ;
        chmod a+x /gfw.press/make_user.sh ;

        /gfw.press/make_user.sh ;
        rm -f /gfw.press/user.txt ;
        cp /gfw.press/user.tx_ /gfw.press/user.txt ;

        declare -i _mem ;
        _mem=`free -m |grep Mem |awk '{print $2}'` ;
        if (($_mem<600)); then
                sed -i "s/-Xms512M/-Xms256M/g" /gfw.press/server.sh ;
                sed -i "s/-Xmx512M/-Xmx256M/g" /gfw.press/server.sh ;
        fi ;
        if (($_mem>1800)); then
                sed -i "s/-Xms512M/-Xms1024M/g" /gfw.press/server.sh ;
                sed -i "s/-Xmx512M/-Xmx1024M/g" /gfw.press/server.sh ;
        fi ;

        iptables -F ;
        iptables -A INPUT -p tcp --dport 22 -j ACCEPT ;
        iptables -A INPUT -p tcp --dport 80 -j ACCEPT ;
        iptables -A INPUT -p tcp --dport 443 -j ACCEPT ;
        iptables -A INPUT -p tcp --dport 25 -j ACCEPT ;
        iptables -A INPUT -p tcp --dport 110 -j ACCEPT ;
        iptables -A INPUT -p tcp --dport 10000:10100 -j ACCEPT ;
        iptables -P INPUT DROP ;
        iptables -P FORWARD DROP ;
        iptables -P OUTPUT ACCEPT ;
        iptables -A INPUT -i lo -j ACCEPT ;
        iptables -A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT ;
        iptables -A INPUT -p icmp -j ACCEPT ;
        /sbin/service iptables save ;
        /sbin/service iptables restart ;

        echo ;
        echo "已完成 GFW.Press 服务器安装" ;
        echo ;
        echo "端口和密码在文件 /gfw.press/user.txt" ;
        echo ;
        echo "启动服务器请执行 /gfw.press/server.sh " ;

fi;

echo ;
echo ;

