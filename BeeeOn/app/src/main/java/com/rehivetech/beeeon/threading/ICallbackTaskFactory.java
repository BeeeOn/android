package com.rehivetech.beeeon.threading;

public interface ICallbackTaskFactory<T> {

	/**
	 * Factory method for creating tasks.
	 *
	 * @return
	 */
	CallbackTask<T> createTask();

	/**
	 * Factory method for creating task's param.
	 *
	 * @return
	 */
	T createParam();

}