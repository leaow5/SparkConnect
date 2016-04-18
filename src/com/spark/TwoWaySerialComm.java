package com.spark;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.spark.utils.ArrayUtils;
import com.spark.utils.StringTransformUtil;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

public class TwoWaySerialComm {

	private static int codeType = 0;

	public TwoWaySerialComm() {
		super();
	}

	public void connect(String portName) throws Exception {
		CommPortIdentifier portIdentifier = CommPortIdentifier
				.getPortIdentifier(portName);
		if (portIdentifier.isCurrentlyOwned()) {
			System.out.println("Error: Port is currently in use");
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(),
					2000);

			if (commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8,
						SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

				InputStream in = serialPort.getInputStream();
				OutputStream out = serialPort.getOutputStream();

				(new Thread(new SerialReader(in))).start();
				(new Thread(new SerialWriter(out))).start();

			} else {
				System.out.println(
						"Error: Only serial ports are handled by this example.");
			}
		}
	}

	/** */
	public static class SerialReader implements Runnable {
		InputStream in;

		public SerialReader(InputStream in) {
			this.in = in;
		}

		public void run() {
			byte[] buffer = new byte[4096];
			int len = -1;
			try {
				while ((len = this.in.read(buffer)) > -1) {
					if (len != 0) {
						if (codeType == 1) {
							System.out.println(StringTransformUtil
									.bytesToHexString(ArrayUtils
											.subBytes(buffer, 0, len)));
						} else {
							System.out.println(StringTransformUtil
									.bytesToAsciiString(ArrayUtils
											.subBytes(buffer, 0, len)));
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** */
	public static class SerialWriter implements Runnable {
		OutputStream out;

		public SerialWriter(OutputStream out) {
			this.out = out;
		}

		public void run() {
			try {
				int c = 0;
				byte[] readBuffer = new byte[4096];

				while ((c = System.in.read(readBuffer)) > -1) {
					String d=StringTransformUtil.bytesToAsciiString(readBuffer);
					String e =StringTransformUtil.asciiStrToHexStr(d.trim());
					this.out.write(e.getBytes());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
