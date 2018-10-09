package io.betty.server.digester;

import org.apache.commons.digester.ObjectCreateRule;
import org.xml.sax.Attributes;

import com.google.inject.Injector;

public class GuiceObjectCreateRule extends ObjectCreateRule {
	
	private Injector injector;

	public GuiceObjectCreateRule(Injector injector, String className) {
		super(className);
		this.injector = injector;
	}

	@Override
	public void begin(Attributes attributes) throws Exception {

        // Identify the name of the class to instantiate
        String realClassName = className;
        if (attributeName != null) {
            String value = attributes.getValue(attributeName);
            if (value != null) {
                realClassName = value;
            }
        }
        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug("[ObjectCreateRule]{" + digester.getMatch() +
                    "}New " + realClassName);
        }

        // Instantiate the new object and push it on the context stack
        Class<?> clazz = digester.getClassLoader().loadClass(realClassName);
        Object instance = injector.getInstance(clazz);
        digester.push(instance);

    }
}
