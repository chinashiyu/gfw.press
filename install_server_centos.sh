#!/bin/bash

function checkSystem() {

	RELEASE=8

        if [ -e /etc/redhat-release ]; then
		for ((i=$RELEASE; i<100; i++)); do
			OK=$(grep "release $i." /etc/redhat-release)
			if [ ! "$OK" == "" ]; then
				return 0
			fi
		done
	fi
	
	echo
	echo "系统不符合安装要求！请使用 CentOS$RELEASE 或者更高版本的系统"
	echo

	exit 1

}

checkSystem

function checkUser() {

        if [ "$EUID" -eq 0 ]; then
                return 0
        fi

	echo
	echo "用户权限不符合安装要求！请使用 root 用户进行安装"
	echo

	exit 1

}

checkUser

function installSystem() {

	# echo -n "▋"
	# yum -q -y update > /dev/null 2>&1

	echo -n "▋"
	yum -q -y install epel-release elrepo-release > /dev/null 2>&1

	# echo -n "▋"
	# yum -q -y config-manager --set-enabled PowerTools > /dev/null 2>&1

	echo -n "▋"
	yum -q -y install wget iptraf-ng net-tools bind-utils langpacks-zh_CN pwgen tuned firewalld > /dev/null 2>&1

}

function setupSystem() {

	echo -n "▋"
	setenforce 0 > /dev/null 2>&1
	sed -i 's/^SELINUX=.*/SELINUX=disabled/g' /etc/selinux/config

	echo -n "▋"
	timedatectl set-timezone Asia/Shanghai > /dev/null 2>&1

	echo -n "▋"
	if [ ! -f /swap ] && [ "$(grep SwapTotal /proc/meminfo | awk '{print $2}')" == "0" ]; then
		dd if=/dev/zero of=/swap bs=2048 count=1048576  > /dev/null 2>&1
		chmod 600 /swap > /dev/null 2>&1
		mkswap /swap > /dev/null 2>&1
		swapon /swap > /dev/null 2>&1
		echo /swap swap swap defaults 0 0 >> /etc/fstab
	fi

	echo -n "▋"
	echo 'LANG="zh_CN.UTF-8"' > /etc/locale.conf
	echo 'LC_ALL="zh_CN.UTF-8"' >> /etc/locale.conf
	source /etc/locale.conf > /dev/null 2>&1

	echo -n "▋"
	echo > /etc/security/limits.d/99-perf.conf
	echo '* soft nproc 65536' >> /etc/security/limits.d/99-perf.conf
	echo '* hard nproc 65536' >> /etc/security/limits.d/99-perf.conf
	echo '* soft nofile 65536' >> /etc/security/limits.d/99-perf.conf
	echo '* hard nofile 65536' >> /etc/security/limits.d/99-perf.conf

	echo -n "▋"
	echo > /etc/sysctl.d/99-perf.conf
	#
	echo 'vm.swappiness = 10' >> /etc/sysctl.d/99-perf.conf
	echo 'net.ipv4.tcp_congestion_control = bbr' >> /etc/sysctl.d/99-perf.conf
	echo 'net.core.default_qdisc = fq' >> /etc/sysctl.d/99-perf.conf
	echo 'net.ipv4.ip_forward = 1' > /etc/sysctl.d/99-perf.conf
	echo 'net.core.somaxconn = 4096' >> /etc/sysctl.d/99-perf.conf
	echo 'net.core.netdev_max_backlog = 1048576' >> /etc/sysctl.d/99-perf.conf

	echo 'net.core.rmem_default = 262144' >> /etc/sysctl.d/99-perf.conf
	echo 'net.core.rmem_max = 33554432' >> /etc/sysctl.d/99-perf.conf
	echo 'net.core.wmem_default = 262144' >> /etc/sysctl.d/99-perf.conf
	echo 'net.core.wmem_max = 2097152' >> /etc/sysctl.d/99-perf.conf
	echo 'net.ipv4.tcp_mem = 8192 16384 32768' >> /etc/sysctl.d/99-perf.conf
	echo 'net.ipv4.tcp_rmem = 8192 131072 4194304' >> /etc/sysctl.d/99-perf.conf
	echo 'net.ipv4.tcp_wmem = 8192 131072 4194304' >> /etc/sysctl.d/99-perf.conf
	echo 'net.ipv4.udp_mem = 16384 32768 65536' >> /etc/sysctl.d/99-perf.conf
	echo 'net.ipv4.udp_rmem_min = 8192' >> /etc/sysctl.d/99-perf.conf
	echo 'net.ipv4.udp_wmem_min = 8192' >> /etc/sysctl.d/99-perf.conf

	#
	echo 'net.ipv4.tcp_syncookies = 1' >> /etc/sysctl.d/99-perf.conf
	echo 'net.ipv4.tcp_max_syn_backlog = 4096' >> /etc/sysctl.d/99-perf.conf
	echo 'net.ipv4.tcp_synack_retries = 3' >> /etc/sysctl.d/99-perf.conf

	sysctl -q --system > /dev/null 2>&1

	echo -n "▋"
	systemctl -q enable tuned > /dev/null 2>&1
	systemctl -q start tuned > /dev/null 2>&1
	tuned-adm profile network-latency > /dev/null 2>&1

	echo -n "▋"
	systemctl -q enable firewalld > /dev/null 2>&1
	systemctl -q start firewalld > /dev/null 2>&1

	echo -n "▋"
	if [ "$(firewall-cmd --permanent --query-service=ssh)" == "no" ]; then firewall-cmd -q --permanent --add-service=ssh > /dev/null 2>&1 ; fi
	if [ "$(firewall-cmd --permanent --query-masquerade)" == "no" ]; then firewall-cmd -q --permanent --add-masquerade > /dev/null 2>&1 ; fi

	echo -n "▋"
	firewall-cmd -q --reload > /dev/null 2>&1

}

