package io.betty.server;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.NullConfiguration;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.SimpleMessage;

import com.lmax.disruptor.Sequence;

import io.betty.BettyModuleListenerProvider;
import io.betty.lifecycle.Lifecycle;
import io.betty.lifecycle.LifecycleEvent;
import io.betty.lifecycle.LifecycleEventExt;
import io.betty.lifecycle.LifecycleListener;
import io.betty.server.bootstrap.BettyServerBootStrap;

public class BettyServerFlowLogListenerProvder implements BettyModuleListenerProvider {

	@Override
	public LifecycleListener get() {
		return new BettyServerFlowLogListener();
	}
	
	private static class BettyServerFlowLogListener implements LifecycleListener {
		
		private static boolean ninit = true;
		private static final Object init_lock = new Object();
		private RollingFileAppender fflogAppender;
		
		public BettyServerFlowLogListener() {
			init();
		}
		
		private void init() {
			if(ninit) {
				synchronized (init_lock) {
					if(ninit) {
						ninit = false;
						init0();
					}
				}
			}
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private void init0() {
			//
			TimeBasedTriggeringPolicy.Builder policyBuilder = TimeBasedTriggeringPolicy.newBuilder();
			policyBuilder.withInterval(1);
			policyBuilder.withModulate(true);
			//
			String logbase = BettyServerBootStrap.getPwd() + "/logs";
			RollingFileAppender.Builder builder = RollingFileAppender.newBuilder();
			builder.withName("fflog");
			builder.withFilePattern(logbase + "/flow.log.%d{yyyy-MM-dd}");
			builder.withLayout(PatternLayout.newBuilder().build());
			builder.withPolicy(policyBuilder.build());
			builder.setConfiguration(new NullConfiguration());
			//
			fflogAppender = builder.build();
			fflogAppender.initialize();
			fflogAppender.start();
		}

		@Override
		public void onEvent(LifecycleEventExt eventExt, long sequence, boolean endOfBatch) throws Exception {
			Lifecycle source = eventExt.getLifecycle();
			if(source instanceof BettyService) {
				BettyService service = (BettyService) source;
				BettyServerContext bctx = (BettyServerContext) eventExt.getData();
				LogEvent logEvent = createLogEvent(service.formatflow(bctx));
				fflogAppender.getManager().getPatternProcessor().setCurrentFileTime(bctx.getStartTime());
				fflogAppender.append(logEvent);
			}
		}
		
		private LogEvent createLogEvent(String message) {
			Log4jLogEvent.Builder builder = Log4jLogEvent.newBuilder();
			builder.setMessage(new SimpleMessage(message));
			return builder.build();
		}

		@Override
		public void setSequenceCallback(Sequence sequence) {
		}

		@Override
		public void lifecycleEvent(LifecycleEvent event) {
		}
	}
	
//	private static class MyDefaultRolloverStrategy extends AbstractRolloverStrategy implements DirectFileRolloverStrategy {
//		
//		private PatternProcessor tempCompressedFilePattern;
//
//	    protected MyDefaultRolloverStrategy(StrSubstitutor strSubstitutor) {
//			super(strSubstitutor);
//		}
//
//
//	    /**
//	     * Performs the rollover.
//	     *
//	     * @param manager The RollingFileManager name for current active log file.
//	     * @return A RolloverDescription.
//	     * @throws SecurityException if an error occurs.
//	     */
//	    @Override
//	    public RolloverDescription rollover(final RollingFileManager manager) throws SecurityException {
//	        final String currentFileName = manager.getFileName();
//	        return new RolloverDescriptionImpl(currentFileName, false, null, null);
//	    }
//
//		@Override
//		public String getCurrentFileName(RollingFileManager manager) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//	}

}
