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
package com.heliosapm.aws.metadata;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.heliosapm.aws.json.JSONUtil;
import com.heliosapm.utils.config.ConfigurationHelper;
import com.heliosapm.utils.lang.StringHelper;
import com.heliosapm.utils.url.URLHelper;

/**
 * <p>Title: MetaDataReader</p>
 * <p>Description: Reads the EC2 instance meta-data from the <b><code>http://169.254.169.254</code></b> end point.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.aws.metadata.MetaDataReader</code></p>
 * TODO: Support for ip6
 * TODO: Support for multiple network interfaces
 */
@JsonDeserialize(using=MetaDataReader.Deser.class)
public class MetaDataReader {
	
	public static final Charset UTF8 = Charset.forName("UTF8");
	
	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	
	// ==================================================================================
	//	Meta Data Endpoint
	// ==================================================================================
	/** The base HTTP URL to retrieve meta data from */
	public static final String META_URL = "http://%s/latest/meta-data/%s/";
	/** The default IP of the meta-data endpoint */
	public static final String DEFAULT_PUB = "169.254.169.254";
	/** The system property key to override the IP of the meta-data endpoint */
	public static final String PUB_CONFIG_KEY = "com.heliosapm.aws.metadata.endpoint";
	
	// ==================================================================================
	//	Amazon Machine Image Data
	// ==================================================================================
	/** The amazon machine image id lookup key */
	public static final String AMI_ID = "ami-id";
	/** The amazon machine image launch index lookup key */
	public static final String AMI_LAUNCH_INDEX = "ami-launch-index";
	/** The amazon machine image manifest path lookup key */
	public static final String AMI_MANIFEST_PATH = "ami-manifest-path";
	// ==================================================================================
	//	Block device mapping
	// ==================================================================================
	/** The image path lookup key */
	public static final String BLOCK_DEV_AMI = "block-device-mapping/ami";
	/** The root partition path lookup key */
	public static final String BLOCK_DEV_ROOT = "block-device-mapping/root";
	// ==================================================================================
	//	Instance host naming
	// ==================================================================================
	/** The host name lookup key */
	public static final String HOST_NAME = "hostname";
	/** The local host name lookup key */
	public static final String LOCAL_HOST_NAME = "local-hostname";
	/** The public host name */
	public static final String PUBLIC_HOST_NAME = "public-hostname";
	// ==================================================================================
	//	Instance ip addresses
	// ==================================================================================
	/** The local V4 ip lookup key */
	public static final String LOCAL_V4_IP = "local-ipv4";
	/** The public V4 ip lookup key */
	public static final String PUBLIC_V4_IP = "public-ipv4";
	/** The mac address lookup key */
	public static final String MAC_ADDR = "mac";	
	// ==================================================================================
	//	Instance meta-data
	// ==================================================================================
	/** The instance id for this instance lookup key */
	public static final String INSTANCE_ID = "instance-id";
	/** The instance action for this instance lookup key */
	public static final String INSTANCE_ACTION = "instance-action";
	/** The instance type for this instance lookup key */
	public static final String INSTANCE_TYPE = "instance-type";
	// ==================================================================================
	//	IAM information
	// ==================================================================================
	/** The JSON for IAM information lookup key */
	public static final String IAM_INFO_JSON = "iam/info";
	
	/** The JSON key for the last update timestamp lookup key */
	public static final String JSON_KEY_IAM_LAST_UPDATED = "LastUpdated";
	/** The JSON key for the instance profile ARN lookup key */
	public static final String JSON_KEY_IAM_PROFILE_ARN = "InstanceProfileArn";
	/** The JSON key for the status code lookup key */
	public static final String JSON_KEY_IAM_CODE = "Code";
	/** The JSON key for the instance profile Id lookup key */
	public static final String JSON_KEY_IAM_PROFILE_ID = "InstanceProfileId";
	

