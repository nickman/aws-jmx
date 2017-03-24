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

/**
 * <p>Title: AWSJVMMetaDataServiceMBean</p>
 * <p>Description: JMX MBean interface for {@link AWSJVMMetaDataService} instances</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.aws.jmx.AWSJVMMetaDataServiceMBean</code></p>
 */

public interface AWSJVMMetaDataServiceMBean {
	
	/** The default object name */
	public static final String DEFAULT_OBJECT_NAME = "com.heliosapm.aws.jmx:service=AWSJVMMetaDataService";	
	/** The system property key to override the default object name */
	public static final String SYSPROP_OBJECT_NAME_KEY = "com.heliosapm.aws.jmx.metaobjectname";
	
	
	/**
	 * Returns the meta-data as a JSON string
	 * @return a JSON string
	 * @see com.heliosapm.aws.metadata.MetaDataReader#toJSON()
	 */
	public String toJSON();

	/**
	 * Returns the region this JVM is running in
	 * @return the region this JVM is running in
	 */
	public String getRegion();
	
	/**
	 * Returns the domain this JVM is running in
	 * @return the domain this JVM is running in
	 */
	public String getDomain();
	
	/**
	 * Returns the available endpoints
	 * @return the available endpoints
	 */
	public String[] getAvailableEndpoints();
	
	
	
	/**
	 * Returns the region partition this JVM is running in
	 * @return the region partition this JVM is running in
	 */
	public String getPartition();
	
	
	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getMetaEndpoint()
	 */
	public String getMetaEndpoint();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getAmiId()
	 */
	public String getAmiId();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getAmiLaunchIndex()
	 */
	public String getAmiLaunchIndex();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getAmiManifestPath()
	 */
	public String getAmiManifestPath();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getAmiBlockDevice()
	 */
	public String getAmiBlockDevice();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getRootBlockDevice()
	 */
	public String getRootBlockDevice();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getHostName()
	 */
	public String getHostName();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getLocalHostName()
	 */
	public String getLocalHostName();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getPublicHostName()
	 */
	public String getPublicHostName();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getLocalV4Ip()
	 */
	public String getLocalV4Ip();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getPublicV4Ip()
	 */
	public String getPublicV4Ip();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getMacAddress()
	 */
	public String getMacAddress();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getInstanceId()
	 */
	public String getInstanceId();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getInstanceAction()
	 */
	public String getInstanceAction();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getInstanceType()
	 */
	public String getInstanceType();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getIamInfoLastUpdate()
	 */
	public String getIamInfoLastUpdate();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getIamInfoInstanceProfileArn()
	 */
	public String getIamInfoInstanceProfileArn();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getIamInfoInstanceProfileId()
	 */
	public String getIamInfoInstanceProfileId();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getIamInfoStatus()
	 */
	public String getIamInfoStatus();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getIamCredentialsId()
	 */
	public String getIamCredentialsId();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getIamCredsLastUpdate()
	 */
	public String getIamCredsLastUpdate();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getIamCredsExpiration()
	 */
	public String getIamCredsExpiration();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getIamCredsType()
	 */
	public String getIamCredsType();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getIamCredsStatus()
	 */
	public String getIamCredsStatus();

	/**
	 * @return
	 * @see com.heliosapm.aws.metadata.MetaDataReader#getElapsed()
	 */
	public long getElapsed();

}
