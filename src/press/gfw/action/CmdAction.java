package press.gfw.action;


import org.apache.log4j.Logger;

import press.gfw.client.CmdClient;
import press.gfw.server.Server;
import press.gfw.utils.CommandUtils;

/**
 * 
 * Copyright © Mritd. All rights reserved.
 *
 * @ClassName: CmdAction
 * @Description: TODO 命令行操作统一入口
 * @author: mritd
 * @date: 2016年5月28日 下午11:16:43
 */
public class CmdAction {
	
	private static Logger logger = Logger.getLogger(CmdAction.class);
	
	public static void main(String[] args) {
		if (args==null||args.length==0) {
			logger.error("请输入执行命令,可选命令如下\nserver: 启动服务端\nclient: 启动客户端\nonline: 统计在线人数");
			return;
		}
		
		if (args.length!=1) {
			logger.error("执行参数有误,可选命令如下\nserver: 启动服务端\nclient: 启动客户端\nonline: 统计在线人数");
			return;
		}
		
		switch (args[0]) {
		
			case "server":
				Server.main(null);
				break;
				
			case "client":
				CmdClient.main(null);
				break;
				
			case "online":
				try {
					String result = CommandUtils.execute("online.sh");
					logger.info(result);
				} catch (Exception e) {
					logger.error("统计在线人数失败: ",e);
				}
				break;
			default:
				logger.error("不支持该命令!");
				break;
		}
		
	}

}
