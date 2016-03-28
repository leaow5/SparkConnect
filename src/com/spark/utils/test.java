package com.spark.utils;

import java.io.UnsupportedEncodingException;

public class test {

	public static void main(String[] args) throws UnsupportedEncodingException {
		String chinaString = "你好";

		byte[] s1 =HexAsciiStringConvertUtil.gbkToBytes(chinaString);
		String s2 =HexAsciiStringConvertUtil.bytesToGbk(s1);

		System.out.println(s2);
	}

}
