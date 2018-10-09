package io.betty.server.services;

import java.util.Arrays;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorVararg;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import io.betty.lifecycle.Lifecycle;
import io.betty.lifecycle.LifecycleBase;
import io.betty.lifecycle.LifecycleEvent;
import io.betty.lifecycle.LifecycleEventExt;
import io.betty.lifecycle.LifecycleException;
import io.betty.lifecycle.LifecycleState;
import io.betty.util.InternalSlf4JLoggerFactory;

/**
 * ServiceAsyncEventLoop
 * Async even process loop based on disruptor.</br>
 * Processes some asyncrous operation, such as:
 * <li>Flow log write</li>
 * <li>Remote log write</li>
 * <li>Request report</li>
 * <li>Colored user report</li>
 * 
 */
public class ServiceAsyncEventLoop extends LifecycleBase implements Lifecycle, EventTranslatorVararg<LifecycleEventExt> {
	
	private static final Logger logger = InternalSlf4JLoggerFactory.getLogger(ServiceAsyncEventLoop.class);
	
	private volatile Disruptor<LifecycleEventExt> disruptor;
	
	private int ringBufferSize = 1024;
	
	public void publishAsyncEvent(Lifecycle lifecycle, LifecycleState state, Object data) {
        try {
            if(!disruptor.getRingBuffer().tryPublishEvent(this, 
            		new Object[]{lifecycle, state, data})) {
            	//handle full...
            }
        } catch (final NullPointerException npe) {
            // LOG4J2-639: catch NPE if disruptor field was set to null in stop()
			// LOGGER.warn("[{}] Ignoring log event after log4j was shut down:
			// {} [{}] {}", contextName,
			// translator.level, translator.loggerName,
			// translator.message.getFormattedMessage()
			// + (translator.thrown == null ? "" : Throwables.toStringList(translator.thrown)));
        }
    }
	
	@Override
	public String getName() {
		return "ServiceAsyncEventLoop";
	}

	@Override
	protected void initInternal() throws LifecycleException {
		
		disruptor = new Disruptor<LifecycleEventExt>(new EventFactory<LifecycleEventExt>() {

			@Override
			public LifecycleEventExt newInstance() {
				return new LifecycleEventExt();
			}
		}, ringBufferSize, new ThreadFactory() {
			
			private final AtomicInteger THREAD_NUMBER = new AtomicInteger(1);
			
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "disruptor-ServiceAsyncEventLoop-" + THREAD_NUMBER.getAndIncrement());
			}
		}, ProducerType.MULTI,
				new SleepingWaitStrategy());
		//
		disruptor.setDefaultExceptionHandler(new ExceptionHandler<LifecycleEvent>() {

			@Override
			public void handleEventException(final Throwable t, final long sequence, final LifecycleEvent event) {
				try {
		            // Careful to avoid allocation in case of memory pressure.
		            // Sacrifice performance for safety by writing directly
		            // rather than using a buffer.
		            System.err.print("ServiceAsyncEventLoop error handling event seq=");
		            System.err.print(sequence);
		            System.err.print(", value='");
		            try {
		                System.err.print(event);
		            } catch (final Throwable t1) {
		                System.err.print("ERROR calling toString() on ");
		                System.err.print(event.getClass().getName());
		                System.err.print(": ");
		                System.err.print(t.getClass().getName());
		                System.err.print(": ");
		                System.err.print(t.getMessage());
		            }
		            System.err.print("': ");
		            System.err.print(t.getClass().getName());
		            System.err.print(": ");
		            System.err.println(t.getMessage());
		            // Attempt to print the full stack trace, which may fail if we're already
		            // OOMing We've already provided sufficient information at this point.
		            t.printStackTrace(System.err);
		        } catch (final Throwable ignored) {
		            // LOG4J2-2333: Not much we can do here without risking an OOM.
		            // Throwing an error here may kill the background thread.
		        }
			}

			@Override
			public void handleOnStartException(Throwable t) {
				System.err.println("AsyncLogger error starting:");
			}
			
			@Override
			public void handleOnShutdownException(Throwable t) {
				System.err.println("AsyncLogger error shutting down:");
			}
		});
	}
	
	public void handleEventsWith(@SuppressWarnings("unchecked") EventHandler<? super LifecycleEventExt>... handlers) {
		if(logger.isDebugEnabled()) {
			logger.debug("handleEventsWith: {}", Arrays.toString(handlers));
		}
		disruptor.handleEventsWith(handlers);
	}

	@Override
	protected void startInternal() throws LifecycleException {
		setState(LifecycleState.STARTING);
		//
		disruptor.start();
	}

	@Override
	protected void stopInternal() throws LifecycleException {
		setState(LifecycleState.STOPPING);

	}

	@Override
	protected void destroyInternal() throws LifecycleException {
		disruptor.shutdown();
	}
	
	
	@Override
	public void translateTo(final LifecycleEventExt eventExt, final long sequence, final Object... args) {
		
		final Lifecycle lifecycle = (AbstractService) args[0];
		final LifecycleState state = (LifecycleState) args[1];
		final Object data = args[2];
		final Thread currentThread = Thread.currentThread();
		final long threadId = currentThread.getId();
        final String threadName = currentThread.getName();
		eventExt.setValue(lifecycle, state, data, threadId, threadName);
	}
}
