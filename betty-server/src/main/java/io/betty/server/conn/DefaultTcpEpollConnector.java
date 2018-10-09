package io.betty.server.conn;

import io.betty.lifecycle.LifecycleException;
import io.betty.server.BettyConnector;
import io.betty.server.DefaultServerChannelInitializer;
import io.betty.util.InternalSlf4JLoggerFactory;
import io.betty.util.IntrospectionUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.handler.logging.LoggingHandler;

public class DefaultTcpEpollConnector extends DefaultConnector implements BettyConnector {
	
	private EventLoopGroup bossGroup;
	
	private EventLoopGroup workerGroup;
	
	@Override
	protected void initInternal() throws LifecycleException {
		super.initInternal();

		// Configure the server.
		EventLoopGroup bossGroup = new EpollEventLoopGroup(1);
		EventLoopGroup workerGroup = null;
		if(nWorkerThreads > 0) {
			workerGroup = new EpollEventLoopGroup(nWorkerThreads);
		} else {
			workerGroup = new EpollEventLoopGroup();
		}
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workerGroup);
		bootstrap.channel(EpollServerSocketChannel.class);
		// default options
		bootstrap.option(ChannelOption.SO_BACKLOG, 100);
		bootstrap.option(ChannelOption.SO_REUSEADDR, true);
		bootstrap.option(EpollChannelOption.SO_REUSEPORT, true);
		bootstrap.option(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
		// options
		try {
			IntrospectionUtils.addOption(bootstrap, getOptions());
		} catch (Exception e) {
			new RuntimeException("StandardTcpConnector initInternal", e);
		}
		if(InternalSlf4JLoggerFactory.isEnableDump()) {
			bootstrap.handler(new LoggingHandler());
		}
		bootstrap.childHandler(new DefaultServerChannelInitializer<EpollSocketChannel>(this, getProtocolCoder()));
		//
		this.bootstrap = bootstrap;
		this.bossGroup = bossGroup;
		this.workerGroup = workerGroup;
	}
	
	@Override
	protected void destroyInternal() throws LifecycleException {
		//
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
		//
		super.destroyInternal();
	}
}
