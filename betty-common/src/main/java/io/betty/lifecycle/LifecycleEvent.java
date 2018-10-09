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
package io.betty.lifecycle;

import java.util.EventObject;

/**
 * General event for notifying listeners of significant changes on a component
 * that implements the Lifecycle interface.
 *
 * @author Craig R. McClanahan
 */
public class LifecycleEvent extends EventObject {

    private static final long serialVersionUID = 1L;


    /**
     * Construct a new LifecycleEvent with the specified parameters.
     *
     * @param lifecycle Component on which this event occurred
     * @param type Event type (required)
     * @param data Event data (if any)
     */
    public LifecycleEvent(Lifecycle lifecycle, LifecycleState state, Object data) {
        super(lifecycle);
        this.state = state;
        this.data = data;
    }


    /**
     * The event data associated with this event.
     */
    protected Object data;


    /**
     * The event type this instance represents.
     */
    protected LifecycleState state;


    /**
     * @return the event data of this event.
     */
    public Object getData() {
        return data;
    }


    /**
     * @return the Lifecycle on which this event occurred.
     */
    public Lifecycle getLifecycle() {
        return (Lifecycle) getSource();
    }


    /**
     * @return the event type of this event.
     */
    public LifecycleState getType() {
        return this.state;
    }


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[source=");
		builder.append(((Lifecycle)source).getName());
		builder.append(", state=");
		builder.append(state);
		builder.append(", data=");
		builder.append(data);
		builder.append("]");
		return builder.toString();
	}
    
}
