package io.betty.example;

import org.slf4j.Logger;

import com.lmax.disruptor.Sequence;

import io.betty.lifecycle.Lifecycle;
import io.betty.lifecycle.LifecycleEvent;
import io.betty.lifecycle.LifecycleEventExt;
import io.betty.lifecycle.LifecycleListener;
import io.betty.util.InternalSlf4JLoggerFactory;

/**
 * Just a simple test {@link LifecycleListener}. 
 * We recommend to put *Listener classes files into package <b>com.company_name.buziness_name.listeners</b>
 */
public class SimpleLifecycleListener implements LifecycleListener {
	
	private static final Logger logger = InternalSlf4JLoggerFactory.getLogger(SimpleLifecycleListener.class);

	@Override
	public void lifecycleEvent(LifecycleEvent event) {
		if(logger.isDebugEnabled()) {
			logger.debug("Rcved lifecycle event: {}, {}", ((Lifecycle)event.getSource()).getName(), event.getType());
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
