package com.heliosapm.aws.jmx;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Properties;

import com.heliosapm.utils.config.ConfigurationHelper;
import com.heliosapm.utils.io.StdInCommandHandler;
import com.heliosapm.utils.jmx.JMXHelper;
import com.heliosapm.utils.time.SystemClock;
import com.heliosapm.utils.time.SystemClock.ElapsedTime;
import com.heliosapm.utils.url.URLHelper;

import sun.net.www.protocol.s3.Handler;

public class TestCPUrl {

	public TestCPUrl() {
		// TODO Auto-generated constructor stub
	}
	
	/*
	 * s3://mykey:mysecret@us-east-1/le-bucket/le-filename
	 * s3://mykey:mysecret@le-bucket/le-filename
	 * s3://le-bucket/le-filename
	 */

	static String enc(final String s) {
		try {
			return URLEncoder.encode(s, "UTF8");
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	static String[] readCredentials() {
		final String source = ConfigurationHelper.getSystemThenEnvProperty("aws.credentials", "NONE");
		if("NONE".equals(source) || source==null || source.trim().isEmpty()) throw new RuntimeException("No credentials source defined");
		return readCredentials(source);
	}
	
	static String[] readCredentials(final String source) {  
		final URL url = URLHelper.toURL(source);
		final Properties p = URLHelper.readProperties(url);
		return new String[]{
			p.getProperty("access", "null"),
			p.getProperty("secret", "null")
		};
	}
	
	static final String[] URLS = {
		"s3://mykey:mysecret@us-east-1/le-bucket/le-filename",
//		"s3://mykey:mysecret@le-bucket/le-filename",
//		"s3://le-bucket/le-filename",
//		"s3://le-bucket/le-directory/le-filename",
		
	};
	
	public static final Charset UTF8 = Charset.forName("UTF8");
	
	static void log(Object msg) {
		System.out.println(msg);
	}
	
	public static void main(String[] args) {
		JMXHelper.fireUpJMXMPServer(2194);
		try {
			final String[] localCreds = readCredentials();
			final String url = "s3://" + enc(localCreds[0]) + ":" + enc(localCreds[1]) + "@us-east-1/nwhitehead.test/rivercalleddenial/test.txt";
			final URL s3Url = new URL(url);
//			URLConnection urc = s3Url.openConnection();
//			InputStream is = urc.getInputStream();
//			final int size = urc.getContentLength();
//			final ByteArrayOutputStream baos = new ByteArrayOutputStream(size); 
//			final byte[] buff = new byte[1024];
//			int bytesread = -1;
//			while((bytesread = is.read(buff))!=-1) {
//				baos.write(buff, 0, bytesread);
//			}
//			final String output = new String(baos.toByteArray(), UTF8);
//			log("Output: [" + output + "]");
//			//is.close();
//			is = null;
//			urc = null;
//			//System.gc();
			final int LOOPS = 100;
			final ElapsedTime et = SystemClock.startClock();
			for(int i = 0; i < LOOPS; i++) {
				final String val = URLHelper.getTextFromURL(s3Url).trim();
				if(!"Hello Neptune !".equals(val)) {
					System.err.println("Val mismatch [" + val + "]");
				}
			}
			log("\n\nELAPSED:" + et.printAvg("Requests", LOOPS));
			StdInCommandHandler.getInstance()
				.registerCommand("refs", new Runnable(){
					public void run() {
						log("S3 URLConn Refs:" + Handler.refCount());
					}
				})
				.run();
					
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
	
	public static void mainx(String[] args) {
		
		for(String s: URLS) {
			try {
				final URL url = new URL(s);				
				log("URL: [" + url + "]");
				log("Protocol: [" + url.getProtocol() + "]");
				log("Host: [" + url.getHost() + "]");
				log("File: [" + url.getFile() + "]");
				log("Path: [" + url.getPath() + "]");
				log("Query: [" + url.getQuery() + "]");
				log("UserInfo: [" + url.getUserInfo() + "]");
				log("Authority: [" + url.getAuthority() + "]");
				log("Ref: [" + url.getRef() + "]");
				try {
					url.openConnection();
				} catch (Exception x) {
					x.printStackTrace(System.err);
				}
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
			} finally {
				log("===============================================================");
			}
		}

	}

}
