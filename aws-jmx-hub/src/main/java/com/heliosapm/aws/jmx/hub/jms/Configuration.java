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
package com.heliosapm.aws.jmx.hub.jms;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.DnsResolver;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.heliosapm.aws.jmx.hub.jms.credentials.CredentialType;
import com.heliosapm.aws.json.JSONUtil;
import com.heliosapm.utils.url.URLHelper;

/**
 * <p>Title: Configuration</p>
 * <p>Description: The JMS listener configuration</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.aws.jmx.hub.jms.Configuration</code></p>
 */
@JsonDeserialize(using=Configuration.ConfigurationDeserializer.class)
public class Configuration {
	/** The default configuration resource path */
	public static final String DEFAULT_CONFIG = "config/default-config.json";
	
	/** Sets of queue names to listen on within a map keyed by the region */
	protected final EnumMap<Regions, Set<String>> regionQueues = new EnumMap<Regions, Set<String>>(Regions.class); 
	/** Arbitrary info about the configuration */
	protected final String info;
	/** The JMS message prefetch count */
	protected int prefetch = 1;
	/** The optional credentials */
	protected String[] credentials = null;
	/** The credentials node */
	protected ObjectNode credNode = null;
	/** The credentials provider */
	protected AWSCredentialsProvider credentialsProvider = null;
	/** The client configuration */
	protected final ClientConfiguration clientConfiguration = new ClientConfiguration();
	
	
	
	/**
	 * Reads the configuration from the specified URL, file or classpath resource
	 * @param json A URL, file or classpath resource. If null, reads the default config.
	 * @return the read configuration
	 */
	public static Configuration readConfiguration(final String json) {
		final URL url = URLHelper.toURL((json==null || json.trim().isEmpty()) ? DEFAULT_CONFIG : json.trim());
		return JSONUtil.parseToObject(url, Configuration.class);
	}
	
	/**
	 * Reads the default configuration 
	 * @return the read configuration
	 */
	public static Configuration readConfiguration() {
		return readConfiguration(null);
	}
	
	public static void main(String[] args) {
		System.out.println(readConfiguration());
	}
	
