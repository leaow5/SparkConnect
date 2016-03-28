package com.spark.utils;

public final class ArrayUtils {

	private ArrayUtils() {

	}

	/**
	 * 从一个byte[]数组中截取一部分
	 * 
	 * @param src
	 * @param begin
	 * @param count
	 * @return
	 */
	public static byte[] subBytes(byte[] src, int begin, int count) {
		byte[] bs = new byte[count];
		for (int i = begin; i < begin + count; i++)
			bs[i - begin] = src[i];
		return bs;
	}

	/**
	 * 调式.
	 * 
	 * @param bs
	 */
	static void printByteArray(byte[] bs) {
		for (int i = 0, sz = bs.length; i < sz; i++)
			System.out.print(Integer.toHexString((int) bs[i]) + "\t");
	}
}