	// ==================================================================================
	//	IAM security credentials
	// ==================================================================================
	/** The IAM credentials id lookup key */
	public static final String IAM_CREDS_ID = "iam/security-credentials";
	/** The JSON for IAM security credentials lookup key, to be formatted with {@link #IAM_CREDS_ID} */
	public static final String IAM_CREDENTIAL_JSON = "iam/security-credentials/%s";
	
	
	/** The JSON key for the credentials last update timestamp lookup key */
	public static final String JSON_KEY_IAM_CREDS_LAST_UPDATED = "LastUpdated";
	/** The JSON key for the credentials expiration timestamp lookup key */
	public static final String JSON_KEY_IAM_CREDS_EXPIRE = "Expiration";
	/** The JSON key for the credential type lookup key */
	public static final String JSON_KEY_IAM_CREDS_TYPE = "Type";
	/** The JSON key for the credentialing status code lookup key */
	public static final String JSON_KEY_IAM_CREDS_CODE = "Code";
	
	
	
	
	/** The configured or default meta-data endpoint ip */
	private final String metaEndpoint;
	/** The amazon machine image id */
	private final String amiId;
	/** The amazon machine image launch index */
	private final String amiLaunchIndex;
	/** The amazon machine image manifest path */
	private final String amiManifestPath;
	/** The ami block device mapping */
	private final String amiBlockDevice;
	/** The root block device mapping */
	private final String rootBlockDevice;
	/** The host name */
	private final String hostName;
	/** The local host name */
	private final String localHostName;
	/** The private host name */
	private final String publicHostName;
	/** The local v4 ip address */
	private final String localV4Ip;
	/** The private v4 ip address */
	private final String publicV4Ip;
	/** The nic mac address */
	private final String macAddress;
	/** The instance id for this instance */
	private final String instanceId;
	/** The instance action for this instance */
	private final String instanceAction;
	/** The instance type for this instance */
	private final String instanceType;
	/** The IAM info last update timestamp */
	private final String iamInfoLastUpdate;
	/** The IAM info instance profile ARN */
	private final String iamInfoInstanceProfileArn;
	/** The IAM info instance profile id */
	private final String iamInfoInstanceProfileId;
	/** The IAM info status code */
	private final String iamInfoStatus;
	/** The IAM credentials id */
	private final String iamCredentialsId;
	/** The IAM credentials last update timestamp */
	private final String iamCredsLastUpdate;
	/** The IAM credentials expiration timestamp */
	private final String iamCredsExpiration;
	/** The IAM credential type */
	private final String iamCredsType;
	/** The IAM credentialing status code */
	private final String iamCredsStatus;
	/** The elapsed time to populate this reader */
	private final long elapsed;
	/** The public key names */
	private final String[] publicKeys;
	
