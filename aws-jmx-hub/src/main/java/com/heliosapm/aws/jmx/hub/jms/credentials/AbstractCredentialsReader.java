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
package com.heliosapm.aws.jmx.hub.jms.credentials;

import java.net.URL;

import com.amazonaws.auth.AWSCredentials;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.heliosapm.utils.url.URLHelper;

/**
 * <p>Title: AbstractCredentialsReader</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.aws.jmx.hub.jms.credentials.AbstractCredentialsReader</code></p>
 */

public abstract class AbstractCredentialsReader implements AWSCredentialsReader, AWSCredentials {
	/** The AWS Access Key Id */
	protected String keyId = null;
	/** The AWS Secret Key */
	protected String secretKey = null;
	/** The credentials configuration node */
	protected final ObjectNode credsNode;
	/** The URL to read from */
	final URL url;
	/** The reference key for the key id */
	final String keyIdPropsKey;
	/** The reference key for the secret key */
	final String secretKeyPropsKey;
	
	
	
	/**
	 * Creates a new AbstractCredentialsReader
	 * @param credsNode The credentials configuration node
	 */
	protected AbstractCredentialsReader(final ObjectNode credsNode) {
		if(credsNode==null) throw new IllegalArgumentException("The passed credentials configuration node was null");
		this.credsNode = credsNode;
		if(credsNode.has("url")) {
			url = URLHelper.toURL(credsNode.get("url").textValue());
		} else {
			url = null;
		}
		keyIdPropsKey = credsNode.has(KEY_ID) ? credsNode.get(KEY_ID).textValue() : KEY_ID;
		secretKeyPropsKey = credsNode.has(SECRET_KEY) ? credsNode.get(SECRET_KEY).textValue() : SECRET_KEY;
		refresh();
	}
	
	/**
	 * {@inheritDoc}
	 * @see com.amazonaws.auth.AWSCredentials#getAWSSecretKey()
	 */
	@Override
	public String getAWSSecretKey() {		
		return secretKey;
	}

	/**
	 * {@inheritDoc}
	 * @see com.amazonaws.auth.AWSCredentials#getAWSAccessKeyId()
	 */
	@Override
	public String getAWSAccessKeyId() {		
		return keyId;
	}
	
	/**
	 * {@inheritDoc}
	 * @see com.amazonaws.auth.AWSCredentialsProvider#refresh()
	 */
	@Override
	public void refresh() {
		final String[] creds = readCredentials(credsNode);
		keyId = creds[0];
		secretKey = creds[1];
	}
	
	/**
	 * {@inheritDoc}
	 * @see com.amazonaws.auth.AWSCredentialsProvider#getCredentials()
	 */
	@Override
	public AWSCredentials getCredentials() {
		return this;
	}
}
