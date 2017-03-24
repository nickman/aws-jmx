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
package com.heliosapm.aws.sqs;

/**
 * <p>Title: SQSManagerMBean</p>
 * <p>Description: JMX MBean interface for {@link SQSManager}</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.aws.sqs.SQSManagerMBean</code></p>
 */

public interface SQSManagerMBean {
	/** The default object name */
	public static final String DEFAULT_OBJECT_NAME = "com.heliosapm.aws.jmx:service=SQSManager";	
	/** The system property key to override the default object name */
	public static final String SYSPROP_OBJECT_NAME_KEY = "com.heliosapm.aws.jmx.sqsobjectname";
	
	/** The default queue name */
	public static final String DEFAULT_QUEUE_NAME = "jmx-agents";	
	/** The system property key to override the default queue name */
	public static final String SYSPROP_QUEUE_NAME_KEY = "com.heliosapm.aws.sqs.qname";
	
	/**
	 * Returns the SQS http endpoint
	 * @return the SQS http endpoint
	 */
	public String getSqsEndpoint();
	
	
	/**
	 * Returns the default queue name
	 * @return the default queue name
	 */
	public String getDefaultQueueName();
	
	/**
	 * Prepares and sends an SQS message
	 * @param queueName The optional queue name. If null, uses the default queue.
	 * @param message The message to send
	 * @param jsonMap An optional map of message attributes expressed in JSON format
	 * @return A json string representing the send results
	 */
	public String sendMessage(final String queueName, final String message, final String jsonMap);
	
	/**
	 * Prepares and sends an SQS message to the default queue
	 * @param message The message to send
	 * @param jsonMap An optional map of message attributes expressed in JSON format
	 * @return A json string representing the send results
	 */
	public String sendMessage(final String message, final String jsonMap);
	
	/**
	 * Prepares and sends an SQS message to the default queue
	 * @param message The message to send
	 * @return A json string representing the send results
	 */
	public String sendMessage(final String message);
	
	

}
