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
package com.heliosapm.aws.metrics;

import javax.management.ObjectName;

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClientBuilder;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.PutMetricDataResult;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.heliosapm.aws.jmx.AWSJVMMetaDataService;
import com.heliosapm.aws.threads.SharedThreadPoolService;
import com.heliosapm.utils.config.ConfigurationHelper;
import com.heliosapm.utils.jmx.JMXHelper; 

/**
 * <p>Title: MetricService</p>
 * <p>Description: Service for submitting metrics to Cloudwatch</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.aws.metrics.MetricService</code></p>
 */

public class MetricService implements MetricServiceMBean, AsyncHandler<PutMetricDataRequest,PutMetricDataResult> {
	/** The singleton instance */
	private static volatile MetricService instance = null;
	/** The singleton instance ctor lock */
	private static final Object lock = new Object();
	
	private final ObjectName objectName;
	private final AmazonCloudWatchAsync asyncClient;
	private final MetricRegistry registry = new MetricRegistry();
	private final Meter errorMeter = registry.meter("MetricService.Submission.Errors");
	private final Meter submissionMeter = registry.meter("MetricService.Submission.Submitted");
	private final JmxReporter jmxReporter = JmxReporter.forRegistry(registry)
		.registerWith(JMXHelper.getHeliosMBeanServer())
		.build();
	
	
	
	/**
	 * Acquires the MetricService singleton instance
	 * @return the MetricService singleton instance
	 */
	public static MetricService getInstance() {
		if(instance==null) {
			synchronized (lock) {
				if(instance==null) {
					instance = new MetricService();
				}
			}
		}
		return instance;
	}

	/**
	 * Creates a new MetricService
	 */
	public MetricService() {
		final String on = ConfigurationHelper.getSystemThenEnvProperty(SYSPROP_OBJECT_NAME_KEY, DEFAULT_OBJECT_NAME);
		objectName = JMXHelper.objectName(on);
		try { JMXHelper.unregisterMBean(objectName); } catch (Exception x) {/* No Op */}
		JMXHelper.registerMBean(this, objectName);
		jmxReporter.start();
		asyncClient = AmazonCloudWatchAsyncClientBuilder.standard()
				.withExecutorFactory(SharedThreadPoolService.getInstance())
				.withRegion(AWSJVMMetaDataService.getInstance().getRegion())
				.build();

	}
	
	@Override
	public void onError(final Exception ex) {
		errorMeter.mark();
		System.err.println("MetricService error:" + ex + ". Stack trace follows.");
		ex.printStackTrace(System.err);
		
	}
	
	@Override
	public void onSuccess(final PutMetricDataRequest request, final PutMetricDataResult result) {
		submissionMeter.mark(request.getMetricData().size());
	}

}
