package com.spark.utils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;

public final class WinEnvUtils {

	/**
	 * 日志.
	 */
	static Logger logger = LogManager.getLogger(WinEnvUtils.class.getName());

	/**
	 * 获取端口. 返回例如 COM1,COM2.
	 */
	public static List<String> getPortId() {
		final java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier
				.getPortIdentifiers();
		CommPortIdentifier portId;
		final List<String> portName = new ArrayList<String>();
		while (portEnum.hasMoreElements()) {
			portId = (CommPortIdentifier) portEnum.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				portName.add(portId.getName());
				logger.info("获取到串行接口：" + portId.getName());
			}
		}
		return portName;
	}

	/**
	 * 返回端口类型.
	 * 
	 * @param portType
	 * @return
	 */
	public static String getPortTypeName(int portType) {
		switch (portType) {
		case CommPortIdentifier.PORT_I2C:
			return "I2C";
		case CommPortIdentifier.PORT_PARALLEL:
			return "Parallel";
		case CommPortIdentifier.PORT_RAW:
			return "Raw";
		case CommPortIdentifier.PORT_RS485:
			return "RS485";
		case CommPortIdentifier.PORT_SERIAL:
			return "Serial";
		default:
			return "unknown type";
		}
	}

	/**
	 * @return A HashSet containing the CommPortIdentifier for all serial ports
	 *         that are not currently being used.
	 */
	public static boolean getAvailableSerialPorts(String comStr) {
		logger.info("传递串口单号：" + comStr);
		final Enumeration thePorts = CommPortIdentifier.getPortIdentifiers();
		while (thePorts.hasMoreElements()) {
			CommPortIdentifier com = (CommPortIdentifier) thePorts
					.nextElement();
			if (com.getName().equalsIgnoreCase(comStr)) {
				switch (com.getPortType()) {
				case CommPortIdentifier.PORT_SERIAL:
					try {
						CommPort thePort = com.open("test", 50);
						thePort.close();
						return true;
					} catch (PortInUseException e) {
						logger.info("Port, " + com.getName() + ", is in use.");
						return false;
					} catch (Exception e) {
						logger.debug(
								"Failed to open port " + com.getName() + e);
						return false;
					}
				}
			}
		}
		return false;
	}

}
