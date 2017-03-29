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

import com.amazonaws.auth.AWSCredentialsProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * <p>Title: AWSCredentialsReader</p>
 * <p>Description: Defines the configured credential reader</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.aws.jmx.hub.jms.credentials.AWSCredentialsReader</code></p>
 */

public interface AWSCredentialsReader extends AWSCredentialsProvider {

	public static final String KEY_ID = "accesskeyid";
	public static final String SECRET_KEY = "secretkey";
	
	
	public String[] readCredentials(ObjectNode credsNode);
}
