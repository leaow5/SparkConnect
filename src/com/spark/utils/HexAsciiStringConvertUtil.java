package com.spark.utils;

import java.io.UnsupportedEncodingException;

final public class HexAsciiStringConvertUtil {
	private HexAsciiStringConvertUtil() {

	}

	/**
	 * Convert byte[] to hex
	 * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
	 * 
	 * @param src
	 *            byte[] data
	 * @return hex string
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * 字节数组到ascii.
	 * 
	 * @param bytes
	 *            byte[]
	 * @return String
	 */
	static public String bytesToAscii(byte[] bytes) {

		StringBuffer tStringBuf = new StringBuffer();
		char[] tChars = new char[bytes.length];

		for (int i = 0; i < bytes.length; i++)
			tChars[i] = (char) bytes[i];

		tStringBuf.append(tChars);

		return tStringBuf.toString();
	}

	/**
	 * 字符串到数组.
	 * 
	 * @param bytes
	 *            byte[]
	 * @return String
	 * @throws UnsupportedEncodingException
	 */
	static public String bytesToGbk(byte[] bytes)
			throws UnsupportedEncodingException {
		String s1 = new String(bytes, "ISO-8859-1");
		String s2 = new String(s1.getBytes("ISO-8859-1"), "gb2312");
		return s2;
	}

	/**
	 * Convert hex string to byte[]
	 * 
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4
					| charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	/**
	 * 字符串到数组.
	 * 
	 * @param ascii
	 *            String
	 * @return byte[]
	 * @throws UnsupportedEncodingException
	 */
	static public byte[] asciiToBytes(String ascii)
			throws UnsupportedEncodingException {
		return ascii.getBytes("US-ASCII");
	}

	/**
	 * 字符串到数组.
	 * 
	 * @param ascii
	 *            String
	 * @return byte[]
	 * @throws UnsupportedEncodingException
	 */
	static public byte[] gbkToBytes(String gbk)
			throws UnsupportedEncodingException {
		String s1 = new String(gbk.getBytes("gb2312"), "ISO-8859-1");
		return s1.getBytes("ISO-8859-1");
	}

	/**
	 * Convert char to byte
	 * 
	 * @param c
	 *            char
	 * @return byte
	 */
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	/** */
	/**
	 * @函数功能: BCD码转为10进制串(阿拉伯数据)
	 * @输入参数: BCD码
	 * @输出结果: 10进制串
	 */
	public static String bcdToStr(byte[] bytes) {
		StringBuffer temp = new StringBuffer(bytes.length * 2);

		for (int i = 0; i < bytes.length; i++) {
			temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
			temp.append((byte) (bytes[i] & 0x0f));
		}
		return temp.toString().substring(0, 1).equalsIgnoreCase("0")
				? temp.toString().substring(1) : temp.toString();
	}

	/**
	 * @函数功能: 10进制串转为BCD码
	 * @输入参数: 10进制串
	 * @输出结果: BCD码
	 */
	public static byte[] strToBcd(String asc) {
		int len = asc.length();
		int mod = len % 2;

		if (mod != 0) {
			asc = "0" + asc;
			len = asc.length();
		}

		byte abt[] = new byte[len];
		if (len >= 2) {
			len = len / 2;
		}

		byte bbt[] = new byte[len];
		abt = asc.getBytes();
		int j, k;

		for (int p = 0; p < asc.length() / 2; p++) {
			if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
				j = abt[2 * p] - '0';
			} else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
				j = abt[2 * p] - 'a' + 0x0a;
			} else {
				j = abt[2 * p] - 'A' + 0x0a;
			}

			if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
				k = abt[2 * p + 1] - '0';
			} else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
				k = abt[2 * p + 1] - 'a' + 0x0a;
			} else {
				k = abt[2 * p + 1] - 'A' + 0x0a;
			}

			int a = (j << 4) + k;
			byte b = (byte) a;
			bbt[p] = b;
		}
		return bbt;
	}

	/**
	 * @函数功能: BCD码转ASC码
	 * @输入参数: BCD串
	 * @输出结果: ASC码
	 */
	public static String BcdToAsc(byte[] bytes) {
		StringBuffer temp = new StringBuffer(bytes.length * 2);

		for (int i = 0; i < bytes.length; i++) {
			int h = ((bytes[i] & 0xf0) >>> 4);
			int l = (bytes[i] & 0x0f);
			temp.append(BToA[h]).append(BToA[l]);
		}
		return temp.toString();
	}

	public final static char[] BToA = "0123456789abcdef".toCharArray();
}
