package com.spark;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.UnsupportedCommOperationException;

/**
 * 串口数据读取类,用于windows的串口数据读取
 * 
 * 
 * @author Lei Zhang
 * @version 2012-6-14
 */
public class SerialReader2 {
	static CommPortIdentifier portId;
	// public ComContent tempContent = new ComContent();
	int delayRead = 200;
	int numBytes; // buffer中的实际数据字节数

	private static byte[] readBuffer = new byte[4096]; // 4k的buffer空间,缓存串口读入的数据

	static Enumeration portList;

	InputStream inputStream;

	SerialPort serialPort;

	HashMap serialParams;

	// 端口读入数据事件触发后,等待n毫秒后再读取,以便让数据一次性读完
	public static final String PARAMS_DELAY = "delay read"; // 延时等待端口数据准备的时间

	public static final String PARAMS_TIMEOUT = "timeout"; // 超时时间
	public static final String PARAMS_PORT = "port name"; // 端口名称

	public static final String PARAMS_DATABITS = "data bits"; // 数据位
	public static final String PARAMS_STOPBITS = "stop bits"; // 停止位
	public static final String PARAMS_PARITY = "parity"; // 奇偶校验

	public static final String PARAMS_RATE = "rate"; // 波特率

	/**
	 * 初始化端口操作的参数.
	 * 
	 * 
	 * @see
	 */
	// public ComContent getContetn() {
	// return this.tempContent;
	// }

	// public void setContent(String content) {
	// this.tempContent.setContent(content);
	// }

	// public void setTempContent(ComContent tempContent) {
	// this.tempContent = tempContent;
	// }

	// public SerialReader(HashMap params, ComContent content) {
	// serialParams = params;
	// init(content);
	//
	// }

	private void init() {
		try {
			// 参数初始化
			int timeout = Integer
					.parseInt(serialParams.get(PARAMS_TIMEOUT).toString());
			int rate = Integer
					.parseInt(serialParams.get(PARAMS_RATE).toString());
			int dataBits = Integer
					.parseInt(serialParams.get(PARAMS_DATABITS).toString());
			int stopBits = Integer
					.parseInt(serialParams.get(PARAMS_STOPBITS).toString());
			int parity = Integer
					.parseInt(serialParams.get(PARAMS_PARITY).toString());
			delayRead = Integer
					.parseInt(serialParams.get(PARAMS_DELAY).toString());
			// System.out.println("参数初始化完成");
			String port = serialParams.get(PARAMS_PORT).toString();

			// 打开端口
			portId = CommPortIdentifier.getPortIdentifier(port);
			serialPort = (SerialPort) portId.open("SerialReader", timeout);
			inputStream = serialPort.getInputStream();
			// serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			serialPort.setSerialPortParams(rate, dataBits, stopBits, parity);
			try {
				// 等待1秒钟让串口把数据全部接收后在处理
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				numBytes = inputStream.read(readBuffer);
				byte[] temp = new byte[numBytes];
				System.arraycopy(readBuffer, 0, temp, 0, numBytes);
				// String temps = content.getContent() == null ? "" : content
				// .getContent();
				// content.setContent((temps + new String(temp)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (PortInUseException e) {
			// content.setContent("电子磅端口已经被占用,请关闭使用电子磅的程式重试!");
			e.printStackTrace();
		} catch (UnsupportedCommOperationException e) {
			// content.setContent("端口操作命令不支持!");
			e.printStackTrace();
		} catch (NoSuchPortException e) {
			// content.setContent("程序设置的电子磅端口不存在!");
			e.printStackTrace();
		} catch (IOException e) {
			// content.setContent("程序执行中遇到错误!" + e);
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e1) {
				// content.setContent("关闭输入流时异常出现：" + e1.getMessage());
			}
			try {
				serialPort.close();
			} catch (Exception e) {
				// content.setContent("关闭端口时异常出现：" + e.getMessage());
			}
		}
	}

	/**
	 * Method declaration
	 * 
	 * 
	 * @see
	 */
	public void run() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Method declaration
	 * 
	 * 
	 * @param event
	 * 
	 * @see
	 */
	public void serialEvent(SerialPortEvent event) {
		try {
			// 等待1秒钟让串口把数据全部接收后在处理
			Thread.sleep(delayRead);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		switch (event.getEventType()) {

		case SerialPortEvent.BI: // 10
		case SerialPortEvent.OE: // 7
		case SerialPortEvent.FE: // 9
		case SerialPortEvent.PE: // 8
		case SerialPortEvent.CD: // 6
		case SerialPortEvent.CTS: // 3
		case SerialPortEvent.DSR: // 4
		case SerialPortEvent.RI: // 5
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2
			break;
		case SerialPortEvent.DATA_AVAILABLE: // 1
			try {
				// 多次读取,将所有数据读入
				numBytes = inputStream.read(readBuffer);
				changeMessage(readBuffer, numBytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}
	}

	// 通过observer pattern将收到的数据发送给observer
	// 将buffer中的空字节删除后再发送更新消息,通知观察者
	public void changeMessage(byte[] message, int length) {
		byte[] temp = new byte[length];
		System.arraycopy(message, 0, temp, 0, length);
		System.out.println("msg[" + numBytes + "]: [" + new String(temp) + "]");
	}

	static void listPorts() {
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier portIdentifier = (CommPortIdentifier) portEnum
					.nextElement();
			System.out.println(portIdentifier.getName() + " - "
					+ getPortTypeName(portIdentifier.getPortType()));
		}
	}

	static String getPortTypeName(int portType) {
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
	public static HashSet<CommPortIdentifier> getAvailableSerialPorts() {
		HashSet<CommPortIdentifier> h = new HashSet<CommPortIdentifier>();
		Enumeration thePorts = CommPortIdentifier.getPortIdentifiers();
		while (thePorts.hasMoreElements()) {
			CommPortIdentifier com = (CommPortIdentifier) thePorts
					.nextElement();
			switch (com.getPortType()) {
			case CommPortIdentifier.PORT_SERIAL:
				try {
					CommPort thePort = com.open("CommUtil", 50);
					thePort.close();
					h.add(com);
				} catch (PortInUseException e) {

					System.out
							.println("Port, " + com.getName() + ", is in use.");

				} catch (Exception e) {
					System.out.println(
							"Failed to open port " + com.getName() + e);
				}
			}
		}
		return h;
	}

}
