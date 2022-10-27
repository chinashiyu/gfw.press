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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * GFW.Press加密及解密管理
 *
 * @author chinashiyu ( chinashiyu@gfw.press ; http://gfw.press )
 *
 */
public class Encrypt {

	public static final String CHARSET = "UTF-8";

	public static final int BLOCK_MAX_FILE = 64 * 1024 * 1024; // 64MB，被加密数据块的字节最大长度，用于文件

	public static final int ENCRYPT_SIZE = 30; // 加密数据长度值加密后的字节长度，固定30个字节，解密后固定14个字节

	public static final int IV_SIZE = 16; // IV字节长度，16

	public static final int NOISE_MAX = 1024 * 4; // 噪音数据最大长度，4K

	public static void main(String[] args) {

		Encrypt aes = new Encrypt();

		// 文件加密测试
		// aes.testEncryptFile();

		// 测试
		// aes.testSecureRandom();

		aes.testIsPassword();

	}

	private SecureRandom secureRandom = null;

	private Cipher cipher = null;

	private KeyGenerator keyGenerator = null;

	public Encrypt() {

		super();

		secureRandom = new SecureRandom();

		try {

			cipher = Cipher.getInstance("AES/CFB/NoPadding"); // Advanced Encryption Standard - Cipher Feedback Mode - No Padding

			keyGenerator = KeyGenerator.getInstance("AES");

		} catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {

			throw new RuntimeException(ex);

		}

	}

	/**
	 * 解密
	 *
	 * @param key           SecretKey
	 * @param encrypt_bytes 头部包含16字节IV的加密数据
	 *
	 * @return 解密数据
	 *
	 */
	public byte[] decrypt(SecretKey key, byte[] encrypt_bytes) {

		if (key == null || encrypt_bytes == null || encrypt_bytes.length < IV_SIZE) {

			return null;

		}

		byte[] IV = new byte[IV_SIZE];

		byte[] part2 = new byte[encrypt_bytes.length - IV_SIZE];

		System.arraycopy(encrypt_bytes, 0, IV, 0, IV.length);

		System.arraycopy(encrypt_bytes, IV.length, part2, 0, part2.length);

		return decrypt(key, part2, IV);

	}

	/**
	 * 解密
	 *
	 * @param key         SecretKey
	 * @param cipher_data 加密数据
	 * @param IV          IV
	 *
	 * @return 解密数据
	 *
	 */
	public byte[] decrypt(SecretKey key, byte[] cipher_data, byte[] IV) {

		if (key == null || cipher_data == null || cipher_data.length == 0 || IV == null || IV.length == 0) {

			return null;

		}

		IvParameterSpec IVSpec = new IvParameterSpec(IV);

		try {

			cipher.init(Cipher.DECRYPT_MODE, key, IVSpec);

		} catch (InvalidKeyException | InvalidAlgorithmParameterException ex) {

			log("初始化Cipher出错：");

			ex.printStackTrace();

			return null;

		}

		try {

			return cipher.doFinal(cipher_data);

		} catch (IllegalBlockSizeException | BadPaddingException ex) {

			log("加密数据出错：");

			ex.printStackTrace();

			return null;

		}

	}

	/**
	 * 解密文件
	 *
	 * @param key  SecretKey
	 * @param src  加密的文件
	 * @param dest 解密后的文件
	 * @return 解密是否成功
	 */
	public boolean decryptFile(SecretKey key, File src, File dest) {

		if (src == null || !src.exists() || src.isDirectory() || !src.canRead() || dest == null) {

			return false;

		}

		long len = src.length(); // 文件大小

		if (len < ENCRYPT_SIZE) {

			return false;

		}

		byte[] encrypt_size_bytes = null;

		byte[] encrypt_block_bytes = null;

		InputStream bis = null;

		OutputStream bos = null;

		boolean close = true;

		try {

			bis = new BufferedInputStream(new FileInputStream(src));

			bos = new BufferedOutputStream(new FileOutputStream(dest));

			for (;;) {

				encrypt_size_bytes = new byte[ENCRYPT_SIZE];

				int read_size = bis.read(encrypt_size_bytes); // 读数据

				if (read_size == -1) {

					break;

				}

				if (read_size != encrypt_size_bytes.length) {

					return false;

				}

				byte[] size_bytes = decrypt(key, encrypt_size_bytes);

				if (size_bytes == null) {

					return false;

				}

				int block_size = getBlockSize(size_bytes);

				if (block_size == 0) {

					return false;

				}

				encrypt_block_bytes = new byte[block_size];

				for (int read_count = 0; read_count < block_size;) {

					read_size = bis.read(encrypt_block_bytes, read_count, block_size - read_count); // 读数据

					if (read_size == -1) {

						return false;

					}

					read_count += read_size;

				}

				byte[] block_bytes = decrypt(key, encrypt_block_bytes); // 解密数据

				if (block_bytes == null) {

					return false;

				}

				bos.write(block_bytes);

			}

		} catch (IOException ex) {

			log("解密文件出错：");

			ex.printStackTrace();

			return false;

		} finally {

			if (bos != null) { // 关闭输出流

				try {

					bos.close();

				} catch (IOException ex) {

					log("关闭输出流出错：");

					ex.printStackTrace();

					close = false;

				}

			}

			if (bis != null) { // 关闭输入流

				try {

					bis.close();

				} catch (IOException ex) {

					log("关闭输入流出错：");

					ex.printStackTrace();

					close = false;

				}

			}

			encrypt_block_bytes = null;

		}

		if (!close || !dest.exists() || dest.length() < len) {

			return false;

		}

		return true;

	}

	/**
	 * 加密
	 *
	 * @param key  SecretKey
	 * @param data 数据
	 *
	 * @return 加密数据
	 *
	 */
	public byte[] encrypt(SecretKey key, byte[] data) {

		if (key == null || data == null) {

			return null;

		}

		byte[] IV = getSecureRandom(IV_SIZE);

		IvParameterSpec IVSpec = new IvParameterSpec(IV);

		try {

			cipher.init(Cipher.ENCRYPT_MODE, key, IVSpec);

		} catch (InvalidKeyException | InvalidAlgorithmParameterException ex) {

			log("初始化Cipher出错：");

			ex.printStackTrace();

			return null;

		}

		byte[] cipher_bytes = null;

		try {

			cipher_bytes = cipher.doFinal(data);

		} catch (IllegalBlockSizeException | BadPaddingException ex) {

			log("加密数据出错：");

			ex.printStackTrace();

			return null;

		}

		byte[] iv_cipher_bytes = new byte[cipher_bytes.length + IV_SIZE];

		System.arraycopy(IV, 0, iv_cipher_bytes, 0, IV.length);

		System.arraycopy(cipher_bytes, 0, iv_cipher_bytes, IV.length, cipher_bytes.length);

		return iv_cipher_bytes;

	}

	/**
	 * 加密文件
	 *
	 * @param key  SecretKey
	 * @param src  原文件
	 * @param dest 加密文件
	 *
	 * @return 加密是否成功
	 *
	 */
	public boolean encryptFile(SecretKey key, File src, File dest) {

		if (src == null || !src.exists() || src.isDirectory() || !src.canRead() || dest == null) {

			return false;

		}

		long len = src.length(); // 文件大小

		if (len == 0L) {

			return false;

		}

		byte[] block_bytes = new byte[BLOCK_MAX_FILE];

		byte[] read_bytes = null;

		InputStream bis = null;

		OutputStream bos = null;

		boolean close = true;

		try {

			bis = new BufferedInputStream(new FileInputStream(src));

			bos = new BufferedOutputStream(new FileOutputStream(dest));

			for (;;) {

				int read_size = bis.read(block_bytes); // 读数据

				if (read_size == -1) {

					break;

				}

				read_bytes = new byte[read_size];

				System.arraycopy(block_bytes, 0, read_bytes, 0, read_size); // 复制实际读到的数据

				byte[] cipher_bytes = encrypt(key, read_bytes); // 加密数据

				if (cipher_bytes == null) {

					return false;

				}

				byte[] size_cipher = encrypt(key, getBlockSizeBytes(cipher_bytes.length)); // 加密数据长度加密，24个字节

				if (size_cipher == null) {

					return false;

				}

				bos.write(size_cipher);

				bos.write(cipher_bytes);

			}

		} catch (IOException ex) {

			log("加密文件出错：");

			ex.printStackTrace();

			return false;

		} finally {

			if (bos != null) { // 关闭输出流

				try {

					bos.close();

				} catch (IOException ex) {

					log("关闭输出流出错：");

					ex.printStackTrace();

					close = false;

				}

			}

			if (bis != null) { // 关闭输入流

				try {

					bis.close();

				} catch (IOException ex) {

					log("关闭输入流出错：");

					ex.printStackTrace();

					close = false;

				}

			}

			block_bytes = null;

			read_bytes = null;

		}

		if (!close || !dest.exists() || dest.length() < len) {

			return false;

		}

		return true;

	}

	/**
	 * 加密网络数据
	 *
	 * @param key   SecretKey
	 *
	 * @param bytes 原始数据
	 *
	 * @return [加密数据+噪音数据]长度值的加密数据 + [加密数据 + 噪音数据]
	 *
	 */
	public byte[] encryptNet(SecretKey key, byte[] bytes) {

		// if (key == null || bytes == null || bytes.length == 0) {
		if (key == null || bytes == null) {

			return null;

		}

		byte[] IV = getSecureRandom(IV_SIZE);

		IvParameterSpec IVSpec = new IvParameterSpec(IV);

		try {

			cipher.init(Cipher.ENCRYPT_MODE, key, IVSpec);

		} catch (InvalidKeyException | InvalidAlgorithmParameterException ex) {

			log("初始化Cipher出错：");

			ex.printStackTrace();

			return null;

		}

		// 加密数据
		byte[] cipher_bytes = null;

		try {

			cipher_bytes = cipher.doFinal(bytes);

		} catch (IllegalBlockSizeException | BadPaddingException ex) {

			log("加密数据出错：");

			ex.printStackTrace();

			return null;

		}

		// long start = System.currentTimeMillis();

		// 噪音数据
		byte[] noise_bytes = (cipher_bytes.length < NOISE_MAX / 2) ? getSecureRandom(secureRandom.nextInt(NOISE_MAX)) : new byte[0];

		// long end = System.currentTimeMillis();

		// log("噪音字节长度：" + noise_bytes.length);

		// log("制造噪音时间：" + (end - start));

		// [IV+加密数据+噪音数据]的长度值加密，30个字节
		byte[] size_bytes = encrypt(key, getBlockSizeBytes((IV_SIZE + cipher_bytes.length), noise_bytes.length));

		if (size_bytes == null || size_bytes.length != ENCRYPT_SIZE) {

			return null;

		}

		byte[] all_cipher = new byte[size_bytes.length + IV_SIZE + cipher_bytes.length + noise_bytes.length];

		System.arraycopy(size_bytes, 0, all_cipher, 0, size_bytes.length);

		System.arraycopy(IV, 0, all_cipher, size_bytes.length, IV.length);

		System.arraycopy(cipher_bytes, 0, all_cipher, size_bytes.length + IV.length, cipher_bytes.length);

		if (noise_bytes.length > 0) { // 是否加噪音数据

			System.arraycopy(noise_bytes, 0, all_cipher, size_bytes.length + IV.length + cipher_bytes.length, noise_bytes.length);

		}

		size_bytes = null;

		IV = null;

		cipher_bytes = null;

		noise_bytes = null;

		return all_cipher;

	}

	/**
	 * 还原块长度值
	 *
	 * @param bytes 块长度字节数组
	 *
	 * @return 块长度值
	 *
	 */
	public int getBlockSize(byte[] bytes) {

		if (bytes == null) {

			return 0;

		}

		try {

			String size = new String(bytes, CHARSET);

			if (!size.matches("\\d+")) {

				return 0;

			}

			return Integer.valueOf(size);

		} catch (UnsupportedEncodingException | NumberFormatException ex) {

			ex.printStackTrace();

			return 0;

		}

	}

	/**
	 * 生成块长度值的字节数组
	 *
	 * @param size 块长度
	 * @return 块长度值字节数组
	 */
	public byte[] getBlockSizeBytes(int size) {

		try {

			return String.format("%08d", size).getBytes(CHARSET);

		} catch (UnsupportedEncodingException ex) {

			ex.printStackTrace();

			return null;

		}

	}

	/**
	 * 块长度值转换为字节数组
	 *
	 * @param size       加密后的数据块总长度值
	 *
	 * @param noise_size 加密前的噪音数据块长度值
	 *
	 * @return 块长度值字节数组
	 */
	public byte[] getBlockSizeBytes(int data_size, int noise_size) {

		try {

			return String.format("%08d,%05d", data_size, noise_size).getBytes(CHARSET);

		} catch (UnsupportedEncodingException ex) {

			ex.printStackTrace();

			return null;

		}

	}

	/**
	 * 从字节数组还原块长度值
	 *
	 * @param bytes 长度值字节数组，格式 %08d,%05d
	 * @return int[2]
	 */
	public int[] getBlockSizes(byte[] bytes) {

		if (bytes == null) {

			return null;

		}

		try {

			String size = new String(bytes, CHARSET);

			if (!size.matches("\\d+,\\d+")) {

				return null;

			}

			String[] sizes = size.split(",");

			return new int[] { Integer.valueOf(sizes[0]), Integer.valueOf(sizes[1]) };

		} catch (UnsupportedEncodingException | NumberFormatException ex) {

			ex.printStackTrace();

			return null;

		}

	}

	/**
	 * 生成256位SecretKey
	 *
	 * @return 256位SecretKey
	 *
	 */
	public SecretKey getKey() {

		return getKey(256);

	}

	/**
	 * 生成指定加密位数的AES SecretKey
	 *
	 * @param bits 加密位数
	 *
	 * @return SecretKey
	 *
	 */
	public SecretKey getKey(int bits) {

		if (bits < 128) {

			return null;

		}

		try {

			keyGenerator.init(bits);

			return keyGenerator.generateKey();

		} catch (InvalidParameterException ex) {

			log("生成AES SecretKey出错：");

			ex.printStackTrace();

			return null;

		}

	}

	/**
	 * 使用密码生成SecretKey
	 *
	 * @param password 密码，必须符合isPassword()要求的标准
	 *
	 * @return SecretKey
	 *
	 */
	public SecretKey getPasswordKey(String password) {

		if (!isPassword(password)) {

			return null;

		}

		try {

			return getSecretKey(DigestUtils.md5Hex(password.getBytes(CHARSET)));

		} catch (UnsupportedEncodingException ex) {

			log("使用密码生成SecretKey出错：");

			ex.printStackTrace();

			return null;

		}

	}

	/**
	 * 使用SecretKey字符串还原SecretKey
	 *
	 * @param stringKey SecretKey字符串
	 *
	 * @return SecretKey
	 *
	 */
	public SecretKey getSecretKey(String stringKey) {

		if (stringKey == null || (stringKey = stringKey.trim()).length() == 0) {

			return null;

		}

		byte[] bytes = Base64.decodeBase64(stringKey);

		return new SecretKeySpec(bytes, 0, bytes.length, "AES");

	}

	/**
	 * 生成指定长度的SecureRandom
	 *
	 * @param size 指定长度
	 * @return
	 */
	public byte[] getSecureRandom(int size) {

		byte[] bytes = new byte[size];

		secureRandom.nextBytes(bytes);

		return bytes;

	}

	/**
	 * 获取SecretKey的字符串
	 *
	 * @param secretKey SecretKey
	 *
	 * @return SecretKey的字符串
	 *
	 */
	public String getStringKey(SecretKey secretKey) {

		if (secretKey == null) {

			return null;

		}

		return Base64.encodeBase64String(secretKey.getEncoded());

	}

	/**
	 * 检查密码是否合格
	 *
	 * 1、长度至少为八个字符 2、至少包含一个数字 3、至少包含一个大写字母 4、至少包含一个小写字母 5、不得包含空格
	 *
	 * @param password
	 * @return
	 */
	public boolean isPassword(String password) {

		if (password == null || !password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$")) {

			return false;

		}

		return true;

		/*
		 * 2、至少包含一个数字 3、至少包含一个大写字母 4、至少包含一个小写字母 5、不得包含空格
		 */
		// return
		// password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

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

	public void test() {

		byte[] bytes = getBlockSizeBytes(888, 0);

		byte[] size_bytes = encrypt(getKey(), bytes);

		log("长度：" + size_bytes.length);

		log(new String(bytes));

		int[] sizes = this.getBlockSizes(bytes);

		System.out.println(sizes[0]);

		System.out.println(sizes[1]);

	}

	public void testEncryptFile() throws Exception {

		SecretKey key = getPasswordKey("abc123");

		log("文件加密测试");

		String ext = ".msi";

		File f1 = new File("d:/" + 1 + ext);

		File f2 = new File("d:/" + 2 + ext);

		File f3 = new File("d:/" + 3 + ext);

		long start = System.currentTimeMillis();

		encryptFile(key, f1, f2);

		long end = System.currentTimeMillis();

		log("时间：" + (end - start));

		log("每秒：" + f1.length() * 1000 / (end - start) / 1024 / 1024 + "M");

		log("文件解密测试");

		start = System.currentTimeMillis();

		decryptFile(key, f2, f3);

		end = System.currentTimeMillis();

		log("时间：" + (end - start));

		log("每秒：" + f2.length() * 1000 / (end - start) / 1024 / 1024 + "M");

	}

	public void testIsPassword() {

		String password = "xxXxab12";

		log(isPassword(password));

	}

	public void testSecureRandom() {

		// secureRandom.nextBytes(bytes);

		// secureRandom.nextBytes(bytes);

		long start = System.currentTimeMillis();

		byte[] bytes = this.getSecureRandom(1024);

		long end = System.currentTimeMillis();

		log("时间：" + (end - start));

		try {

			log(new String(bytes, "UTF-8"));

		} catch (UnsupportedEncodingException ex) {

			ex.printStackTrace();

		}

	}

}
