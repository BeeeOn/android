package com.rehivetech.beeeon.asynctask;

public interface CallbackTaskFactory<T> {

	/**
	 * Factory method for creating tasks.
	 * @return
	 */
	CallbackTask<T> createTask();

	/**
	 * Factory method for creating task's param.
	 * @return
	 */
	T createParam();

}