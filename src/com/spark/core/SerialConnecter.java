package com.spark.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSON;
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

	public ReceiveMessage getSynCallBackReceived(CallBack cb) {
		if (cb instanceof SynCallBack) {
			return null;
		}
		long startTime = System.currentTimeMillis();
		if (retValue.get(StringTransformUtil.bytesToHexString(cb.getOrderMessage())) != null) {
			ReceiveMessage value = retValue.get(StringTransformUtil.bytesToHexString(cb.getOrderMessage()));
			retValue.put(StringTransformUtil.bytesToHexString(cb.getOrderMessage()), null);
			return value;
		} else {

			while (System.currentTimeMillis() - startTime < 2000) {
				if (retValue.get(StringTransformUtil.bytesToHexString(cb.getOrderMessage())) != null) {
					ReceiveMessage value = retValue.get(StringTransformUtil.bytesToHexString(cb.getOrderMessage()));
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
	static private volatile PriorityBlockingQueue<abstractCallBack> preSendQueue = new PriorityBlockingQueue<abstractCallBack>();
	// 先进先出队列:已收到的命令
	static private volatile BlockingQueue<ReceiveMessage> receiveQueue = new ArrayBlockingQueue<ReceiveMessage>(200);
	// 先进先出队列:已经发出的命令
	static private volatile BlockingQueue<abstractCallBack> sendedQueue = new ArrayBlockingQueue<abstractCallBack>(100);
	static private volatile Map<String, ReceiveMessage> retValue = new HashMap<String, ReceiveMessage>();
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
		preSendQueue.clear();
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
	public boolean sendMessage(abstractCallBack arg) {
		preSendQueue.offer(arg);
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
					SerialConnecter.preSendQueue.clear();
					SerialConnecter.sendedQueue.clear();
					SerialConnecter.receiveQueue.clear();
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
		serialConnecter.reader = new Thread(new SerialReader(serialConnecter.serialPort.getInputStream()));
		serialConnecter.reader.setDaemon(true);
		// 写命令
		serialConnecter.writer = new Thread(new SerialWriter(serialConnecter.serialPort.getOutputStream()));
		serialConnecter.writer.setDaemon(true);
		// 消费命令
		serialConnecter.consumer = new Thread(new SerialConsumer());
		serialConnecter.consumer.setDaemon(true);
		// 启动
		serialConnecter.reader.start();
		serialConnecter.writer.start();
		serialConnecter.consumer.start();
	}

	/** 先取消息再取队列里面的元素调用 */
	public static class SerialReader implements Runnable {
		InputStream in;

		public SerialReader(InputStream in) {
			this.in = in;
		}

		public void run() {
			byte[] buffer = new byte[8192];
			int len = -1;
			StringBuffer sb = new StringBuffer();
			while (notExit) {
				try {
					// 重点，利用短路，不然退不出去
					if (notExit && (len = this.in.read(buffer)) > -1) {
						if (len > 0) {
							String temp = StringTransformUtil.bytesToHexString(ArrayUtils.subBytes(buffer, 0, len));
							sb.append(temp);
							if (StringUtils.indexOfIgnoreCase(sb.toString(), "0D") >= 0) {
								// 担心消息不停，要识别截至帧
								String mess = sb.substring(0, StringUtils.indexOfIgnoreCase(sb.toString(), "0D") + 2);
								// 放入消息队列中去
								ReceiveMessage tempRec = new ReceiveMessage(mess);
								if (!receiveQueue.offer(tempRec)) {
									logger.info("接收器[命令]丢弃:" + mess + ";uuid=" + tempRec.getUuid());
								} else {
									logger.info("接收器[命令]接受:" + mess + ";uuid=" + tempRec.getUuid());
								}
								// 把剩下的消息缓存起来
								sb = new StringBuffer(
										sb.substring(StringUtils.indexOfIgnoreCase(sb.toString(), "0D") + 2));
							}
						} else {
							// 这里其实永远无法执行到
							if (sb.length() > 0) {
								logger.info("接收器[命令]接受:无法消耗的命令" + sb.toString());
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

		public SerialWriter(OutputStream out) {
			this.out = out;
		}

		public void run() {

			while (notExit) {
				try {
					// poll取走BlockingQueue里排在首位的对象,取不到时返回null
					abstractCallBack call = preSendQueue.poll();
					if (call != null) {
						byte[] e = call.getOrderMessage();
						boolean isOX = call.getCharset();
						String temp = "";
						if (isOX) {
							temp = StringTransformUtil.bytesToHexString(e);
							logger.info("[发送器][命令:十六进制]发送:" + temp);
						} else {
							temp = StringTransformUtil.bytesToAsciiString(e);
							logger.info("[发送器][命令:ASCII]发送:" + temp);
						}

						this.out.write(e);
						logger.info("[发送器][放入已发命令集合]:" + temp);
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

		public SerialConsumer() {
		}

		/**
		 * 异常信息的处理.
		 * 
		 * @return
		 */
		private CommandLineCallBack findFirstCommandLine() {
			// 退出队首有2个条件，A是null，B是回到队首
			// 确认队首
			abstractCallBack start = sendedQueue.peek();
			abstractCallBack temp = null;
			// 退出标记
			logger.info("循环前打印:" + JSON.toJSONString(sendedQueue));
			boolean flag = false;
			while ((temp = sendedQueue.poll()) != null) {
				// 如果再次相遇就退出
				if (start.getUuid().equals(temp.getUuid())) {
					if (flag) {
						logger.info("第二次碰撞，退出");
						// 碰撞后要塞回去
						sendedQueue.offer(temp);
						break;
					} else {
						logger.info("第一次碰撞");
						flag = true;
					}
				}
				if (temp instanceof CommandLineCallBack) {
					return (CommandLineCallBack) temp;
				} else {
					sendedQueue.offer(temp);
				}
			}
			return null;
		}

		/**
		 * 正常的循环
		 * 
		 * @param revOrder
		 */
		private abstractCallBack findGernerlCalback(ReceiveMessage revOrder) {
			logger.info("[消费者][已收消息][通用路线]:" + JSON.toJSONString(revOrder));
			// 退出队首有2个条件，A是null，B是回到队首
			// 确认队首
			abstractCallBack start = sendedQueue.peek();
			abstractCallBack temp = null;
			abstractCallBack commandLine = null;
			String sendedOrder = "";
			// 退出标记
			logger.info("循环前打印:" + JSON.toJSONString(sendedQueue));
			boolean flag = false;
			while ((temp = sendedQueue.poll()) != null) {
				
				if ((temp instanceof CommandLineCallBack) && commandLine == null) {
					commandLine = temp;
				}
				// 如果再次相遇就退出
				if (start.getUuid().equals(temp.getUuid())) {
					if (flag) {
						logger.info("第二次碰撞，退出");
						// 碰撞后要塞回去
						sendedQueue.offer(temp);
						temp = null;
						break;
					} else {
						logger.info("第一次碰撞");
						flag = true;
					}
				}
				
				sendedOrder = StringTransformUtil.bytesToHexString(temp.getOrderMessage());
				//如果小于10直接跳过，不能匹配的上
				if(sendedOrder.length()<10){
					continue;
				}
				if (sendedOrder.substring(4, 10).equalsIgnoreCase(revOrder.getMessage().substring(4, 10))) {
					logger.info("[消费者][已发命令][匹配成功]:" + JSON.toJSONString(temp));
					return temp;
				} else {
					// 要塞回去
					sendedQueue.offer(temp);
					temp = null;
				}
			}
			logger.info("循环后打印:" + JSON.toJSONString(sendedQueue));
			if (commandLine != null && flag && temp == null) {
				logger.info("[消费者][已发命令][无匹配]:" + JSON.toJSONString(commandLine));
				sendedQueue.remove(commandLine);
				return commandLine;
			} else if (temp != null) {
				return temp;
			} else {
				return null;
			}
		}

		public void run() {
			/**
			 * 三重循环，第一个循环是用来控制退出的
			 */
			try {
				while (notExit) {
					while (!receiveQueue.isEmpty()) {

						logger.info("[扫描][已收消息][全集][" + JSON.toJSONString(receiveQueue) + "]");
						logger.info("[扫描][已发命令][全集][" + JSON.toJSONString(sendedQueue) + "]");
						// 开始匹配命令：第5位到第8位是一样的，就是匹配上了
						// !注意： 这个是接受到的命令！！
						ReceiveMessage revOrder = receiveQueue.poll();

						logger.info("[消费者][已收消息][队列取出消息][消息：" + JSON.toJSONString(revOrder) + "]");
						// 如果命令为空，就直接作废掉，防止溢出
						if (sendedQueue.size() == 0) {
							logger.info("[消费者][已发命令][没有操作者][作废已收到消息:" + JSON.toJSONString(revOrder) + "]");
							logger.error("[消费者][已发命令][没有操作者][作废已收到的消息:" + JSON.toJSONString(revOrder) + "]");
							continue;
						}

						abstractCallBack tem = null;
						if (revOrder.getMessage().length() >= 10) {
							logger.info("[消费者][消息][通用路径]收到的消息是：" + JSON.toJSONString(revOrder));
							tem = findGernerlCalback(revOrder);
						} else {
							// 自定义命令，一般是比较紧急的事情
							logger.info("[消费者][消息][自定义路径]收到的消息是：" + JSON.toJSONString(revOrder));
							tem = findFirstCommandLine();
						}

						// 提交异步处理
						if (tem != null) {
							logger.info("[消费者][已发命令][验证通过]:" + JSON.toJSONString(tem) + ",消息："
									+ JSON.toJSONString(revOrder));
							ExecutorServices.getExecutorServices().submit(new abstrackRunnable(tem, revOrder));
						} else {
							logger.info("[消费者][验证不通过][丢弃]:" + JSON.toJSONString(revOrder));
						}
						continue;

					}
				}

			} catch (Exception e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}

	}

}
