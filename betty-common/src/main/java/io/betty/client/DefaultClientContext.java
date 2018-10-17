package io.betty.client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import co.paralleluniverse.fibers.SuspendExecution;
import io.betty.BettyClientContext;
import io.betty.BettyLoadBalance;
import io.betty.BettyLoadBalance.BasicSn;
import io.betty.BettyProtocolCodec;
import io.betty.util.MiscUtils;
import io.netty.channel.ChannelHandlerContext;
import kilim.Pausable;

public class DefaultClientContext implements BettyClientContext {
	
	private int seq;
	
	private long uid;
	
	private long startTime;
	
	private SocketAddress local;
	
	private SocketAddress remote;
	
	private Object data;
	
	private Object waiter;
	
	private int retCode;
	
	private String retMessage;
	
	private long waitTime;
	
	private BasicSn sn;
	
	@SuppressWarnings("rawtypes")
	private BettyLoadBalance loadBalance;
	
	private BettyProtocolCodec protocolcodec;
	
	/**
	 * To create non-zero code response.
	 */
	public DefaultClientContext(int seq, long uid, int retCode, String retMessage) {
		this(seq, uid, null, retCode, retMessage);
	}
	
	/**
	 * To create zero code response.
	 */
	public DefaultClientContext(int seq, long uid, Object data) {
		this(seq, uid, data, 0, "ok");
	}
	
	/**
	 * To create a new request.
	 */
	public DefaultClientContext(long uid, Object data) {
		this(MiscUtils.SG.getAndIncrement(), uid, data, -1, "req-init");
	}
	
	public DefaultClientContext(int seq, long uid, Object data, int retCode, String retMessage) {
		this.startTime = System.currentTimeMillis();
		this.seq = seq;
		this.uid = uid;
		this.retCode = retCode;
		this.data = data;
		this.retMessage = retMessage;
	}
	
	@Override
	public void startContext(ChannelHandlerContext ctx) throws Pausable, SuspendExecution, Exception {
		// 
	}

	@Override
	public int getSeq() {
		return seq;
	}

	@Override
	public long getUid() {
		return uid;
	}

	@Override
	public long getStartTime() {
		return startTime;
	}
	
	@Override
	public void setRemote(SocketAddress remote) {
		this.remote = remote;
	}

	@Override
	public void setLocal(SocketAddress local) {
		this.local = local;
	}

	@Override
	public SocketAddress getRemote() {
		return (InetSocketAddress) remote;
	}

	@Override
	public SocketAddress getLocal() {
		return (InetSocketAddress) local;
	}

	@Override
	public int getRetCode() {
		return retCode;
	}

	@Override
	public String getRetMessage() {
		return retMessage;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <D> D getData() {
		return (D) data;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <W> W getResultWaiter() {
		return (W) waiter;
	}

	@Override
	public void setResultWaiter(Object waiter) {
		this.waiter = waiter;
	}

	/**
	 * @return the waitTime
	 */
	public long getWaitTime() {
		return waitTime;
	}

	/**
	 * @param waitTime the waitTime to set
	 */
	public void setWaitTime(long waitTime) {
		this.waitTime = waitTime;
	}

	/**
	 * @return the sn
	 */
	public BasicSn getBasicSn() {
		return sn;
	}

	/**
	 * @param sn the sn to set
	 */
	public void setBasicSn(BasicSn sn) {
		this.sn = sn;
	}

	/**
	 * @return the loadBalance
	 */
	@SuppressWarnings("rawtypes")
	public BettyLoadBalance getLoadBalance() {
		return loadBalance;
	}

	/**
	 * @param loadBalance the loadBalance to set
	 */
	@SuppressWarnings("rawtypes")
	public void setLoadBalance(BettyLoadBalance loadBalance) {
		this.loadBalance = loadBalance;
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
}
