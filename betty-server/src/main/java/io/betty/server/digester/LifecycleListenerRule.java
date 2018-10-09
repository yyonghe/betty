/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.betty.server.digester;


import java.awt.Container;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

import com.google.inject.Injector;

import io.betty.lifecycle.Lifecycle;
import io.betty.lifecycle.LifecycleListener;


/**
 * Rule that creates a new {@link LifecycleListener} and associates it with the
 * top object on the stack which must implement {@link Container}. The
 * implementation class to be used is determined by:
 * <ol>
 * <li>Does the top element on the stack specify an implementation class using
 *     the attribute specified when this rule was created?</li>
 * <li>Does the parent {@link Container} of the {@link Container} on the top of
 *     the stack specify an implementation class using the attribute specified
 *     when this rule was created?</li>
 * <li>Use the default implementation class specified when this rule was
 *     created.</li>
 * </ol>
 */
public class LifecycleListenerRule extends Rule {
	
	private Injector injector;
	
    // --------------------------------------------------------- Public Methods
	
	public LifecycleListenerRule(Injector injector) {
		this.injector = injector;
	}


    /**
     * Handle the beginning of an XML element.
     *
     * @param attributes The attributes of this element
     *
     * @exception Exception if a processing error occurs
     */
    @Override
    public void begin(String namespace, String name, Attributes attributes)
        throws Exception {

    	Lifecycle c = (Lifecycle) digester.peek();

        // Check the container for the specified attribute
        String className = attributes.getValue("className");

        // Instantiate a new LifecycleListener implementation object
        Class<?> clazz = Class.forName(className);
        LifecycleListener listener = (LifecycleListener) injector.getInstance(clazz);

        // Add this LifecycleListener to our associated component
        c.addLifecycleListener(listener);
    }


}