SOFTWRE_HOME=/gfw.press

function installSoftware() {

	# echo -n "▋"
	# yum install -y -q httpd mod_ssl php php-* > /dev/null 2>&1

	# echo -n "▋"
	# yum install -y -q mysql-server > /dev/null 2>&1

	echo -n "▋"
	yum install -y -q java-latest-openjdk-devel git squid > /dev/null 2>&1

	echo -n "▋"
        cd /  > /dev/null 2>&1
        git clone -q https://github.com/chinashiyu/gfw.press.git > /dev/null 2>&1
	cd  > /dev/null 2>&1

}

function setupSoftware() {

	echo -n "▋"
        chmod +x /gfw.press/server.sh  > /dev/null 2>&1
        chmod +x /gfw.press/stop.sh  > /dev/null 2>&1

        for x in $(for ((i=0;i<30;i++)); do expr $RANDOM % 20000 + 10000 ; done | sort -u); do echo $x $(pwgen -c -n -s -B 10 1); done > /gfw.press/user.tx_
        rm -f /gfw.press/user.txt  > /dev/null 2>&1
        cp /gfw.press/user.tx_ /gfw.press/user.txt  > /dev/null 2>&1

        declare -i _mem
        _mem=$(free -m |grep Mem |awk '{print $2}')
        if (($_mem<600)); then
                sed -i "s/-Xms512M/-Xms256M/g" /gfw.press/server.sh
                sed -i "s/-Xmx512M/-Xmx256M/g" /gfw.press/server.sh
        fi
        if (($_mem>1800)); then
                sed -i "s/-Xms512M/-Xms1024M/g" /gfw.press/server.sh
                sed -i "s/-Xmx512M/-Xmx1024M/g" /gfw.press/server.sh
        fi

        sed -i "s/\/gfw.press\/server.sh//g" /etc/rc.d/rc.local
        echo "/gfw.press/server.sh" >>  /etc/rc.d/rc.local
        chmod +x /etc/rc.d/rc.local	

	echo -n "▋"
	if [ -e "/etc/squid/squid.conf" ] && [ "$(grep 'shutdown_lifetime 3 seconds' /etc/squid/squid.conf)" == "" ]; then
		echo "" >> /etc/squid/squid.conf
		echo "shutdown_lifetime 3 seconds" >> /etc/squid/squid.conf
		echo "access_log none" >> /etc/squid/squid.conf
		echo "cache_log /dev/null" >> /etc/squid/squid.conf
		echo "logfile_rotate 0" >> /etc/squid/squid.conf
		echo "cache deny all" >> /etc/squid/squid.conf
		echo "cache_mem 0 MB" >> /etc/squid/squid.conf
		echo "maximum_object_size_in_memory 0 KB" >> /etc/squid/squid.conf
		echo "memory_cache_mode disk" >> /etc/squid/squid.conf
		echo "memory_cache_shared off" >> /etc/squid/squid.conf
		echo "memory_pools off" >> /etc/squid/squid.conf
		echo "memory_pools_limit 0 MB" >> /etc/squid/squid.conf
		echo "acl NCACHE method GET" >> /etc/squid/squid.conf
		echo "no_cache deny NCACHE" >> /etc/squid/squid.conf
		echo "acl flash rep_mime_type application/x-shockwave-flash" >> /etc/squid/squid.conf
		echo "http_reply_access deny flash" >> /etc/squid/squid.conf
	fi

	echo -n "▋"
	systemctl -q enable squid > /dev/null 2>&1
	systemctl -q start squid > /dev/null 2>&1

	echo -n "▋"
	if [ "$(firewall-cmd --permanent --query-port=10000-30000/tcp)" == "no" ]; then firewall-cmd -q --permanent --add-port=10000-30000/tcp > /dev/null 2>&1 ; fi

	echo -n "▋"
	firewall-cmd -q --reload > /dev/null 2>&1

	/gfw.press/server.sh

}

