
欢迎使用 GFW.Press 新一代军用级高强度加密抗干扰网络数据高速传输软件


一、客户端

请(翻墙)下载客户端安装包

Windows 版: 
http://gfw.press/GFW.Press.msi

Android 版: 
http://gfw.press/GFW.Press.apk

或者使用 eMule 下载

Windows 版: 

ed2k://|file|[GFW.Press][%E7%BF%BB%E5%A2%99][%E5%A4%A7%E6%9D%80%E5%99%A8][Windows%E7%89%88][MD5][3ac2d799280bd88156844dab9b6dd9ae].msi|51766272|4B3126EBF941F8D448678F3F54C3244F|h=STP3OBYXXH2PGKQVHA3VMM2MWFGNLHCA|/

Android 版: 

ed2k://|file|[GFW.Press][%E7%BF%BB%E5%A2%99][%E5%A4%A7%E6%9D%80%E5%99%A8][Android%E7%89%88][MD5][9d65698139f2167aafd31c0dd754bd2f].apk|1329970|669ECE2ADA9430DAFE6A9A92B465868B|h=QD4OYU34QLLHYNWIY7EXSDVPB35YK2Q6|/

请(翻墙)访问 http://gfw.press 获取连接配置信息

配置填写完成，点击“确定”按钮

设置浏览器代理，地址： 127.0.0.1  端口： 3128


二、服务器

以 CentOS 为例:

第一步：下载 gfw.press

cd / && git clone https://github.com/chinashiyu/gfw.press.git ;

第二步：安装 JDK

yum install java-1.8.0-openjdk -y ;

第三步：安装代理软件

yum install squid -y ; 
或者 
yum install 3proxy -y ;

第四步：修改连接帐号文件user.txt

每行表示一个帐号，由 端口号+空格+密码 组成，密码长度至少8位，必需包含大小写字母和数字

第五步：修改脚本可执行属性

chmod u+x /gfw.press/server.sh ;
chmod u+x /gfw.press/stop.sh ;

第六步：运行

/gfw.press/server.sh ;

第七步：停止

/gfw.press/stop.sh ;


赞助捐款帐号(PayPal 或 Skrill)： donate@gfw.press 

所获捐款主要用于吃喝嫖赌，偶尔用于购买服务器和带宽


请访问 http://twitter.com/chinashiyu 发表意见及建议

祝您好运

chinashiyu 

2016-04-16 23:49:00
2016-12-05 11:44:00
2016-12-05 22:00:00


