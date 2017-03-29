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
package com.heliosapm.aws.jmx.hub.jms;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.TextMessage;

import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.databind.JsonNode;
import com.heliosapm.aws.jmx.hub.consul.ConsulService;
import com.heliosapm.aws.json.JSONUtil;
import com.heliosapm.utils.io.StdInCommandHandler;

/**
 * <p>Title: JMSListener</p>
 * <p>Description: AWS SQS JMS event listener</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.aws.jmx.hub.jms.JMSListener</code></p>
 */

public class JMSListener implements MessageListener {
	/** A map of JMS connection factories keyed by the region */
	protected final EnumMap<Regions, RegionSQSConnection> regionConnections = new EnumMap<Regions, RegionSQSConnection>(Regions.class); 

	protected final ConsulService consul;
	/**
	 * Creates a new JMSListener
	 */
	public JMSListener() {
		consul = new ConsulService();
		final Configuration cfg = Configuration.readConfiguration();
		for(Map.Entry<Regions, Set<String>> entry: cfg.getRegionQueues().entrySet()) {
			RegionSQSConnection conn = new RegionSQSConnection(				
					entry.getKey(),
					cfg.getCredentialsProvider(), 
					cfg.getClientConfiguration(),
					cfg.getPrefetch(),
					entry.getValue(),
					this
				);
			regionConnections.put(entry.getKey(), conn);
		}
		System.out.println("JMSListener Started");
	}
	
	public static void main(String[] args) {
		final JMSListener jms = new JMSListener();
		StdInCommandHandler.getInstance().registerCommand("stop", new Runnable(){
			public void run() {
				try { 
					jms.consul.close();
				} catch (Exception x) {}
				for(RegionSQSConnection conn: jms.regionConnections.values()) {
					conn.close();
				}
				jms.regionConnections.clear();
				System.out.println("JMSListener Stopped");
			}
		});
	}

	@Override
	public void onMessage(final Message message) {
		if(message instanceof TextMessage) {
			try {
				final String text = ((TextMessage)message).getText();
				System.out.println(text);
				final JsonNode on = JSONUtil.parseToNode(text);
				if(on.has("eventType")) {
					final String eventType = on.get("eventType").textValue();
					if("JVMUp".equalsIgnoreCase(eventType)) {
						try { 
							consul.register(on);
						} catch (Exception x) {
							x.printStackTrace(System.err);
						}
					} else if("JVMDown".equalsIgnoreCase(eventType)) {
						try { 
							consul.deregister(on);
						} catch (Exception x) {
							x.printStackTrace(System.err);
						}
					}
					final String agentId = on.get("jmxagentid").textValue();
					final String region = on.get("region").textValue();
					System.out.println(String.format("Received event [%s] from agent [%s] in region [%s]", eventType, agentId, region));											
				}
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
			}
		} else {
			System.err.println("Unexpected message type:" + message);
		}
	}
	
}
