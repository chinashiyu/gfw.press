package press.gfw.client;

import org.json.simple.JSONObject;

import press.gfw.utils.Config;

/**
 * 
 * Copyright © Mritd. All rights reserved.
 *
 * @ClassName: CmdClient
 * @Description: TODO 命令行客户端
 * @author: mritd
 * @date: 2016年5月28日 下午10:34:40
 */
public class CmdClient {
	
	/**
	 * 
	 * @Title: main
	 * @Description: TODO 主方法
	 * @param args
	 * @return: void
	 */
	public static void main(String[] args) {
		
		// 服务器 IP
		String serverHost = "";
		// 服务器端口
		String serverPort = "";
		// 服务器密码
		String password = "";
		// 本地代理端口
		String proxyPort ="";
		
		JSONObject json = new Config().getClientConfig();

		if (json != null) {

			serverHost = json.get("ServerHost") == null ? "" : (String) json.get("ServerHost");

			serverPort = json.get("ServerPort") == null ? "" : (String) json.get("ServerPort");

			password = json.get("Password") == null ? "" : (String) json.get("Password");

			proxyPort = json.get("ProxyPort") == null ? "" : (String) json.get("ProxyPort");

		}
		
		Client client = new Client(serverHost, serverPort, password, proxyPort);

		client.start();
	}
}
