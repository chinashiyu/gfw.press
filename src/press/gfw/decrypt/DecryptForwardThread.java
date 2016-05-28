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
package press.gfw.decrypt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;

import press.gfw.server.PointThread;

/**
 * GFW.Press解密及转发线程
 * 
 * @author chinashiyu ( chinashiyu@gfw.press ; http://gfw.press )
 *
 */
public class DecryptForwardThread extends Thread {
	
	private static Logger logger = Logger.getLogger(DecryptForwardThread.class);

	private static final int BUFFER_SIZE_MAX = 1024 * 768; // 缓冲区可接受的最大值，768K

	private InputStream inputStream = null;

	private OutputStream outputStream = null;

	private PointThread parent = null;

	private Encrypt aes = null;

	private SecretKey key = null;

	/**
	 * 构造方法
	 * 
	 * @param parent
	 *          父线程
	 * @param inputStream
	 *          输入流
	 * @param outputStream
	 *          输出流
	 * 
	 */
	public DecryptForwardThread(PointThread parent, InputStream inputStream, OutputStream outputStream, SecretKey key) {

		this.parent = parent;

		this.inputStream = inputStream;

		this.outputStream = outputStream;

		this.key = key;

		aes = new Encrypt();

	}


	/**
	 * 解密转发
	 */
	public void run() {

		byte[] buffer = null;

		byte[] size_bytes = null;

		int[] sizes = null;

		byte[] decrypt_bytes = null;

		try {

			while (true) {

				buffer = new byte[Encrypt.ENCRYPT_SIZE];

				int read_num = inputStream.read(buffer);

				if (read_num == -1 || read_num != Encrypt.ENCRYPT_SIZE) {

					break;

				}

				size_bytes = aes.decrypt(key, buffer);

				if (size_bytes == null) {

					break; // 解密出错，退出

				}

				sizes = aes.getBlockSizes(size_bytes);

				if (sizes == null || sizes.length != 2 || sizes[0] > BUFFER_SIZE_MAX) {

					break;

				}

				int size_count = sizes[0] + sizes[1];

				buffer = new byte[size_count];

				int read_count = 0;

				while (read_count < size_count) {

					read_num = inputStream.read(buffer, read_count, size_count - read_count);

					if (read_num == -1) {

						break;

					}

					read_count += read_num;

				}

				if (read_count != size_count) {

					break;

				}

				if (sizes[1] > 0) { // 如果存在噪音数据

					byte[] _buffer = new byte[sizes[0]];

					System.arraycopy(buffer, 0, _buffer, 0, _buffer.length);

					decrypt_bytes = aes.decrypt(key, _buffer);

				} else {

					decrypt_bytes = aes.decrypt(key, buffer);

				}

				if (decrypt_bytes == null) {

					break;

				}

				outputStream.write(decrypt_bytes);

				outputStream.flush();

			}

		} catch (IOException ex) {
			
			logger.error("解密转发出错: ",ex);

		}

		buffer = null;

		size_bytes = null;

		sizes = null;

		decrypt_bytes = null;

		parent.over();

	}

}
