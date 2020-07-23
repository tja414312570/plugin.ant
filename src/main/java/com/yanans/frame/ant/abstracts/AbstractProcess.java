package com.yanan.frame.ant.abstracts;

/**
 * 抽象任务执行服务
 * 
 * @author yanan
 *
 */
public abstract class AbstractProcess implements Runnable {
	/**
	 * 任务是否可执行
	 */
	private boolean executeAvailable = true;
	/**
	 * 任务创建时间
	 */
	private long recoderTime;
	/**
	 * 任务最大可执行时间
	 */
	private long maxAvailableTime = 0;

	public AbstractProcess() {
		this.recoderTime = System.currentTimeMillis();
	}

	/**
	 * 任务执行逻辑
	 */
	public abstract void execute();

	/**
	 * 内部执行实现
	 */
	@Override
	public void run() {
		/**
		 * 如果任务不可执行
		 */
		if (!executeAvailable)
			return;
		/**
		 * 如果任务设置了最大执行时间
		 */
		if (this.maxAvailableTime > 0) {
			/**
			 * 获取当前时间
			 */
			long now = System.currentTimeMillis();
			/**
			 * 任务执行时间已过
			 */
			if (now > recoderTime + maxAvailableTime) {
				destory();
				return;
			}
		}
		/**
		 * 执行任务
		 */
		this.execute();
	}

	/**
	 * 判断当前任务是否可执行
	 * 
	 * @return
	 */
	public boolean isExecuteAvailable() {
		return executeAvailable;
	}

	/**
	 * 设置当前任务是否可执行
	 * 
	 * @param executeAvailable
	 */
	public void setExecuteAvailable(boolean executeAvailable) {
		this.executeAvailable = executeAvailable;
	}

	/**
	 * 将当前任务设置不可执行
	 */
	public void destory() {
		this.executeAvailable = false;
	}

	/**
	 * 获取任务的记录时间
	 * 
	 * @return
	 */
	public long getRecoderTime() {
		return recoderTime;
	}

	/**
	 * 设置任务的记录时间
	 * 
	 * @param recoderTime
	 */
	public void setRecoderTime(long recoderTime) {
		this.recoderTime = recoderTime;
	}

	/**
	 * 重置任务的记录时间
	 * 
	 * @param recoderTime
	 */
	public void restRecoderTime() {
		this.recoderTime = System.currentTimeMillis();
	}
	/**
	 * 获取任务最长可以等待执行的时间
	 * @return
	 */
	public long getMaxAvailableTime() {
		return maxAvailableTime;
	}
	/**
	 * 设置任务最长可以等待执行的时间
	 * @param maxAvailableTime
	 */
	public void setMaxAvailableTime(long maxAvailableTime) {
		this.maxAvailableTime = maxAvailableTime;
	}

}