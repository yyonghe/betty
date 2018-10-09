package io.betty.client;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ManagedChannelGroup {

	private static final Logger logger = LoggerFactory.getLogger(ManagedChannelGroup.class);
	private static final AtomicInteger nextChannelIndex = new AtomicInteger(0);
	
	private final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE) {
		public boolean add(Channel channel) {
			boolean ok = super.add(channel);
			if(ok && doNotify) {
				doNotify = false;
				synchronized (waitter) {
					waitter.notifyAll();
				}
			}
			return ok;
		};
		public io.netty.channel.group.ChannelGroupFuture close() {
			currentChannels.decrementAndGet();
			return super.close();
		};
	};

	private AtomicInteger currentChannels = new AtomicInteger(0); // current channel number.
    private final Bootstrap bootstrap;
    private final SocketAddress addr;
    private final int maxChannels;
    private volatile boolean doNotify = true;
    private Object waitter = new Object();
    

    public ManagedChannelGroup(Bootstrap bootstrap, SocketAddress addr) {
        this(bootstrap, addr, 16);
    }
    
    public ManagedChannelGroup(Bootstrap bootstrap, SocketAddress addr, int maxChannels) {
        this.bootstrap = bootstrap;
        this.addr = addr;
        this.maxChannels = maxChannels;
    }

    public boolean write(Object msg, ChannelFutureListener listener){
    	int n = currentChannels.get();
    	while(n < maxChannels) {
    		int nn = n + 1;
    		if(currentChannels.compareAndSet(n, nn)) {
    			 return doConnectAndWrite(msg, listener);
    		}
    		// next try.
    		n = currentChannels.get();
    	}
    	return doWrite(msg, listener);
    }
    
    private boolean doWrite(Object msg, ChannelFutureListener listener) {
    	if(doNotify) { // Waiting for connection available...
    		synchronized (waitter) {
    			try {
					waitter.wait(500);
				} catch (InterruptedException e) {
					throw new IllegalStateException("Waiting for channel connection interrupted", e);
				}
			}
    		if(doNotify) { // it's timeout
    			throw new IllegalStateException("Waiting for channel connection timeout");
    		}
    	}
    	Object[] ca = channels.toArray();
        int size = ca.length;
        int nextChannel = Math.abs(nextChannelIndex.getAndIncrement() % size);
        Channel c = (Channel) ca[nextChannel];
        if(listener != null) {
        	c.write(msg).addListener(listener);
        } else {
        	c.write(msg);
        }
        c.flush();
        return true;
    }

	private boolean doConnectAndWrite(final Object msg, final ChannelFutureListener listener) {
		bootstrap.connect(addr).addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if(future.isSuccess()) {
					if(logger.isDebugEnabled()) {
						logger.debug("New channel establied to -> {}", addr);
					}
					future.channel().localAddress();
					channels.add(future.channel());
					doWrite(msg, listener);
				} else {
					logger.error("Connect to -> {} failed", addr);
					currentChannels.decrementAndGet();
					if(listener != null) {
						listener.operationComplete(future);
					}
				}
			}
		});
		return true;
	}
	
	public void shutdown() {
		try {
			channels.close().sync();
		} catch (InterruptedException e) {
			logger.error("InterruptedException on channel group shutdwon", e);
		}
	}
	
	public SocketAddress getRemote() {
		return addr;
	}
	
	public SocketAddress getLocal() {
		return addr;
	}
}
