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
package com.heliosapm.aws.jmx;

import java.util.Collection;

import javax.management.ObjectName;

import com.amazonaws.regions.Regions;
import com.heliosapm.aws.metadata.MetaDataReader;
import com.heliosapm.utils.config.ConfigurationHelper;
import com.heliosapm.utils.io.StdInCommandHandler;
import com.heliosapm.utils.jmx.JMXHelper;

/**
 * <p>Title: AWSJVMMetaDataService</p>
 * <p>Description: JMX MBean service to expose the JVM's AWS EC2 meta data</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.aws.jmx.AWSJVMMetaDataService</code></p>
 */

public class AWSJVMMetaDataService implements AWSJVMMetaDataServiceMBean {
	private static volatile AWSJVMMetaDataService instance = null;
	private static final Object lock = new Object();
	
	private final MetaDataReader reader;
	private final ObjectName objectName;
	private final String region;
	private final String domain;
	private final String partition;
	private final String[] endpoints;
	
	
	public static AWSJVMMetaDataService getInstance() {
		if(instance==null) {
			synchronized(lock) {
				if(instance==null) {
					instance = new AWSJVMMetaDataService(); 
				}
			}
		}
		return instance;
	}
	
	/**
	 * Creates a new AWSJVMMetaDataService
	 */
	private AWSJVMMetaDataService() {
		reader = new MetaDataReader();
		region = region();
		domain = domain();
		partition = partition();
		endpoints = availableEndpoints();
		final String on = ConfigurationHelper.getSystemThenEnvProperty(SYSPROP_OBJECT_NAME_KEY, DEFAULT_OBJECT_NAME);
		objectName = JMXHelper.objectName(on);
		try { JMXHelper.unregisterMBean(objectName); } catch (Exception x) {/* No Op */}
		JMXHelper.registerMBean(this, objectName);
	}
	
	public static void main(String[] args) {
		if(args.length > 0) {
			if(!System.getProperties().containsKey(MetaDataReader.PUB_CONFIG_KEY)) {
				System.setProperty(MetaDataReader.PUB_CONFIG_KEY, args[0]);
			}
		}
		AWSJVMMetaDataService.getInstance();
		System.out.println("Registered AWSJVMMetaDataService");
		JMXHelper.fireUpJMXMPServer(8006);
		StdInCommandHandler.getInstance().run();
	}
	
	private String region() {
		try {
			return Regions.getCurrentRegion().getName();
		} catch (Exception ex) {
			return null;
		}		
	}

	
	private String partition() {		
		try {
			return Regions.getCurrentRegion().getPartition();
		} catch (Exception ex) {
			return null;
		}				
	}
	
	private String domain() {
		try {
			return Regions.getCurrentRegion().getDomain();
		} catch (Exception ex) {
			return null;
		}				
	}	
	
	private String[] availableEndpoints() {
		try {
			final Collection<String> eps = Regions.getCurrentRegion().getAvailableEndpoints(); 
			return eps.toArray(new String[0]);
		} catch (Exception ex) {
			return new String[0];
		}						
	}
	
	
	
	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getRegion()
	 */
	@Override
	public String getRegion() {
		return region;
	}
	
	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getPartition()
	 */
	@Override
	public String getPartition() {
		return partition;
	}
	
	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getDomain()
	 */
	@Override
	public String getDomain() {
		return domain;
	}	

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getAvailableEndpoints()
	 */
	@Override
	public String[] getAvailableEndpoints() {
		return endpoints;
	}
	
	
	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#toJSON()
	 */
	public String toJSON() {
		return reader.toJSON();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getMetaEndpoint()
	 */
	public String getMetaEndpoint() {
		return reader.getMetaEndpoint();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getAmiId()
	 */
	public String getAmiId() {
		return reader.getAmiId();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getAmiLaunchIndex()
	 */
	public String getAmiLaunchIndex() {
		return reader.getAmiLaunchIndex();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getAmiManifestPath()
	 */
	public String getAmiManifestPath() {
		return reader.getAmiManifestPath();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getAmiBlockDevice()
	 */
	public String getAmiBlockDevice() {
		return reader.getAmiBlockDevice();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getRootBlockDevice()
	 */
	public String getRootBlockDevice() {
		return reader.getRootBlockDevice();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getHostName()
	 */
	public String getHostName() {
		return reader.getHostName();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getLocalHostName()
	 */
	public String getLocalHostName() {
		return reader.getLocalHostName();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getPublicHostName()
	 */
	public String getPublicHostName() {
		return reader.getPublicHostName();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getLocalV4Ip()
	 */
	public String getLocalV4Ip() {
		return reader.getLocalV4Ip();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getPublicV4Ip()
	 */
	public String getPublicV4Ip() {
		return reader.getPublicV4Ip();
	}
	
	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getPublicKeys()
	 */
	@Override
	public String[] getPublicKeys() {		
		return reader.getPublicKeys();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getMacAddress()
	 */
	public String getMacAddress() {
		return reader.getMacAddress();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getInstanceId()
	 */
	public String getInstanceId() {
		return reader.getInstanceId();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getInstanceAction()
	 */
	public String getInstanceAction() {
		return reader.getInstanceAction();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getInstanceType()
	 */
	public String getInstanceType() {
		return reader.getInstanceType();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getIamInfoLastUpdate()
	 */
	public String getIamInfoLastUpdate() {
		return reader.getIamInfoLastUpdate();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getIamInfoInstanceProfileArn()
	 */
	public String getIamInfoInstanceProfileArn() {
		return reader.getIamInfoInstanceProfileArn();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getIamInfoInstanceProfileId()
	 */
	public String getIamInfoInstanceProfileId() {
		return reader.getIamInfoInstanceProfileId();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getIamInfoStatus()
	 */
	public String getIamInfoStatus() {
		return reader.getIamInfoStatus();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getIamCredentialsId()
	 */
	public String getIamCredentialsId() {
		return reader.getIamCredentialsId();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getIamCredsLastUpdate()
	 */
	public String getIamCredsLastUpdate() {
		return reader.getIamCredsLastUpdate();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getIamCredsExpiration()
	 */
	public String getIamCredsExpiration() {
		return reader.getIamCredsExpiration();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getIamCredsType()
	 */
	public String getIamCredsType() {
		return reader.getIamCredsType();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getIamCredsStatus()
	 */
	public String getIamCredsStatus() {
		return reader.getIamCredsStatus();
	}

	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean#getElapsed()
	 */
	public long getElapsed() {
		return reader.getElapsed();
	}

}
