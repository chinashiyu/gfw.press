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
package press.gfw.client;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.sql.Timestamp;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.json.simple.JSONObject;

import press.gfw.utils.Config;

/**
 * 
 * GFW.Press客户端图形界面
 * 
 * @author chinashiyu ( chinashiyu@gfw.press ; http://gfw.press )
 *
 */
public class Windows extends JFrame {

	/**
	 * 退出
	 */
	private class ButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent ae) {

			String command = ae.getActionCommand();

			if (command == null) {

				return;

			}

			switch (command) {

				case "退出":

					setVisible(false);

					if (tray != null && icon != null) {

						tray.remove(icon);

					}

					System.exit(0);

					break;

				case "确定":

					setVisible(false);

					boolean edit = false;

					if (!serverHost.equals(serverHostField.getText().trim())) {

						serverHost = serverHostField.getText().trim();

						edit = true;

					}

					if (!serverPort.equals(serverPortField.getText().trim())) {

						serverPort = serverPortField.getText().trim();

						edit = true;

					}

					String _password = new String(passwordField.getPassword()).trim();

					if (!password.equals(_password)) {

						password = _password;

						edit = true;

					}

					// if (!AES256CFB.isPassword(password)) {

					// passwordField.setBackground(Color.ORANGE);

					// passwordField.setToolTipText("密码需包含大小写字母和数字，至少八个字符。");

					// }

					if (!proxyPort.equals(proxyPortField.getText().trim())) {

						proxyPort = proxyPortField.getText().trim();

						edit = true;

					}

					if (edit) {

						saveConfig();

					}

					start();

					break;

				case "取消":

					setVisible(false);

					serverHostField.setText(serverHost);

					serverPortField.setText(serverPort);

					passwordField.setText(password);

					proxyPortField.setText(proxyPort);

					break;

			}

		}

	}

	/**
	 * 系统托盘
	 */
	private class TrayListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			toFront();

			setVisible(true);

		}

	}

	/**
	 * 主窗口
	 */
	private class WindowsListener implements WindowListener {

		@Override
		public void windowActivated(WindowEvent e) {

		}

		@Override
		public void windowClosed(WindowEvent e) {

		}

		@Override
		public void windowClosing(WindowEvent e) {

			setVisible(false);

		}

		@Override
		public void windowDeactivated(WindowEvent e) {

		}

		@Override
		public void windowDeiconified(WindowEvent e) {

		}

		@Override
		public void windowIconified(WindowEvent e) {

		}

		@Override
		public void windowOpened(WindowEvent e) {

		}

	}

	private static final long serialVersionUID = -7964262019916663094L;

	public static void main(String[] args) throws IOException {

		Windows windows = new Windows();

		windows.start();

	}

	private Client client = null;

	private SystemTray tray = null;

	private TrayIcon icon = null;

	private JButton exitButton = null;

	private JButton okButton = null;

	private JButton cancelButton = null;

	private Image logo = null;

	private Config config = null;

	private String serverHost = "", serverPort = "", password = "", proxyPort = "";

	private JTextField serverHostField = new JTextField(), serverPortField = new JTextField(), proxyPortField = new JTextField();

	private JPasswordField passwordField = new JPasswordField();

	public Windows() {

		super("GFW.Press");

		config = new Config();

		initTray();

		initWindows();

		initForm();

		initButton();

		initBorder();

		if (password.length() < 8) {

			setVisible(true);

		}

	}

	private void initBorder() {

		// 备用
		add(new JLabel(), BorderLayout.EAST);

		add(new JLabel(), BorderLayout.NORTH);

		add(new JLabel(), BorderLayout.WEST);

	}

	private void initButton() {

		ButtonListener buttonActionListener = new ButtonListener();

		// 按钮
		JPanel buttonPanel = new JPanel();

		buttonPanel.setPreferredSize(new Dimension(getWidth(), 60));

		exitButton = new JButton("退出");

		exitButton.addActionListener(buttonActionListener);

		okButton = new JButton("确定");

		okButton.addActionListener(buttonActionListener);

		cancelButton = new JButton("取消");

		cancelButton.addActionListener(buttonActionListener);

		buttonPanel.add(exitButton);

		buttonPanel.add(new JLabel("     "));

		buttonPanel.add(okButton);

		buttonPanel.add(new JLabel("     "));

		buttonPanel.add(cancelButton);

		add(buttonPanel, BorderLayout.SOUTH);

	}

	private void initForm() {

		// 服务面板
		JPanel serverPanel = new JPanel();

		GridLayout serverLayout = new GridLayout(4, 2, 0, 16);

		serverPanel.setLayout(serverLayout);

		// 服务信息

		serverPanel.add(new JLabel("节点地址："));

		serverPanel.add(serverHostField);

		serverPanel.add(new JLabel("节点端口："));

		serverPanel.add(serverPortField);

		serverPanel.add(new JLabel("连接密码："));

		serverPanel.add(passwordField);

		serverPanel.add(new JLabel("本地端口："));

		serverPanel.add(proxyPortField);

		loadConfig();

		add(serverPanel, BorderLayout.CENTER);

	}

	private void initTray() {

		logo = Toolkit.getDefaultToolkit().getImage("logo.png");

		setIconImage(logo);

		if (!SystemTray.isSupported()) {

			return;

		}

		icon = new TrayIcon(logo, null, null);

		icon.setImageAutoSize(true);

		icon.addActionListener(new TrayListener());

		tray = SystemTray.getSystemTray();

		try {

			tray.add(icon);

		} catch (AWTException ex) {

			log("添加系统托盘图标出错：");

			ex.printStackTrace();

		}

	}

	private void initWindows() {

		Dimension dimemsion = Toolkit.getDefaultToolkit().getScreenSize();

		setSize(480, 270);

		setLocation((int) (dimemsion.getWidth() - getWidth()) / 2, (int) (dimemsion.getHeight() - getHeight()) / 2);

		// setAlwaysOnTop(true);

		setResizable(false);

		addWindowListener(new WindowsListener());

		// 主窗口布局
		BorderLayout windowsLayout = new BorderLayout(20, 20);

		setLayout(windowsLayout);

	}

	private void loadConfig() {

		JSONObject json = config.getClientConfig();

		if (json != null) {

			serverHost = json.get("ServerHost") == null ? "" : (String) json.get("ServerHost");

			serverPort = json.get("ServerPort") == null ? "" : (String) json.get("ServerPort");

			password = json.get("Password") == null ? "" : (String) json.get("Password");

			proxyPort = json.get("ProxyPort") == null ? "" : (String) json.get("ProxyPort");

		}

		// 服务信息
		serverHostField.setText(serverHost);

		serverPortField.setText(serverPort);

		passwordField.setText(password);

		proxyPortField.setText(proxyPort);

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

	@SuppressWarnings("unchecked")
	private void saveConfig() {

		JSONObject json = new JSONObject();

		json.put("ServerHost", serverHost);

		json.put("ServerPort", serverPort);

		json.put("Password", password);

		json.put("ProxyPort", proxyPort);

		config.saveClientConfig(json);

	}

	public void start() {

		if (client != null && !client.isKill()) {

			if (serverHost.equals(client.getServerHost()) && serverPort.equals(String.valueOf(client.getServerPort())) && password.equals(client.getPassword()) && proxyPort.equals(String.valueOf(client.getListenPort()))) {

				return;

			} else {

				client.kill();

			}

		}

		client = new Client(serverHost, serverPort, password, proxyPort);

		client.start();

		// log(client.getName());

	}

}
