package com.heliosapm.aws.jmx;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Properties;

import com.heliosapm.utils.io.StdInCommandHandler;
import com.heliosapm.utils.jmx.JMXHelper;
import com.heliosapm.utils.url.URLHelper;

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
	
	static void log(Object msg) {
		System.out.println(msg);
	}
	
	public static void main(String[] args) {
		JMXHelper.fireUpJMXMPServer(2194);
		try {
			final String[] localCreds = readCredentials("/tmp/creds.txt");
			final String url = "s3://" + enc(localCreds[0]) + ":" + enc(localCreds[1]) + "@us-east-1/nwhitehead.test/rivercalleddenial/test.txt";
			final URL s3Url = new URL(url);
			URLConnection urc = s3Url.openConnection();
			InputStream is = urc.getInputStream();
			final byte[] buff = new byte[1024];
			int bytesread = -1;
			while((bytesread = is.read(buff))!=-1) {
				
			}
			urc = null;
			System.gc();
			StdInCommandHandler.getInstance().run();
					
//			final String text = URLHelper.getTextFromURL(s3Url, 1, 1);
//			log("Text: [" + text + "]");
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
