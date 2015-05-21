package com.rehivetech.beeeon.persistence;

import com.rehivetech.beeeon.network.INetwork;

public abstract class BaseModel {

	protected final INetwork mNetwork;

	public BaseModel(INetwork network) {
		mNetwork = network;
	}
}
