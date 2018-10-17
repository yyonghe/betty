package io.betty.server;

import java.net.SocketAddress;

import co.paralleluniverse.fibers.SuspendExecution;
import io.betty.BettyProtocolCodec;
import io.betty.util.MiscUtils;
import io.netty.channel.ChannelHandlerContext;
import kilim.Pausable;

public class DefaultServerContext implements BettyServerContext {
	
	/**
	 * Request processed sequence, auto created.
	 */
	private final int seq;
	
	private final long start;

	private long uid;
	
	private SocketAddress remote;
	
	private SocketAddress local;
	
	private String cmd;
	
	private String subcmd;
	
	private Object request;
	
	private Object response;
	
	private BettyService service;
	
	private int retCode;
	
	private String retMessage = "ok";
	
	private BettyProtocolCodec protocolcodec;
	
	public DefaultServerContext(long uid, String cmd, String subcmd, Object request) {
		this.seq = MiscUtils.SG.getAndIncrement();
		this.start = System.currentTimeMillis();
		this.uid = uid;
		this.cmd = cmd;
		this.subcmd = subcmd;
		this.request = request;
	}
	
	@Override
	public int getSeq() {
		return seq;
	}

	/**
	 * @return the uid
	 */
	public long getUid() {
		return uid;
	}

	/**
	 * @param uid the uid to set
	 */
	public void setUid(long uid) {
		this.uid = uid;
	}

	/**
	 * @return the remote
	 */
	public SocketAddress getRemote() {
		return remote;
	}

	/**
	 * @param remote the remote to set
	 */
	public void setRemote(SocketAddress remote) {
		this.remote = remote;
	}

	/**
	 * @return the local
	 */
	public SocketAddress getLocal() {
		return local;
	}

	/**
	 * @param local the local to set
	 */
	public void setLocal(SocketAddress local) {
		this.local = local;
	}
	
	/**
	 * @return the cmd
	 */
	public String getCmd() {
		return cmd;
	}

	/**
	 * @return the subcmd
	 */
	public String getSubcmd() {
		return subcmd;
	}

	/**
	 * @return the raw
	 */
	@SuppressWarnings("unchecked")
	public <T> T getRequest() {
		return (T)request;
	}

	/**
	 * @param service the service to set
	 */
	public void setService(BettyService service) {
		this.service = service;
	}

	/**
	 * @return the service
	 */
	public BettyService getService() {
		return service;
	}

	@Override
	public void setResponse(Object response) {
		this.response = response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getResponse() {
		return (T) response;
	}
	
	/**
	 * @return the start time
	 */
	public long getStartTime() {
		return start;
	}
	
	/**
	 * @return the retCode
	 */
	public int getRetCode() {
		return retCode;
	}

	/**
	 * @param retCode the retCode to set
	 */
	public void setRetCode(int retCode) {
		this.retCode = retCode;
	}

	/**
	 * @return the retMessage
	 */
	public String getRetMessage() {
		return retMessage;
	}

	/**
	 * @param retMessage the retMessage to set
	 */
	public void setRetMessage(String retMessage) {
		this.retMessage = retMessage;
	}

	/**
	 * @return the protocolcodec
	 */
	public BettyProtocolCodec getProtocolCodec() {
		return protocolcodec;
	}

	/**
	 * @param protocolcodec the protocolcodec to set
	 */
	public void setProtocolCodec(BettyProtocolCodec protocolcodec) {
		this.protocolcodec = protocolcodec;
	}
	
	@Override
	public void startContext(ChannelHandlerContext ctx) throws Pausable, SuspendExecution, Exception {
		service.service(ctx, this);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StandardContext [seq=");
		builder.append(seq);
		builder.append(", start=");
		builder.append(start);
		builder.append(", uid=");
		builder.append(uid);
		builder.append(", local=");
		builder.append(local);
		builder.append(", remote=");
		builder.append(remote);
		builder.append(", request=");
		builder.append(protocolcodec.toString(request));
		builder.append(", response=");
		builder.append(protocolcodec.toString(response));
		builder.append(", service=");
		builder.append(service);
		builder.append("]");
		return builder.toString();
	}
}
