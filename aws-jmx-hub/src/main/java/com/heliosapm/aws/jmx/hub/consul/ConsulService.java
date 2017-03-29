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
package com.heliosapm.aws.jmx.hub.consul;

import java.util.Arrays;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.catalog.model.CatalogDeregistration;
import com.ecwid.consul.v1.catalog.model.CatalogRegistration;
import com.ecwid.consul.v1.catalog.model.CatalogRegistration.Service;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * <p>Title: ConsulService</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.aws.jmx.hub.consul.ConsulService</code></p>
 */

public class ConsulService {
	protected final ConsulClient client;
	/**
	 * Creates a new ConsulService
	 */
	public ConsulService() {
		client = new ConsulClient();
	}
	
	public void deregister(final JsonNode node) {
		final CatalogDeregistration dereg = new CatalogDeregistration();
		dereg.setDatacenter(node.get("region").textValue());
		dereg.setNode(node.get("instanceId").textValue());
		dereg.setServiceId(node.get("jmxagentid").textValue());
		client.catalogDeregister(dereg);
	}
	
	public void register(final JsonNode node) {		
		final CatalogRegistration reg = new CatalogRegistration();
		reg.setAddress(node.get("publicHostName").textValue());
		reg.setDatacenter(node.get("region").textValue());
		reg.setNode(node.get("instanceId").textValue());
		final Service service = new Service();
		service.setAddress(node.get("publicjmxserviceurl").textValue());
		
		service.setId(node.get("jmxagentid").textValue());
		service.setPort(node.get("jmxmpPort").intValue());
		service.setService("JAVA-JMXMP");
		service.setTags(Arrays.asList(
				tag(node, "localV4Ip"),
				tag(node, "publicV4Ip"),
				tag(node, "macAddress"),
				tag(node, "instanceId"),
				tag(node, "instanceType"),
				tag(node, "pid")
		));
		reg.setService(service);
		client.catalogRegister(reg);
	}
	
	public void close() {
		try {
			
		} catch (Exception x) {}
	}
	
	
	protected static String tag(final JsonNode node, final String key) {
		return new StringBuilder(key).append("=").append(node.get(key).textValue()).toString();
	}

}
