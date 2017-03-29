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

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;



/**
 * <p>Title: JSONUtil</p>
 * <p>Description: JSON Helper utilities</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.aws.json.JSONUtil</code></p>
 */

public class JSONUtil {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final JsonFactory jsonFactory = objectMapper.getFactory();
	
	static {
		//objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
	}
	
	/** Type reference for common string/object maps */
	public static final TypeReference<HashMap<String, Object>> TR_STR_OBJ_HASH_MAP = 
	    new TypeReference<HashMap<String, Object>>() {};
	/** Type reference for common string/string maps */
	public static final TypeReference<HashMap<String, String>> TR_STR_STR_HASH_MAP = 
	    new TypeReference<HashMap<String, String>>() {};
	/** Type reference for common string hash set */
	public static final TypeReference<HashSet<String>> TR_STR_HASH_SET = 
	    new TypeReference<HashSet<String>>() {};
	    
	
	/**
	 * Returns the shared object mapper
	 * @return the shared object mapper
	 */
	public static ObjectMapper objectMapper() {
		return objectMapper;
	}
	
	/**
	 * Returns the shared object json factory
	 * @return the shared object json factory
	 */
	public static JsonFactory jsonFactory() {
		return jsonFactory;
	}
	
	/**
	 * Creates a new empty ObjectNode
	 * @return a new empty ObjectNode
	 */
	public static ObjectNode newObjectNode() {
		return objectMapper.createObjectNode();
	}
	
	/**
	 * Parses the passed stringy to a JsonNode
	 * @param cs The stringy to parse
	 * @return the parsed JsonNode
	 */
	public static JsonNode parseToNode(final CharSequence cs) {
		if(cs==null) throw new IllegalArgumentException("The passed string was null");
		final String s = cs.toString().trim();
		if(s.isEmpty()) throw new IllegalArgumentException("The passed string was empty");
		try {
			return objectMapper.readTree(s);
		} catch (Exception ex) {
			throw new JSONException("Failed to parse to node", ex);
		}
	}
	
	/**
	 * Parses the passed stringy to an object of the specified type
	 * @param cs The stringy to parse
	 * @param type A type reference
	 * @return the parsed object
	 */
	public static final <T> T parseToObject(final CharSequence cs, final TypeReference<T> type) {
		if(cs==null) throw new IllegalArgumentException("The passed string was null");
		final String json = cs.toString().trim();
		if(json.isEmpty()) throw new IllegalArgumentException("The passed string was empty");		
		if (type == null) throw new IllegalArgumentException("Missing type reference");
		try {
			return objectMapper.readValue(json, type);
		} catch (Exception ex) {
			throw new JSONException("Failed to parse string to object", ex);
		}
	}
	
	/**
	 * Reads and parses the passed inputstream to an object of the specified type
	 * @param is The inputstream to read and parse
	 * @param type A type reference
	 * @return the parsed object
	 */
	public static final <T> T parseToObject(final InputStream is, final TypeReference<T> type) {
		if(is==null) throw new IllegalArgumentException("The passed InputStream was null");
		if (type == null) throw new IllegalArgumentException("Missing type reference");
		try {
			return objectMapper.readValue(is, type);
		} catch (Exception ex) {
			throw new JSONException("Failed to parse InputStream to object", ex);
		}
	}
	
	/**
	 * Reads and parses the content read from the passed URL to an object of the specified type
	 * @param url The URL to read and parse
	 * @param type A type reference
	 * @return the parsed object
	 */
	public static final <T> T parseToObject(final URL url, final TypeReference<T> type) {
		if(url==null) throw new IllegalArgumentException("The passed URL was null");
		if (type == null) throw new IllegalArgumentException("Missing type reference");
		try {
			return objectMapper.readValue(url, type);
		} catch (Exception ex) {
			throw new JSONException("Failed to parse URL [" + url + "] to object", ex);
		}
	}
	
