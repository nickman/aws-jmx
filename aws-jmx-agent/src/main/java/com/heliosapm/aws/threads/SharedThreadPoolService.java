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
package com.heliosapm.aws.threads;

import java.util.concurrent.ExecutorService;

import javax.management.ObjectName;

import com.amazonaws.client.builder.ExecutorFactory;
import com.heliosapm.utils.config.ConfigurationHelper;
import com.heliosapm.utils.jmx.JMXHelper;
import com.heliosapm.utils.jmx.JMXManagedThreadPool;



/**
 * <p>Title: SharedThreadPoolService</p>
 * <p>Description: The agent's shared thread pool</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.aws.threads.SharedThreadPoolService</code></p>
 */

public class SharedThreadPoolService implements ExecutorFactory {
	/** The singleton instance */
	private static volatile SharedThreadPoolService instance = null;
	/** The singleton instance ctor lock */
	private static final Object lock = new Object();
	/** The default object name */
	public static final String DEFAULT_OBJECT_NAME = "com.heliosapm.aws.jmx:service=SharedThreadPool";	
	/** The system property key to override the default object name */
	public static final String SYSPROP_OBJECT_NAME_KEY = "com.heliosapm.aws.jmx.tpoolobjectname";
	
	
	private final ObjectName objectName;
	private final JMXManagedThreadPool threadPool;

	/**
	 * Acquires the SharedThreadPoolService singleton instance
	 * @return the SharedThreadPoolService singleton instance
	 */
	public static SharedThreadPoolService getInstance() {
		if(instance==null) {
			synchronized (lock) {
				if(instance==null) {
					instance = new SharedThreadPoolService();
				}
			}
		}
		return instance;
	}
	

	/**
	 * Creates a new SharedThreadPoolService
	 */
	private SharedThreadPoolService() {
		final String on = ConfigurationHelper.getSystemThenEnvProperty(SYSPROP_OBJECT_NAME_KEY, DEFAULT_OBJECT_NAME);
		objectName = JMXHelper.objectName(on);
		threadPool = new JMXManagedThreadPool(objectName, "SharedThreadPool", 1, Runtime.getRuntime().availableProcessors() * 2, 128, 60000, 100, 99, true);
		threadPool.prestartAllCoreThreads();
	}
	
	/**
	 * {@inheritDoc}
	 * @see com.amazonaws.client.builder.ExecutorFactory#newExecutor()
	 */
	@Override
	public ExecutorService newExecutor() {		
		return threadPool;
	}

}
