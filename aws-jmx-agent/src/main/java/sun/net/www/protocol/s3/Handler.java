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
package sun.net.www.protocol.s3;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLStreamHandler;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.cliffc.high_scale_lib.NonBlockingHashMapLong;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.ClientConfigurationFactory;
import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.metrics.AwsSdkMetrics;
import com.amazonaws.metrics.MetricType;
import com.amazonaws.metrics.RequestMetricCollector;
import com.amazonaws.metrics.RequestMetricType;
import com.amazonaws.metrics.internal.cloudwatch.PredefinedMetricTransformer;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.AWSRequestMetrics;
import com.codahale.metrics.Timer;
import com.heliosapm.aws.metrics.MetricService;
import com.heliosapm.utils.ref.ReferenceService;


/**
 * <p>Title: Handler</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>sun.net.www.protocol.s3.Handler</code></p>
 */

public class Handler extends URLStreamHandler {
	
	private static final MetricService metricService = MetricService.getInstance();
	
	
	
	private static final RequestMetricCollector requestMetricCollector = new RequestMetricCollector() {
		private final String REQUEST_TYPE = "RequestType";
		private final PredefinedMetricTransformer transformer = new PredefinedMetricTransformer();
		private final Map<String, Timer> metrics = new NonBlockingHashMap<String, Timer>();
		{
			metrics.put("GetObjectRequest/HttpClientSendRequestTime", metricService.timer("s3url.GetObjectRequest.HttpClientSendRequestTime"));
			metrics.put("GetObjectRequest/ClientExecuteTime", metricService.timer("s3url.GetObjectRequest.ClientExecuteTime"));
			metrics.put("GetObjectRequest/HttpRequestTime", metricService.timer("s3url.GetObjectRequest.HttpRequestTime"));
			metrics.put("GetObjectRequest/HttpClientReceiveResponseTime", metricService.timer("s3url.GetObjectRequest.HttpClientReceiveResponseTime"));
		}
		private String getRequestType(final MetricDatum md) {
			String requestType = null;
			if(md!=null) {
				final List<Dimension> dims = md.getDimensions();
				if(dims!=null && !dims.isEmpty()) {
					for(Dimension d: dims) {
						if(d==null) continue;
						if(REQUEST_TYPE.equals(d.getName())) {
							requestType = d.getValue();
							break;
						}
						
					}
				}
			}
			return requestType;
		}
		@Override
		public void collectMetrics(final Request<?> request, final Response<?> response) {
			final AWSRequestMetrics arm = request.getAWSRequestMetrics();
	        if (arm == null || !arm.isEnabled()) {
	            return;
	        }
	        for (MetricType type: AwsSdkMetrics.getPredefinedMetrics()) {
	            if (!(type instanceof RequestMetricType))
	                continue;
	            for (MetricDatum datum : transformer.toMetricData(type, request, response)) {
	            	final String key = getRequestType(datum) + "/" + type.name();
	            	final Timer timer = metrics.get(key);
	            	if(timer!=null) {
	            		timer.update(datum.getValue().longValue(), TimeUnit.MILLISECONDS);
	            	}
	            }
	        }

		}
	};
	private static final AtomicLong phantomId = new AtomicLong();
	private static final NonBlockingHashMapLong<Reference<URLConnection>> connRefs = new NonBlockingHashMapLong<Reference<URLConnection>>();
	
	/** User info splitter */
	public static final Pattern USER_INFO_SPLITTER = Pattern.compile(":");
	/** File splitter */
	public static final Pattern FILE_PATH_SPLITTER = Pattern.compile("/");
	
	
	
	private static Regions tryRegion(final String reg) {
		try {
			return Regions.fromName(reg.trim().toLowerCase());
		} catch (Exception ex) {
			return null;
		}
	}
	
	static {
		metricService.gauge(5, "s3.urlconnections", new Callable<Integer>(){
			@Override
			public Integer call() throws Exception {				
				return connRefs.size();
			}
		});
	}
	
	public static int refCount() {
		return connRefs.size();
	}
	
	private static void closer(final URLConnection conn, final S3Object s3Obj) {
		final long refId = phantomId.incrementAndGet();
		final Runnable r = new Runnable() {
			public void run() {
//				System.out.println("Closing S3 URLConn Reference:" + refId);
				try { s3Obj.close(); } catch (Exception x) {/* No Op */}
				connRefs.remove(refId);
			}
		};
		final Reference<URLConnection> ref = ReferenceService.getInstance().newPhantomReference(conn, r);
		connRefs.put(refId, ref);
//		System.out.println("Created S3 URLConn Reference:" + refId);
		
	}
	
	
	private static String join(final LinkedList<String> fileParts) {
		final StringBuilder b = new StringBuilder();
		for(String s: fileParts) {
			b.append(s.replace("/", "")).append("/");
		}
		return b.deleteCharAt(b.length()-1).toString();
	}
	
