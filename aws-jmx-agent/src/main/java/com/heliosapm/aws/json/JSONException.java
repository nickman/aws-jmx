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
package com.heliosapm.aws.json;

/**
 * <p>Title: JSONException</p>
 * <p>Description: Runtime exception wrapper for JSON codec operations</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.aws.json.JSONException</code></p>
 */

public class JSONException extends RuntimeException {


	/**  */
	private static final long serialVersionUID = 8156534409788554268L;

	/**
	 * Creates a new JSONException
	 */
	public JSONException() {

	}

	/**
	 * Creates a new JSONException
	 * @param message The exception message
	 */
	public JSONException(String message) {
		super(message);
	}

	/**
	 * Creates a new JSONException
	 * @param cause The underlying exception cause
	 */
	public JSONException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new JSONException
	 * @param message The exception message
	 * @param cause The underlying exception cause
	 */
	public JSONException(String message, Throwable cause) {
		super(message, cause);
	}

}