	/**
	 * Creates a new Configuration
	 */
	private Configuration(final String info) {
		this.info = info;
	}
	
	
	public static class ConfigurationDeserializer extends JsonDeserializer<Configuration> {
		@Override
		public Configuration deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
			final JsonNode rootNode = p.readValueAsTree();
			final Configuration config = new Configuration(rootNode.get("info").asText());
			final JsonNode queueNodes = rootNode.get("regionqueues");
			for(final Iterator<Entry<String, JsonNode>> iter = queueNodes.fields(); iter.hasNext();) {
				final Entry<String, JsonNode> entry = iter.next();
				final HashSet<String> queueNames = JSONUtil.objectMapper().readerFor(JSONUtil.TR_STR_HASH_SET).readValue(entry.getValue());
				final Regions region = Regions.fromName(entry.getKey());
				Set<String> queueSet = config.regionQueues.get(region);
				if(queueSet==null) {
					queueSet = new HashSet<String>();
					config.regionQueues.put(region, queueSet);
				}
				queueSet.addAll(queueNames);				
			}
			if(rootNode.has("prefetch")) {
				config.prefetch = rootNode.get("prefetch").asInt(1);
			}
			if(rootNode.has("credentials")) {
				config.credNode = (ObjectNode)rootNode.get("credentials");
				config.credentialsProvider = CredentialType.getCredentialsProvider(config.credNode);
			}
			if(rootNode.has("clientConfiguration")) {
				config.applyClientConfiguration(rootNode.get("clientConfiguration"));
			}
			return config;
		}
	}
	
	
	protected void applyClientConfiguration(final JsonNode ccNode) {
		JsonNode node = null;
		final ClientConfiguration cc = clientConfiguration;
		if(ccNode.has("userAgentSuffix")) {
			try {
				node = ccNode.get("userAgentSuffix");
				if(node!=null && NullNode.instance != node) {
					cc.setUserAgentSuffix(node.textValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [userAgentSuffix]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("userAgentPrefix")) {
			try {
				node = ccNode.get("userAgentPrefix");
				if(node!=null && NullNode.instance != node) {
					cc.setUserAgentPrefix(node.textValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [userAgentPrefix]");
				ex.printStackTrace(System.err);
			}
		}
		
		if(ccNode.has("socketBufferSizeHints")) {
			try {
				node = ccNode.get("socketBufferSizeHints");
				if(node!=null && NullNode.instance != node) {
					final int[] socketBufferSizes = JSONUtil.parseToObject(node, int[].class);
					if(socketBufferSizes.length==2 && socketBufferSizes[0] > -1 && socketBufferSizes[1] > -1) {
						cc.setSocketBufferSizeHints(socketBufferSizes[0], socketBufferSizes[1]);
					}
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [socketBufferSizeHints]");
				ex.printStackTrace(System.err);
			}
		}
		// TODO: implement this
//		if(ccNode.has("retryPolicy")) {
//			try {
//				node = ccNode.get("retryPolicy");
//				cc.setRetryPolicy(retryPolicy);
//			} catch (Exception ex) {
//				System.err.println("Failed to set client config value for [retryPolicy]");
//				ex.printStackTrace(System.err);
//			}
//		}
		if(ccNode.has("proxyUsername")) {
			try {
				node = ccNode.get("proxyUsername");		
				if(node!=null && NullNode.instance != node) {
					cc.setProxyUsername(node.textValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [proxyUsername]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("connectionTTL")) {
			try {
				node = ccNode.get("connectionTTL");
				if(node!=null && NullNode.instance != node) {
					cc.setConnectionTTL(node.longValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [connectionTTL]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("cacheResponseMetadata")) {
			try {
				node = ccNode.get("cacheResponseMetadata");
				if(node!=null && NullNode.instance != node) {
					cc.setCacheResponseMetadata(node.booleanValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [cacheResponseMetadata]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("clientExecutionTimeout")) {
			try {
				node = ccNode.get("clientExecutionTimeout");
				if(node!=null && NullNode.instance != node) {
					cc.setClientExecutionTimeout(node.intValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [clientExecutionTimeout]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("proxyPort")) {
			try {
				node = ccNode.get("proxyPort");
				if(node!=null && NullNode.instance != node) {
					cc.setProxyPort(node.intValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [proxyPort]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("protocol")) {
			try {
				node = ccNode.get("protocol");
				if(node!=null && NullNode.instance != node) {
					cc.setProtocol(Protocol.valueOf(node.textValue().trim().toUpperCase()));
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [protocol]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("proxyWorkstation")) {
			try {
				node = ccNode.get("proxyWorkstation");
				if(node!=null && NullNode.instance != node) {
					cc.setProxyWorkstation(node.textValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [proxyWorkstation]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("nonProxyHosts")) {
			try {
				node = ccNode.get("nonProxyHosts");
				if(node!=null && NullNode.instance != node) {
					cc.setNonProxyHosts(node.textValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [nonProxyHosts]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("maxConsecutiveRetriesBeforeThrottling")) {
			try {
				node = ccNode.get("maxConsecutiveRetriesBeforeThrottling");
				if(node!=null && NullNode.instance != node) {
					cc.setMaxConsecutiveRetriesBeforeThrottling(node.intValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [maxConsecutiveRetriesBeforeThrottling]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("signerOverride")) {
			try {
				node = ccNode.get("signerOverride");
				if(node!=null && NullNode.instance != node) {
					cc.setSignerOverride(node.textValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [signerOverride]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("useExpectContinue")) {
			try {
				node = ccNode.get("useExpectContinue");
				if(node!=null && NullNode.instance != node) {
					cc.setUseExpectContinue(node.booleanValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [useExpectContinue]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("socketTimeout")) {
			try {
				node = ccNode.get("socketTimeout");
				if(node!=null && NullNode.instance != node) {
					cc.setSocketTimeout(node.intValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [socketTimeout]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("connectionMaxIdleMillis")) {
			try {				
				node = ccNode.get("connectionMaxIdleMillis");
				if(node!=null && NullNode.instance != node) {
					cc.setConnectionMaxIdleMillis(node.longValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [connectionMaxIdleMillis]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("connectionTimeout")) {
			try {
				node = ccNode.get("connectionTimeout");
				if(node!=null && NullNode.instance != node) {
					cc.setConnectionTimeout(node.intValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [connectionTimeout]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("requestTimeout")) {
			try {
				node = ccNode.get("requestTimeout");
				if(node!=null && NullNode.instance != node) {
					cc.setRequestTimeout(node.intValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [requestTimeout]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("maxConnections")) {
			try {
				node = ccNode.get("maxConnections");
				if(node!=null && NullNode.instance != node) {
					cc.setMaxConnections(node.intValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [maxConnections]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("algorithm")) {
			try {				
				node = ccNode.get("algorithm");
				if(node!=null && NullNode.instance != node) {
					final String algo = node.textValue();
					if(ccNode.has("provider")) {						
						final String provider = ccNode.get("provider").textValue();
						if(!provider.trim().isEmpty() && !algo.trim().isEmpty()) {
							cc.setSecureRandom(SecureRandom.getInstance(algo, provider));
						}
					} else {
						if(!algo.trim().isEmpty()) {
							cc.setSecureRandom(SecureRandom.getInstance(algo));
						}
					}
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [algorithm]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("headers")) {
			try {
				node = ccNode.get("headers");
				if(node!=null && NullNode.instance != node) {
					final Map<String, String> headers = JSONUtil.parseToObject(node, JSONUtil.TR_STR_STR_HASH_MAP);
					for(Map.Entry<String, String> header: headers.entrySet()) {
						cc.addHeader(header.getKey(), header.getValue());
					}
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [headers]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("maxErrorRetry")) {
			try {
				node = ccNode.get("maxErrorRetry");
				if(node!=null && NullNode.instance != node) {
					final int value = node.intValue();
					if(value > -1) {
						cc.setMaxErrorRetry(value);
					}
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [maxErrorRetry]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("proxyPassword")) {
			try {
				node = ccNode.get("proxyPassword");
				if(node!=null && NullNode.instance != node) {
					cc.setProxyPassword(node.textValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [proxyPassword]");
				ex.printStackTrace(System.err);
			}
		}
//		if(ccNode.has("apacheHttpClientConfig")) {
//			try {
//				node = ccNode.get("apacheHttpClientConfig");
//				final Class<ConnectionSocketFactory> clazz = (Class<ConnectionSocketFactory>)Class.forName(node.textValue()); 
//				final ApacheHttpClientConfig acc = new ApacheHttpClientConfig().withSslSocketFactory(clazz.newInstance()); 
//			} catch (Exception ex) {
//				System.err.println("Failed to set client config value for [apacheHttpClientConfig]");
//				ex.printStackTrace(System.err);
//			}
//		}
		if(ccNode.has("userAgent")) {
			try {
				node = ccNode.get("userAgent");
				if(node!=null && NullNode.instance != node) {
					cc.setUserAgent(node.textValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [userAgent]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("dnsResolver")) {
			try {
				node = ccNode.get("dnsResolver");
				if(node!=null && NullNode.instance != node) {
					final String className = node.textValue();
					if(!className.trim().isEmpty()) {
						@SuppressWarnings("unchecked")
						final Class<DnsResolver> clazz = (Class<DnsResolver>)Class.forName(node.textValue());
						cc.setDnsResolver(clazz.newInstance());
					}
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [dnsResolver]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("proxyHost")) {
			try {
				node = ccNode.get("proxyHost");
				if(node!=null && NullNode.instance != node) {
					cc.setProxyHost(node.textValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [proxyHost]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("validateAfterInactivityMillis")) {
			try {
				node = ccNode.get("validateAfterInactivityMillis");
				if(node!=null && NullNode.instance != node) {
					cc.setValidateAfterInactivityMillis(node.intValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [validateAfterInactivityMillis]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("responseMetadataCacheSize")) {
			try {
				node = ccNode.get("responseMetadataCacheSize");
				if(node!=null && NullNode.instance != node) {
					cc.setResponseMetadataCacheSize(node.intValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [responseMetadataCacheSize]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("proxyDomain")) {
			try {
				node = ccNode.get("proxyDomain");
				if(node!=null && NullNode.instance != node) {
					cc.setProxyDomain(node.textValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [proxyDomain]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("localAddress")) {
			try {
				node = ccNode.get("localAddress");
				if(node!=null && NullNode.instance != node) {
					cc.setLocalAddress(InetAddress.getByName(node.textValue()));
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [localAddress]");
				ex.printStackTrace(System.err);
			}
		}
		if(ccNode.has("preemptiveBasicProxyAuth")) {
			try {
				node = ccNode.get("preemptiveBasicProxyAuth");
				if(node!=null && NullNode.instance != node) {
					cc.setPreemptiveBasicProxyAuth(node.booleanValue());
				}
			} catch (Exception ex) {
				System.err.println("Failed to set client config value for [preemptiveBasicProxyAuth]");
				ex.printStackTrace(System.err);
			}
		}
		
	}


	/**
	 * Returns the region queue map 
	 * @return the region queue map
	 */
	public Map<Regions, Set<String>> getRegionQueues() {
		return Collections.unmodifiableMap(regionQueues);
	}


	/**
	 * Returns the configuration info
	 * @return the info
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Configuration [regionQueues=").append(regionQueues).append(", info=").append(info)
				.append(", prefetch=").append(prefetch).append(", credentials=").append(Arrays.toString(credentials))
				.append(", credNode=").append(credNode).append(", credentialsProvider=").append(credentialsProvider)
				.append(", clientConfiguration=").append(clientConfiguration).append("]");
		return builder.toString();
	}

	/**
	 * Returns 
	 * @return the prefetch
	 */
	public int getPrefetch() {
		return prefetch;
	}

	/**
	 * Returns 
	 * @return the credentialsProvider
	 */
	public AWSCredentialsProvider getCredentialsProvider() {
		return credentialsProvider;
	}

	/**
	 * Returns 
	 * @return the clientConfiguration
	 */
	public ClientConfiguration getClientConfiguration() {
		return clientConfiguration;
	}


	
	

}