	private static void split(final String path, final LinkedList<String> fileParts) {
		
		
		for(String s: FILE_PATH_SPLITTER.split(path)) {
			if(s==null || s.trim().isEmpty()) continue;
			fileParts.add(s.trim());
		}
		
	}

	
	protected URLConnection openConnection(URL url) throws IOException {
		
		return new URLConnection(url) {
			ObjectMetadata metadata = null;
			AmazonS3 client = null;
			S3Object s3obj = null;
			BasicAWSCredentials credentials = null;
			String bucket = null;
			String key = null;
			final AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard().withMetricsCollector(requestMetricCollector);
			final ClientConfiguration cc = new ClientConfigurationFactory().getConfig(); 
			final URLConnection conn = this;
			{
				if (url.getUserInfo() != null) {
					String[] creds = USER_INFO_SPLITTER.split(url.getUserInfo());
					if(creds[0].indexOf('%')!=-1) {
						creds[0] = URLDecoder.decode(creds[0], "UTF8");
					}
					if(creds[1].indexOf('%')!=-1) {
						creds[1] = URLDecoder.decode(creds[1], "UTF8");
					}					
					credentials = new BasicAWSCredentials(creds[0], creds[1]);
					builder.setCredentials(new AWSStaticCredentialsProvider(credentials));
				} else {
					credentials = null;
				}
				final String host = url.getHost();
				
				final LinkedList<String> bucketKey = new LinkedList<String>();
				bucketKey.add(host);
				split(url.getFile(), bucketKey);
				Regions regions = tryRegion(host);
				if(regions==null) {
					try {
						builder.setRegion(Regions.getCurrentRegion().getName());
					} catch (Exception ex) {/* No Op */}				
				} else {
					builder.setRegion(regions.getName());
					bucketKey.removeFirst();
				}
				
				bucket = bucketKey.removeFirst();			
				key = join(bucketKey);
			}
			
			
			//  s3://<access-key>:<secret-key>@<bucket>.s3.amazonaws.com/<filename>
			//	s3://[<access-key>:<secret-key>@][<region>]
			
			/*
			 * s3://mykey:mysecret@us-east-1/le-bucket/le-filename
			 * s3://mykey:mysecret@le-bucket/le-filename
			 * s3://le-bucket/le-filename
			 */
			
									
			@Override
			public InputStream getInputStream() throws IOException {
				checkConect();
				if(s3obj==null) throw new IOException("SE Object Still Null");
				final S3ObjectInputStream s3In = s3obj.getObjectContent();
				
				return new S3ObjectInputStream(s3In, s3In.getHttpRequest(), true) {
					@Override
					public void close() throws IOException {
						if(isConnected()) {
							try { s3obj.close(); } catch (Exception x) {/* No Op */}
							markClosed();
						}
					}
				};
			}
			
			protected void markClosed() {
				this.connected = false;
			}
			
			protected boolean isConnected() {
				return this.connected;
			}
			
			@Override
			public void connect() throws IOException {
				if(!this.connected) {
					try {
						builder.setClientConfiguration(cc);
						client = builder.build();
						s3obj = client.getObject(bucket, key);
						closer(conn, s3obj);												
						metadata = s3obj.getObjectMetadata();
						this.connected = true;
					} catch (Exception e) {
						throw new IOException(e);
					}					
				}
			}
			
			private void checkConect() {
				if(!this.connected) {
					try {
						connect();
					} catch (IOException iex) {
						throw new RuntimeException("Could not establish S3 connection", iex);
					}
				}
			}
			
			/**
			 * {@inheritDoc}
			 * @see java.net.URLConnection#setConnectTimeout(int)
			 */
			@Override
			public void setConnectTimeout(final int timeout) {
				cc.setConnectionTimeout(timeout);
			}
			
			/**
			 * {@inheritDoc}
			 * @see java.net.URLConnection#setReadTimeout(int)
			 */
			@Override
			public void setReadTimeout(final int timeout) {				
				cc.setRequestTimeout(timeout);
			}

			/**
			 * {@inheritDoc}
			 * @see java.net.URLConnection#getLastModified()
			 */
			@Override
			public long getLastModified() {		
				checkConect();
				return metadata.getLastModified().getTime();
			}

			/**
			 * {@inheritDoc}
			 * @see java.net.URLConnection#getContentLengthLong()
			 */
			@Override
			public long getContentLengthLong() {
				checkConect();
				return metadata.getContentLength();
			}

			/**
			 * {@inheritDoc}
			 * @see java.net.URLConnection#getContentType()
			 */
			@Override
			public String getContentType() {
				checkConect();
				return metadata.getContentType();
			}

			/**
			 * {@inheritDoc}
			 * @see java.net.URLConnection#getContentEncoding()
			 */
			@Override
			public String getContentEncoding() {
				checkConect();
				return metadata.getContentEncoding();
			}

			/**
			 * {@inheritDoc}
			 * @see java.net.URLConnection#getDate()
			 */
			@Override
			public long getDate() {			
				checkConect();
				return metadata.getLastModified().getTime();
			}

		};		
	}
	
	
}
