package io.betty.server.digester;

import org.xml.sax.Attributes;

import com.google.inject.Injector;

import io.betty.server.BettyServer;
import io.betty.server.BettyService;


/**
 * Rule implementation that creates a connector.
 */

public class ServiceCreateRule extends ObjectHexAttrCreateRule {

    public ServiceCreateRule(Injector injector, String... attributes) {
		super(injector, (String)null, attributes);
	}

	// --------------------------------------------------------- Public Methods
    
    /**
     * Process the beginning of this element.
     *
     * @param attributes The attribute list of this element
     */
    @Override
    public void begin(Attributes attributes) throws Exception {
    	super.begin(attributes);
    	
		BettyServer server= (BettyServer) digester.peek(1);
		BettyService instance = (BettyService) digester.peek();
		instance.setServer(server);
    }
}