	/**
	 * Creates a new MetaDataReader
	 */
	public MetaDataReader() {
		final long start = System.currentTimeMillis();
		metaEndpoint = ConfigurationHelper.getSystemThenEnvProperty(PUB_CONFIG_KEY, DEFAULT_PUB);
		amiId = lookupOrNull(AMI_ID);
		amiLaunchIndex = lookupOrNull(AMI_LAUNCH_INDEX);
		amiManifestPath = lookupOrNull(AMI_MANIFEST_PATH);
		amiBlockDevice = lookupOrNull(BLOCK_DEV_AMI);
		rootBlockDevice = lookupOrNull(BLOCK_DEV_ROOT);
		hostName = lookupOrNull(HOST_NAME);
		localHostName = lookupOrNull(LOCAL_HOST_NAME);
		publicHostName = lookupOrNull(PUBLIC_HOST_NAME);
		localV4Ip = lookupOrNull(LOCAL_V4_IP);
		publicV4Ip = lookupOrNull(PUBLIC_V4_IP);
		macAddress = lookupOrNull(MAC_ADDR);
		instanceId = lookupOrNull(INSTANCE_ID);
		instanceAction = lookupOrNull(INSTANCE_ACTION);
		publicKeys = pubKeys();
		instanceType = lookupOrNull(INSTANCE_TYPE);
		final Map<String, String> iamInfo = getJSONValuesForKey(IAM_INFO_JSON);
		iamInfoLastUpdate = iamInfo.get(JSON_KEY_IAM_LAST_UPDATED);
		iamInfoInstanceProfileArn = iamInfo.get(JSON_KEY_IAM_PROFILE_ARN);
		iamInfoInstanceProfileId = iamInfo.get(JSON_KEY_IAM_PROFILE_ID);
		iamInfoStatus = iamInfo.get(JSON_KEY_IAM_CODE);
		iamCredentialsId = lookup(IAM_CREDS_ID);
		final Map<String, String> iamCreds = getJSONValuesForKey(String.format(IAM_CREDENTIAL_JSON, iamCredentialsId));
		iamCredsLastUpdate = iamCreds.get(JSON_KEY_IAM_CREDS_LAST_UPDATED);
		iamCredsExpiration = iamCreds.get(JSON_KEY_IAM_CREDS_EXPIRE);
		iamCredsType = iamCreds.get(JSON_KEY_IAM_CREDS_TYPE);
		iamCredsStatus = iamCreds.get(JSON_KEY_IAM_CREDS_CODE);
		elapsed = System.currentTimeMillis() - start;
	}
	
//	{
//		  "metaEndpoint": "localhost:8394",
//		  "amiId": "ami-0b33d91d",
//		  "amiLaunchIndex": "0",
//		  "amiManifestPath": "(unknown)",
//		  "amiBlockDevice": "/dev/xvda",
//		  "rootBlockDevice": "/dev/xvda",
//		  "hostName": "ip-172-31-39-72.ec2.internal",
//		  "localHostName": "ip-172-31-39-72.ec2.internal",
//		  "publicHostName": "ec2-54-159-119-172.compute-1.amazonaws.com",
//		  "localV4Ip": "172.31.39.72",
//		  "publicV4Ip": "54.159.119.172",
//		  "macAddress": "0e:67:41:13:a3:00",
//		  "instanceId": "i-06cb251a132350cae",
//		  "instanceAction": "none",
//		  "instanceType": "t2.micro",
//		  "iamInfoLastUpdate": "2017-03-22T18:44:37Z",
//		  "iamInfoInstanceProfileArn": "arn:aws:iam::098841670625:instance-profile/JMXNotifier",
//		  "iamInfoInstanceProfileId": "AIPAIQ5ASTVF5ITWAQVSW",
//		  "iamInfoStatus": "Success",
//		  "iamCredentialsId": "JMXNotifier",
//		  "iamCredsLastUpdate": "2017-03-22T18:45:15Z",
//		  "iamCredsExpiration": "2017-03-23T01:19:12Z",
//		  "iamCredsType": "AWS-HMAC",
//		  "iamCredsStatus": "Success",
//		  "elapsed": 594
//		}	
	
	/**
	 * Creates a new MetaDataReader from a JSON stringy
	 * @param json the JSON stringy
	 */
	public MetaDataReader(final CharSequence json) {
		this(JSONUtil.parseToNode(json));
	}
	
