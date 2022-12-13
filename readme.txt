

欢迎使用 GFW.Press 新一代军用级高强度加密抗干扰网络数据高速传输软件


使用翻墙大杀器只需两步：


第一步、安装服务器：

	1、在境外创建一个 Ubuntu 20 / 22 云服务器

	2、以root用户登录执行一键安装命令：

		apt -y -q install wget ; wget -q -O gfw https://raw.githubusercontent.com/chinashiyu/gfw.press/master/ubuntu.sh.txt ; sh gfw

	3、查看配置和运行：

		节点地址就是云服务器的公网IP地址，可以使用 ifconfig 命令查看

		节点端口和连接密码在 /gfw.press/user.txt 文件里面，使用下面的命令查看

		cat  /gfw.press/user.txt

		手工重启大杀器服务器，使用下面的命令

		/gfw.press/server.sh

		手工停止大杀器服务器，使用下面的命令

		/gfw.press/stop.sh


第二步、安装客户端

	1、下载和安装

		  Windows版: 
		  https://github.com/chinashiyu/gfw.press/releases/download/Pack/GFW.Press.msi

		  Android版: 
		  https://github.com/chinashiyu/gfw.press/releases/download/Pack/GFW.Press.apk

	2、配置和运行

		  客户端安装完成后，填写好节点地址、节点端口和连接密码，就可以运行了

		  在 Windows 和部分 Android  手机，需要手工设置代理，地址为：127.0.0.1，端口为 3128


推特主页 https://twitter.com/chinashiyu


祝您好运

石斑鱼大爷 

2016-04-16 23:49:00


