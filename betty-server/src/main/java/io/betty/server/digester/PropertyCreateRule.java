package io.betty.server.digester;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.digester.Rule;
import org.slf4j.Logger;
import org.xml.sax.Attributes;

import io.betty.server.DefaultServer;
import io.betty.util.InternalSlf4JLoggerFactory;


/**
 * Rule implementation that creates a connector.
 */

public class PropertyCreateRule extends Rule {
	
	private static final Logger logger = InternalSlf4JLoggerFactory.getLogger(PropertyCreateRule.class);

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
    	
    	DefaultServer server = (DefaultServer) digester.peek();
    	
    	String type = attributes.getValue("type");
    	if("kv".equals(type)) {
    		String key = attributes.getValue("key");
    		String value = attributes.getValue("value");
    		server.addProperty(key, value);
    	} else if("file".equals(type)) {
    		String filepath = attributes.getValue("filePath");
    		if(filepath == null || filepath.isEmpty()) {
    			throw new IllegalArgumentException("file path is empty for <file> type property");
    		}
    		Properties properties = loadProperties(filepath);
    		for(Entry<Object, Object> entry : properties.entrySet()) {
    			server.addProperty(entry.getKey().toString(), entry.getValue().toString());
    		}
    	}
    	
    }

    
    private Properties loadProperties(String filepath) throws IOException {

    	Properties properties = new Properties();
    	
    	InputStream inStream = null;
    	
    	try {
    		
    		File file = new File(filepath);
    		if(file.exists() && file.canRead()) {
    			inStream = new FileInputStream(file);
    		} else {
    			inStream = PropertyCreateRule.class.getClassLoader().getResourceAsStream(filepath);
    			if(inStream == null) {
    				inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filepath);
    			}
    		}
    		
    		if(inStream != null) {
    			properties.load(inStream);
    		} else {
    			logger.warn("Load property file failed: {}", filepath);
    		}
		} catch (IOException e) {
			throw e;
		} finally {
			if(inStream != null) {
				try {
					inStream.close();
				} catch (IOException ignore) {
				}
			}
		}
    	return properties;
    }

}