	/**
	 * Creates a new MetaDataReader from a JsonNode
	 * @param node the JsonNode
	 */
	public MetaDataReader(final JsonNode node) {
		final long start = System.currentTimeMillis();
	    metaEndpoint = node.has("metaEndpoint") ? node.get("metaEndpoint").textValue() : null;
		amiId = node.has("amiId") ? node.get("amiId").textValue() : null;
		amiLaunchIndex = node.has("amiLaunchIndex") ? node.get("amiLaunchIndex").textValue() : null;
		amiManifestPath = node.has("amiManifestPath") ? node.get("amiManifestPath").textValue() : null;
		amiBlockDevice = node.has("amiBlockDevice") ? node.get("amiBlockDevice").textValue() : null;
		rootBlockDevice = node.has("rootBlockDevice") ? node.get("rootBlockDevice").textValue() : null;
		hostName = node.has("hostName") ? node.get("hostName").textValue() : null;
		localHostName = node.has("localHostName") ? node.get("localHostName").textValue() : null;
		publicHostName = node.has("publicHostName") ? node.get("publicHostName").textValue() : null;
		localV4Ip = node.has("localV4Ip") ? node.get("localV4Ip").textValue() : null;
		publicV4Ip = node.has("publicV4Ip") ? node.get("publicV4Ip").textValue() : null;
		macAddress = node.has("macAddress") ? node.get("macAddress").textValue() : null;
		instanceId = node.has("instanceId") ? node.get("instanceId").textValue() : null;
		instanceAction = node.has("instanceAction") ? node.get("instanceAction").textValue() : null;
		instanceType = node.has("instanceType") ? node.get("instanceType").textValue() : null;
		iamInfoLastUpdate = node.has("iamInfoLastUpdate") ? node.get("iamInfoLastUpdate").textValue() : null;
		iamInfoInstanceProfileArn = node.has("iamInfoInstanceProfileArn") ? node.get("iamInfoInstanceProfileArn").textValue() : null;
		iamInfoInstanceProfileId = node.has("iamInfoInstanceProfileId") ? node.get("iamInfoInstanceProfileId").textValue() : null;
		iamInfoStatus = node.has("iamInfoStatus") ? node.get("iamInfoStatus").textValue() : null;
		iamCredentialsId = node.has("iamCredentialsId") ? node.get("iamCredentialsId").textValue() : null;
		iamCredsLastUpdate = node.has("iamCredsLastUpdate") ? node.get("iamCredsLastUpdate").textValue() : null;
		iamCredsExpiration = node.has("iamCredsExpiration") ? node.get("iamCredsExpiration").textValue() : null;
		iamCredsType = node.has("iamCredsType") ? node.get("iamCredsType").textValue() : null;
		iamCredsStatus = node.has("iamCredsStatus") ? node.get("iamCredsStatus").textValue() : null;
		publicKeys = node.has("publicKeys") ? JSONUtil.parseToObject(node.get("publicKeys"), String[].class) : new String[0];
	    elapsed = System.currentTimeMillis() - start;		
	}
	
