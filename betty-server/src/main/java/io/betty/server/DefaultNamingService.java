package io.betty.server;

import io.betty.lifecycle.Lifecycle;
import io.betty.lifecycle.LifecycleBase;
import io.betty.lifecycle.LifecycleException;

public class DefaultNamingService extends LifecycleBase implements Lifecycle {

	/**
     * The name of this service.
     */
	private String name;
	
	
	private int cmd;
	
	private int subcmd;
	
	
	private String req;
	
	private String rsp;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the cmd
	 */
	public int getCmd() {
		return cmd;
	}

	/**
	 * @param cmd the cmd to set
	 */
	public void setCmd(int cmd) {
		this.cmd = cmd;
	}

	/**
	 * @return the subcmd
	 */
	public int getSubcmd() {
		return subcmd;
	}

	/**
	 * @param subcmd the subcmd to set
	 */
	public void setSubcmd(int subcmd) {
		this.subcmd = subcmd;
	}

	/**
	 * @return the req
	 */
	public String getReq() {
		return req;
	}

	/**
	 * @param req the req to set
	 */
	public void setReq(String req) {
		this.req = req;
	}

	/**
	 * @return the rsp
	 */
	public String getRsp() {
		return rsp;
	}

	/**
	 * @param rsp the rsp to set
	 */
	public void setRsp(String rsp) {
		this.rsp = rsp;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[name=");
		builder.append(name);
		builder.append(", cmd=");
		builder.append(cmd);
		builder.append(", subcmd=");
		builder.append(subcmd);
		builder.append(", req=");
		builder.append(req);
		builder.append(", rsp=");
		builder.append(rsp);
		builder.append("]");
		return builder.toString();
	}

	@Override
	protected void initInternal() throws LifecycleException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void startInternal() throws LifecycleException {
		
	}

	@Override
	protected void stopInternal() throws LifecycleException {
		
	}

	@Override
	protected void destroyInternal() throws LifecycleException {
		// TODO Auto-generated method stub
		
	}
	
}
