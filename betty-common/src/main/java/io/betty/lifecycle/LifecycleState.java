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

/**
 * The list of valid states for components that implement {@link Lifecycle}.
 * See {@link Lifecycle} for the state transition diagram.
 */
public enum LifecycleState {
	/*
	 * starup state
	 */
    NEW(false, null),
    INITIALIZING(false, Lifecycle.BEFORE_INIT_EVENT),
    INITIALIZED(false, Lifecycle.AFTER_INIT_EVENT),
    CONFIGURE_START_EVENT(false, Lifecycle.CONFIGURE_START_EVENT),
    STARTING_PREP(false, Lifecycle.BEFORE_START_EVENT),
    STARTING(true, Lifecycle.START_EVENT),
    STARTED(true, Lifecycle.AFTER_START_EVENT),
    STOPPING_PREP(true, Lifecycle.BEFORE_STOP_EVENT),
    STOPPING(false, Lifecycle.STOP_EVENT),
    STOPPED(false, Lifecycle.AFTER_STOP_EVENT),
    DESTROYING(false, Lifecycle.BEFORE_DESTROY_EVENT),
    DESTROYED(false, Lifecycle.AFTER_DESTROY_EVENT),
    FAILED(false, null),
    /*
     * request handle state
     */
    REQUEST_HANDLED_EVENT(false, Lifecycle.REQUEST_HANDLED_EVENT),
    SERVER_OVERLOAD_EVENT(false, Lifecycle.SERVER_OVERLOAD_EVENT),
    REQUEST_RETRY_HANDLED_EVENT(false, Lifecycle.REQUEST_RETRY_HANDLED_EVENT),
    DISPATCH_TO_EXECUTE_FAILED_EVENT(false, "dispatch_to_execute_failed"),
    
    DEFAULT_SOURCE_EVENT(false, Lifecycle.DEFAULT_SOURCE_EVENT)
    ;

    private final boolean available;
    private final String lifecycleEvent;

    private LifecycleState(boolean available, String lifecycleEvent) {
        this.available = available;
        this.lifecycleEvent = lifecycleEvent;
    }

    /**
     * May the public methods other than property getters/setters and lifecycle
     * methods be called for a component in this state? It returns
     * <code>true</code> for any component in any of the following states:
     * <ul>
     * <li>{@link #STARTING}</li>
     * <li>{@link #STARTED}</li>
     * <li>{@link #STOPPING_PREP}</li>
     * </ul>
     *
     * @return <code>true</code> if the component is available for use,
     *         otherwise <code>false</code>
     */
    public boolean isAvailable() {
        return available;
    }

    public String getLifecycleEvent() {
        return lifecycleEvent;
    }
}
