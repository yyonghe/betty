package io.betty.client;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;

import co.paralleluniverse.fibers.SuspendExecution;
import io.betty.BettyClient;
import io.betty.BettyClientContext;
import io.betty.BettyLoadBalance;
import io.betty.BettyProtocolCoder;
import io.betty.BettyResultWaitStrategy;
import io.betty.BettyLoadBalance.BasicSn;
import io.betty.BettyLoadBalance.BasicSnParam;
import io.betty.lb.RoundRobineLoadBalance;
import io.betty.util.InternalSlf4JLoggerFactory;
import io.betty.util.IntrospectionUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import kilim.Pausable;

@SuppressWarnings({"rawtypes", "unchecked"})
public class DefaultClient implements BettyClient {
	
	private static final Logger logger = InternalSlf4JLoggerFactory.getLogger(DefaultClient.class);

	private static EventLoopGroup DEFAULT_NIO_LOOP;
	
	private static EventLoopGroup DEFAULT_EPOLL_LOOP;
	
	private static final Object create_event_loop_lock = new Object();
	
	private final Map<String, ManagedChannelGroup> groups = new ConcurrentHashMap<String, ManagedChannelGroup>();
	
	private final Map<Integer, BettyClientContext> lookups = new ConcurrentHashMap<Integer, BettyClientContext>();
	
	protected String[] hosts;
	
	protected int[] ports;
	
	private BettyResultWaitStrategy resultWaitStrategy;
	
	private BettyProtocolCoder protocolCoder;
	
	protected BettyLoadBalance loadBalance = new RoundRobineLoadBalance();
	
	private Bootstrap bootstrap;
	
	private EventLoopGroup workerGroup;
	
	private int nWorkerThreads;
	
	private int rcvBufSize = -1;
	
	private String options;
	
	private boolean tcp = true;
	
	private boolean epoll = false;
	
	private Boolean INIT = true;
	
	public DefaultClient(String[] hosts, int[] ports, 
			BettyResultWaitStrategy resultWaitStrategy, BettyProtocolCoder protocolCoder) {
		this.hosts = hosts;
		this.ports = ports;
		this.resultWaitStrategy = resultWaitStrategy;
		this.protocolCoder = protocolCoder;
	}
	
	
	public BettyClientContext send(Object pkgdata, int seq, long uid, long timeout) {
		
		initialize();
		
		DefaultClientContext reqctx = new DefaultClientContext(seq, uid, pkgdata);
		
		BasicSnParam params = loadBalance.createParams(null, reqctx.getSeq(), uid);
		BasicSn sn = loadBalance.getCurBestServer(params);
		String host = sn.host;
		int port = sn.port;
		String key = host + ":" + port;
		
		ManagedChannelGroup group = groups.get(key);
		if(group == null) {
			group = new ManagedChannelGroup(bootstrap, new InetSocketAddress(host, port));
			groups.put(key, group);
		}
		//
		reqctx.setLocal(group.getLocal());
		reqctx.setRemote(group.getRemote());
		reqctx.setBasicSn(sn);
		reqctx.setLoadBalance(loadBalance);
		reqctx.setWaitTime(timeout);
		reqctx.setProtocolCoder(protocolCoder);
		//
		return send(group, reqctx);
	}
	