function removeSoftware() {

	echo -n "▋"
	sed -i "s/\/gfw.press\/server.sh//g" /etc/rc.d/rc.local
	/gfw.press/stop.sh
	rm -f -r "/gfw.press" > /dev/null 2>&1

	echo -n "▋"
	systemctl -q stop squid > /dev/null 2>&1
	systemctl -q disable squid > /dev/null 2>&1

	echo -n "▋"
	if [ "$(firewall-cmd --permanent --query-port=10000-30000/tcp)" == "yes" ]; then firewall-cmd -q --permanent --remove-port=10000-30000/tcp > /dev/null 2>&1 ; fi

	echo -n "▋"
	firewall-cmd -q --reload > /dev/null 2>&1
	
}

function echoHelp() {

        echo "恭喜你！已成功安装并启动翻墙大杀器服务"
        echo
        echo "查看端口密码请执行 cat /gfw.press/user.txt "
        echo
        echo "重新启动服务请执行 /gfw.press/server.sh "

}

function displayMenu () {

        clear
        echo
        echo "系统已经安装翻墙大杀器服务，你可以："
        echo

        echo "   1) 删除翻墙大杀器服务并重新安装"
        echo "   2) 删除翻墙大杀器服务"
        echo "   3) 退出"

        echo

        until [[ "$MENU_NO" =~ ^[1-3]$ ]]; do
                read -rp "请输入一个序号 [1-3]: " MENU_NO
        done

        case $MENU_NO in
                1)
                        echo
                        read -rp "确定删除翻墙大杀器服务并重新安装? [yes/no]: " -e  OK
                        if [[ "$OK" = 'yes' ]]; then
                                echo
                                echo
                                echo "正在重新安装翻墙大杀器服务，请稍候 ..."
                                echo
				removeSoftware
				installSystem
				setupSystem
				installSoftware
				setupSoftware
                                echo
                                echo
                                echoHelp
                                echo
                        fi
                        echo
                ;;
                2)
                        echo
                        read -rp "确定删除翻墙大杀器服务? [yes/no]: " -e  OK
                        if [[ "$OK" = 'yes' ]]; then
                                echo
                                echo
                                echo "正在删除翻墙大杀器服务，请稍候 ..."
                                echo
                                removeSoftware
                                echo
                                echo
                                echo "已经成功删除翻墙大杀器服务"
                                echo
                        fi
                        echo
                ;;
                3)
                        echo
                        exit 0
                ;;
        esac
}

if [[ -e $SOFTWRE_HOME ]]; then

        displayMenu

else

        echo
        echo
        echo "正在安装翻墙大杀器服务，请稍候 ..."
        echo
	installSystem
	setupSystem
	installSoftware
	setupSoftware
        echo
        echo
        echoHelp
        echo
        echo

fi

# PUBLIC_IP=$(dig TXT +short o-o.myaddr.l.google.com @ns1.google.com | tr -d '"*[:space:]*')

