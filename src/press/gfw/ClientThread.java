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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Timestamp;

import javax.crypto.SecretKey;
import javax.net.SocketFactory;

/**
 *
 * GFW.Press客户端线程
 *
 * @author chinashiyu ( chinashiyu@gfw.press ; http://gfw.press )
 *
 */
public class ClientThread extends PointThread {

	private String serverHost = null;

	private int serverPort = 0;

	private SecretKey key = null;

	private Socket agentSocket = null;

	private Socket serverSocket = null;

	private int overN = 0;

	private InputStream agentIn = null;

	private OutputStream agentOut = null;

	private InputStream serverIn = null;

	OutputStream serverOut = null;

	public ClientThread(Socket agentSocket, String serverHost, int serverPort, SecretKey key) {

		this.agentSocket = agentSocket;

		this.serverHost = serverHost;

		this.serverPort = serverPort;

		this.key = key;

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

	private void close() {

		close(agentIn);

		close(serverOut);

		close(serverIn);

		close(agentOut);

		close(agentSocket);

		close(serverSocket);

	}

	private void close(Closeable o) {

		if (o == null) {

			return;

		}

		try {

			o.close();

		} catch (IOException e) {

		}

	}

	/**
	 * 打印信息
	 *
	 * @param o
	 */
	private void log(Object o) {

		String time = (new Timestamp(System.currentTimeMillis())).toString().substring(0, 19);

		System.out.println("[" + time + "] " + o.toString());

	}

	@Override
	public synchronized void over() {

		overN++;

		if (overN < 2) {

			return;

		}

		_sleep(OVER_TIMEOUT);

		close();

	}

	/**
	 * 启动客户端与服务器之间的转发线程，并对数据进行加密及解密
	 */
	@Override
	@SuppressWarnings("preview")
	public void run() {

		try {

			// 连接服务器
			serverSocket = SocketFactory.getDefault().createSocket();
			serverSocket.connect(new InetSocketAddress(serverHost, serverPort), CONN_TIMEOUT);

			serverSocket.setSoTimeout(SOCK_TIMEOUT);
			agentSocket.setSoTimeout(SOCK_TIMEOUT);

			serverSocket.setTcpNoDelay(true);
			agentSocket.setTcpNoDelay(true);

			// 打开 keep-alive
			serverSocket.setKeepAlive(true);
			agentSocket.setKeepAlive(true);

			// 获取输入输出流
			agentIn = agentSocket.getInputStream();
			agentOut = agentSocket.getOutputStream();

			serverIn = serverSocket.getInputStream();
			serverOut = serverSocket.getOutputStream();

		} catch (IOException ex) {

			log("连接服务器出错：" + serverHost + ":" + serverPort);

			close();

			return;

		}

		EncryptForwardThread forwardServer = new EncryptForwardThread(this, agentIn, serverOut, key);
		// startVirtualThread(forwardServer);
		forwardServer.start();

		DecryptForwardThread forwardAgent = new DecryptForwardThread(this, serverIn, agentOut, key);
		// startVirtualThread(forwardAgent);
		forwardAgent.start();

	}

}
