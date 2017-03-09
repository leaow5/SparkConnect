package com.spark.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.spark.utils.ArrayUtils;
import com.spark.utils.ConstType;
import com.spark.utils.StringTransformUtil;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;

public class SerialConnecter {
	/**
	 * 私有方法.
	 */
	private SerialConnecter() {

	}

	public String getSynCallBackReceived(CallBack cb) {
		if (cb.getClass() != SynCallBack.class) {
			return null;
		}
		long startTime = System.currentTimeMillis();
		if (retValue.get(StringTransformUtil.bytesToHexString(cb.getOrderMessage())) != null) {
			String value = retValue.get(StringTransformUtil.bytesToHexString(cb.getOrderMessage()));
			retValue.put(StringTransformUtil.bytesToHexString(cb.getOrderMessage()), null);
			return value;
		} else {

			while (System.currentTimeMillis() - startTime < 2000) {
				if (retValue.get(StringTransformUtil.bytesToHexString(cb.getOrderMessage())) != null) {
					String value = retValue.get(StringTransformUtil.bytesToHexString(cb.getOrderMessage()));
					retValue.put(StringTransformUtil.bytesToHexString(cb.getOrderMessage()), null);
					return value;
				}
			}
			return null;
		}
	}

	static Logger logger = LogManager.getLogger(SerialConnecter.class.getName());
	private static SerialConnecter serialConnecter = null;
	// 连接实例
	private volatile SerialPort serialPort;

	public SerialPort getSerialPort() {
		return serialPort;
	}

	public void setSerialPort(SerialPort serialPort) {
		this.serialPort = serialPort;
	}

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
	static private volatile Map<String, String> retValue = new HashMap<String, String>();
	// 控制线程退出
	private static volatile boolean notExit = true;

	// 设置退出命令
	public static void setNotExit(boolean notExit) {
		SerialConnecter.notExit = notExit;
	}

	// 是否阻止发送队列继续加入，退出时控制
	private static volatile boolean join = true;

