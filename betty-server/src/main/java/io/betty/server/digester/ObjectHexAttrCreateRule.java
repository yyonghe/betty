package io.betty.server.digester;

import java.util.HashMap;

import org.apache.commons.beanutils.BeanUtils;
import org.xml.sax.Attributes;

import com.google.inject.Injector;

import io.betty.server.BettyServer;


/**
 * Rule implementation that creates a connector.
 */

public class ObjectHexAttrCreateRule extends GuiceObjectCreateRule {

	private String[] attributes = null;
	
    public ObjectHexAttrCreateRule(Injector injector, String className, String... attributes) {
		super(injector, className);
		this.attributes = attributes;
	}

	// --------------------------------------------------------- Public Methods
    
    /**
     * Process the beginning of this element.
     *
     * @param attributes The attribute list of this element
     */
    @Override
    public void begin(Attributes attributes) throws Exception {
    	
    	if(className == null) {
    		BettyServer server= (BettyServer) digester.peek();
    		className = server.getServiceClass();
    	}

        super.begin(attributes);
        //
        // Build a set of attribute names and corresponding values
        HashMap<String, String> values = new HashMap<String, String>();
        //
    	int al = attributes.getLength();
		for (int i = 0; i < al; i++) {
    		String qn = attributes.getQName(i).trim();
    		String value = attributes.getValue(i).trim();
    		if(isCmdAttr(qn)) {
    			if(value.startsWith("0x")) {
    				//hex cmd config
    				value = value.substring("0x".length(), value.length());
    				value = String.valueOf(Integer.parseInt(value, 16));
    			}
    		}
    		values.put(qn, value);
    	}
		Object instance = digester.peek();
		BeanUtils.populate(instance, values);
    }

    private boolean isCmdAttr(String qn) {
    	for(String string : attributes) {
    		if(string.equals(qn)) {
    			return true;
    		}
    	}
    	return false;
    }
}
