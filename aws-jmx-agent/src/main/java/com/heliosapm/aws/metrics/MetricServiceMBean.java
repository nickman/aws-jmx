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

/**
 * <p>Title: MetricServiceMBean</p>
 * <p>Description: JMX MBean for the {@link MetricService} instance</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.aws.metrics.MetricServiceMBean</code></p>
 */

public interface MetricServiceMBean {
	/** The default object name */
	public static final String DEFAULT_OBJECT_NAME = "com.heliosapm.aws.jmx:service=MetricService";	
	/** The system property key to override the default object name */
	public static final String SYSPROP_OBJECT_NAME_KEY = "com.heliosapm.aws.jmx.metricobjectname";

}
