package com.spark.core;

import java.io.IOException;

import com.spark.utils.ConstType;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;

public class SerialPortFactory {
	private static volatile SerialPort instance = null;

	/**
	 * 
	 * @param portName
	 *            端口号，例如： COM1
	 * @return SerialPort
	 * @throws IOException
	 * @throws Exception
	 * @throws Exception
	 */
	public static SerialPort getSerialPort(String portName)
			throws IOException, Exception, Exception {
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

}