	public static class Deser extends JsonDeserializer<MetaDataReader> {
		@Override
		public MetaDataReader deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
			System.out.println("Deserializing....");
			final Map<String, String> map = new HashMap<String, String>();
			String fieldName = null;
			while((fieldName = p.nextFieldName()) != null) {
				
				map.put(fieldName, p.nextTextValue());
				p.clearCurrentToken();
			}			
			return new MetaDataReader(JSONUtil.serializeToNode(map));
		}
	}
	
	
	
	public static void main(String[] args) {
//		if(args.length > 0) {
//			if(!System.getProperties().containsKey(PUB_CONFIG_KEY)) {
//				System.setProperty(PUB_CONFIG_KEY, args[0]);
//			}
//		}
//		System.out.println(new MetaDataReader());
		
		final String json = "{\"metaEndpoint\":\"localhost:8394\",\"amiId\":\"ami-0b33d91d\",\"amiLaunchIndex\":\"0\",\"amiManifestPath\":\"(unknown)\",\"amiBlockDevice\":\"/dev/xvda\",\"rootBlockDevice\":\"/dev/xvda\",\"hostName\":\"ip-172-31-39-72.ec2.internal\",\"localHostName\":\"ip-172-31-39-72.ec2.internal\",\"publicHostName\":\"ec2-54-159-119-172.compute-1.amazonaws.com\",\"localV4Ip\":\"172.31.39.72\",\"publicV4Ip\":\"54.159.119.172\",\"macAddress\":\"0e:67:41:13:a3:00\",\"instanceId\":\"i-06cb251a132350cae\",\"instanceAction\":\"none\",\"instanceType\":\"t2.micro\",\"iamInfoLastUpdate\":\"2017-03-22T18:44:37Z\",\"iamInfoInstanceProfileArn\":\"arn:aws:iam::098841670625:instance-profile/JMXNotifier\",\"iamInfoInstanceProfileId\":\"AIPAIQ5ASTVF5ITWAQVSW\",\"iamInfoStatus\":\"Success\",\"iamCredentialsId\":\"JMXNotifier\",\"iamCredsLastUpdate\":\"2017-03-22T18:45:15Z\",\"iamCredsExpiration\":\"2017-03-23T01:19:12Z\",\"iamCredsType\":\"AWS-HMAC\",\"iamCredsStatus\":\"Success\",\"elapsed\":594}";
		System.out.println("Building...");
		System.out.println(JSONUtil.parseToObject(json, MetaDataReader.class));
		
	}
	
	protected String[] pubKeys() {
		final String keys = lookupOrNull("public-keys");
		if(keys==null) return new String[0];
		final String[] lines = StringHelper.splitString(keys, '\n');
		final Set<String> names = new HashSet<String>();
		for(String line: lines) {
			final String[] pair = StringHelper.splitString(line, '=');
			if(pair.length==2) {
				names.add(pair[1]);
			}
		}
		return names.toArray(new String[names.size()]);
	}
	
	protected String lookupOrNull(final String key) {
		try {
			return lookup(key);
		} catch (Exception ex) {
			return null;
		}
	}
	
	protected String lookup(final String key) {
		final String url = String.format(META_URL, metaEndpoint, key);
		try {
			return URLHelper.getTextFromURL(url);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to resolve value for URL [" + url + "]", ex);
		}
	}
	
	public Map<String, String> getJSONValuesForKey(final String key) {
		return JSONUtil.parseToObject(
				lookup(key),
				JSONUtil.TR_STR_STR_HASH_MAP
		);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MetaDataReader [\n\tmetaEndpoint=").append(metaEndpoint).append("\n\tamiId=").append(amiId)
				.append("\n\tamiLaunchIndex=").append(amiLaunchIndex).append("\n\tamiManifestPath=").append(amiManifestPath)
				.append("\n\tamiBlockDevice=").append(amiBlockDevice).append("\n\trootBlockDevice=").append(rootBlockDevice)
				.append("\n\thostName=").append(hostName).append("\n\tlocalHostName=").append(localHostName)
				.append("\n\tpublicHostName=").append(publicHostName).append("\n\tlocalV4Ip=").append(localV4Ip)
				.append("\n\tpublicV4Ip=").append(publicV4Ip).append("\n\tmacAddress=").append(macAddress)
				.append("\n\tinstanceId=").append(instanceId).append("\n\tinstanceAction=").append(instanceAction)
				.append("\n\tinstanceType=").append(instanceType).append("\n\tiamInfoLastUpdate=").append(iamInfoLastUpdate)
				.append("\n\tiamInfoInstanceProfileArn=").append(iamInfoInstanceProfileArn)
				.append("\n\tiamInfoInstanceProfileId=").append(iamInfoInstanceProfileId).append("\n\tiamInfoStatus=")
				.append(iamInfoStatus)
				.append("\n\tiamCredentialsId=").append(iamCredentialsId)
				.append("\n\tiamCredsLastUpdate=").append(iamCredsLastUpdate)
				.append("\n\tiamCredsExpiration=").append(iamCredsExpiration).append("\n\tiamCredsType=")
				.append(iamCredsType).append("\n\tiamCredsStatus=").append(iamCredsStatus)
				.append("\n]\nCreated in ").append(elapsed).append(" ms.");
		return builder.toString();
	}
	
	/**
	 * Renders this meta-data as JSON
	 * @return a JSON string
	 */
	public String toJSON() {		
		return JSONUtil.serializeToString(this);
	}
	
	/**
	 * Encodes this meta-data to a JsonNode
	 * @return the JsonNode
	 */
	public JsonNode toJsonNode() {
		return JSONUtil.serializeToNode(this);
	}

	/**
	 * Returns the configured metadata endpoint
	 * @return the metadata endpoint
	 */
	public String getMetaEndpoint() {
		return metaEndpoint;
	}

	/**
	 * Returns the amazon machine image id
	 * @return the amazon machine image id
	 */
	public String getAmiId() {
		return amiId;
	}

	/**
	 * Returns the AMI launch index
	 * @return the launch index
	 */
	public String getAmiLaunchIndex() {
		return amiLaunchIndex;
	}

	/**
	 * Returns the AMI manifest path
	 * @return the AMI manifest path
	 */
	public String getAmiManifestPath() {
		return amiManifestPath;
	}

	/**
	 * Returns the AMI block device mapping
	 * @return the AMI block device mapping
	 */
	public String getAmiBlockDevice() {
		return amiBlockDevice;
	}

	/**
	 * Returns the root block device mapping
	 * @return the root block device mapping
	 */
	public String getRootBlockDevice() {
		return rootBlockDevice;
	}

	/**
	 * Returns the host name
	 * @return the host name
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * Returns the host local name
	 * @return the host local name
	 */
	public String getLocalHostName() {
		return localHostName;
	}

	/**
	 * Returns the host public name
	 * @return the host public name
	 */
	public String getPublicHostName() {
		return publicHostName;
	}

	/**
	 * Returns 
	 * @return the localV4Ip
	 */
	public String getLocalV4Ip() {
		return localV4Ip;
	}

	/**
	 * Returns the public V4 IP address
	 * @return the public V4 IP address
	 */
	public String getPublicV4Ip() {
		return publicV4Ip;
	}
	
	/**
	 * Returns the public keys
	 * @return the public keys
	 */
	public String[] getPublicKeys() {
		return publicKeys;
	}

	/**
	 * Returns the mac address
	 * @return the mac address
	 */
	public String getMacAddress() {
		return macAddress;
	}

	/**
	 * Returns the EC2 instance id
	 * @return the instance Id
	 */
	public String getInstanceId() {
		return instanceId;
	}

	/**
	 * Returns the instance action
	 * @return the instance action
	 */
	public String getInstanceAction() {
		return instanceAction;
	}

	/**
	 * Returns the EC2 instance type
	 * @return the EC2 instance type
	 */
	public String getInstanceType() {
		return instanceType;
	}

	/**
	 * Returns the IAM info last update timestamp
	 * @return the IAM info last update timestamp
	 */
	public String getIamInfoLastUpdate() {
		return iamInfoLastUpdate;
	}

	/**
	 * Returns the IAM info instance profile ARN
	 * @return the IAM info instance profile ARN
	 */
	public String getIamInfoInstanceProfileArn() {
		return iamInfoInstanceProfileArn;
	}

	/**
	 * Returns the IAM info instance profile id
	 * @return the IAM info instance profile id
	 */
	public String getIamInfoInstanceProfileId() {
		return iamInfoInstanceProfileId;
	}

	/**
	 * Returns the IAM info status code
	 * @return the IAM info status code
	 */
	public String getIamInfoStatus() {
		return iamInfoStatus;
	}

	/**
	 * Returns the IAM credentials id
	 * @return the IAM credentials id
	 */
	public String getIamCredentialsId() {
		return iamCredentialsId;
	}

	/**
	 * Returns the IAM credentials last update timestamp
	 * @return the IAM credentials last update timestamp
	 */
	public String getIamCredsLastUpdate() {
		return iamCredsLastUpdate;
	}

	/**
	 * Returns the IAM credentials expiration timestamp
	 * @return the IAM credentials expiration timestamp
	 */
	public String getIamCredsExpiration() {
		return iamCredsExpiration;
	}

	/**
	 * Returns the IAM credentials type
	 * @return the IAM credentials type
	 */
	public String getIamCredsType() {
		return iamCredsType;
	}

	/**
	 * Returns the IAM credentialing status code
	 * @return the IAM credentialing status code
	 */
	public String getIamCredsStatus() {
		return iamCredsStatus;
	}

	/**
	 * Returns the elapsed time to initialize the meta-data in ms.
	 * @return the elapsed time to initialize the meta-data
	 */
	public long getElapsed() {
		return elapsed;
	}

}
