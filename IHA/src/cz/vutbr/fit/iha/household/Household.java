package cz.vutbr.fit.iha.household;

import java.util.List;

import cz.vutbr.fit.iha.adapter.Adapter;

/**
 * Represents "household" for logged user with all adapters and custom lists.
 * 
 * @author Robyer
 */
public class Household {

	/** Logged in user. */
	public final ActualUser user = new ActualUser();

	/** List of adapters that this user has access to (either as owner, user or guest). */
	public List<Adapter> adapters;
	
}
