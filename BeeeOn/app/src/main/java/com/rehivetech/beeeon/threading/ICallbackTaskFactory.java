package com.rehivetech.beeeon.threading;

import android.support.annotation.Nullable;

public interface ICallbackTaskFactory<T> {

	/**
	 * Factory method for creating tasks.
	 *
	 * @return instance of CallbackTask or null if task can't be created
	 */
	@Nullable
	CallbackTask<T> createTask();

	/**
	 * Factory method for creating task's param.
	 *
	 * @return parameter object or null for no parameter
	 */
	@Nullable
	T createParam();

}