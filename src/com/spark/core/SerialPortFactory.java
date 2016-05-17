package com.spark.core;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.spark.utils.ConstType;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;

public final class SerialPortFactory {
	private static volatile SerialPort instance = null;
	private static volatile SerialConnecter connecter = null;

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
	public static SerialPort getSerialPort(String portName)
			throws IOException, Exception {
		if (StringUtils.isEmpty(portName)) {
			return instance;
		}
		// 如果当前的连接有效的话，先关闭
		if (instance != null && StringUtils.isNotEmpty(portName)) {
			instance.close();
		}
		// 初始化连接
		if (instance == null) {
			synchronized (SerialPortFactory.class) {
				if (instance == null) {
					try {
						CommPortIdentifier portId = CommPortIdentifier
								.getPortIdentifier(portName);

						// 使用portId对象服务打开串口，并获得串口对象
						instance = (SerialPort) portId
								.open(ConstType.SERIAL_PORT_OWER, 2000);
					} catch (NoSuchPortException ex) {
						throw new Exception(ex.toString());
					} catch (PortInUseException ex) {
						throw new Exception(ex.toString());
					}
				}
			}
		}
		return instance;
	}

	public static SerialConnecter getSerialConnecter()
			throws IOException, Exception {
		if (connecter != null) {
			return connecter;
		}

		if (connecter == null) {
			connecter = SerialConnecter.newConnect();
			if (connecter != null) {
				return connecter;
			}
		}
		return connecter;
	}

	/**
	 * 初始化上下文
	 * @throws IOException
	 * @throws Exception
	 */
	public static void initConnect() throws IOException, Exception {
		if (instance != null && connecter == null) {
			synchronized (SerialPortFactory.class) {
				if (instance != null && connecter == null) {

					connecter = SerialConnecter.newConnect();
					connecter.initConnect();
				}
			}
		}
	}
	/**
	 * 发送消息.
	 * @param arg
	 * @return
	 */
	public static boolean  sendMessage(CallBack arg){
		connecter.sendMessage(arg);
		return true;
	}
}
