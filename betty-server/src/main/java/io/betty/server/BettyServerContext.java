package io.betty.server;

import io.betty.BettyContext;

/**
 * Context for current request. 
 *
 */
public interface BettyServerContext extends BettyContext{

	/**
	 * @return the request data currently sent to the server.
	 */
	public <T> T getRequest();
	
	/**
	 * @param the response data.
	 */
	public void setResponse(Object response);
	
	/**
	 * @return the response data.
	 */
	public <T> T getResponse();

	/**
	 * @return the service of current request
	 */
	public BettyService getService();
	/**
	 * @param service the current service.
	 */
	public void setService(BettyService service);
	/**
	 * @param result code.
	 */
	public void setRetCode(int retCode);
	/**
	 * @param message
	 */
	public void setRetMessage(String message);
	
	/**
	 * @return the cmd
	 */
	public String getCmd();

	/**
	 * @return the subcmd
	 */
	public String getSubcmd();
}
