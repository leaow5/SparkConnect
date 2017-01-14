package com.spark.core.test;

import java.io.IOException;

import com.spark.core.ComponentRepaintCallBack;
import com.spark.core.SerialPortFactory;
import com.spark.utils.StringTransformUtil;

public class SerialConnecterTest {

	public static void main(String[] args) throws IOException, Exception {
		SerialPortFactory.connect("COM1");
		SerialPortFactory.initConnect("COM1");
		// 消息一
		// ComponentRepaintCallBack crcb2 = new ComponentRepaintCallBack(null);
		// crcb2.setOrderMessage(StringTransformUtil.hexToBytes("1234567"));
		// crcb2.setCallBackState(CallBackState.MESSAGE_READY);
		// crcb2.setPriority(20);
		// SerialPortFactory.sendMessage(crcb2);
		// 消息二
		ComponentRepaintCallBack crcb = new ComponentRepaintCallBack(null);
		crcb.setOrderMessage(StringTransformUtil.hexToBytes("55AA01080100F60D"));
//		crcb.setCallBackState(CallBackState.MESSAGE_READY);
		crcb.setPriority(0);
		SerialPortFactory.sendMessage(crcb);
		// ini
	}

}
