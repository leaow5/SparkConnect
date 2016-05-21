package com.spark.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.commons.lang3.StringUtils;

import com.spark.utils.ArrayUtils;
import com.spark.utils.StringTransformUtil;

import gnu.io.SerialPort;

public class SerialConnecter {
	/**
	 * 私有方法.
	 */
	private SerialConnecter() {

	}

	private static SerialConnecter sc = null;
	// 连接实例
	private volatile SerialPort instance;
	private Thread reader = null;
	private Thread writer = null;
	// 优先级队列
	private PriorityBlockingQueue<CallBack> queue = new PriorityBlockingQueue<CallBack>();
	// 控制线程退出
	private static volatile boolean notExist = true;

	/**
	 * 发送消息.
	 * 
	 * @param arg
	 *            CallBack
	 * @return boolean
	 */
	public boolean sendMessage(CallBack arg) {
		queue.put(arg);
		return true;
	}

	// 初始化连接器
	public static SerialConnecter newConnect() throws IOException, Exception {
		if (sc == null) {
			synchronized (SerialConnecter.class) {
				if (sc == null) {
					sc = new SerialConnecter();
				}
			}
		}

		if (sc.instance == null) {
			synchronized (SerialPortFactory.class) {
				if (sc.instance == null) {
					sc.instance = SerialPortFactory.getSerialPort(null);
					// 清空
					sc.queue.clear();
				}
			}
		}
		return sc;
	}

	/**
	 * 开始新线程
	 * 
	 * @throws IOException
	 */
	public static void initConnect() throws IOException {
		// 读命令
		sc.reader = new Thread(
				new SerialReader(sc.instance.getInputStream(), sc.queue));
		// 写命令
		sc.writer = new Thread(
				new SerialWriter(sc.instance.getOutputStream(), sc.queue));
		sc.reader.start();
		sc.writer.start();
	}

	/** 先取消息再取队列里面的元素调用 */
	public static class SerialReader implements Runnable {
		InputStream in;
		// 优先级队列
		private PriorityBlockingQueue<CallBack> queue = new PriorityBlockingQueue<CallBack>();

		public SerialReader(InputStream in,
				PriorityBlockingQueue<CallBack> arg2) {
			this.in = in;
			queue = arg2;
		}

		public void run() {
			byte[] buffer = new byte[4096];
			int len = -1;

			while (notExist) {
				try {
					if ((len = this.in.read(buffer)) > -1) {
						if (len > 0) {
							StringBuffer sb = new StringBuffer();
							String temp = StringTransformUtil.bytesToHexString(
									ArrayUtils.subBytes(buffer, 0, len));
							if (StringUtils.indexOfIgnoreCase(temp,
									"0D") >= 0) {
								// 担心消息不停，要识别截至帧
								sb.append(StringUtils.substring(temp, 0,
										StringUtils.indexOfIgnoreCase(temp,
												"0D")));
								// 获取执行队列
								CallBack call = queue.take();
								if (call.getCallBackState() == CallBackState.MESSAGE_SENDED) {
									call.execute(sb.toString());
								}
								// 把剩下的消息缓存起来
								sb = new StringBuffer();
								sb.append(StringUtils.substring(temp,
										StringUtils.indexOf(temp, "0D") + 1));
							} else {
								sb.append(StringTransformUtil.bytesToHexString(
										ArrayUtils.subBytes(buffer, 0, len)));
							}

						}

					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	/** 首先放入队列，然后再发送消息 */
	public static class SerialWriter implements Runnable {
		OutputStream out;
		// 优先级队列
		private PriorityBlockingQueue<CallBack> queue = new PriorityBlockingQueue<CallBack>();

		public SerialWriter(OutputStream out,
				PriorityBlockingQueue<CallBack> arg2) {
			this.out = out;
			queue = arg2;
		}

		public void run() {

			while (notExist) {
				try {
					CallBack call = queue.peek();
					if (call != null && call
							.getCallBackState() == CallBackState.MESSAGE_READY) {
						call.setCallBackState(CallBackState.MESSAGE_SENDING);
						byte[] e = call.getOrderMessage();
						this.out.write(e);
						call.setCallBackState(CallBackState.MESSAGE_SENDED);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	public static void main(String[] args) throws IOException, Exception {
		SerialPortFactory.getSerialPort("COM3");
		SerialPortFactory.initConnect();
		//消息一
//		ComponentRepaintCallBack crcb2 = new ComponentRepaintCallBack(null);
//		crcb2.setOrderMessage(StringTransformUtil.hexToBytes("1234567"));
//		crcb2.setCallBackState(CallBackState.MESSAGE_READY);
//		crcb2.setPriority(20);
//		SerialPortFactory.sendMessage(crcb2);
		//消息二
		ComponentRepaintCallBack crcb = new ComponentRepaintCallBack(null);
		crcb.setOrderMessage(StringTransformUtil.hexToBytes("55AA01080100F60D"));
		crcb.setCallBackState(CallBackState.MESSAGE_READY);
		crcb.setPriority(0);
		SerialPortFactory.sendMessage(crcb);
		// ini
	}
}
