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

import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.heliosapm.aws.json.JSONUtil;
import com.heliosapm.utils.url.URLHelper;

/**
 * <p>Title: JsonURLCredentialsReader</p>
 * <p>Description: Reads credentials from the provided json URL in the credentials configuration node field named <b>url</b></p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.aws.jmx.hub.jms.credentials.JsonURLCredentialsReader</code></p>
 */

public class JsonURLCredentialsReader extends PropertiesURLCredentialsReader {

	/**
	 * Creates a new JsonURLCredentialsReader
	 * @param credsNode The credentials configuration node
	 */
	public JsonURLCredentialsReader(final ObjectNode credsNode) {
		super(credsNode);
	}

	
	/**
	 * {@inheritDoc}
	 * @see com.heliosapm.aws.jmx.hub.jms.credentials.PropertiesURLCredentialsReader#readCredentials(com.fasterxml.jackson.databind.node.ObjectNode)
	 */
	@Override
	public String[] readCredentials(final ObjectNode credsNode) {
		final JsonNode node = JSONUtil.parseToNode(URLHelper.getTextFromURL(url));
		return new String[]{
			node.get(keyIdPropsKey).textValue(),
			node.get(secretKeyPropsKey).textValue()
		};
	}

}
