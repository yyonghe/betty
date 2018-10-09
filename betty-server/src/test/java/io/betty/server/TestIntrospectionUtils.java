package io.betty.server;

import java.util.ServiceLoader;

import io.betty.BettyModuleProvider;
import io.betty.lifecycle.LifecycleEventExt;
import io.betty.lifecycle.LifecycleListener;
import io.betty.lifecycle.LifecycleState;
import io.betty.server.BettyServerFlowLogListenerProvder;
import io.betty.server.DefaultServerContext;
import io.betty.server.services.AbstractService;
import io.betty.server.services.DefaultProtocolBufferService;
import io.betty.util.MiscUtils;
import junit.framework.TestCase;

public class TestIntrospectionUtils extends TestCase {

	public void testTestType() {
		System.out.println(MiscUtils.getLocalInet4Address("eth8"));
	}

	
	public void testSpi() {
		for(BettyModuleProvider provider : ServiceLoader.load(BettyModuleProvider.class)) {
			System.out.println(provider.getClass().getName());
		}
	}
	
	
	public void testFFLogAppender() throws Exception {
		LifecycleListener listener = new BettyServerFlowLogListenerProvder().get();
		
		AbstractService service = new DefaultProtocolBufferService();
		DefaultServerContext bctx = new DefaultServerContext(31231, "0", "0", "1111111");
		
		bctx.setService(service);
		
		bctx.setResponse("2222222");
		
		LifecycleEventExt eventExt = new LifecycleEventExt();
		eventExt.setValue(service, LifecycleState.REQUEST_HANDLED_EVENT, bctx, 0, "test");
		
		listener.onEvent(eventExt, 0, false);
		listener.onEvent(eventExt, 0, false);
		listener.onEvent(eventExt, 0, false);
	}
}
