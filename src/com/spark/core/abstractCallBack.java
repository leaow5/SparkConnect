package com.spark.core;

import com.spark.utils.StringTransformUtil;

public abstract class abstractCallBack implements CallBack, Comparable<abstractCallBack>, Cloneable {

	/**
	 * 默认构造函数.
	 */
	abstractCallBack() {
		uuid = StringTransformUtil.getUUID();
	}

	/**
	 * 设置权限.
	 * 
	 * @param param
	 *            int
	 */
	public void setPriority(int param) {
		priority = param;
	}

	/**
	 * 获取权限.
	 * 
	 * @return int
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * 唯一性判断.
	 */
	private String uuid;

	/**
	 * 唯一性判断.
	 * 
	 * @return String
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * 唯一性判断.
	 * 
	 * @param uuid
	 *            String
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		abstractCallBack other = (abstractCallBack) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

	/**
	 * 权限.
	 */
	private int priority = 10;
	// 默认16进制
	private boolean isOX = true;

	@Override
	public boolean getCharset() {
		return isOX;
	}

	public void setCharset(boolean isOX) {
		this.isOX = isOX;
	}

	@Override
	public int compareTo(abstractCallBack o) {
		// 复写此方法进行任务执行优先级排序
		// return priority < o.priority ? 1 :
		// (priority > o.priority ? -1 : 0);
		if (priority < o.priority) {
			return -1;
		} else {
			if (priority > o.priority) {
				return 1;
			} else {
				return 0;
			}
		}
	}
	
	@Override
	public Object clone() {
		abstractCallBack o = null;
		try {
			o = (abstractCallBack) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return o;
	}

}
