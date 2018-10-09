package io.betty.server.digester;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

import com.google.inject.Inject;
import com.google.inject.Injector;

import io.betty.BettyExecutor;


/**
 * Rule implementation that creates a connector.
 */

public class ExecutorCreateRule extends Rule {
	
	private Injector injector;
	
	@Inject
	public ExecutorCreateRule(Injector injector) {
		this.injector = injector;
	}

    // --------------------------------------------------------- Public Methods

    /**
     * Process the beginning of this element.
     *
     * @param namespace the namespace URI of the matching element, or an
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just
     *   the element name otherwise
     * @param attributes The attribute list for this element
     */
    @Override
    public void begin(String namespace, String name, Attributes attributes)
            throws Exception {
    	
    	BettyExecutor executor;
    	
    	String n = attributes.getValue("name");
    	
    	Class<?> clazz = Class.forName(n);
		executor = (BettyExecutor) injector.getInstance(clazz);
    	//
		digester.push(executor);
    }
    
    @Override
    public void end(String namespace, String name) throws Exception {
    	digester.pop();
    }

}
