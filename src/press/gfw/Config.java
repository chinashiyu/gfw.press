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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.Hashtable;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * GFW.Press配置文件管理
 * 
 * @author chinashiyu ( chinashiyu@gfw.press ; http://gfw.press )
 *
 */
public class Config {
	
	private Logger logger = Logger.getLogger(Config.class);

	public static final String CHARSET = "utf-8";

	public static void main(String[] args) {

		Config c = new Config();

		Hashtable<String, String> users = c.getUser();

		System.out.println(users);

	}

	private File clientFile = null;

	private File serverFile = null;

	private File userFile = null;

	private long userFileTime = 0L;

	public Config() {

		clientFile = new File("client.json");

		serverFile = new File("server.json");

		userFile = new File("user.txt");

	}

	/**
	 * 获取客户端配置文件
	 * 
	 * @return
	 */
	public JSONObject getClientConfig() {

		return getJSON(read(clientFile));

	}

	/**
	 * 字符串转JSON对象
	 * 
	 * @param data
	 * @return
	 */
	public JSONObject getJSON(String data) {

		if (data == null || (data = data.trim()).length() == 0) {

			return null;

		}

		JSONParser p = new JSONParser();

		try {

			return (JSONObject) p.parse(data);

		} catch (ParseException ex) {

			logger.error("字符串转化 JSON 对象失败: ",ex);

		}

		return null;

	}

	/**
	 * 获取服务器配置
	 * 
	 * @return
	 */
	public JSONObject getServerConfig() {

		return getJSON(read(serverFile));

	}

	public Hashtable<String, String> getUser() {

		if (userFile.lastModified() == userFileTime) {

			return null;

		}

		userFileTime = userFile.lastModified();

		String text = read(userFile);

		if (StringUtils.isBlank(text)) {

			logger.warn("用户配置文件读取失败");
			return null;

		}

		String[] lines = text.trim().split("\n");

		text = null;

		if (lines == null || lines.length == 0) {
			
			logger.warn("用户配置文件为空");
			return null;

		}

		Hashtable<String, String> users = new Hashtable<String, String>(lines.length);

		for (int i = 0; i < lines.length; i++) {

			String[] cols = lines[i].trim().split(" ");

			if (cols == null || cols.length < 2 || !(cols[0] = cols[0].trim()).matches("\\d+") || (cols[cols.length - 1] = cols[cols.length - 1].trim()).length() < 8) {

				continue;

			}

			users.put(cols[0], cols[cols.length - 1]);

		}

		return users.size() > 0 ? users : null;

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

	/**
	 * 读文件内容
	 * 
	 * @param file
	 * @return
	 */
	public String read(File file) {

		int size = 0;

		if (file == null || !file.exists() || (size = (int) file.length()) == 0) {
			
			logger.error("待加载配置文件为空!");
			return null;

		}

		byte[] bytes = new byte[size];

		FileInputStream fis = null;

		int count = 0;

		try {

			fis = new FileInputStream(file);

			for (; size != count;) {

				int read = fis.read(bytes, count, size - count);

				if (read == -1) {

					break;

				}

				count += read;

			}

		} catch (IOException ex) {

			logger.error("读取 JSON 配置文件出现异常: ",ex);

			return null;

		} finally {

			try {

				fis.close();

			} catch (IOException ex) {
				logger.error("加载 JSON 配置文件出错，资源释放失败: ",ex);
			}

		}

		if (count != size) {

			logger.error("读取配置文件不完整，配置文件加载失败");
			return null;

		}

		try {

			return new String(bytes, CHARSET);

		} catch (UnsupportedEncodingException ex) {

			logger.error("文件内容转化字符串出错: ",ex);

		}

		return null;

	}

	/**
	 * 保存内容到文件
	 * 
	 * @param file
	 * @param text
	 * @return
	 */
	public boolean save(File file, String text) {

		if (file == null || text == null || (text = text.trim()).length() == 0) {

			return false;

		}

		FileOutputStream fos = null;

		try {

			fos = new FileOutputStream(file);

			fos.write(text.getBytes(CHARSET));

			fos.flush();

		} catch (IOException ex) {

			log("写文件出错：");

			ex.printStackTrace();

			return false;

		} finally {

			try {

				fos.close();

			} catch (IOException ex) {

			}

		}

		return true;

	}

	/**
	 * 保存客户端配置文件
	 * 
	 * @param json
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean saveClientConfig(JSONObject json) {

		if (json == null) {

			return false;

		}

		JSONObject _json = getClientConfig();

		if (_json == null) {

			_json = json;

		} else {

			_json.putAll(json);

		}

		return save(clientFile, _json.toJSONString());

	}

	/**
	 * 保存服务器配置文件
	 * 
	 * @param json
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean saveServerConfig(JSONObject json) {

		if (json == null) {

			return false;

		}

		JSONObject _json = getServerConfig();

		if (_json == null) {

			_json = json;

		} else {

			_json.putAll(json);

		}

		return save(serverFile, _json.toJSONString());

	}

}