	/**
	 * Reads and parses the content read from the passed URL to an object of the specified type
	 * @param url The URL to read and parse
	 * @param type A type reference
	 * @return the parsed object
	 */
	public static final <T> T parseToObject(final URL url, final Class<T> type) {
		if(url==null) throw new IllegalArgumentException("The passed URL was null");
		if (type == null) throw new IllegalArgumentException("Missing type reference");
		try {
			return objectMapper.readValue(url, type);
		} catch (Exception ex) {
			throw new JSONException("Failed to parse URL [" + url + "] to object", ex);
		}
	}
	
	
	/**
	 * Parses the passed stringy to an object of the specified type
	 * @param cs The stringy to parse
	 * @param type The type
	 * @return the parsed object
	 */	
	public static final <T> T parseToObject(final CharSequence cs, final Class<T> type) {
		if(cs==null) throw new IllegalArgumentException("The passed string was null");
		final String json = cs.toString().trim();
		if(json.isEmpty()) throw new IllegalArgumentException("The passed string was empty");		
		if (type == null) throw new IllegalArgumentException("Missing class");
		try {
			return (T)objectMapper.readValue(json, type);
		} catch (Exception ex) {
			throw new JSONException("Failed to parse string to object", ex);
		}
	}
	
	/**
	 * Reads and parses the passed inputstream to an object of the specified type
	 * @param is The inputstream to read and parse
	 * @param type The type
	 * @return the parsed object
	 */
	public static final <T> T parseToObject(final InputStream is, final Class<T> type) {
		if(is==null) throw new IllegalArgumentException("The passed InputStream was null");
		if (type == null) throw new IllegalArgumentException("Missing class");
		try {
			return objectMapper.readValue(is, type);
		} catch (Exception ex) {
			throw new JSONException("Failed to parse InputStream to object", ex);
		}
	}
	
	
	/**
	 * Serializes the given object to a JSON string
	 * @param object The object to serialize
	 * @return A JSON formatted string
	 * @throws IllegalArgumentException if the object was null
	 * @throws JSONException if the object could not be serialized
	 */
	public static final String serializeToString(final Object object) {
		if (object == null)
			throw new IllegalArgumentException("Object was null");
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new JSONException(e);
		}
	}
	
	/**
	 * Converts the passed object to a JsonNode
	 * @param object The object to convert
	 * @return the resulting JsonNode
	 */
	public static final JsonNode serializeToNode(final Object object) {
		if (object == null)
			throw new IllegalArgumentException("Object was null");
		return objectMapper.convertValue(object, JsonNode.class);
	}	
	
	/**
	 * Deserializes a JSON formatted string to a specific class type
	 * <b>Note:</b> If you get mapping exceptions you may need to provide a 
	 * TypeReference
	 * @param json The string to deserialize
	 * @param pojo The class type of the object used for deserialization
	 * @return An object of the {@code pojo} type
	 * @throws IllegalArgumentException if the data or class was null or parsing 
	 * failed
	 * @throws JSONException if the data could not be parsed
	 */
	public static final <T> T parseToObject(final JsonNode json, final TypeReference<T> pojo) {
		if (json == null)
			throw new IllegalArgumentException("Incoming data was null or empty");
		if (pojo == null)
			throw new IllegalArgumentException("Missing class type");

		try {
			return objectMapper.convertValue(json, pojo);	
		} catch (Exception e) {
			throw new JSONException(e);
		}
	}
	
	/**
	 * Deserializes a JSON node to a specific class type
	 * <b>Note:</b> If you get mapping exceptions you may need to provide a 
	 * TypeReference
	 * @param json The node to deserialize
	 * @param pojo The class type of the object used for deserialization
	 * @return An object of the {@code pojo} type
	 * @throws IllegalArgumentException if the data or class was null or parsing 
	 * failed
	 * @throws JSONException if the data could not be parsed
	 */
	public static final <T> T parseToObject(final JsonNode json, final Class<T> pojo) {
		if (json == null)
			throw new IllegalArgumentException("Incoming data was null or empty");
		if (pojo == null)
			throw new IllegalArgumentException("Missing class type");
		return objectMapper.convertValue(json, pojo);		
	}
	
	
	
	
	private JSONUtil(){}

}
