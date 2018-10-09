package io.betty;

import io.betty.BettyLoadBalance.BasicSn;

/**
 * Context for request to remote. 
 *
 */
public interface BettyClientContext extends BettyContext{

	/**
	 * @return the waiter object to wait for remote result.
	 */
	public <W> W getResultWaiter();
	
	public void setResultWaiter(Object waiter);

	/**
	 * @return data Request data or response data.
	 */
	public <D> D getData();
	
	public long getWaitTime();
	
	public BasicSn getBasicSn();
	
	@SuppressWarnings("rawtypes")
	public BettyLoadBalance getLoadBalance();
}
