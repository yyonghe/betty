package io.betty.server.conn;

import org.slf4j.Logger;

import io.betty.lifecycle.LifecycleException;
import io.betty.server.BettyConnector;
import io.betty.server.DefaultServerChannelInitializer;
import io.betty.util.InternalSlf4JLoggerFactory;
import io.betty.util.IntrospectionUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.epoll.EpollSocketChannel;

public class DefaultUdpEpollConnector extends DefaultConnector implements BettyConnector {
	
	private static final Logger logger = InternalSlf4JLoggerFactory.getLogger(DefaultUdpEpollConnector.class);
	
	private EventLoopGroup workerGroup;
	
	private int rcvBufSize = -1;
	
	@Override
	protected void initInternal() throws LifecycleException {
		super.initInternal();

		// Configure the server.
		Bootstrap bootstrap = new Bootstrap();
		EventLoopGroup workerGroup = null;
		if(nWorkerThreads > 0) {
			workerGroup = new EpollEventLoopGroup(nWorkerThreads);
		} else {
			workerGroup = new EpollEventLoopGroup();
		}
		bootstrap.group( workerGroup);
		bootstrap.channel(EpollSocketChannel.class);
		// default options
		bootstrap.option(ChannelOption.SO_REUSEADDR, true);
		bootstrap.option(EpollChannelOption.SO_REUSEPORT, true);
		bootstrap.option(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
		// options
		if(rcvBufSize > 0) {
			FixedRecvByteBufAllocator alloc = new FixedRecvByteBufAllocator(rcvBufSize);
			bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, alloc);
		}
		//
		try {
			IntrospectionUtils.addOption(bootstrap, getOptions());
		} catch (Exception e) {
			new RuntimeException("StandardTcpConnector initInternal", e);
		}
		bootstrap.handler(new DefaultServerChannelInitializer<EpollDatagramChannel>(this, getProtocolCoder()));
		//
		this.bootstrap = bootstrap;
		this.workerGroup = workerGroup;
	}
	
	@Override
	protected void startInternal() throws LifecycleException {
		//
		ChannelFuture future = null;
		
		int nWorker = 4;
		if(nWorkerThreads > 0) {
			nWorker = nWorkerThreads;
		}
		for(int n=0;n<nWorker;n++) {
			future = bootstrap.bind(getHost(), getPort());
		}
		
		try {
			if(future != null) {
				future.sync();
			}
		} catch (InterruptedException e) {
			logger.error("bootstrap bind InterruptedException", e);
		}
	}
	
	@Override
	protected void destroyInternal() throws LifecycleException {
		workerGroup.shutdownGracefully();
		//
		super.destroyInternal();
	}
}
