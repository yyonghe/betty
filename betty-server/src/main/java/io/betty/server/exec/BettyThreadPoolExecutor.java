package io.betty.server.exec;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import io.betty.BettyContext;
import io.betty.BettyExecutor;
import io.betty.BettyResultWaitStrategy;
import io.betty.lifecycle.Lifecycle;
import io.betty.lifecycle.LifecycleBase;
import io.betty.lifecycle.LifecycleException;
import io.betty.util.InternalSlf4JLoggerFactory;
import io.netty.channel.ChannelHandlerContext;

public class BettyThreadPoolExecutor extends LifecycleBase implements Lifecycle, BettyExecutor {
	
	private static final Logger logger = InternalSlf4JLoggerFactory.getLogger(BettyThreadPoolExecutor.class);
	
	private int corePoolSize = 2;
	
	private int maximumPoolSize = 64;
	
	private long keepAliveTime = 300;
	
	private int nQueueSize = 1024;
	
	private ThreadPoolExecutor executor;
	
	private BettyResultWaitStrategy resultWaitStrategy = new BettyThreadPoolResultWaitStrategy();

	@Override
	public BettyResultWaitStrategy getResultWaitStrategy() {
		return resultWaitStrategy;
	}
	
	@Override
	protected void initInternal() throws LifecycleException {
		
		BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(nQueueSize);
		
		executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
				keepAliveTime, TimeUnit.SECONDS, workQueue,
				new BettyThreadPoolExecutorThreadFactory(), new BettyThreadPoolExecutorRejectedExecutionHandler());
	}

	@Override
	public void execute(ChannelHandlerContext ctx, BettyContext bctx) {
		executor.execute(new BettyThreadPoolExecutorContext(ctx, bctx));
	}

	@Override
	public boolean inLoop() {
		return true;
	}

	@Override
	public String getName() {
		return this.getClass().getCanonicalName();
	}

	@Override
	protected void startInternal() throws LifecycleException {
		
	}

	@Override
	protected void stopInternal() throws LifecycleException {
		
	}

	@Override
	protected void destroyInternal() throws LifecycleException {
		executor.shutdown();
	}
	
	static class BettyThreadPoolExecutorThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        
        public BettyThreadPoolExecutorThreadFactory() {
        	SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" + poolNumber.getAndIncrement() + "-BettyThreadPoolExecutor-";
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (t.isDaemon())
				t.setDaemon(false);
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
		
	}
	
	static class BettyThreadPoolExecutorRejectedExecutionHandler implements RejectedExecutionHandler {

		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			// BettyThreadPoolExecutorContext bctx = (BettyThreadPoolExecutorContext) r;
			// TODO Error process.
		}
		
	}

	static class BettyThreadPoolExecutorContext implements Runnable {
		private final ChannelHandlerContext ctx;
		private final BettyContext bctx;
		public BettyThreadPoolExecutorContext(ChannelHandlerContext ctx, BettyContext bctx) {
			this.ctx = ctx;
			this.bctx = bctx;
		}
		
		@Override
		public void run() {
			try {
				bctx.startContext(ctx);
			} catch (Exception e) {
				logger.error("Should never happen!", e);
			}
		}
	}
}
