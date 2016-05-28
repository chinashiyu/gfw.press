/**
* 
*    GFW.Press
*    Copyright (C) 2016  chinashiyu ( chinashiyu@gfw.press ; http://gfw.press )
*
*    This program is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*    
**/
package press.gfw;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;

/**
 * 
 * GFW.Press服务器线程
 * 
 * @author chinashiyu ( chinashiyu@gfw.press ; http://gfw.press )
 *
 */
public class ServerThread extends PointThread {
	
	private static Logger logger = Logger.getLogger(ServerThread.class);

	private String proxyHost = null;

	private int proxyPort = 0;

	private Socket clientSocket = null;

	private Socket proxySocket = null;

	private SecretKey key = null;

	private boolean forwarding = false;

	public ServerThread(Socket clientSocket, String proxyHost, int proxyPort, SecretKey key) {

		this.clientSocket = clientSocket;

		this.proxyHost = proxyHost;

		this.proxyPort = proxyPort;

		this.key = key;

	}


	/**
	 * 关闭所有连接，此线程及转发子线程调用
	 */
	public synchronized void over() {

		try {

			if (proxySocket!=null) {
				proxySocket.close();
			}

			if (clientSocket!=null) {
				clientSocket.close();
			}

		} catch (Exception e) {
			logger.error("关闭 资源 失败: ",e);

		}

		if (forwarding) {

			forwarding = false;

		}

	}

	/**
	 * 启动服务器与客户端之间的转发线程，并对数据进行加密及解密
	 */
	public void run() {

		InputStream clientIn = null;

		OutputStream clientOut = null;

		InputStream proxyIn = null;

		OutputStream proxyOut = null;

		try {

			// 连接代理服务器
			logger.info("连接代理服务器,Host: "+proxyHost+" PORT: "+proxyPort);
			proxySocket = new Socket(proxyHost, proxyPort);

			// 设置3分钟超时
			logger.debug("设置超时3分钟...");
			proxySocket.setSoTimeout(180000);
			clientSocket.setSoTimeout(180000);

			// 打开 keep-alive
			logger.debug("开启 keep-alive ...");
			proxySocket.setKeepAlive(true);
			clientSocket.setKeepAlive(true);

			// 获取输入输出流
			logger.debug("获取客户端 I/O 流...");
			clientIn = clientSocket.getInputStream();
			clientOut = clientSocket.getOutputStream();

			logger.debug("发起心跳包测试...");
			proxySocket.sendUrgentData(0xFF);
			
			logger.debug("获取代理服务器 I/O 流...");
			proxyIn = proxySocket.getInputStream();
			proxyOut = proxySocket.getOutputStream();

		} catch (IOException ex) {

			logger.error("连接代理服务器出错：" + proxyHost + ":" + proxyPort,ex);

			over();

			return;

		}

		// 开始转发
		logger.debug("开始转发数据...");
		forwarding = true;

		if (clientSocket.isClosed()||clientSocket.isInputShutdown()||clientSocket.isOutputShutdown()) {
			logger.error("ClientSocket I/O Shutdown!");
			over();
			return;
		}
		
		if (proxySocket.isInputShutdown()||proxySocket.isOutputShutdown()) {
			logger.error("ProxySocket I/O Shutdown!");
			over();
			return;
		}
		
		DecryptForwardThread forwardProxy = new DecryptForwardThread(this, clientIn, proxyOut, key);
		forwardProxy.start();

		EncryptForwardThread forwardClient = new EncryptForwardThread(this, proxyIn, clientOut, key);
		forwardClient.start();

	}

}
