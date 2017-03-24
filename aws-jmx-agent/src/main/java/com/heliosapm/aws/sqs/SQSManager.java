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

import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.heliosapm.aws.jmx.AWSJVMMetaDataService;
import com.heliosapm.aws.json.JSONUtil;
import com.heliosapm.utils.collections.FluentMap;
import com.heliosapm.utils.config.ConfigurationHelper;
import com.heliosapm.utils.jmx.JMXHelper;

/**
 * <p>Title: SQSManager</p>
 * <p>Description: The agent's SQS sender</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.aws.sqs.SQSManager</code></p>
 */

public class SQSManager implements SQSManagerMBean {
	/** The singleton instance */
	private static volatile SQSManager instance = null;
	/** The singleton instance ctor lock */
	private static final Object lock = new Object();
	
	private final ObjectName objectName;
	private final AWSJVMMetaDataService metaService;
	private final String sqsEndpoint;
	private final String defaultQueueName;
	private final AmazonSQS sqs;
	
	/**
	 * Acquires the SQSManager singleton instance
	 * @return the SQSManager singleton instance
	 */
	public static SQSManager getInstance() {
		if(instance==null) {
			synchronized (lock) {
				if(instance==null) {
					instance = new SQSManager();
				}
			}
		}
		return instance;
	}
	/**
	 * Creates a new SQSManager
	 */
	private SQSManager() {
		final String on = ConfigurationHelper.getSystemThenEnvProperty(SYSPROP_OBJECT_NAME_KEY, DEFAULT_OBJECT_NAME);
		defaultQueueName = ConfigurationHelper.getSystemThenEnvProperty(SYSPROP_QUEUE_NAME_KEY, DEFAULT_QUEUE_NAME);
		objectName = JMXHelper.objectName(on);
		try { JMXHelper.unregisterMBean(objectName); } catch (Exception x) {/* No Op */}
		JMXHelper.registerMBean(this, objectName);
		metaService = AWSJVMMetaDataService.getInstance();
		sqsEndpoint = "https://sqs." + metaService.getRegion() + ".amazonaws.com";
		sqs = AmazonSQSClient.builder()
				.withRegion(metaService.getRegion())
				.build();
		
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.sqs.SQSManagerMBean#getSqsEndpoint()
	 */
	@Override
	public String getSqsEndpoint() {
		return sqsEndpoint;
	}
	
	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.sqs.SQSManagerMBean#getDefaultQueueName()
	 */
	@Override
	public String getDefaultQueueName() {
		return defaultQueueName;
	}
	
	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.sqs.SQSManagerMBean#sendMessage(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String sendMessage(final String queueName, final String message, final String jsonMap) {
		if(message==null || message.trim().isEmpty()) throw new IllegalArgumentException("The passed message was null or empty");
		final String _q = queueName==null ? defaultQueueName : queueName.trim();
		final SendMessageRequest smr = new SendMessageRequest(_q, message.trim());
		if(jsonMap!=null) {
			final Map<String, Object> headers = JSONUtil.parseToObject(jsonMap.trim(), JSONUtil.TR_STR_OBJ_HASH_MAP);
			if(headers.size() > 10) throw new IllegalArgumentException("Number of message attributes [" + headers.size() + "] is over the maximum of 10");
			for(Map.Entry<String, Object> entry: headers.entrySet()) {
				final MessageAttributeValue mav = new MessageAttributeValue();
				mav.setDataType("String");
				mav.setStringValue(JSONUtil.serializeToString(entry.getValue()));
				smr.addMessageAttributesEntry(entry.getKey(), mav);
			}
		}
		final SendMessageResult result = sqs.sendMessage(smr);
		final HashMap<String, String> resultMap = new HashMap<String, String>(
				FluentMap.newMap(FluentMap.MapType.HASH, String.class, String.class)
					.fput("messageid", result.getMessageId())
					.fput("requestid", result.getSdkResponseMetadata().getRequestId())
					.fput("sequence", result.getSequenceNumber())
				
		);
		return JSONUtil.serializeToString(resultMap);
	}
	
	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.sqs.SQSManagerMBean#sendMessage(java.lang.String, java.lang.String)
	 */
	@Override
	public String sendMessage(final String message, final String jsonMap) {
		return sendMessage(null, message, jsonMap);
	}
	
	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.sqs.SQSManagerMBean#sendMessage(java.lang.String)
	 */
	@Override
	public String sendMessage(final String message) {
		return sendMessage(null, message, null);
	}
	

}
