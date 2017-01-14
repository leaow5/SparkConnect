package com.spark.core;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gnu.io.SerialPort;

public final class SerialPortFactory {
	private static Logger logger = LogManager.getLogger(SerialPortFactory.class.getName());
	// private static volatile SerialPort serialPort = null;
	private static volatile SerialConnecter connecter = null;

	/**
	 * 关闭连接.
	 * 
	 * @param portName
	 */
	public static void disConnect(String portName) {
		// 关闭连接，可能先需要清理缓存数据
		final Boolean isForce = true;
		logger.info("[info]:disconnect事件强制关闭");
		connecter.close(isForce);
		// serialPort.close();
		// 清空
		logger.info("[info]:disconnect对象清空");
		connecter = null;
		// serialPort=null;
	}

	/**
	 * 这个方法用来设置和获取连接类的唯一入口. 分两种情况： 1）当传入参数的时候，初始化连接，如果当前连接对象非空，
	 * 会先关闭当前连接，按照传入的端口连接 2）当传入空参数的时候，返回连接。
	 * 
	 * @param portName
	 *            端口号，例如： COM1
	 * @return SerialPort
	 * @throws IOException
	 * @throws Exception
	 */
	public static SerialPort connect(String portName) throws IOException, Exception {
		if (StringUtils.isEmpty(portName)) {
			throw new Exception("端口号为空");
		}
		
		return initConnect(portName).getSerialPort();
	}

	/**
	 * 初始化上下文
	 * 
	 * @throws IOException
	 * @throws Exception
	 */
	public static SerialConnecter initConnect(String portName) throws IOException, Exception {
		if (connecter == null) {
			synchronized (SerialPortFactory.class) {
				if (connecter == null) {
					connecter = SerialConnecter.newConnect(portName);
					connecter.setNotExit(true);
					connecter.initConnect();
				}
			}
		}
		return connecter;

	}

	/**
	 * 发送消息.
	 * 
	 * @param arg
	 * @return
	 * @throws Exception
	 */
	public static boolean sendMessage(CallBack arg) throws Exception {
		if (connecter == null) {
			throw new Exception("连接未初始化。");
		}
		connecter.sendMessage(arg);
		return true;
	}

	/**
	 * 重置清空
	 * 
	 * @return
	 */
	public static boolean reset() {
		connecter.reset();
		return true;
	}

	public static String getSynCallBackReceived(CallBack cb) {

		return connecter.getSynCallBackReceived(cb);
	}
}
