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

import com.heliosapm.aws.jmx.AWSJVMMetaDataService;

/**
 * <p>Title: BindAddressProvider</p>
 * <p>Description: Provides a network bind address</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.aws.jmx.agent.BindAddressProvider</code></p>
 */

public interface BindAddressProvider {
	/**
	 * Returns a bind address in the form of an ip or host name
	 * @return a bind address
	 */
	public String getBindAddress();
	
	public static final AWSJVMMetaDataService META_SERVICE = AWSJVMMetaDataService.getInstance();
}
