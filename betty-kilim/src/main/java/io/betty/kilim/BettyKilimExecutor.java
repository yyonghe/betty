package io.betty.kilim;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.google.inject.Inject;

import io.betty.BettyContext;
import io.betty.BettyExecutor;
import io.betty.BettyResultWaitStrategy;
import io.betty.lifecycle.Lifecycle;
import io.betty.lifecycle.LifecycleBase;
import io.betty.lifecycle.LifecycleException;
import io.betty.util.InternalSlf4JLoggerFactory;
import io.betty.util.MiscUtils;
import io.netty.channel.ChannelHandlerContext;
import kilim.Mailbox;
import kilim.Pausable;
import kilim.Task;

public class BettyKilimExecutor extends LifecycleBase implements Lifecycle, BettyExecutor {
	
	private static final Logger logger = InternalSlf4JLoggerFactory.getLogger(BettyKilimExecutor.class);
	
	private final AtomicInteger sequence = new AtomicInteger(0);
	
	/**
	 * (power of 2)
	 */
	private int taskNum = 1 << 18;
	private int taskNumMask = taskNum - 1;
	
	private int workerThreadsNum;
	
	private kilim.Scheduler scheduler;
	
	private KilimWorkerTask tasks[];
	
	private BettyResultWaitStrategy resultWaitStrategy;
	
	@Inject
	public BettyKilimExecutor() {
		this.resultWaitStrategy = new BettyKilimExecutorResultWaitStrategy();
	}
	
	@Override
	public void execute(ChannelHandlerContext ctx, BettyContext bctx) {
		
		int seq = 0;
		int index = 0;
		
		int retry = 0; // try 3 times
		boolean inqueued = false;
		do {
			seq = sequence.getAndIncrement();
			index = seq & taskNumMask;
			
			if(tasks[index].offer(new TaskContext(ctx, bctx))) {
				inqueued = true;
				break;
			}
		}while((retry++) < 3);
		
		if(inqueued) {
			
		}
	}
	
	@Override
	protected void initInternal() throws LifecycleException {
		
		//
		this.taskNum = MiscUtils.ceilingNextPowerOfTwo(taskNum);
		this.taskNumMask = this.taskNum - 1;
		//
		kilim.Scheduler scheduler;
		if(workerThreadsNum > 0) {
			scheduler = new kilim.Scheduler(workerThreadsNum);
		} else {
			workerThreadsNum = kilim.Scheduler.defaultNumberThreads;
			scheduler = kilim.Scheduler.getDefaultScheduler();
		}
		this.scheduler = scheduler;
		//
		int rTaskNum = taskNum;
		tasks = new KilimWorkerTask[rTaskNum];
		for (int i = 0; i < rTaskNum; i++) {
			tasks[i] = new KilimWorkerTask();
			tasks[i].setScheduler(this.scheduler);
		}
	}

	@Override
	protected void startInternal() throws LifecycleException {
		//
		for (int i = 0; i < taskNum; i++) {
			tasks[i].start();
		}
		//
		if(logger.isInfoEnabled()) {
			logger.info("Executor started with {}, {}, {} on {}", taskNum, taskNumMask, workerThreadsNum, scheduler);
		}
	}

	@Override
	protected void stopInternal() throws LifecycleException {

	}

	@Override
	protected void destroyInternal() throws LifecycleException {
		//
		for(int i=0;i<taskNum;i++) {
			tasks[i].terminate();
		}
		this.scheduler.shutdown();
	}
	
	@Override
	public boolean inLoop() {
		return true;
	}
	
	@Override
	public BettyResultWaitStrategy getResultWaitStrategy() {
		return resultWaitStrategy;
	}

	/**
	 * @return the taskNum
	 */
	public int getTaskNum() {
		return taskNum;
	}

	/**
	 * @param taskNum the taskNum to set
	 */
	public void setTaskNum(int taskNum) {
		this.taskNum = taskNum;
	}

	/**
	 * @return the workerThreadsNum
	 */
	public int getWorkerThreadsNum() {
		return workerThreadsNum;
	}

	/**
	 * @param workerThreadsNum the workerThreadsNum to set
	 */
	public void setWorkerThreadsNum(int workerThreadsNum) {
		this.workerThreadsNum = workerThreadsNum;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[taskNum=");
		builder.append(taskNum);
		builder.append(", workerThreadsNum=");
		builder.append(workerThreadsNum);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public String getName() {
		return this.getClass().getCanonicalName();
	}
	
	static class TaskContext {
		public final ChannelHandlerContext ctx;
		public final BettyContext bctx;
		public TaskContext(ChannelHandlerContext ctx, BettyContext bctx) {
			super();
			this.ctx = ctx;
			this.bctx = bctx;
		}
	}
	
	static class KilimWorkerTask extends Task<Void> {
		
		private Mailbox<TaskContext> mailbox = new Mailbox<TaskContext>(16);
		
		private boolean running = true;
		
		@Override
		public void execute() throws Pausable, Exception {
			while(running) {
				try {
					while(running) {
						TaskContext ctx = mailbox.get();
						
						if(logger.isDebugEnabled()) {
							logger.debug("Rcved request from {}", ctx.ctx);
						}
						
						BettyContext bctx = ctx.bctx;

						bctx.startContext(ctx.ctx);
					}
				} catch (Exception e) {
					logger.error("Unknown exception", e);
				}
			}
		}
		
		public boolean offer(TaskContext taskContext) {
			return mailbox.putnb(taskContext);
		}
		
		public void terminate() {
			running = false;
		}
	}
}