	protected BettyClientContext send(ManagedChannelGroup group, BettyClientContext reqctx) {
		reqctx.setResultWaiter(resultWaitStrategy.newResultWaiter(reqctx));
		lookups.put(reqctx.getSeq(), reqctx);
		
		//
		group.write(reqctx, new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if(future.isSuccess()) {
//					onSent(reqctx);
				} else {
					simplefailed(reqctx, future.cause());
				}
			}
		});
		//
		return reqctx;
	}
	
	/**
	 * Initialize
	 */
	public void initialize() {
		if(INIT) {
			synchronized (INIT) {
				if(INIT) {
					List<BasicSn> servers = new ArrayList<BasicSn>(hosts.length);
					
					for(int jj = 0;jj<hosts.length;jj++) {
						servers.add(new BasicSn(hosts[jj], ports[jj]));
					}
					loadBalance.initServers(servers);
					
					bootstrap = buildBootstrap();
					
					INIT = false;
				}
			}
		}
	}
	
	public void received(ChannelHandlerContext ctx, BettyClientContext rspctx) {
		int seq = rspctx.getSeq();
		BettyClientContext reqctx = lookups.remove(seq);
		if(reqctx != null) { // Maybe timeout item.
			//
			updateLoadBalance(reqctx, true);
			//
			resultWaitStrategy.notify(reqctx.getResultWaiter(), rspctx.getData());
		} else {
			logger.warn("Recved timeout item {}, {}", rspctx.getSeq(), rspctx.getUid());
		}
	}
	
	protected void updateLoadBalance(BettyClientContext reqctx, boolean succ) {
		int seq = reqctx.getSeq();
		long uid = reqctx.getUid();
		BettyLoadBalance loadBalance = reqctx.getLoadBalance();
		BasicSnParam snp = loadBalance.createParams(reqctx.getBasicSn(), seq, uid);
		loadBalance.updateUsedServer(reqctx.getBasicSn(), snp, succ);
	}
	
	@Override
	public <T> T waitFor(BettyClientContext reqctx) throws Pausable, SuspendExecution, Exception {
		
		T data = null;
		Exception exception = null;
		try {
			data = resultWaitStrategy.waitFor(reqctx.getResultWaiter(), reqctx.getWaitTime());
		} catch (Exception e) {
			exception = e;
		}
		if(data == null || exception != null) {
			Exception cause = exception;
			if(exception == null) {
				cause = new TimeoutException("Timeout on wait result.");
			}
			simplefailed(reqctx, cause);
			throw cause;
		}
		return data;
	}
	
	protected void simplefailed(BettyClientContext reqctx, Throwable cause) {
		if(lookups.remove(reqctx.getSeq()) != null) {// clear lookups
			updateLoadBalance(reqctx, false);
			// TODO Should notify failed as soon as possible.
		}
		// log ---
		StringBuilder strbuilder = new StringBuilder("Send reqeust failed,");
		strbuilder.append("remote: ").append(reqctx.getRemote()).append(',');
		strbuilder.append(reqctx.getSeq()).append(',');
		strbuilder.append(reqctx.getUid()).append(',');
		strbuilder.append('<').append(reqctx.getProtocolCoder().toString(reqctx.getData())).append('>').append(',');
		strbuilder.append('<').append(cause.getMessage()).append('>');
		logger.error(strbuilder.toString(), cause);
	}
	
	protected Bootstrap buildBootstrap() {
		// Configure the server.
		EventLoopGroup workerGroup = this.workerGroup;
		if(workerGroup == null) {
			if(nWorkerThreads > 0) {
				if(epoll) {
					workerGroup = new EpollEventLoopGroup(nWorkerThreads);
				} else {
					workerGroup = new NioEventLoopGroup(nWorkerThreads);
				}
			} else {
				if(epoll) {
					workerGroup = getDefaultEpollEventLoopGroup();
				} else {
					workerGroup = getDefaultNioEventLoopGroup();
				}
			}
			this.workerGroup = workerGroup;
		}
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(workerGroup);
		if(tcp) {
			if(epoll) {
				bootstrap.channel(EpollSocketChannel.class);
				// default options
				bootstrap.option(EpollChannelOption.SO_REUSEPORT, true);
				bootstrap.option(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
				// Channel Initializer
				bootstrap.handler(new DefaultClientChannelInitializer<EpollSocketChannel>(this, protocolCoder, tcp));
			} else {
				bootstrap.channel(NioSocketChannel.class);
				// Channel Initializer
				bootstrap.handler(new DefaultClientChannelInitializer<NioSocketChannel>(this, protocolCoder, tcp));
			}
			// default options
			bootstrap.option(ChannelOption.SO_BACKLOG, 100);
			bootstrap.option(ChannelOption.SO_REUSEADDR, true);
		} else {
			if(epoll) {
				bootstrap.channel(EpollDatagramChannel.class);
				// default options
				bootstrap.option(EpollChannelOption.SO_REUSEPORT, true);
				bootstrap.option(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
				// Channel Initializer
				bootstrap.handler(new DefaultClientChannelInitializer<EpollDatagramChannel>(this, protocolCoder, tcp));
			} else {
				bootstrap.channel(NioDatagramChannel.class);
				// Channel Initializer
				bootstrap.handler(new DefaultClientChannelInitializer<NioDatagramChannel>(this, protocolCoder, tcp));
			}
			// default options
			bootstrap.option(ChannelOption.SO_REUSEADDR, true);
			// options
			if(rcvBufSize > 0) {
				FixedRecvByteBufAllocator alloc = new FixedRecvByteBufAllocator(rcvBufSize);
				bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, alloc);
			}
		}
		// options
		try {
			IntrospectionUtils.addOption(bootstrap, options);
		} catch (Exception e) {
			new RuntimeException("StandardTcpConnector initInternal", e);
		}
		return bootstrap;
	}
	
	/**
	 * @return the loadbalance
	 */
	public BettyLoadBalance getLoadbalance() {
		return loadBalance;
	}

	/**
	 * @param loadbalance the loadbalance to set
	 */
	public void setLoadbalance(BettyLoadBalance loadbalance) {
		this.loadBalance = loadbalance;
	}

	/**
	 * @return the workerGroup
	 */
	public EventLoopGroup getWorkerGroup() {
		return workerGroup;
	}

	/**
	 * @param workerGroup the workerGroup to set
	 */
	public void setWorkerGroup(EventLoopGroup workerGroup) {
		this.workerGroup = workerGroup;
	}

	/**
	 * @return the nWorkerThreads
	 */
	public int getnWorkerThreads() {
		return nWorkerThreads;
	}

	/**
	 * @param nWorkerThreads the nWorkerThreads to set
	 */
	public void setnWorkerThreads(int nWorkerThreads) {
		this.nWorkerThreads = nWorkerThreads;
	}

	/**
	 * @return the rcvBufSize
	 */
	public int getRcvBufSize() {
		return rcvBufSize;
	}

	/**
	 * @param rcvBufSize the rcvBufSize to set
	 */
	public void setRcvBufSize(int rcvBufSize) {
		this.rcvBufSize = rcvBufSize;
	}

	/**
	 * @return the options
	 */
	public String getOptions() {
		return options;
	}

	/**
	 * @param options the options to set
	 */
	public void setOptions(String options) {
		this.options = options;
	}

	/**
	 * @return
	 */
	public boolean isTcp() {
		return tcp;
	}
	/**
	 * @return
	 */
	public boolean isUdp() {
		return !tcp;
	}

	/**
	 * @param tcp the tcp to set
	 */
	public void useTcp() {
		this.tcp = true;
	}
	/**
	 * @param tcp the tcp to set
	 */
	public void useUdp() {
		this.tcp = false;
	}

	/**
	 * @return the epoll
	 */
	public boolean isEpoll() {
		return epoll;
	}

	/**
	 * @param epoll the epoll to set
	 */
	public void setEpoll(boolean epoll) {
		this.epoll = epoll;
	}

	/**
	 * @return the bootstrap
	 */
	public Bootstrap getBootstrap() {
		return bootstrap;
	}

	public BettyResultWaitStrategy getResultWaitStrategy() {
		return resultWaitStrategy;
	}
	
	public void shutdown() {
		for(ManagedChannelGroup group : groups.values()) {
			group.shutdown();
		}
		//
		workerGroup.shutdownGracefully();
	}
	
	public static EventLoopGroup getDefaultNioEventLoopGroup() {
		if(DEFAULT_NIO_LOOP == null) {
			synchronized (create_event_loop_lock) {
				if(DEFAULT_NIO_LOOP == null) {
					DEFAULT_NIO_LOOP = new NioEventLoopGroup();
				}
			}
		}
		return DEFAULT_NIO_LOOP;
	}
	
	public static EventLoopGroup getDefaultEpollEventLoopGroup() {
		if(DEFAULT_EPOLL_LOOP == null) {
			synchronized (create_event_loop_lock) {
				if(DEFAULT_EPOLL_LOOP == null) {
					DEFAULT_EPOLL_LOOP = new EpollEventLoopGroup();
				}
			}
		}
		return DEFAULT_EPOLL_LOOP;
	}
}
