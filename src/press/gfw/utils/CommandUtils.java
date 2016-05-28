package press.gfw.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * 
 * Copyright © Mritd. All rights reserved.
 *
 * @ClassName: CommandUtils
 * @Description: TODO 命令行执行工具
 * @author: mritd
 * @date: 2016年5月28日 下午11:13:55
 */
public class CommandUtils {
	
	private static Logger logger = Logger.getLogger(CommandUtils.class);
	
	/**
	 * 执行命令
	 * 
	 * @param commandLine
	 * @return String 执行结果
	 * @throws Exception
	 */
	public static String execute(String commandLine) throws Exception {
		
		logger.info("执行命令: "+commandLine);

		String[] cmd = null ;
		Properties props = System.getProperties();
		String osName = props.getProperty("os.name").toLowerCase();
		logger.debug("当前操作系统: "+osName);
		String charset = null;
		String result = "";

		if (osName.startsWith("windows")) {
			cmd = new String[3];
			cmd[0] = "cmd.exe";
			cmd[1] = "/C";
			cmd[2] = commandLine;
			charset = "GBK";
		} else if (osName.startsWith("linux")) {
			cmd = new String[2];
			cmd[0] = "bash";
			cmd[1] = commandLine;
			charset = "UTF-8";
		}
		
		logger.debug("最终执行数组: "+Arrays.toString(cmd));

		Process ps = Runtime.getRuntime().exec(cmd);
		String line = null;
		BufferedReader input = new BufferedReader(new InputStreamReader(ps.getInputStream(), charset));
		while ((line = input.readLine()) != null) {
			result += line + "\n";
		}
		input.close();
		ps.destroy();
		return result;
	}
}
