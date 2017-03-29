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

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * <p>Title: CredentialsReaderFactory</p>
 * <p>Description: Defines a factory that creates a new {@link AWSCredentialsReader}.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.aws.jmx.hub.jms.credentials.CredentialsReaderFactory</code></p>
 */

public interface CredentialsReaderFactory {
	/**
	 * Creates a new AWSCredentialsReader
	 * @param credsNode The credentials configuration node
	 * @return the created AWSCredentialsReader
	 */
	public AWSCredentialsReader newReader(final ObjectNode credsNode);
}
