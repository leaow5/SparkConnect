package com.spark.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.spark.test.HelloLogger;
import com.spark.utils.ArrayUtils;
import com.spark.utils.StringTransformUtil;

import gnu.io.SerialPort;

public class SerialConnecter {
	/**
	 * 私有方法.
	 */
	private SerialConnecter() {

	}

	static Logger logger = LogManager.getLogger(SerialConnecter.class.getName());
	private static SerialConnecter sc = null;
	// 连接实例
	private volatile SerialPort instance;
	private Thread reader = null;
	private Thread writer = null;
	// 真正的消费者，只有这个线程才能消耗掉命令
	private Thread consumer = null;
	// 优先级队列：准备发出的命令，因为有优先级，所以实际是动态的
	static private volatile PriorityBlockingQueue<CallBack> sendQueue = new PriorityBlockingQueue<CallBack>();
	// 先进先出队列:已收到的命令
	static private volatile BlockingQueue<String> receiveQueue = new ArrayBlockingQueue<String>(100);
	// 先进先出队列:已经发出的命令
	static private volatile BlockingQueue<CallBack> sendedQueue = new ArrayBlockingQueue<CallBack>(100);
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
		sendQueue.put(arg);
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
					sc.sendQueue.clear();
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
		sc.reader = new Thread(new SerialReader(sc.instance.getInputStream(), sc.sendQueue));
		// 写命令
		sc.writer = new Thread(new SerialWriter(sc.instance.getOutputStream(), sc.sendQueue));
		sc.consumer = new Thread(new SerialWriter(sc.instance.getOutputStream(), sc.sendQueue));
		sc.reader.start();
		sc.writer.start();
	}

	/** 先取消息再取队列里面的元素调用 */
	public static class SerialReader implements Runnable {
		InputStream in;
		// 优先级队列
		// private PriorityBlockingQueue<CallBack> queue = new
		// PriorityBlockingQueue<CallBack>();

		public SerialReader(InputStream in, PriorityBlockingQueue<CallBack> arg2) {
			this.in = in;
			// queue = arg2;
		}

		public void run() {
			byte[] buffer = new byte[4096];
			int len = -1;

			while (notExist) {
				try {
					if ((len = this.in.read(buffer)) > -1) {
						if (len > 0) {
							StringBuffer sb = new StringBuffer();
							String temp = StringTransformUtil.bytesToHexString(ArrayUtils.subBytes(buffer, 0, len));
							if (StringUtils.indexOfIgnoreCase(temp, "0D") >= 0) {
								// 担心消息不停，要识别截至帧
								sb.append(StringUtils.substring(temp, 0, StringUtils.indexOfIgnoreCase(temp, "0D")));
								// 获取执行队列
								// CallBack call = queue.take();
								// 放入消息队列中去
								if (!receiveQueue.offer(sb.toString())) {
									logger.error("消息队列已满，丢弃消息" + sb.toString());
								}
								// if (call.getCallBackState() ==
								// CallBackState.MESSAGE_SENDED) {
								// call.execute(sb.toString());
								// }
								// 把剩下的消息缓存起来
								sb = new StringBuffer();
								sb.append(StringUtils.substring(temp, StringUtils.indexOf(temp, "0D") + 1));
							} else {
								sb.append(StringTransformUtil.bytesToHexString(ArrayUtils.subBytes(buffer, 0, len)));
							}

						}

					}
				} catch (IOException e) {
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

		public SerialWriter(OutputStream out, PriorityBlockingQueue<CallBack> arg2) {
			this.out = out;
			queue = arg2;
		}

		public void run() {

			while (notExist) {
				try {
					CallBack call = queue.peek();
					if (call != null && call.getCallBackState() == CallBackState.MESSAGE_READY) {
						call.setCallBackState(CallBackState.MESSAGE_SENDING);
						byte[] e = call.getOrderMessage();
						this.out.write(e);
						call.setCallBackState(CallBackState.MESSAGE_SENDED);
						sendedQueue.offer(call);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	/** 首先放入队列，然后再发送消息 */
	public static class SerialConsumer implements Runnable {
		private BlockingQueue<String> bq1;
		private BlockingQueue<CallBack> bq2;

		public SerialConsumer(BlockingQueue<String> arg1, BlockingQueue<CallBack> arg2) {
			this.bq1 = arg1;
			this.bq2 = arg2;
		}

		public void run() {

			while (notExist) {
				while (bq1.size() > 0) {

				}
				// try {
				// CallBack call = queue.peek();
				// if (call != null && call.getCallBackState() ==
				// CallBackState.MESSAGE_READY) {
				// call.setCallBackState(CallBackState.MESSAGE_SENDING);
				// byte[] e = call.getOrderMessage();
				// this.out.write(e);
				// call.setCallBackState(CallBackState.MESSAGE_SENDED);
				// sendedQueue.offer(call);
				// }
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
			}

		}
	}

	public static void main(String[] args) throws IOException, Exception {
		SerialPortFactory.getSerialPort("COM3");
		SerialPortFactory.initConnect();
		// 消息一
		// ComponentRepaintCallBack crcb2 = new ComponentRepaintCallBack(null);
		// crcb2.setOrderMessage(StringTransformUtil.hexToBytes("1234567"));
		// crcb2.setCallBackState(CallBackState.MESSAGE_READY);
		// crcb2.setPriority(20);
		// SerialPortFactory.sendMessage(crcb2);
		// 消息二
		ComponentRepaintCallBack crcb = new ComponentRepaintCallBack(null);
		crcb.setOrderMessage(StringTransformUtil.hexToBytes("55AA01080100F60D"));
		crcb.setCallBackState(CallBackState.MESSAGE_READY);
		crcb.setPriority(0);
		SerialPortFactory.sendMessage(crcb);
		// ini
	}
}
