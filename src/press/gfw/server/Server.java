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
package press.gfw.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.crypto.SecretKey;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import press.gfw.decrypt.Encrypt;
import press.gfw.utils.Config;

/**
 * 
 * GFW.Press服务器
 * 
 * @author chinashiyu ( chinashiyu@gfw.press ; http://gfw.press )
 *
 */
public class Server extends Thread {
	
	private static Logger logger = Logger.getLogger(Server.class);

	private File lockFile = null;

	private String proxyHost = "127.0.0.1"; // 默认为本机地址

	private int proxyPort = 3128; // 默认为HTTP代理标准端口

	private int listenPort = 0;

	private String password = null;

	private SecretKey key = null;

	private Encrypt encrypt = null;

	private boolean kill = false;

	private Config config = null;

	private ServerSocket serverSocket = null;

	/**
	 * 构造方法，主线程
	 */
	public Server() {

		logger.debug("创建锁文件 server.lock");
		lockFile = new File("server.lock");

		logger.debug("初始化配置文件");
		config = new Config();

		logger.info("加载配置文件");
		loadConfig(); // 获取配置参数

	}

	/**
	 * 构造方法，用户线程
	 * 
	 * @param proxyHost
	 * @param proxyPort
	 * @param listenPort
	 * @param password
	 */
	public Server(String proxyHost, int proxyPort, int listenPort, String password) {

		super();

		this.proxyHost = proxyHost;

		this.proxyPort = proxyPort;

		this.listenPort = listenPort;

		this.password = password;

		encrypt = new Encrypt();

		if (encrypt.isPassword(this.password)) {

			key = encrypt.getPasswordKey(this.password);

		}

	}

	/**
	 * 构造方法，用户线程
	 * 
	 * @param proxyHost
	 * @param proxyPort
	 * @param listenPort
	 * @param password
	 */
	public Server(String proxyHost, int proxyPort, String listenPort, String password) {

		this(proxyHost, proxyPort, (listenPort != null && (listenPort = listenPort.trim()).matches("\\d+")) ? Integer.valueOf(listenPort) : 0, password);

	}

	/**
	 * 主启动函数
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		Server server = new Server();

		server.service();

	}
	
	
	/**
	 * 暂停
	 * 
	 * @param m
	 */
	private void _sleep(long m) {

		try {

			sleep(m);

		} catch (InterruptedException ie) {

		}

	}

	/**
	 * 获取密码
	 * 
	 * @return
	 */
	public synchronized String getPassword() {

		return password;
	}

	/**
	 * @return the kill
	 */
	public synchronized boolean isKill() {

		return kill;

	}

	public synchronized void kill() {

		kill = true;

		if (serverSocket != null && !serverSocket.isClosed()) {

			try {

				serverSocket.close();

			} catch (IOException ex) {

			}

			serverSocket = null;

		}

	}

	/**
	 * 获取配置参数
	 */
	private void loadConfig() {

		JSONObject json = config.getServerConfig();

		if (json != null) {

			logger.info("加载监听地址和端口");
			
			String _proxyHost = (String) json.get("ProxyHost");

			proxyHost = StringUtils.isBlank(_proxyHost) ? proxyHost : _proxyHost;

			String _proxyPort = (String) json.get("ProxyPort");

			proxyPort = ( StringUtils.isBlank(_proxyPort) || !(_proxyPort = _proxyPort.trim()).matches("\\d+")) ? proxyPort : Integer.valueOf(_proxyPort);
		}

	}


	/**
	 * 用户线程
	 */
	public void run() {

		logger.info("监听端口：" + listenPort);

		if (encrypt == null || listenPort < 1024 || listenPort > 65536) {

			kill = true;

			logger.error("监听端口：" + listenPort + " 线程参数不符合条件，线程结束。");

			return;

		}

		try {

			serverSocket = new ServerSocket(listenPort);

		} catch (IOException ex) {

			kill = true;

			logger.error("监听端口：" + listenPort + " 线程启动时出错，线程结束：",ex);

			return;

		}

		while (!kill) {

			Socket clientSocket = null;

			try {

				clientSocket = serverSocket.accept();

			} catch (IOException ex) {

				if (kill) {

					break;

				}

				if (serverSocket != null && !serverSocket.isClosed()) {

					logger.error("监听端口：" + listenPort + " 线程运行时出错，暂停3秒钟后重试。",ex);

					_sleep(3000L);

					continue;

				} else {

					logger.error("监听端口：" + listenPort + " 线程运行时出错，线程结束。",ex);

					break;

				}

			}

			ServerThread serverThread = new ServerThread(clientSocket, proxyHost, proxyPort, key);

			serverThread.start();

		}

		kill = true;

		if (serverSocket != null && !serverSocket.isClosed()) {

			try {

				serverSocket.close();

			} catch (IOException ex) {
				logger.error("serverSocket 关闭失败: ",ex);
			}

			serverSocket = null;

		}

	}

	/**
	 * 主线程
	 */
	public void service() {

		if (System.currentTimeMillis() - lockFile.lastModified() < 30 * 000L) {
			
			logger.warn("服务器已经在运行中");

			logger.warn("如果确定没有运行，请删除 " + lockFile.getAbsolutePath() + "文件，重新启动");

			return;

		}

		try {

			logger.debug("创建锁文件");
			lockFile.createNewFile();

		} catch (IOException ioe) {
			logger.error("锁文件创建失败: ",ioe);
		}

		lockFile.deleteOnExit();

		logger.info("GFW.Press服务器开始运行......");

		logger.info("代理主机：" + proxyHost);

		logger.info("代理端口：" + proxyPort);

		Hashtable<String, String> users = null; // 用户

		Hashtable<String, Server> threads = new Hashtable<String, Server>(); // 用户线程

		while (true) {

			lockFile.setLastModified(System.currentTimeMillis());

			_sleep(20 * 1000L); // 暂停20秒

			logger.debug("开始加载用户列表...");
			users = config.getUser(); // 获取用户列表

			if (users == null || users.size() == 0) {

				continue;

			}
			logger.debug("加载完成...");

			Enumeration<String> threadPorts = threads.keys(); // 用户线程的所有端口

			while (threadPorts.hasMoreElements()) { // 删除用户及修改密码处理

				String threadPort = threadPorts.nextElement();

				String userPassword = users.remove(threadPort);

				if (userPassword == null) { // 用户已删除

					threads.remove(threadPort).kill();

					logger.warn("删除用户，端口：" + threadPort);

				} else {

					Server thread = threads.get(threadPort);

					if (!userPassword.equals(thread.getPassword())) { // 用户改密码

						logger.warn("修改密码，端口：" + threadPort);

						threads.remove(threadPort);

						thread.kill();

						thread = new Server(proxyHost, proxyPort, threadPort, userPassword);

						threads.put(threadPort, thread);

						thread.start();

					}

				}

			}

			Enumeration<String> userPorts = users.keys();

			while (userPorts.hasMoreElements()) { // 新用户

				String userPort = userPorts.nextElement();

				Server thread = new Server(proxyHost, proxyPort, userPort, users.get(userPort));

				threads.put(userPort, thread);

				thread.start();

			}

			users.clear();

		}

	}

}
