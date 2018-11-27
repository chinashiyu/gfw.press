# Linux系统中配合Chrome使用大杀器

### 客户端开启大杀器软件
  在`Linux`上使用大杀器，可以下载Java版本或者c语言版本，其中c语言版下载后需要编译（make），需要c语言版本的可以点[这里](https://github.com/chinashiyu/gfw.press.c)

  在`client.json`中修改节点地址（IP）、端口（port）以及连接密码，保存后；
  java版本在终端运行 `sh client.sh`，c语言版本终端运行`./gfw.press`即可

  java版本的运行地比较稳定，只是稍微内存占用高于C语言版本的
  
### Chrome端配置

Chrome端需要先安装Proxy SwitchyOmega或者Proxy SwitchySharp插件，但是chrome的扩展插件需要先去商店下载...
所以先修改linux系统的全局Proxy设置，下载好插件后，即可关闭全局proxy，配置插件即可。
**需要注意的是**：Chrome的一些版本在Linux下可能不支持Proxy SwitchyOmega，可以尝试使用Proxy SwitchySharp。


