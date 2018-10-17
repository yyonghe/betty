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
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class DefaultUdpConnector extends DefaultConnector implements BettyConnector {
	
	private static final Logger logger = InternalSlf4JLoggerFactory.getLogger(DefaultUdpConnector.class);
	
	private EventLoopGroup workerGroup;
	
	private int rcvBufSize = -1;
	
	@Override
	protected void initInternal() throws LifecycleException {
		super.initInternal();

		// Configure the server.
		Bootstrap bootstrap = new Bootstrap();
		EventLoopGroup workerGroup = null;
		if(nWorkerThreads > 0) {
			workerGroup = new NioEventLoopGroup(nWorkerThreads);
		} else {
			workerGroup = new NioEventLoopGroup();
		}
		bootstrap.group( workerGroup);
		bootstrap.channel(NioDatagramChannel.class);
		// default options
		bootstrap.option(ChannelOption.SO_REUSEADDR, true);
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
		bootstrap.handler(new DefaultServerChannelInitializer<DatagramChannel>(this, getProtocolCodec()));
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
