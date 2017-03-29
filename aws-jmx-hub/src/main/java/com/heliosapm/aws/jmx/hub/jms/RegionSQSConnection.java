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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;

import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

/**
 * <p>Title: RegionSQSConnection</p>
 * <p>Description: Wraps all the JMS objects for a region connection</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.aws.jmx.hub.jms.RegionSQSConnection</code></p>
 */

public class RegionSQSConnection implements ExceptionListener, MessageListener {
	protected ConnectionFactory connectionFactory;
	protected Set<Queue> queues;
	protected Map<String, MessageConsumer> messageConsumers; 
	protected Connection connection = null;
	protected Map<String, Session> sessions;
	protected Set<String> queueNames;
	protected MessageListener outerListener = null;
	protected final Map<String, LongAdder> queueMessageCounts;
	protected final Regions region;
	
	
	
	
	/**
	 * Creates a new RegionSQSConnection
	 */
	public RegionSQSConnection(				
				final Regions region,
				final AWSCredentialsProvider awsCredentialsProvider, 
				final ClientConfiguration clientConfig, 
				final int prefetch,
				final Set<String> queueNames,
				final MessageListener listener
			) {
		outerListener = listener;
		this.queueNames = queueNames;
		this.region = region;
		queueMessageCounts = new HashMap<String, LongAdder>(queueNames.size());
		sessions = new ConcurrentHashMap<String, Session>(queueNames.size());
		messageConsumers = new HashMap<String, MessageConsumer>(queueNames.size());		
		connectionFactory = SQSConnectionFactory.builder()
			.withAWSCredentialsProvider(awsCredentialsProvider)
			.withClientConfiguration(clientConfig)
			.withNumberOfMessagesToPrefetch(prefetch)
			.withRegion(Region.getRegion(region))
			.build();
		init();
	}
	
	protected void init() {
		try {			
			connection = connectionFactory.createConnection();
			connection.setExceptionListener(this);
			for(final String queueName : queueNames) {
				final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				sessions.put(queueName, session);
				queueMessageCounts.put(queueName, new LongAdder());
				final Queue queue = session.createQueue(queueName);
				final MessageConsumer consumer = session.createConsumer(queue);
				consumer.setMessageListener(this);
				messageConsumers.put(queueName, consumer);
			}
			connection.start();
		} catch (Exception ex) {
			cleanup();
			throw new RuntimeException("Failed to create RegionSQSConnection for [" + region.name() + "]", ex);
		}		
	}
	
	protected void cleanup() {
		for(MessageConsumer consumer: messageConsumers.values()) {
			try { consumer.close(); } catch (Exception x) {/* No Op */}
		}
		messageConsumers.clear();
		for(Session session: sessions.values()) {
			try { session.close(); } catch (Exception x) {/* No Op */}
		}
		sessions.clear();
		try { connection.close(); } catch (Exception x) {/* No Op */}
	}
	
	public void close() {
		cleanup();
	}
	
	@Override
	public void onException(final JMSException exception) {
		exception.printStackTrace(System.err);
		
	}
	
	@Override
	public void onMessage(final Message message) {		
		try {
			final Queue q = (Queue)message.getJMSDestination();
			//System.out.println("Message from [" + q.getQueueName() + "]");
			queueMessageCounts.get(q.getQueueName()).increment();
		} catch (JMSException jex) {
			// TODO
		}
		outerListener.onMessage(message);
	}

}