	/**
	 * 关闭连接.
	 * 
	 * @param isForce
	 *            boolean 是否强制
	 * @return
	 */
	public static boolean close(boolean isForce) {
		if (isForce && serialConnecter.serialPort != null) {
			notExit = false;
			// 强制关闭IO，让子线程抛出异常中止
			logger.info("[info]:notifyOnDataAvailable");
			serialConnecter.serialPort.notifyOnDataAvailable(false);

			// logger.info("[info]:removeEventListener");
			// sc.instance.removeEventListener();
			try {
				serialConnecter.serialPort.getInputStream().close();
				serialConnecter.serialPort.getOutputStream().close();
				serialConnecter.serialPort.close();
				serialConnecter.serialPort = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		} else {
			join = false;
			while (!receiveQueue.isEmpty()) {
				// 空循环
			}
			return true;
		}
	}

	/**
	 * 清空队列.
	 * 
	 * @return boolean
	 */
	public boolean reset() {
		sendQueue.clear();
		receiveQueue.clear();
		sendedQueue.clear();
		retValue.clear();
		return true;
	}

	/**
	 * 发送消息.
	 * 
	 * @param arg
	 *            CallBack
	 * @return boolean
	 */
	public boolean sendMessage(CallBack arg) {
		sendQueue.offer(arg);
		return true;
	}

	// 初始化连接器
	public static SerialConnecter newConnect(String portName) throws IOException, Exception {
		if (serialConnecter == null) {
			synchronized (SerialConnecter.class) {
				if (serialConnecter == null) {
					serialConnecter = new SerialConnecter();
				}
			}
		}

		if (serialConnecter.serialPort == null) {
			synchronized (SerialPortFactory.class) {
				if (serialConnecter.serialPort == null) {
					serialConnecter.serialPort = newSerialPort(portName);
					// 清空
					serialConnecter.sendQueue.clear();
					serialConnecter.sendedQueue.clear();
					serialConnecter.receiveQueue.clear();
				}
			}
		}
		return serialConnecter;
	}

	public static SerialPort newSerialPort(String portName) throws IOException, Exception {
		if (StringUtils.isEmpty(portName)) {
			throw new Exception("端口号为空");
		}
		SerialPort serialPort = null;
		// 初始化连接
		if (serialConnecter.serialPort == null) {
			synchronized (SerialConnecter.class) {
				if (serialConnecter.serialPort == null) {
					try {
						CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(portName);

						// 使用portId对象服务打开串口，并获得串口对象
						serialPort = (SerialPort) portId.open(ConstType.SERIAL_PORT_OWER, 2000);
					} catch (NoSuchPortException ex) {
						throw new Exception(ex.toString());
					} catch (PortInUseException ex) {
						throw new Exception(ex.toString());
					}
				}
			}
		}
		return serialPort;
	}

	/**
	 * 开始新线程
	 * 
	 * @throws IOException
	 */
	public static void initConnect() throws IOException {
		// 读命令
		serialConnecter.reader = new Thread(
				new SerialReader(serialConnecter.serialPort.getInputStream(), serialConnecter.sendQueue));
		serialConnecter.reader.setDaemon(true);
		// 写命令
		serialConnecter.writer = new Thread(
				new SerialWriter(serialConnecter.serialPort.getOutputStream(), serialConnecter.sendQueue));
		serialConnecter.writer.setDaemon(true);
		// 消费命令
		serialConnecter.consumer = new Thread(
				new SerialConsumer(serialConnecter.receiveQueue, serialConnecter.sendedQueue));
		serialConnecter.consumer.setDaemon(true);
		// 启动
		serialConnecter.reader.start();
		serialConnecter.writer.start();
		serialConnecter.consumer.start();
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
			StringBuffer sb = new StringBuffer();
			while (notExit) {
				try {
					// 重点，利用短路，不然退不出去
					if (notExit && (len = this.in.read(buffer)) > -1) {
						if (len > 0) {

							String temp = StringTransformUtil.bytesToHexString(ArrayUtils.subBytes(buffer, 0, len));
							if (StringUtils.indexOfIgnoreCase(temp, "0D") >= 0) {
								// 担心消息不停，要识别截至帧
								sb.append(
										StringUtils.substring(temp, 0, StringUtils.indexOfIgnoreCase(temp, "0D") + 2));
								// 获取执行队列
								// CallBack call = queue.take();
								// 放入消息队列中去
								if (!receiveQueue.offer(sb.toString())) {
									logger.info("接收器[命令]丢弃:" + sb.toString());
								} else {
									logger.info("接收器[命令]接受:" + sb.toString());
								}
								// if (call.getCallBackState() ==
								// CallBackState.MESSAGE_SENDED) {
								// call.execute(sb.toString());
								// }
								// 把剩下的消息缓存起来
								sb = new StringBuffer();
								sb.append(StringUtils.substring(temp, StringUtils.indexOfIgnoreCase(temp, "0D") + 2));
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

			while (notExit) {
				try {
					// poll取走BlockingQueue里排在首位的对象,取不到时返回null
					CallBack call = queue.poll();
					if (call != null) {
						byte[] e = call.getOrderMessage();
						boolean isOX = call.getCharset();
						String temp = "";
						if (isOX) {
							temp = StringTransformUtil.bytesToHexString(e);
							logger.info("发送器[命令:十六进制]发送:" + temp);
						} else {
							temp = StringTransformUtil.bytesToAsciiString(e);
							logger.info("发送器[命令:ASCII]发送:" + temp);
						}

						
						this.out.write(e);
						logger.info("发送器[命令][放入已发命令集合]:" + temp);
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
		// 消息队列
		private volatile BlockingQueue<String> msgList;
		// 命令队列
		private volatile BlockingQueue<CallBack> optList;

		public SerialConsumer(BlockingQueue<String> arg1, BlockingQueue<CallBack> arg2) {
			this.msgList = arg1;
			this.optList = arg2;
		}

		public void run() {
			/**
			 * 三重循环，第一个循环是用来控制退出的
			 */
			while (notExit) {
				while (!msgList.isEmpty()) {
					// 如果命令队列为空，就清掉消息队列
					if (optList.size() == 0) {
						// 说明命令已经执行完了，又收到下位机的命令，直接抛弃
						msgList.clear();
						continue;
					}

					// 开始匹配命令：第5位到第8位是一样的，就是匹配上了
					String revOrder = msgList.poll();
					logger.info("消费者[命令][队列取出消息]:" + revOrder);
					// 如果命令为空，就直接作废掉，防止溢出
					if (optList.size() == 0) {
						logger.info("消费者[命令][没有操作者]，直接作废收到的命令:" + revOrder);
						logger.error("消费者[命令][没有操作者]，直接作废收到的命令:" + revOrder);

					}

					// 获取第一个命令，用来判断是否当前数组是否遍历一个循环
					CallBack head = optList.poll();
					CallBack firstCommandLineCallBack = null;
					String sendedOrder = StringTransformUtil.bytesToHexString(head.getOrderMessage());
					//取第一个CommandLineCallBack
					if(head instanceof CommandLineCallBack && firstCommandLineCallBack==null){
						firstCommandLineCallBack = head;
					}
					// 判断是否是匹配
					logger.info("消费者[命令][队首待验证]:" + sendedOrder);
					if (StringTransformUtil.bytesToHexString(head.getOrderMessage()).substring(4, 10)
							.equalsIgnoreCase(revOrder.substring(4, 10))) {
						logger.info("消费者[命令][队首验证通过]:" + sendedOrder);
						if (head instanceof CommandLineCallBack || head instanceof ComponentRepaintCallBack) {
							// 提交异步处理
							ExecutorServices.getExecutorServices().submit(new abstrackRunnable(head, revOrder));
							firstCommandLineCallBack = null;
							continue;
						} else {
							logger.info("消费者[命令][队首验证不通过：命令不匹配]放回结果集:" + sendedOrder);
							retValue.put(StringTransformUtil.bytesToHexString(head.getOrderMessage()), revOrder);
						}
					} else {
						// 移到队尾去
						logger.info("消费者[命令][队首验证不通过]移到队尾去:" + sendedOrder);
						optList.offer(head);
					}

					CallBack item = null;
					
					while ((item = optList.poll()) != head) {
						
						if (item == null) {
							break;
						}
						
						if(item instanceof CommandLineCallBack && firstCommandLineCallBack==null){
							firstCommandLineCallBack = item;
						}
						
						
						// 判断是否是匹配
						sendedOrder = StringTransformUtil.bytesToHexString(item.getOrderMessage());
						logger.info("消费者[命令][待验证]:" + sendedOrder);
						if (sendedOrder.substring(4, 9).equalsIgnoreCase(revOrder.substring(4, 9))) {
							logger.info("消费者[命令][验证通过]:" + sendedOrder);
							if (item.getClass() == CommandLineCallBack.class
									|| item.getClass() == ComponentRepaintCallBack.class) {
								// 提交异步处理
								ExecutorServices.getExecutorServices().submit(new abstrackRunnable(item, revOrder));
								firstCommandLineCallBack = null;
							} else {
								logger.info("消费者[命令][验证不通过：命令不匹配]放回结果集:" + sendedOrder);
								retValue.put(StringTransformUtil.bytesToHexString(item.getOrderMessage()), revOrder);
							}
						} else {
							// 移到队尾去
							logger.info("消费者[命令][验证不通过]移到队尾去:" + sendedOrder);
							optList.offer(item);
						}

					}
					// 说明遍历一遍没有找到相应的命令，建议丢弃
					if (item == head) {
						if (firstCommandLineCallBack != null) {
							logger.info("消费者[命令][自定义命令]firstCommandLineCallBack:不为空,接受命令为:" + revOrder);
							logger.info("消费者[命令][自定义命令]firstCommandLineCallBack:不为空,firstCommandLineCallBack本身命令为:" + StringTransformUtil.bytesToHexString(firstCommandLineCallBack.getOrderMessage()));
							optList.remove(firstCommandLineCallBack);
							ExecutorServices.getExecutorServices()
									.submit(new abstrackRunnable(firstCommandLineCallBack, revOrder));
						} // 需求修改，如果没有匹配的就找第一个CommandLineCallBack 消耗掉
						logger.info("消费者[命令][丢弃]没有找到匹配的命令:" + revOrder);
						logger.error("没有找到匹配的命令，丢弃");
					}
					// 出对列，重新赋值
					// bq2 = null;，这里可能会导致消息漏掉，删除
					// bq2 = bq3;

				}
			}

		}
	}

}
