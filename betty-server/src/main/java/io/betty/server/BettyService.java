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


package io.betty.server;

import co.paralleluniverse.fibers.SuspendExecution;
import io.betty.lifecycle.Lifecycle;
import io.netty.channel.ChannelHandlerContext;
import kilim.Pausable;

/**
 * A <strong>Service</strong> is a group of one or more
 * <strong>Connectors</strong> that share a single <strong>Container</strong>
 * to process their incoming requests.  This arrangement allows, for example,
 * a non-SSL and SSL connector to share the same population of web apps.
 * <p>
 * A given JVM can contain any number of Service instances; however, they are
 * completely independent of each other and share only the basic JVM facilities
 * and classes on the system class path.
 *
 * @author Craig R. McClanahan
 */
public interface BettyService extends Lifecycle {

    // ------------------------------------------------------------- Properties

    /**
     * @return the name of this Service.
     */
    public String getName();

    /**
     * Set the name of this Service.
     *
     * @param name The new service name
     */
    public void setName(String name);

    /**
     * @return the <code>Server</code> with which we are associated (if any).
     */
    public BettyServer getServer();

    /**
     * Set the <code>Server</code> with which we are associated (if any).
     *
     * @param server The server that owns this Service
     */
    public void setServer(BettyServer server);

    /**
     * @return The service handled subcmd.
     */
	public String getSubcmd();
	
	/**
	 * service...
	 * @param ctx
	 * @param object
	 * @throws Pausable
	 * @throws Exception
	 */
	public void service(ChannelHandlerContext ctx, BettyServerContext bctx)throws Pausable, SuspendExecution, Exception;
	
	/**
	 * FF log
	 * @param ctx
	 * @param bctx
	 * @return
	 * @throws Exception
	 */
	public String formatflow(BettyServerContext bctx)throws Exception;
}
