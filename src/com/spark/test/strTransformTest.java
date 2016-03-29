package com.spark.test;

import org.junit.Test;

import com.spark.utils.StringTransformUtil;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class strTransformTest {

	@Test
	public void testAsciiStrToHex() {
		int a = 48;
		String b = StringTransformUtil.asciiStrToHexStr(a);
		assertThat(b, equalTo("30"));
	}

	@Test
	public void testAsciiIntToHex() {
		String c = "0";
		String d = StringTransformUtil.asciiStrToHexStr(c);
		assertThat(d, equalTo("30"));
	}
}
