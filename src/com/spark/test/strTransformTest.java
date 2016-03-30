package com.spark.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.spark.utils.StringTransformUtil;

public class strTransformTest {

	@Rule  
	public ExpectedException expectedEx = ExpectedException.none();  

	@Test
	public void testAsciiStrToHex() {
		int a = 48;
		String b = StringTransformUtil.asciiStrToHexStr(a);
		assertThat(b, equalTo("30"));
		
		StringTransformUtil.asciiStrToHexStr(null);
	}

	@Test
	public void testAsciiIntToHex() {
		String c = "0";
		String d = StringTransformUtil.asciiStrToHexStr(c);
		assertThat(d, equalTo("30"));

		String e = "1";
		String f = StringTransformUtil.asciiStrToHexStr(e);
		assertThat(f, equalTo("31"));
	}

	@Test 
	public void testHexToAsciiString() throws Exception {
		String a = "3C";
		String b = StringTransformUtil.hexStrToAsciiStr(a);
		assertThat(b, equalTo("<"));

		a = "3c";
		b = StringTransformUtil.hexStrToAsciiStr(a);
		assertThat(b, equalTo("<"));
		
		expectedEx.expect(Exception.class);  
		expectedEx.expectMessage("输入字符串必须是偶数个数！");  
		a = "c";
		b = StringTransformUtil.hexStrToAsciiStr(a);
	}
}
