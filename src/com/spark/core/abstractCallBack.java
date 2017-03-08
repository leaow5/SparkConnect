package com.spark.core;

public abstract class abstractCallBack implements CallBack, Comparable<abstractCallBack> {
	/**
	 * 设置权限.
	 * @param param int
	 */
	public void setPriority(int param) {
		priority = param;
	}
	/**
	 * 获取权限.
	 * @return int
	 */
	public int getPriority() {
		return priority;
	}
	
	/**
	 * 权限.
	 */
	private int priority = 10;
	// 默认16进制
	private boolean isOX=true;
	
	@Override
	public boolean getCharset() {
		return isOX;
	}
	
	public void setCharset(boolean isOX){
		this.isOX= isOX;
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
}
