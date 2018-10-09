package io.betty.report;

import org.slf4j.Logger;

import com.lmax.disruptor.Sequence;

import io.betty.lifecycle.LifecycleEvent;
import io.betty.lifecycle.LifecycleEventExt;
import io.betty.lifecycle.LifecycleListener;
import io.betty.util.InternalSlf4JLoggerFactory;

public class BettyReportModuleListener implements LifecycleListener {
	
	private static final Logger logger = InternalSlf4JLoggerFactory.getLogger(BettyReportModuleListener.class);

	@Override
	public void lifecycleEvent(LifecycleEvent event) {
		if(logger.isDebugEnabled()) {
			logger.debug("BettyReportModuleListener: " + event);
		}
	}

	@Override
	public void onEvent(LifecycleEventExt eventExt, long sequence, boolean endOfBatch) throws Exception {
		if(logger.isDebugEnabled()) {
			logger.debug("onEvent: " + eventExt);
		}
	}

	@Override
	public void setSequenceCallback(Sequence sequence) {
		// TODO Auto-generated method stub

	}

}
