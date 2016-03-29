package com.spark.test;

import java.io.UnsupportedEncodingException;

import com.spark.TwoWaySerialComm;

public class test {

	public static void main(String[] args) throws UnsupportedEncodingException {
		String chinaString = "提交git测试";

		try {
			(new TwoWaySerialComm()).connect("COM1");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
