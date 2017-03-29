// This file is part of OpenTSDB.
// Copyright (C) 2010-2016  The OpenTSDB Authors.
//
// This program is free software: you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 2.1 of the License, or (at your
// option) any later version.  This program is distributed in the hope that it
// will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
// of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
// General Public License for more details.  You should have received a copy
// of the GNU Lesser General Public License along with this program.  If not,
// see <http://www.gnu.org/licenses/>.
package com.heliosapm.aws.jmx.agent;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.remote.JMXServiceURL;
import javax.management.remote.jmxmp.JMXMPConnectorServer;

import com.amazonaws.log.CommonsLogFactory;
import com.amazonaws.log.InternalLogFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.heliosapm.aws.jmx.AWSJVMMetaDataService;
import com.heliosapm.aws.json.JSONUtil;
import com.heliosapm.aws.sqs.SQSManager;
import com.heliosapm.utils.config.ConfigurationHelper;
import com.heliosapm.utils.jmx.JMXHelper;

/**
 * <p>Title: Boot</p>
 * <p>Description: Initializes the agent services</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.aws.jmx.agent.Boot</code></p>
 */

public class Boot {
	/** The system property key to override the default jmxmp listening port */
	public static final String SYSPROP_JMXMP_PORT = "com.heliosapm.aws.jmx.port";
	/** The default jmxmp listening port */
	public static final int DEFAULT_JMXMP_PORT = 0;
	
	
	
	private static JMXMPConnectorServer server;
	private static JMXServiceURL serviceUrl;
	private static final AtomicBoolean initialized = new AtomicBoolean(false);
	private static String publicJmxServiceUrl = null;
	
	public static void init() {
		if(initialized.compareAndSet(false, true)) {
			try {
				InternalLogFactory.configureFactory(CommonsLogFactory.getFactory());
				AWSJVMMetaDataService.getInstance();
				SQSManager.getInstance();
				final int jmxmpPort = ConfigurationHelper.getIntSystemThenEnvProperty(SYSPROP_JMXMP_PORT, DEFAULT_JMXMP_PORT);
				final BindAddress ba = BindAddress.bindAddress(); 
				final String bindAddress = ba.getBindAddress();
				server = JMXHelper.fireUpJMXMPServer(bindAddress, jmxmpPort);
				serviceUrl = server.getAddress();
				publicJmxServiceUrl = ba.getJMXServiceURL(server.getAddress().getPort());
				Runtime.getRuntime().addShutdownHook(new Thread(){
					public void run() {
						sendDownSignal();
					}
				});
				sendUpSignal();
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
				initialized.set(false);
			}
		}
	}
	
	
	private static void sendUpSignal() {
		final ObjectNode rootNode = JSONUtil.parseToObject(AWSJVMMetaDataService.getInstance().toJSON(), ObjectNode.class);
		rootNode.put("eventType", "JVMUp");
		rootNode.put("jmxmpPort", serviceUrl.getPort());
		rootNode.put("jmxmpBind", serviceUrl.getHost());
		rootNode.put("pid", ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
		rootNode.set("sysprops", JSONUtil.serializeToNode(System.getProperties()));
		rootNode.put("jvmstart", ManagementFactory.getRuntimeMXBean().getStartTime());
		rootNode.put("jvmuptime", ManagementFactory.getRuntimeMXBean().getUptime());
		rootNode.put("jmxagentid", JMXHelper.getAgentId());
		rootNode.put("publicjmxserviceurl", publicJmxServiceUrl);
		rootNode.set("keypairs", JSONUtil.serializeToNode(AWSJVMMetaDataService.getInstance().getPublicKeys()));
		rootNode.put("region", AWSJVMMetaDataService.getInstance().getRegion());
		SQSManager.getInstance().sendMessage(JSONUtil.serializeToString(rootNode));
	}
	
	private static void sendDownSignal() {
		final ObjectNode rootNode = JSONUtil.newObjectNode();
		rootNode.put("eventType", "JVMDown");
		rootNode.put("jmxmpPort", serviceUrl.getPort());
		rootNode.put("jmxmpBind", serviceUrl.getHost());
		rootNode.put("pid", ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);		
		rootNode.put("jvmstart", ManagementFactory.getRuntimeMXBean().getStartTime());
		rootNode.put("jvmuptime", ManagementFactory.getRuntimeMXBean().getUptime());
		rootNode.put("jmxagentid", JMXHelper.getAgentId());
		rootNode.put("instanceId", AWSJVMMetaDataService.getInstance().getInstanceId());
		rootNode.put("publicjmxserviceurl", publicJmxServiceUrl);
		rootNode.put("region", AWSJVMMetaDataService.getInstance().getRegion());
		SQSManager.getInstance().sendMessage(JSONUtil.serializeToString(rootNode));
	}
	
	

	public Boot() {}

}
