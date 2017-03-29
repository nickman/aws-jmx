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
package com.heliosapm.aws.jmx.agent;

import com.heliosapm.utils.config.ConfigurationHelper;

/**
 * <p>Title: BindAddress</p>
 * <p>Description: Enumerates the supported symbols representing a network port bind address</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.aws.jmx.agent.BindAddress</code></p>
 */

public enum BindAddress implements BindAddressProvider {
	/** The local ip address */
	LOCALIP{
		@Override
		public String getBindAddress() {			
			return META_SERVICE.getLocalV4Ip();
		}
		@Override
		public String getJMXServiceURL(final int jmxmpPort) {			
			return "service:jmx:sshjmxmp://" + getBindAddress()  + ":" + jmxmpPort;
		}
	},
	/** The public ip address */
	PUBLICIP{
		@Override
		public String getBindAddress() {			
			return META_SERVICE.getPublicV4Ip();
		}
		@Override
		public String getJMXServiceURL(final int jmxmpPort) {			
			return "service:jmx:jmxmp://" + getBindAddress()  + ":" + jmxmpPort;
		}
	},
	/** The local host name */
	LOCALNAME{
		@Override
		public String getBindAddress() {			
			return META_SERVICE.getLocalHostName();
		}
		@Override
		public String getJMXServiceURL(final int jmxmpPort) {			
			return "service:jmx:sshjmxmp://" + getBindAddress()  + ":" + jmxmpPort;
		}
	},
	/** The public host name */
	PUBLICNAME{
		@Override
		public String getBindAddress() {			
			return META_SERVICE.getPublicHostName();
		}
		@Override
		public String getJMXServiceURL(final int jmxmpPort) {			
			return "service:jmx:jmxmp://" + getBindAddress()  + ":" + jmxmpPort;
		}		
	},
	/** The default which is <b><code>127.0.0.1</code></b> */
	DEFAULT{
		@Override
		public String getBindAddress() {			
			return DEFAULT_JMXMP_BIND;
		}
		@Override
		public String getJMXServiceURL(final int jmxmpPort) {			
			return "service:jmx:sshjmxmp://" + getBindAddress()  + ":" + jmxmpPort;
		}		
	};	
	
	
	/** The system property key to override the default jmxmp bind interface */
	public static final String SYSPROP_JMXMP_BIND = "com.heliosapm.aws.jmx.iface";
	
	/** The default jmxmp bind interface */
	public static final String DEFAULT_JMXMP_BIND = "127.0.0.1";

	
	public static BindAddress bindAddress() {
		final String jmxmpBind = ConfigurationHelper.getSystemThenEnvProperty(SYSPROP_JMXMP_BIND, DEFAULT_JMXMP_BIND);
		try {
			return valueOf(jmxmpBind.toUpperCase().trim());
		} catch (Exception ex) {
			return DEFAULT;
		}		
	}
	
	/**
	 * Determines the bind address to bind to
	 * @return a bind address
	 */
	public static String getBindAdddress() {
		final String jmxmpBind = ConfigurationHelper.getSystemThenEnvProperty(SYSPROP_JMXMP_BIND, DEFAULT_JMXMP_BIND);
		try {
			return valueOf(jmxmpBind.toUpperCase().trim()).getBindAddress();
		} catch (Exception ex) {
			return DEFAULT_JMXMP_BIND;
		}
	}
}
