package com.spark.utils;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final public class StringTransformUtil {
	private StringTransformUtil() {

	}

	/**
	 * Convert byte[] to param
	 * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
	 * 
	 * @param param
	 *            byte[] data
	 * @return param string
	 */
	public static String bytesToHexString(byte[] param) {
		if (param == null || param.length == 0)
			return "";

		StringBuilder stringBuilder = new StringBuilder("");
		if (param == null || param.length <= 0) {
			return null;
		}
		for (int i = 0; i < param.length; i++) {
			int v = param[i] & 0xFF;
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
	 * @param param
	 *            byte[]
	 * @return String
	 */
	static public String bytesToAsciiString(byte[] param) {
		if (param == null || param.length == 0)
			return "";

		StringBuffer tStringBuf = new StringBuffer();
		char[] tChars = new char[param.length];

		for (int i = 0; i < param.length; i++)
			tChars[i] = (char) param[i];

		tStringBuf.append(tChars);

		return tStringBuf.toString();
	}

	/**
	 * 字符串到数组.
	 * 
	 * @param param
	 *            byte[]
	 * @return String
	 * @throws UnsupportedEncodingException
	 */
	static public String bytesToGbk(byte[] param) throws UnsupportedEncodingException {
		if (param == null || param.length == 0)
			return "";

		String s1 = new String(param, "ISO-8859-1");
		String s2 = new String(s1.getBytes("ISO-8859-1"), "gb2312");
		return s2;
	}

	/**
	 * Convert param string to byte[]
	 * 
	 * @param param
	 *            the param string
	 * @return byte[]
	 */
	public static byte[] hexToBytes(String param) {
		if (param == null || param.equals("")) {
			return null;
		}
		param = param.toUpperCase();
		int length = param.length() / 2;
		char[] hexChars = param.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	/**
	 * 字符串到数组.
	 * 
	 * @param param
	 *            String
	 * @return byte[]
	 * @throws UnsupportedEncodingException
	 */
	static public byte[] asciiToBytes(String param) throws UnsupportedEncodingException {
		return param.getBytes("US-ASCII");
	}

	/**
	 * 字符串到数组.
	 * 
	 * @param param
	 *            String
	 * @return byte[]
	 * @throws UnsupportedEncodingException
	 */
	static public byte[] gbkToBytes(String param) throws UnsupportedEncodingException {
		if (param == null)
			return null;
		String s1 = new String(param.getBytes("gb2312"), "ISO-8859-1");
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
		return (byte) mHexStr.indexOf(c);
	}

	/** */
	/**
	 * @函数功能: BCD码转为10进制串(阿拉伯数据)
	 * @输入参数: BCD码
	 * @输出结果: 10进制串
	 */
	public static String bcdToStr(byte[] bytes) {
		if (bytes == null || bytes.length == 0)
			return "";

		StringBuffer temp = new StringBuffer(bytes.length * 2);

		for (int i = 0; i < bytes.length; i++) {
			temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
			temp.append((byte) (bytes[i] & 0x0f));
		}
		return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp.toString().substring(1) : temp.toString();
	}

	/**
	 * @函数功能: 10进制串转为BCD码
	 * @输入参数: 10进制串
	 * @输出结果: BCD码
	 */
	public static byte[] strToBcd(String param) {
		if (param == null || param.equals(""))
			return null;
		int len = param.length();
		int mod = len % 2;

		if (mod != 0) {
			param = "0" + param;
			len = param.length();
		}

		byte abt[] = new byte[len];
		if (len >= 2) {
			len = len / 2;
		}

		byte bbt[] = new byte[len];
		abt = param.getBytes();
		int j, k;

		for (int p = 0; p < param.length() / 2; p++) {
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
	 * 转换到十六进制.
	 * 
	 * @param param
	 *            String
	 * @return String
	 */
	public static String strToHex(String param) {
		if (param == null || param.equals(""))
			return "";
		StringBuilder stringBuilder = new StringBuilder("");
		int v = Integer.valueOf(param);
		String hv = Integer.toHexString(v);
		if (hv.length() < 2) {
			stringBuilder.append(0);
		}
		stringBuilder.append(hv);
		return stringBuilder.toString();
	}

	/**
	 * @函数功能: BCD码转ASC码
	 * @输入参数: BCD串
	 * @输出结果: ASC码
	 */
	public static String bcdToAsc(byte[] bytes) {
		StringBuffer temp = new StringBuffer(bytes.length * 2);

		for (int i = 0; i < bytes.length; i++) {
			int h = ((bytes[i] & 0xf0) >>> 4);
			int l = (bytes[i] & 0x0f);
			temp.append(BToA[h]).append(BToA[l]);
		}
		return temp.toString();
	}

	private final static char[] BToA = "0123456789abcdef".toCharArray();

	/**
	 * int 转换到byte[].
	 * 
	 * @param integer
	 *            int
	 * @return byte[]
	 */
	public static byte[] intToByteArray(final int integer) {
		int byteNum = (40 - Integer.numberOfLeadingZeros(integer < 0 ? ~integer : integer)) / 8;
		byte[] byteArray = new byte[4];

		for (int n = 0; n < byteNum; n++)
			byteArray[3 - n] = (byte) (integer >>> (n * 8));

		return (byteArray);
	}

	/**
	 * byte[]转换到int.
	 * 
	 * @param b
	 *            byte[]
	 * @param offset
	 *            int 偏移量
	 * @return int
	 */
	public static int byteArrayToInt(byte[] b, int offset) {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			value += (b[i + offset] & 0x000000FF) << shift;
		}
		return value;
	}

	/**
	 * String的字符串转换成unicode的String
	 * 
	 * @param param
	 *            String 全角字符串
	 * @return String 每个unicode之间无分隔符
	 * @throws Exception
	 */
	public static String strToUnicode(String param) throws Exception {
		if (param == null || param.equals(""))
			return "";

		char c;
		StringBuilder str = new StringBuilder();
		int intAsc;
		String strHex;
		for (int i = 0; i < param.length(); i++) {
			c = param.charAt(i);
			intAsc = (int) c;
			strHex = Integer.toHexString(intAsc);
			if (intAsc > 128)
				str.append("\\u");
			else // 低位在前面补00
				str.append("\\u00");
			str.append(strHex);
		}
		return str.toString();
	}

	/**
	 * unicode的String转换成String的字符串
	 * 
	 * @param param
	 *            String 16进制值字符串 （一个unicode为2byte）
	 * @return String 全角字符串
	 * @see CHexConver.unicodeToString("\\u0068\\u0065\\u006c\\u006c\\u006f")
	 */
	public static String unicodeToString(String param) {
		if (param == null || param.equals(""))
			return "";

		int t = param.length() / 6;
		int iTmp = 0;
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < t; i++) {
			String s = param.substring(i * 6, (i + 1) * 6);
			// 将16进制的string转为int
			iTmp = (Integer.valueOf(s.substring(2, 4), 16) << 8) | Integer.valueOf(s.substring(4), 16);
			// 将int转换为字符
			str.append(new String(Character.toChars(iTmp)));
		}
		return str.toString();
	}

	/**
	 * 十六进制字符串转换成 ascii int.
	 * 
	 * @param hexStr
	 *            String
	 * @return String
	 * @throws Exception
	 *             非法字符！
	 */
	public static int hexStrToAsciiInt(String hexStr) throws Exception {
		if (!checkHexStr(hexStr)) {
			throw new Exception("非法字符！");
		}
		return Integer.parseInt(hexStr, 16);
	}

	/**
	 * 十六进制字符串转换成 ASCII字符串.
	 * 
	 * @param str
	 *            String Byte字符串
	 * @return String 对应的字符串
	 * @throws Exception
	 *             输入字符串必须是偶数个数
	 */
	public static String hexStrToAsciiStr(String hexStr) throws Exception {
		hexStr = hexStr.toString().trim().replace(" ", "").toUpperCase(Locale.US);
		// 必须要验证输入的合法性
		int remainder = hexStr.length() % 2;
		if (remainder != 0) {
			throw new Exception("输入字符串必须是偶数个数！");
		}
		char[] hexs = hexStr.toCharArray();
		byte[] bytes = new byte[hexStr.length() / 2];
		int iTmp = 0x00;

		for (int i = 0; i < bytes.length; i++) {
			iTmp = mHexStr.indexOf(hexs[2 * i]) << 4;
			iTmp |= mHexStr.indexOf(hexs[2 * i + 1]);
			bytes[i] = (byte) (iTmp & 0xFF);
		}
		return new String(bytes);
	}

	/**
	 * ascii to String 
	 * @param value String
	 * @return String
	 */
	public static String asciiToString(String value)  
	{  
	    StringBuffer sbu = new StringBuffer();  
	    String[] chars = value.split(",");  
	    for (int i = 0; i < chars.length; i++) {  
	        sbu.append((char) Integer.parseInt(chars[i]));  
	    }  
	    return sbu.toString();  
	}  
	/**
	 * 检查16进制字符串是否有效
	 * 
	 * @param param
	 *            String 16进制字符串
	 * @return boolean
	 */
	public static boolean checkHexStr(String param) {
		if (param == null)
			return false;

		String sTmp = param.toString().trim().replace(" ", "").toUpperCase(Locale.US);
		int iLen = sTmp.length();

		if (iLen > 1 && iLen % 2 == 0) {
			for (int i = 0; i < iLen; i++)
				if (!mHexStr.contains(sTmp.substring(i, i + 1)))
					return false;
			return true;
		} else
			return false;
	}

	/**
	 * 字符串转换成十六进制字符串
	 * 
	 * @param str
	 *            String 待转换的ASCII字符串
	 * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
	 */
	public static String asciiStrToHexStr(String str) {
		if (str == null)
			return "";
		StringBuilder sb = new StringBuilder();
		byte[] bs = str.getBytes();

		for (int i = 0; i < bs.length; i++) {
			sb.append(BToA[(bs[i] & 0xFF) >> 4]);
			sb.append(BToA[bs[i] & 0x0F]);
			sb.append(' ');
		}
		return sb.toString().trim();
	}

	/**
	 * 数字转换成十六进制.
	 * 
	 * @param param
	 *            int
	 * @return String
	 */
	public static String asciiStrToHexStr(int param) {
		return Integer.toHexString(param);
	}

	private final static String mHexStr = "0123456789ABCDEF";

//	public static void main(String[] args) throws UnsupportedEncodingException {
//		String ss= "fr?";
//		byte[] s = asciiToBytes(ss);
//		System.out.println(s);
//		}

	public static String simpleClassName(Object o) {
		if (o == null) {
			return "null_object";
		} else {
			return simpleClassName(o.getClass());
		}
	}

	/**
	 * Generates a simplified name from a {@link Class}. Similar to
	 * {@link Class#getSimpleName()}, but it works fine with anonymous classes.
	 */
	public static String simpleClassName(Class<?> clazz) {
		String className = checkNotNull(clazz, "clazz").getName();
		final int lastDotIdx = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
		if (lastDotIdx > -1) {
			return className.substring(lastDotIdx + 1);
		}
		return className;
	}

	private static final char PACKAGE_SEPARATOR_CHAR = '.';

	public static <T> T checkNotNull(T arg, String text) {
		if (arg == null) {
			throw new NullPointerException(text);
		}
		return arg;
	}
	
	 
    public static String replaceBlank(String str) {  
        String dest = "";  
        if (str!=null) {  
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");  
            Matcher m = p.matcher(str);  
            dest = m.replaceAll("");  
        }  
        return dest;  
    }  
    public static void main(String[] args) {  
        System.out.println(replaceBlank(" just  do it! \n \r"));  
    }  
}
