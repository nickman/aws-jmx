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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * <p>Title: CredentialType</p>
 * <p>Description: Functional enumeration for reading credentials from the hub configuration JSON</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.aws.jmx.hub.jms.credentials.CredentialType</code></p>
 */

public enum CredentialType implements CredentialsReaderFactory {
	VALUES{
		@Override
		public AWSCredentialsReader newReader(final ObjectNode credsNode) {
			return new ProvidedCredentialsReader(credsNode);
		}
	},
	SYSPROP{
		@Override
		public AWSCredentialsReader newReader(final ObjectNode credsNode) {
			return new SysPropsCredentialsReader(credsNode);
		}
	},
	ENV{
		@Override
		public AWSCredentialsReader newReader(final ObjectNode credsNode) {
			return new EnvironmentCredentialsReader(credsNode);
		}
	},
	PROPSURL{
		@Override
		public AWSCredentialsReader newReader(final ObjectNode credsNode) {
			return new PropertiesURLCredentialsReader(credsNode);
		}
	},
	JSONURL{
		@Override
		public AWSCredentialsReader newReader(final ObjectNode credsNode) {
			return new JsonURLCredentialsReader(credsNode);
		}
	};

	
	/**
	 * Returns a credentials provider for the passed node
	 * @param credsNode The credentials configuration node
	 * @return a credentials provider
	 */
	public static AWSCredentialsProvider getCredentialsProvider(final ObjectNode credsNode) {
		if(credsNode==null) throw new IllegalArgumentException("The passed credentials configuration node was null");
		CredentialType ct;
		try {
			ct = valueOf(credsNode.get("type").textValue().trim().toUpperCase());
		} catch (Exception ex) {
			ct = SYSPROP;
		}
		final AWSCredentialsReader reader = ct.newReader(credsNode);
		
		return new AWSCredentialsProvider() {
			@Override
			public AWSCredentials getCredentials() {				
				return reader.getCredentials();
			}
			@Override
			public void refresh() {
				reader.refresh();				
			}
			
		};
	}
	

}
